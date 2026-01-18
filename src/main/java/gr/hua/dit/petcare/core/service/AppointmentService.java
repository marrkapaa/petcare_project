package gr.hua.dit.petcare.core.service;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.AppointmentStatus;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.model.VisitRecord;
import gr.hua.dit.petcare.core.repositories.AppointmentRepository;
import gr.hua.dit.petcare.core.repositories.VisitRecordRepository;
import gr.hua.dit.petcare.core.ports.NotificationPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final VisitRecordRepository visitRecordRepository;
    private final NotificationPort notificationPort;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              VisitRecordRepository visitRecordRepository,
                              NotificationPort notificationPort) {
        this.appointmentRepository = appointmentRepository;
        this.visitRecordRepository = visitRecordRepository;
        this.notificationPort = notificationPort;
    }

    @Transactional
    public Appointment createAppointment(Appointment newAppointment) {

        User vet = newAppointment.getVeterinarian();
        LocalDateTime startTime = newAppointment.getDateTime();
        LocalDateTime endTime = startTime.plusMinutes(30);

        // έλεγχος Επικάλυψης Κτηνιάτρου
        List<Appointment> conflictingAppointments = appointmentRepository
            .findByVeterinarianAndDateTimeBetween(vet, startTime, endTime);

        if (!conflictingAppointments.isEmpty()) {
            throw new IllegalArgumentException("Ο κτηνίατρος " + vet.getUsername() + " δεν είναι διαθέσιμος την ώρα αυτή.");
        }

        LocalDateTime requiredMinimumTime = startTime.minus(7, ChronoUnit.DAYS);

        List<Appointment> recentAppointments = appointmentRepository
            .findByPetAndDateTimeAfter(newAppointment.getPet(), requiredMinimumTime);

        if (!recentAppointments.isEmpty()) {
            LocalDateTime latestAppointmentTime = recentAppointments.stream()
                .map(Appointment::getDateTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);

            throw new IllegalArgumentException(
                "Το κατοικίδιο " + newAppointment.getPet().getName() +
                    " είχε πρόσφατο ραντεβού στις " + latestAppointmentTime.toLocalDate() +
                    ". Πρέπει να μεσολαβήσουν τουλάχιστον 7 ημέρες."
            );
        }

        Appointment savedAppointment = appointmentRepository.save(newAppointment);

        // external service
        String message = String.format("Υπενθύμιση: Ραντεβού για %s στις %s.",
            savedAppointment.getPet().getName(),
            savedAppointment.getDateTime());

        notificationPort.sendNotification(
            savedAppointment.getPet().getOwner().getEmail(),
            message
        );

        return savedAppointment;
    }

    @Transactional
    public VisitRecord saveVisitRecord(VisitRecord record) {
        Appointment appointment = appointmentRepository.findById(record.getAppointment().getId())
            .orElseThrow(() -> new IllegalArgumentException("Appointment not found for this record."));

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        return visitRecordRepository.save(record);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Το ραντεβού δεν βρέθηκε"));

        appointment.setStatus(AppointmentStatus.CANCELLED_OWNER);
        appointmentRepository.save(appointment);
    }

    public Appointment findAppointmentById(Long id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Το ραντεβού δεν βρέθηκε: " + id));
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsByOwner(User owner) {
        return appointmentRepository.findAppointmentsWithOwnerDetails(owner);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsByVeterinarian(User veterinarian) {
        return appointmentRepository.findScheduleByVeterinarian(veterinarian);
    }
}
