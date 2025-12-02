package gr.hua.dit.petcare.core.repositories;

import gr.hua.dit.petcare.core.model.VisitRecord;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {
    //
}
