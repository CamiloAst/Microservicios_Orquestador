package co.edu.uniquindio.ingesis.notification_orchestador.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserCreatedEvent {
    private String email;
    private String fullName;
    private String phoneNumber;
}