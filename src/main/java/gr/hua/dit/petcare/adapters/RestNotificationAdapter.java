package gr.hua.dit.petcare.adapters;

import gr.hua.dit.petcare.config.NotificationClientConfig;
import gr.hua.dit.petcare.core.ports.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@Component
public class RestNotificationAdapter implements NotificationPort {

    private static final Logger logger = LoggerFactory.getLogger(RestNotificationAdapter.class);

    private final RestTemplate restTemplate;
    private final NotificationClientConfig config;

    public RestNotificationAdapter(RestTemplate restTemplate, NotificationClientConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    @Override
    public boolean sendNotification(String recipient, String message) {
        String url = config.getNotificationServiceBaseUrl() + "/api/notify";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer notification-service-secret-123"); // <--- Το κλειδί ασφαλείας

        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("to", recipient);
        requestPayload.put("body", message);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                requestEntity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Ειδοποίηση στάλθηκε επιτυχώς στον: {}", recipient);
                return true;
            } else {
                logger.error("Ειδοποίηση απέτυχε με status code {} για {}", response.getStatusCode(), recipient);
                return false;
            }

        } catch (ResourceAccessException e) {
            logger.error("Αποτυχία σύνδεσης με την εξωτερική υπηρεσία ειδοποιήσεων: {}", url, e);
            return false;
        } catch (RestClientException e) {
            logger.error("Αποτυχία κλήσης ειδοποίησης: {}", e.getMessage(), e);
            return false;
        }
    }
}
