package co.edu.uniquindio.ingesis.notification_orchestador.listeners;

import co.edu.uniquindio.ingesis.notification_orchestador.config.RabbitMQConfigUser;
import co.edu.uniquindio.ingesis.notification_orchestador.entity.Channel;
import co.edu.uniquindio.ingesis.notification_orchestador.entity.Notification;
import co.edu.uniquindio.ingesis.notification_orchestador.events.PasswordChangedEvent;
import co.edu.uniquindio.ingesis.notification_orchestador.events.PasswordRecoveryRequestedEvent;
import co.edu.uniquindio.ingesis.notification_orchestador.events.UserCreatedEvent;
import co.edu.uniquindio.ingesis.notification_orchestador.events.UserLoginEvent;
import co.edu.uniquindio.ingesis.notification_orchestador.messaging.NotificationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventsListener {

    private final NotificationProducer notificationProducer;

    @RabbitListener(queues = RabbitMQConfigUser.USER_EVENTS_QUEUE)
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        String message = "Hola " + event.getFullName() + ", por favor confirma tu email para activar tu cuenta.";

        Notification notification = Notification.builder()
                .recipient(event.getEmail())
                .message(message)
                .channel(Channel.EMAIL)
                .build();

        notificationProducer.sendNotification(notification);
    }

    @RabbitListener(queues = RabbitMQConfigUser .USER_EVENTS_QUEUE)
    public void handleUserLoginEvent(UserLoginEvent event) {
        String message = "Usted ha iniciado sesión para activar tu cuenta.";

        Notification notification = Notification.builder()
                .recipient(event.getEmail())
                .message(message)
                .channel(Channel.EMAIL) // Puedes enviar también SMS si quieres
                .build();
        Notification notificationSms = Notification.builder()
                .recipient(event.getEmail())
                .message(message)
                .channel(Channel.SMS)
                .build();

        notificationProducer.sendNotification(notification);
        notificationProducer.sendNotification(notificationSms);
        // Si quieres enviar SMS también, crea otra notificación con Channel.SMS
    }

    @RabbitListener(queues = RabbitMQConfigUser .USER_EVENTS_QUEUE)
    public void handlePasswordRecoveryRequestedEvent(PasswordRecoveryRequestedEvent event) {
        String message = "Has solicitado recuperar tu contraseña. Usa este enlace para restablecerla: <a href='https://tuapp.com/recover?user=" + event.getUserId() + "'>Recuperar contraseña</a>";

        Notification notification = Notification.builder()
                .recipient(event.getEmail())
                .message(message)
                .channel(Channel.EMAIL)
                .build();

        notificationProducer.sendNotification(notification);
    }

    @RabbitListener(queues = RabbitMQConfigUser .USER_EVENTS_QUEUE)
    public void handlePasswordChangedEvent(PasswordChangedEvent event) {
        String message = "Tu contraseña ha sido actualizada exitosamente. Si no fuiste tú, contacta soporte inmediatamente.";

        Notification notificationEmail = Notification.builder()
                .recipient(event.getEmail())
                .message(message)
                .channel(Channel.EMAIL)
                .build();

        Notification notificationSms = Notification.builder()
                .recipient(event.getEmail())
                .message(message)
                .channel(Channel.SMS)
                .build();

        notificationProducer.sendNotification(notificationEmail);
        notificationProducer.sendNotification(notificationSms);
    }
}

