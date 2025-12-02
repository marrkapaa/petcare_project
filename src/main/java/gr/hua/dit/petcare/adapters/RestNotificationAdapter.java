package gr.hua.dit.petcare.adapters;

import gr.hua.dit.petcare.config.NotificationClientConfig;
import gr.hua.dit.petcare.core.ports.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // ΝΕΟ IMPORT
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException; // ΝΕΟ IMPORT
import org.springframework.web.client.RestClientException;    // ΝΕΟ IMPORT
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class RestNotificationAdapter implements NotificationPort {

    private static final Logger logger = LoggerFactory.getLogger(RestNotificationAdapter.class); // ΠΡΟΣΘΗΚΗ LOGGER

    private final RestTemplate restTemplate;
    private final NotificationClientConfig config; // ΝΕΟ FIELD για να πάρουμε το URL

    // 1. Αλλαγή Constructor για να παίρνει το Config
    public RestNotificationAdapter(RestTemplate restTemplate, NotificationClientConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    @Override
    public boolean sendNotification(String recipient, String message) {
        String url = config.getNotificationServiceBaseUrl() + "/api/notify"; // Χρήση Getter

        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("to", recipient);
        requestPayload.put("body", message);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                requestPayload,
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
            // Σφάλμα σύνδεσης (π.χ. η υπηρεσία είναι DOWN ή το URL είναι λάθος)
            logger.error("Αποτυχία σύνδεσης με την εξωτερική υπηρεσία ειδοποιήσεων: {}", url, e);
            return false;
        } catch (RestClientException e) {
            // Άλλα REST σφάλματα (π.χ. 4xx, 5xx)
            logger.error("Αποτυχία κλήσης ειδοποίησης: {}", e.getMessage(), e);
            return false;
        }
    }
}
