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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RabbitListener(queues = RabbitMQConfigUser.USER_EVENTS_QUEUE, containerFactory = "rabbitListenerContainerFactory")
public class UserEventsListener {

    private final NotificationProducer notificationProducer;
    private static final Logger log = LoggerFactory.getLogger(UserEventsListener.class);


    @RabbitHandler
    @RabbitListener(queues ="user.events")
    public void handle(UserLoginEvent event) {
        String message = "Usted ha iniciado sesión para activar tu cuenta.";
        log.info("📥 Evento recibido desde AuthService: {}", event.getUsername());
        notificationProducer.sendNotification(Notification.builder()
                .recipient(event.getEmail())
                .message(message)
                .channel(Channel.EMAIL)
                .build());
        notificationProducer.sendNotification(Notification.builder()
                .recipient(event.getPhoneNumber())
                .message(message)
                .channel(Channel.SMS)
                .build());
    }

    @RabbitHandler
    public void handle(UserCreatedEvent event) {
        String message = "Hola " + event.getUsername() + ", por favor confirma tu email para activar tu cuenta.";
        notificationProducer.sendNotification(Notification.builder()
                .recipient(event.getEmail())
                .message(message)
                .channel(Channel.EMAIL)
                .build());
        notificationProducer.sendNotification(Notification.builder()
                .recipient(event.getPhoneNumber()) // aquí usas el phone del usuario
                .message("Hola " + event.getUsername() +
                        ", revisa tu correo para activar tu cuenta.") // mensaje más corto para SMS
                .channel(Channel.SMS)
                .build());
    }

    @RabbitHandler
    public void handle(PasswordRecoveryRequestedEvent event) {
        String message = "Has solicitado recuperar tu contraseña. Usa este enlace para restablecerla: "
                + "https://tuapp.com/recover?user=" + event.getUserId();
        notificationProducer.sendNotification(Notification.builder()
                .recipient(event.getEmail())
                .message(message)
                .channel(Channel.EMAIL)
                .build());
    }

    @RabbitHandler
    public void handle(PasswordChangedEvent event) {
        String message = "Tu contraseña ha sido actualizada exitosamente. Si no fuiste tú, contacta soporte.";
        notificationProducer.sendNotification(Notification.builder()
                .recipient(event.getEmail())
                .message(message)
                .channel(Channel.EMAIL)
                .build());
        notificationProducer.sendNotification(Notification.builder()
                .recipient(event.getPhoneNumber())
                .message(message)
                .channel(Channel.SMS)
                .build());
    }
}
