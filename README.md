# Desafío Técnico – API de Tipo de Cambio

Microservicio en Java 17 / Spring Boot que consume **ExchangeRate-API** mediante OpenFeign, expone `POST /api/conversion` para convertir montos entre monedas y registra cada conversión en una base H2 en memoria. Está construido con **Domain-Driven Design** y separación estricta en capas *Domain / Application / Infrastructure*.

---

## Cómo ejecutar

Requisitos: JDK 17 o superior. Maven no es necesario, el proyecto incluye wrapper.

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

La aplicación levanta en `http://localhost:8080`.

La API key del reto ya está configurada en `application.yaml`. Si se quiere usar otra, basta con definir la variable de entorno `EXCHANGE_RATE_API_KEY` antes de arrancar.

### Ejecutar las pruebas

```bash
./mvnw test
```

### Consola H2

| Parámetro | Valor |
|---|---|
| URL | `http://localhost:8080/h2-console` |
| JDBC URL | `jdbc:h2:mem:testdb` |
| Usuario | `sa` |
| Password | `password` |
| Tabla | `conversions` |

---

## Endpoint

### `POST /api/conversion`

**Request**

```bash
curl -X POST http://localhost:8080/api/conversion \
  -H "Content-Type: application/json" \
  -d '{"monto": 100, "monedaOrigen": "USD", "monedaDestino": "PEN"}'
```

```json
{
  "monto": 100,
  "monedaOrigen": "USD",
  "monedaDestino": "PEN"
}
```

**Response `200 OK`**

```json
{
  "monto": 100,
  "montoConvertido": 346.48,
  "monedaOrigen": "USD",
  "monedaDestino": "PEN",
  "tipoCambio": 3.4648
}
```

Los códigos de moneda aceptan minúsculas y espacios alrededor (`" usd "` se normaliza a `USD`). El monto convertido se redondea a 2 decimales con `HALF_UP`; el tipo de cambio se devuelve tal como lo entrega el proveedor.

### Respuestas de error

Todas las respuestas de error comparten el mismo formato:

```json
{
  "timestamp": "2026-11-03T19:58:20.735011",
  "status": 422,
  "error": "UNSUPPORTED_CURRENCY",
  "message": "Currency 'ZZZ' is not supported by the exchange rate provider",
  "details": []
}
```

| HTTP | `error` | Cuándo |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Campos faltantes, monto ≤ 0, código de moneda que no son 3 letras. `details` lista cada campo inválido. |
| 400 | `MALFORMED_REQUEST` | El cuerpo no es JSON válido. |
| 400 | `INVALID_CURRENCY` / `INVALID_AMOUNT` | El dominio rechazó el valor (defensa en profundidad, además de Bean Validation). |
| 422 | `UNSUPPORTED_CURRENCY` | Código bien formado, pero el proveedor no cotiza esa moneda (origen o destino). |
| 503 | `EXCHANGE_RATE_UNAVAILABLE` | ExchangeRate-API no responde, devuelve error HTTP o `result != success`. |
| 500 | `INTERNAL_ERROR` | Error inesperado. Se registra en el log, nunca se expone el detalle al cliente. |

Solo las conversiones exitosas se persisten; cualquier error deja la base sin cambios.

---

## Arquitectura

```
pe.test.exchange
├── domain                               ← sin dependencias de Spring ni JPA
│   ├── model
│   │   ├── Currency                     Value Object (ISO 4217, validado)
│   │   ├── Money                        Value Object (monto > 0 + moneda)
│   │   ├── ExchangeRate                 Value Object (origen, destino, tasa) – sabe convertir y redondear
│   │   └── Conversion                   Aggregate Root – resultado de una conversión
│   ├── service
│   │   ├── CurrencyConversionService    Domain Service: resuelve la tasa y crea la Conversion
│   │   └── ExchangeRateProvider         Puerto de salida (interfaz) hacia el proveedor de tasas
│   ├── repository
│   │   └── ConversionRepository         Puerto de salida (interfaz) de persistencia
│   └── exception                        Jerarquía DomainException: InvalidCurrency, InvalidAmount,
│                                        InvalidExchangeRate, UnsupportedCurrency, ExchangeRateUnavailable
├── application
│   ├── controller
│   │   ├── ConversionController         POST /api/conversion
│   │   └── GlobalExceptionHandler       Excepción → HTTP + ErrorResponse uniforme
│   ├── service
│   │   └── ConversionApplicationService Caso de uso: mapear → convertir → persistir → responder
│   ├── dto                              ConversionRequest (Bean Validation), ConversionResponse, ErrorResponse
│   └── mapper
│       └── ConversionMapper             DTO ↔ dominio
└── infrastructure
    ├── external
    │   ├── ExchangeRateApiClient        Cliente Feign (provisto por el reto)
    │   ├── ExchangeRateApiAdapter       Implementa ExchangeRateProvider; traduce errores Feign a excepciones de dominio
    │   └── ExchangeRateProperties       Binding de exchange-rate.api.*
    ├── persistence
    │   ├── ConversionJpaEntity          Entidad JPA (tabla conversions)
    │   ├── ConversionJpaRepository      Spring Data
    │   └── ConversionRepositoryAdapter  Implementa ConversionRepository; mapea agregado ↔ entidad
    └── config
        └── DomainConfig                 Registra el Domain Service y el Clock como beans
```

