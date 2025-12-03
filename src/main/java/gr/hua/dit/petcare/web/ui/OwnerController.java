package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.Pet;
import gr.hua.dit.petcare.core.model.Role;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.service.AppointmentService;
import gr.hua.dit.petcare.core.service.PetService;
import gr.hua.dit.petcare.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // Σωστό Principal type
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    private final PetService petService;
    private final UserService userService;
    private final AppointmentService appointmentService;

    public OwnerController(PetService petService, UserService userService, AppointmentService appointmentService) {
        this.petService = petService;
        this.userService = userService;
        this.appointmentService = appointmentService;
    }

    /**
     * Βοηθητική μέθοδος: Μετατρέπει τον Principal του Spring (UserDetails) στο πλήρες JPA Entity (User).
     * @param principal Η ταυτότητα του συνδεδεμένου χρήστη από το Spring Security.
     * @return Το πλήρες User Entity (Owner).
     */
    private User getAuthenticatedUser(@AuthenticationPrincipal UserDetails principal) { // ΔΙΟΡΘΩΣΗ: Αλλαγή τύπου σε UserDetails
        // Χρησιμοποιούμε το μοναδικό username για να κάνουμε lookup στη βάση
        return userService.findUserByUsername(principal.getUsername()); 
    }

    // --- PETS HANDLING ---

    // 1. Προβολή λίστας κατοικιδίων
    @GetMapping("/pets")
    public String listOwnerPets(@AuthenticationPrincipal UserDetails principal, Model model) { // ΔΙΟΡΘΩΣΗ: Αλλαγή τύπου
        User owner = getAuthenticatedUser(principal);
        model.addAttribute("pets", petService.findPetsByOwner(owner));
        return "owners/pet-list";
    }

    // 2. Εμφάνιση φόρμας νέου κατοικίδιου
    @GetMapping("/pets/new")
    public String showPetForm(Model model) {
        model.addAttribute("pet", new Pet());
        model.addAttribute("speciesOptions", new String[]{"Dog", "Cat", "Bird", "Other"}); // Παράδειγμα επιλογών
        return "owners/pet-form";
    }

    // 3. Υποβολή φόρμας κατοικίδιου
    @PostMapping("/pets")
    public String savePet(
        @AuthenticationPrincipal UserDetails principal, // ΔΙΟΡΘΩΣΗ: Αλλαγή τύπου
        @Valid @ModelAttribute("pet") Pet pet, 
        BindingResult bindingResult,            
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        // Έλεγχος Validation Errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("speciesOptions", new String[]{"Dog", "Cat", "Bird", "Other"});
            return "owners/pet-form";
        }

        try {
            User owner = getAuthenticatedUser(principal);
            petService.registerNewPet(pet, owner);
            redirectAttributes.addFlashAttribute("successMessage", "Κατοικίδιο καταχωρήθηκε επιτυχώς!");
            return "redirect:/owner/pets";

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Σφάλμα κατά την καταχώρηση: " + e.getMessage());
            model.addAttribute("speciesOptions", new String[]{"Dog", "Cat", "Bird", "Other"});
            return "owners/pet-form";
        }
    }


    // --- APPOINTMENTS HANDLING ---

    // 4. Προβολή λίστας ραντεβού
    @GetMapping("/appointments")
    public String listAppointments(@AuthenticationPrincipal UserDetails principal, Model model) { // ΔΙΟΡΘΩΣΗ: Αλλαγή τύπου
        User owner = getAuthenticatedUser(principal);
        model.addAttribute("appointments", appointmentService.findAppointmentsByOwner(owner));
        return "owners/appointment-list";
    }

    // 5. Εμφάνιση φόρμας νέου ραντεβού
    @GetMapping("/appointments/new")
    public String showAppointmentForm(@AuthenticationPrincipal UserDetails principal, Model model) { // ΔΙΟΡΘΩΣΗ: Αλλαγή τύπου
        User owner = getAuthenticatedUser(principal);
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("ownerPets", petService.findPetsByOwner(owner)); // Τα κατοικίδια του χρήστη
        model.addAttribute("vets", userService.findAllUsersByRole(Role.VETERINARIAN)); // Διαθέσιμοι κτηνίατροι
        return "owners/appointment-form";
    }

    // 6. Υποβολή φόρμας ραντεβού
    @PostMapping("/appointments")
    public String saveAppointment(
        @AuthenticationPrincipal UserDetails principal, // ΔΙΟΡΘΩΣΗ: Αλλαγή τύπου
        @Valid @ModelAttribute("appointment") Appointment appointment, // Προσθήκη @Valid
        BindingResult bindingResult,                                   // Προσθήκη BindingResult
        @RequestParam("petId") Long petId,                            
        @RequestParam("vetId") Long vetId,                            
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        // 1. Έλεγχος Validation Errors
        if (bindingResult.hasErrors()) {
            User owner = getAuthenticatedUser(principal);
            model.addAttribute("ownerPets", petService.findPetsByOwner(owner));
            model.addAttribute("vets", userService.findAllUsersByRole(Role.VETERINARIAN));
            return "owners/appointment-form";
        }

        try {
            // Αυτός είναι ο συνδεδεμένος χρήστης (Owner)
            User owner = getAuthenticatedUser(principal);
            
            // Ανάκτηση των πλήρων Entities για την Business Logic
            Pet pet = petService.findPetById(petId);
            User veterinarian = userService.findUserById(vetId);

            // Έλεγχος Ασφάλειας: Ιδιοκτήτης βλέπει μόνο τα δικά του κατοικίδια/ραντεβού.
            if (!pet.getOwner().getId().equals(owner.getId())) {
                 // Θα πετάξει 500/Internal Error, αλλά το μήνυμα είναι σωστό για το catch
                 throw new SecurityException("Δεν επιτρέπεται η κράτηση για κατοικίδιο που δεν ανήκει στον χρήστη."); 
            }

            appointment.setPet(pet);
            appointment.setVeterinarian(veterinarian);

            appointmentService.createAppointment(appointment);
            redirectAttributes.addFlashAttribute("successMessage", "Το ραντεβού καταχωρήθηκε και αναμένει επιβεβαίωση!");
            return "redirect:/owner/appointments";

        } catch (IllegalArgumentException e) {
            // Business Rule Violation (π.χ. Επικάλυψη, 7ήμερος κανόνας)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        } catch (RuntimeException e) { 
            // Catch all για NotFound exceptions, κλπ.
            redirectAttributes.addFlashAttribute("errorMessage", "Σφάλμα κατά την κράτηση ραντεβού: " + e.getMessage());
        }

        // Σε περίπτωση σφάλματος, επαναφέρουμε το αρχικό appointment object
        redirectAttributes.addFlashAttribute("appointment", appointment);
        return "redirect:/owner/appointments/new";
    }
}
