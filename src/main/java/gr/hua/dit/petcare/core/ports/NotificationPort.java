package gr.hua.dit.petcare.core.ports;

public interface NotificationPort {
    boolean sendNotification(String recipient, String message);
}
