package gr.hua.dit.petcare.core.ports;

import java.util.List;

public interface VaccinePort {
    List<String> getAvailableVaccines(String species);
}

