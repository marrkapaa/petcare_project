package gr.hua.dit.petcare.config;

import org.springframework.beans.factory.annotation.Value; // ΝΕΟ IMPORT
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NotificationClientConfig {

    // Φόρτωση από application.yml (π.χ. notification.service.base-url)
    @Value("${notification.service.base-url:http://localhost:8081}")
    private String notificationServiceBaseUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Προσθήκη Getter για το URL, αν και ο Adapter μπορεί να το πάρει ως dependency
    public String getNotificationServiceBaseUrl() {
        return notificationServiceBaseUrl;
    }
}
