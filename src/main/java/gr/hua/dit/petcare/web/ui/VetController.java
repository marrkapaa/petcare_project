package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.model.VisitRecord;
import gr.hua.dit.petcare.core.ports.VaccinePort;
import gr.hua.dit.petcare.core.service.AppointmentService;
import gr.hua.dit.petcare.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/vets")
public class VetController {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final VaccinePort vaccinePort;
    public VetController(AppointmentService appointmentService, UserService userService, VaccinePort vaccinePort) {
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.vaccinePort = vaccinePort;
    }

    @GetMapping("/schedule")
    public String viewVetSchedule(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User vet = userService.findUserByUsername(principal.getUsername());
        List<Appointment> appointments = appointmentService.findAppointmentsByVeterinarian(vet);
        model.addAttribute("vetName", vet.getUsername());
        model.addAttribute("appointments", appointments);
        return "vets/schedule";
    }

    @GetMapping("/records/new")
    public String showVisitRecordForm(@RequestParam Long appointmentId, Model model) {

        if (!model.containsAttribute("record")) {
            VisitRecord visitRecord = new VisitRecord();
            Appointment app = new Appointment();
            app.setId(appointmentId);
            visitRecord.setAppointment(app);
            model.addAttribute("record", visitRecord);
        }

        List<String> vaccines = vaccinePort.getAvailableVaccines("Dog");
        model.addAttribute("vaccines", vaccines);

        model.addAttribute("appointmentId", appointmentId);
        return "vets/record-form";
    }

    @PostMapping("/records")
    public String saveVisitRecord(
        @Valid @ModelAttribute("record") VisitRecord visitRecord,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        Long appointmentId = visitRecord.getAppointment() != null ? visitRecord.getAppointment().getId() : null;

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.record", bindingResult);
            redirectAttributes.addFlashAttribute("record", visitRecord);
            return "redirect:/vets/records/new?appointmentId=" + appointmentId;
        }

        try {
            appointmentService.saveVisitRecord(visitRecord);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("record", visitRecord);
            return "redirect:/vets/records/new?appointmentId=" + appointmentId;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Το αρχείο επίσκεψης καταχωρήθηκε επιτυχώς!");
        return "redirect:/vets/schedule";
    }

    @GetMapping("/profile")
    public String showProfileForm(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User user = userService.findUserByUsername(principal.getUsername());
        model.addAttribute("user", user);
        return "profile-edit";
    }

    @PostMapping("/profile")
    public String updateProfile(
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
        @RequestParam("email") String email,
        @RequestParam(value = "password", required = false) String password,
        RedirectAttributes redirectAttributes
    ) {
        User user = userService.findUserByUsername(principal.getUsername());
        userService.updateUserProfile(user, email, password);
        redirectAttributes.addFlashAttribute("successMessage", "Το προφίλ ενημερώθηκε επιτυχώς!");
        return "redirect:/vets/profile";
    }
}
