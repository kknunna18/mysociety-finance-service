package com.mysociety.finance.api;

import com.mysociety.finance.service.FinanceService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebhookControllerTest {
    @Test void rejectsWebhookWhenNoProviderVerifierIsConfigured() {
        WebhookController controller=new WebhookController(mock(FinanceService.class),"");
        assertThatThrownBy(() -> controller.receive("unknown","signature","{}",UUID.randomUUID(),UUID.randomUUID(),"event-1","payment.confirmed"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode.value").isEqualTo(401);
    }
}
