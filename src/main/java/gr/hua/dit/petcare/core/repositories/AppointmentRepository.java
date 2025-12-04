package gr.hua.dit.petcare.core.repositories;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // br επικάλυψη κτηνιάτρου
    List<Appointment> findByVeterinarianAndDateTimeBetween(
        User veterinarian,
        LocalDateTime start,
        LocalDateTime end
    );

     //br ελάχιστος χρόνος μεταξύ ραντεβού του ιδιου pet
    List<Appointment> findByPetAndDateTimeAfter(Pet pet, LocalDateTime minimumDate);


    @Query("SELECT a FROM Appointment a " +
        "JOIN FETCH a.pet p " +
        "JOIN FETCH a.veterinarian v " +
        "WHERE p.owner = :owner " +
        "ORDER BY a.dateTime ASC")
    List<Appointment> findAppointmentsWithOwnerDetails(@Param("owner") User owner);

    @Query("SELECT a FROM Appointment a " +
        "JOIN FETCH a.pet p " +
        "JOIN FETCH p.owner o " + 
        "JOIN FETCH a.veterinarian v " +
        "WHERE v = :veterinarian " +
        "ORDER BY a.dateTime ASC")
    List<Appointment> findScheduleByVeterinarian(@Param("veterinarian") User veterinarian);

    List<Appointment> findByPet_Owner(User owner);
}
