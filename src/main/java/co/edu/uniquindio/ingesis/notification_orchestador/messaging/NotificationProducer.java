package co.edu.uniquindio.ingesis.notification_orchestador.messaging;

import co.edu.uniquindio.ingesis.notification_orchestador.config.RabbitMQConfigNotification;
import co.edu.uniquindio.ingesis.notification_orchestador.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(Notification notification) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfigNotification.NOTIFICATION_EXCHANGE,
                RabbitMQConfigNotification.NOTIFICATION_ROUTING_KEY,
                notification
        );
    }
}
