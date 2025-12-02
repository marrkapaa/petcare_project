package gr.hua.dit.petcare.core.model;

public enum AppointmentStatus {
    PENDING,        // αναμένει επιβεβαίωση κτηνιάτρου
    CONFIRMED,      // επιβεβαιωμένο ραντεβού
    COMPLETED,      // ολοκληρωμένη επίσκεψη (μπορεί να έχει VisitRecord)
    CANCELLED_OWNER, // ακυρώθηκε από τον ιδιοκτήτη
    CANCELLED_VET   // ακυρώθηκε από τον κτηνίατρο
}
