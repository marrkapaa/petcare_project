package gr.hua.dit.petcare.core.repositories;

import gr.hua.dit.petcare.core.model.VisitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {
    Optional<VisitRecord> findByAppointment_Id(Long appointmentId);

    List<VisitRecord> findByAppointment_Pet_Id(Long petId);
}
