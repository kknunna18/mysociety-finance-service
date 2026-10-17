package com.mysociety.finance.api;

import com.mysociety.finance.service.FinanceService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    private final FinanceService service;
    private final Map<String, String> secrets;

    public WebhookController(FinanceService service, @Value("${finance.webhook.provider-secrets:}") String configuredSecrets) {
        this.service = service;
        this.secrets = parseSecrets(configuredSecrets);
    }

    @PostMapping("/{provider}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void receive(@PathVariable String provider, @RequestHeader(value = "X-Provider-Signature", required = false) String signature, @RequestBody String rawPayload, @RequestParam UUID societyId, @RequestParam UUID paymentId, @RequestParam @NotBlank String eventId, @RequestParam @NotBlank String eventType) {
        String secret = secrets.get(provider);
        if (secret == null || signature == null || !MessageDigest.isEqual(hmac(rawPayload, secret), signature.getBytes(StandardCharsets.US_ASCII)))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Webhook signature verification failed");
        service.acceptedWebhook(societyId, paymentId, provider, eventId, eventType, rawPayload);
    }

    private static Map<String, String> parseSecrets(String values) {
        Map<String, String> result = new HashMap<>();
        for (String item : values.split(",")) {
            String[] pair = item.split(":", 2);
            if (pair.length == 2 && !pair[1].isBlank()) result.put(pair[0], pair[1]);
        }
        return result;
    }

    private static byte[] hmac(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return hex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))).getBytes(StandardCharsets.US_ASCII);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to verify configured webhook signature", e);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder b = new StringBuilder();
        for (byte value : bytes) b.append(String.format("%02x", value));
        return b.toString();
    }
}
