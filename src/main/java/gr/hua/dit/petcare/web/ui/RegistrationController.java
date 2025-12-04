package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.core.model.Role;
import gr.hua.dit.petcare.core.service.UserService;
import gr.hua.dit.petcare.core.service.model.UserRegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(final UserService userService) {
        this.userService = userService;
    }

    // εμφάνιση φόρμας
    @GetMapping("/register")
    public String showRegistrationForm(final Model model) {
        if (!model.containsAttribute("registrationRequest")) {
            model.addAttribute("registrationRequest", new UserRegistrationRequest());
        }

        model.addAttribute("roles", Role.values());

        return "register";
    }

    // υποβολή φόρμας
    @PostMapping("/register")
    public String handleFormSubmission(
        @Valid @ModelAttribute("registrationRequest") final UserRegistrationRequest request,
        BindingResult bindingResult,
        final Model model,
        RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "register";
        }

        try {
            userService.registerNewUser(request);

            redirectAttributes.addFlashAttribute("successMessage", "Επιτυχής εγγραφή! Παρακαλώ συνδεθείτε.");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", Role.values());
            return "register";
        }
    }
}
