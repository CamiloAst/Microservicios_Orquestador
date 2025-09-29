package co.edu.uniquindio.ingesis.notification_orchestador.entity;
import lombok.*;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    // No usar @Id ni @GeneratedValue porque no es entidad JPA aquí
    private Long id;
    private String title;
    private String message;
    private String recipient;
    private String phoneNumber;
    private Channel channel;
    private NotificationStatus status;
    private Instant scheduledAt;
    private Instant createdAt;
    private Instant sentAt;
}
