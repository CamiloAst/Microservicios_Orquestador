package co.edu.uniquindio.ingesis.notification_orchestador.config;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfigMonitoring {

    public static final String MONITOR_EVENTS_EXCHANGE = "monitor.events.exchange";
    public static final String MONITOR_EVENTS_QUEUE = "monitor.events.queue";
    public static final String MONITOR_EVENTS_ROUTING_KEY = "monitor.events.routingkey";

    @Bean
    public Queue monitorEventsQueue() {
        return new Queue(MONITOR_EVENTS_QUEUE, true);
    }

    @Bean
    public DirectExchange monitorEventsExchange() {
        return new DirectExchange(MONITOR_EVENTS_EXCHANGE);
    }

    @Bean
    public Binding bindingMonitorEvents(Queue monitorEventsQueue, DirectExchange monitorEventsExchange) {
        return BindingBuilder.bind(monitorEventsQueue)
                .to(monitorEventsExchange)
                .with(MONITOR_EVENTS_ROUTING_KEY);
    }
}