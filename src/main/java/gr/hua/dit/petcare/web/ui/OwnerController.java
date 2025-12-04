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
import org.springframework.security.core.userdetails.UserDetails;
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


    private User getAuthenticatedUser(@AuthenticationPrincipal UserDetails principal) {
        return userService.findUserByUsername(principal.getUsername()); 
    }


    // προβολή λίστας κατοικιδίων
    @GetMapping("/pets")
    public String listOwnerPets(@AuthenticationPrincipal UserDetails principal, Model model) {
        User owner = getAuthenticatedUser(principal);
        model.addAttribute("pets", petService.findPetsByOwner(owner));
        return "owners/pet-list";
    }

    // εμφάνιση φόρμας νέου κατοικίδιου
    @GetMapping("/pets/new")
    public String showPetForm(Model model) {
        model.addAttribute("pet", new Pet());
        model.addAttribute("speciesOptions", new String[]{"Dog", "Cat", "Bird", "Other"}); // πχ επιλογών
        return "owners/pet-form";
    }

    // υποβολή φόρμας κατοικίδιου
    @PostMapping("/pets")
    public String savePet(
        @AuthenticationPrincipal UserDetails principal,
        @Valid @ModelAttribute("pet") Pet pet, 
        BindingResult bindingResult,            
        Model model,
        RedirectAttributes redirectAttributes
    ) {
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



    // προβολή λίστας ραντεβού
    @GetMapping("/appointments")
    public String listAppointments(@AuthenticationPrincipal UserDetails principal, Model model) {
        User owner = getAuthenticatedUser(principal);
        model.addAttribute("appointments", appointmentService.findAppointmentsByOwner(owner));
        return "owners/appointment-list";
    }

    // εμφάνιση φόρμας νέου ραντεβού
    @GetMapping("/appointments/new")
    public String showAppointmentForm(@AuthenticationPrincipal UserDetails principal, Model model) { // ΔΙΟΡΘΩΣΗ: Αλλαγή τύπου
        User owner = getAuthenticatedUser(principal);
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("ownerPets", petService.findPetsByOwner(owner)); // Τα κατοικίδια του χρήστη
        model.addAttribute("vets", userService.findAllUsersByRole(Role.VETERINARIAN)); // Διαθέσιμοι κτηνίατροι
        return "owners/appointment-form";
    }

    // υποβολή φόρμας ραντεβού
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
        if (bindingResult.hasErrors()) {
            User owner = getAuthenticatedUser(principal);
            model.addAttribute("ownerPets", petService.findPetsByOwner(owner));
            model.addAttribute("vets", userService.findAllUsersByRole(Role.VETERINARIAN));
            return "owners/appointment-form";
        }

        try {
            User owner = getAuthenticatedUser(principal);
            
            Pet pet = petService.findPetById(petId);
            User veterinarian = userService.findUserById(vetId);

            if (!pet.getOwner().getId().equals(owner.getId())) {
                 throw new SecurityException("Δεν επιτρέπεται η κράτηση για κατοικίδιο που δεν ανήκει στον χρήστη."); 
            }

            appointment.setPet(pet);
            appointment.setVeterinarian(veterinarian);

            appointmentService.createAppointment(appointment);
            redirectAttributes.addFlashAttribute("successMessage", "Το ραντεβού καταχωρήθηκε και αναμένει επιβεβαίωση!");
            return "redirect:/owner/appointments";

        } catch (IllegalArgumentException e) {
            // br violation (π.χ. επικάλυψη, 7ήμερος κανόνας)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        } catch (RuntimeException e) { 
            redirectAttributes.addFlashAttribute("errorMessage", "Σφάλμα κατά την κράτηση ραντεβού: " + e.getMessage());
        }

        redirectAttributes.addFlashAttribute("appointment", appointment);
        return "redirect:/owner/appointments/new";
    }
}
