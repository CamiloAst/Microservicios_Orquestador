package co.edu.uniquindio.ingesis.notification_orchestador.listeners;

import co.edu.uniquindio.ingesis.notification_orchestador.config.RabbitMQConfigMonitoring;
import co.edu.uniquindio.ingesis.notification_orchestador.entity.Channel;
import co.edu.uniquindio.ingesis.notification_orchestador.entity.Notification;
import co.edu.uniquindio.ingesis.notification_orchestador.events.ServiceDownEvent;
import co.edu.uniquindio.ingesis.notification_orchestador.messaging.NotificationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonitorEventsListener {

    private final NotificationProducer notificationProducer;

    @RabbitListener(queues = RabbitMQConfigMonitoring.MONITOR_EVENTS_QUEUE)
    public void handleServiceDownEvent(ServiceDownEvent event) {
        System.out.println("📩 Evento recibido del MonitoringService: " + event.getServiceName() + " -> " + event.getStatus());

        String message = switch (event.getStatus()) {
            case "DOWN" -> "🚨 ALERTA: El servicio " + event.getServiceName() + " (" + event.getServiceUrl() + ") ha caído.";
            case "UP" -> "✅ RECUPERADO: El servicio " + event.getServiceName() + " está en línea nuevamente.";
            default -> "⚠️ Estado desconocido del servicio " + event.getServiceName();
        };

        Notification notification = Notification.builder()
                .recipient("admin@correo.com") // puedes cambiarlo a lista si prefieres
                .message(message)
                .channel(Channel.EMAIL)
                .build();

        notificationProducer.sendNotification(notification);
    }
}