### Decisiones de diseño

- **Dominio puro.** Ningún tipo de `domain` importa Spring, JPA o Feign. Los servicios de dominio se registran como beans desde `infrastructure/config`, no con anotaciones en el dominio.
- **Puertos y adaptadores.** El dominio declara lo que necesita (`ExchangeRateProvider`, `ConversionRepository`); infraestructura lo implementa. Cambiar de proveedor de tasas o de base de datos no toca el dominio ni la aplicación.
- **Value Objects validan al construirse.** `Currency`, `Money` y `ExchangeRate` no pueden existir en estado inválido, por lo que el resto del código no repite validaciones.
- **La regla de negocio vive en el dominio.** `ExchangeRate.convert` concentra la multiplicación y la política de redondeo; `CurrencyConversionService` decide que convertir una moneda a sí misma usa tasa 1 sin llamar al proveedor.
- **Entidad JPA separada del agregado.** `Conversion` es inmutable y se reconstituye desde `ConversionJpaEntity`; las anotaciones de persistencia no contaminan el modelo.
- **Errores traducidos en la frontera.** El adaptador Feign convierte `FeignException` en excepciones de dominio (`UnsupportedCurrencyException` para 404 `unsupported-code`, `ExchangeRateUnavailableException` para el resto), así las capas superiores nunca dependen de Feign.
- **`Clock` inyectable.** Permite fijar la fecha de la operación en los tests.
- **API key configurable.** Se lee de `exchange-rate.api.key`, con override por la variable `EXCHANGE_RATE_API_KEY`. La key del reto queda como valor por defecto solo para que el proyecto se ejecute sin configuración previa; en un entorno real se eliminaría el default y se inyectaría por entorno o gestor de secretos.
- **Sin secretos en los logs.** El adaptador Feign registra solo el código HTTP del fallo y no propaga la `FeignException` como causa, porque su mensaje incluye la URL con la API key.

---

## Pruebas

41 pruebas, divididas por capa:

| Capa | Clase | Qué cubre |
|---|---|---|
| Domain | `CurrencyTest`, `MoneyTest`, `ExchangeRateTest`, `ConversionTest` | Invariantes de los Value Objects, redondeo, creación/reconstitución del agregado |
| Domain | `CurrencyConversionServiceTest` | Uso del proveedor y atajo de tasa identidad |
| Application | `ConversionApplicationServiceTest` | Orquestación del caso de uso con repositorio en memoria |
| Application | `ConversionControllerTest` (`@WebMvcTest`) | Contrato HTTP, Bean Validation y mapeo de cada excepción a su código de estado |
| Infrastructure | `ExchangeRateApiAdapterTest` | Traducción de respuestas y fallos de Feign a excepciones de dominio |
| Infrastructure | `ConversionRepositoryAdapterTest` (`@DataJpaTest`) | Persistencia real en H2 de todas las columnas requeridas |
| End-to-end | `ConversionEndToEndTest` (`@SpringBootTest`) | Flujo completo HTTP → dominio → H2 con el cliente Feign simulado |

Las pruebas no llaman a la API externa: el cliente Feign se simula para que la suite sea determinista y ejecutable sin red.

---

## Stack

| Tecnología | Versión |
|---|---|
| Java | 17+ |
| Spring Boot | 4.0.6 |
| Spring Cloud OpenFeign | 2025.1.1 |
| Spring Data JPA + H2 | Incluido |
| Bean Validation | Incluido |
| Lombok | Incluido |
| JUnit 5, Mockito, AssertJ | Incluido |
