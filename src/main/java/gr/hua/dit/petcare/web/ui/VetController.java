package gr.hua.dit.petcare.web.ui;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.model.VisitRecord;
import gr.hua.dit.petcare.core.service.AppointmentService;
import gr.hua.dit.petcare.core.service.PetNotFoundException;
import gr.hua.dit.petcare.core.service.UserService;
import gr.hua.dit.petcare.core.service.UserNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/vets")
public class VetController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    public VetController(AppointmentService appointmentService, UserService userService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    // 1. Προβολή Προγράμματος (GET /vets/schedule)
    @GetMapping("/schedule")
    public String viewVetSchedule(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {

        // Σωστή ανάκτηση του πλήρους User Entity
        User vet = userService.findUserByUsername(principal.getUsername());

        // Η Repositoy Query φέρνει όλα τα απαραίτητα JOIN FETCH
        List<Appointment> appointments = appointmentService.findAppointmentsByVeterinarian(vet);

        model.addAttribute("vetName", vet.getUsername());
        model.addAttribute("appointments", appointments);

        return "vets/schedule";
    }

    // 2. Εμφάνιση Φόρμας Εγγραφής Επίσκεψης (GET /vets/records/new)
    @GetMapping("/records/new")
    public String showVisitRecordForm(@RequestParam Long appointmentId, Model model) {

        // Χειρισμός του case όπου το validation απέτυχε και έγινε redirect
        if (!model.containsAttribute("record")) {
            VisitRecord visitRecord = new VisitRecord();

            // Δημιουργούμε ένα "light" Appointment μόνο με το ID για να συνδεθεί το VisitRecord
            Appointment app = new Appointment();
            app.setId(appointmentId);
            visitRecord.setAppointment(app);

            model.addAttribute("record", visitRecord);
        }

        model.addAttribute("appointmentId", appointmentId);
        return "vets/record-form";
    }

    // 3. Υποβολή Φόρμας (POST /vets/records) - ΔΙΟΡΘΩΜΕΝΟ ΜΕ VALIDATION
    @PostMapping("/records")
    public String saveVisitRecord(
        @Valid @ModelAttribute("record") VisitRecord visitRecord, // Προσθήκη @Valid
        BindingResult bindingResult,                                // Προσθήκη BindingResult
        RedirectAttributes redirectAttributes
    ) {
        Long appointmentId = visitRecord.getAppointment() != null ? visitRecord.getAppointment().getId() : null;

        // 1. Έλεγχος Validation Errors
        if (bindingResult.hasErrors()) {
            // Αν το validation αποτύχει, επιστρέφουμε τα σφάλματα μέσω Flash Attributes
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.record", bindingResult);
            redirectAttributes.addFlashAttribute("record", visitRecord);
            return "redirect:/vets/records/new?appointmentId=" + appointmentId;
        }

        try {
            // Ο Service αποθηκεύει το VisitRecord και αλλάζει το Status του Appointment σε COMPLETED.
            appointmentService.saveVisitRecord(visitRecord);

        } catch (RuntimeException e) {
            // Συλλαμβάνει IllegalArgumentException, AppointmentNotFoundException, κλπ.
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("record", visitRecord);
            return "redirect:/vets/records/new?appointmentId=" + appointmentId;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Το αρχείο επίσκεψης καταχωρήθηκε επιτυχώς!");
        return "redirect:/vets/schedule";
    }
}
