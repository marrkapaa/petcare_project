package gr.hua.dit.petcare.adapters;

import gr.hua.dit.petcare.core.ports.VaccinePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class RestVaccineAdapter implements VaccinePort {

    private static final Logger logger = LoggerFactory.getLogger(RestVaccineAdapter.class);
    private final RestTemplate restTemplate;

    public RestVaccineAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<String> getAvailableVaccines(String species) {
        String url = "http://localhost:8081/api/vaccines?species=" + species;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", "petcare-secret-key-2024");
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            logger.info("Κλήση External API (GET): {}", url);
            ResponseEntity<String[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, String[].class);

            return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();

        } catch (Exception e) {
            // Fallback Mock Data
            if ("Dog".equalsIgnoreCase(species) || "Σκύλος".equalsIgnoreCase(species)) {
                return Arrays.asList("Rabies (Λύσσα)", "Distemper (Μόρβα)", "Parvovirus");
            } else if ("Cat".equalsIgnoreCase(species) || "Γάτα".equalsIgnoreCase(species)) {
                return Arrays.asList("Feline Herpesvirus", "Calicivirus", "Feline Leukemia");
            } else {
                return Arrays.asList("Γενική Εξέταση", "Αποπαρασίτωση");
            }
        }
    }
}
