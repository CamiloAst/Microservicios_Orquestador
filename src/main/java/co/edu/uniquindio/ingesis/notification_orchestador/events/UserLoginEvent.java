package co.edu.uniquindio.ingesis.notification_orchestador.events;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserLoginEvent {
    private String username;
    private String email;
    private String phoneNumber;
    private String timestamp;
}