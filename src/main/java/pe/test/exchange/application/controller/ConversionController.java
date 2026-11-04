package pe.test.exchange.application.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.application.service.ConversionApplicationService;

/**
 * REST entry point for currency conversions.
 */
@RestController
@RequestMapping("/api/conversion")
public class ConversionController {

    private final ConversionApplicationService conversionService;

    public ConversionController(ConversionApplicationService conversionService) {
        this.conversionService = conversionService;
    }

    @PostMapping
    public ResponseEntity<ConversionResponse> convert(@Valid @RequestBody ConversionRequest request) {
        return ResponseEntity.ok(conversionService.convert(request));
    }
}
