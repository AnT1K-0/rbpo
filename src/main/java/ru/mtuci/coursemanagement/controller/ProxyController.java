package ru.mtuci.coursemanagement.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
@RestController
public class ProxyController {

    @GetMapping("/api/proxy")
    public ResponseEntity<String> proxy(@RequestParam("targetUrl") String targetUrl) {
        try {
            URI uri = URI.create(targetUrl);

            // минимальная валидация чтобы не ловить тупые исключения
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid URL scheme");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid URL host");
            }

            RestTemplate rt = new RestTemplate();
            String body = rt.getForObject(uri, String.class);
            return ResponseEntity.ok(body);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid URL");

        } catch (RestClientException e) {
            log.warn("Proxy upstream request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Upstream request failed");

        } catch (Exception e) {
            log.error("Unexpected proxy error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error");
        }
    }
}
