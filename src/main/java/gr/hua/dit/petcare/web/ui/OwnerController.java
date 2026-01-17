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
@RequestMapping("/owners")
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

    @GetMapping("/pets")
    public String listOwnerPets(@AuthenticationPrincipal UserDetails principal, Model model) {
        User owner = getAuthenticatedUser(principal);
        model.addAttribute("pets", petService.findPetsByOwner(owner));
        return "owners/pet-list";
    }

    @GetMapping("/pets/new")
    public String showPetForm(Model model) {
        model.addAttribute("pet", new Pet());
        model.addAttribute("speciesOptions", new String[]{"Σκύλος", "Γάτα", "Πτηνό", "Άλλο"});
        return "owners/pet-form";
    }

    @PostMapping("/pets")
    public String savePet(
        @AuthenticationPrincipal UserDetails principal,
        @Valid @ModelAttribute("pet") Pet pet,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "owners/pet-form";
        }

        try {
            User owner = getAuthenticatedUser(principal);
            petService.registerNewPet(pet, owner);
            redirectAttributes.addFlashAttribute("successMessage", "Το κατοικίδιο καταχωρήθηκε επιτυχώς!");
            return "redirect:/owners/pets";

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Σφάλμα κατά την καταχώρηση: " + e.getMessage());
            return "owners/pet-form";
        }
    }

    @GetMapping("/appointments")
    public String listAppointments(@AuthenticationPrincipal UserDetails principal, Model model) {
        User owner = getAuthenticatedUser(principal);
        model.addAttribute("appointments", appointmentService.findAppointmentsByOwner(owner));
        return "owners/appointment-list";
    }

    @GetMapping("/appointments/new")
    public String showAppointmentForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        User owner = getAuthenticatedUser(principal);
        model.addAttribute("appointment", new Appointment());

        model.addAttribute("pets", petService.findPetsByOwner(owner));
        model.addAttribute("vets", userService.findAllUsersByRole(Role.VETERINARIAN));

        return "owners/appointment-form";
    }

    @PostMapping("/appointments")
    public String saveAppointment(
        @AuthenticationPrincipal UserDetails principal,
        @Valid @ModelAttribute("appointment") Appointment appointment,
        BindingResult bindingResult,
        @RequestParam(value = "pet", required = false) Long petId,
        @RequestParam(value = "veterinarian", required = false) Long vetId,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        User owner = getAuthenticatedUser(principal);

        if (bindingResult.hasErrors() || petId == null || vetId == null) {
            model.addAttribute("errorMessage", "Παρακαλώ συμπληρώστε όλα τα πεδία σωστά.");
            model.addAttribute("pets", petService.findPetsByOwner(owner));
            model.addAttribute("vets", userService.findAllUsersByRole(Role.VETERINARIAN));
            return "owners/appointment-form";
        }

        try {
            Pet pet = petService.findPetById(petId);
            User veterinarian = userService.findUserById(vetId);

            if (!pet.getOwner().getId().equals(owner.getId())) {
                 throw new SecurityException("Δεν επιτρέπεται η κράτηση για ξένο κατοικίδιο.");
            }

            appointment.setPet(pet);
            appointment.setVeterinarian(veterinarian);

            appointmentService.createAppointment(appointment);

            redirectAttributes.addFlashAttribute("successMessage", "Το ραντεβού κλείστηκε επιτυχώς!");
            return "redirect:/owners/appointments";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/owners/appointments/new";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Σφάλμα συστήματος: " + e.getMessage());
            return "redirect:/owners/appointments/new";
        }
    }
}
