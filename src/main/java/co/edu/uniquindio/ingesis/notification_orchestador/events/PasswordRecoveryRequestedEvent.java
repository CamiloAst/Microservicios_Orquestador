package co.edu.uniquindio.ingesis.notification_orchestador.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordRecoveryRequestedEvent {
    private Long userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String timestamp;
}