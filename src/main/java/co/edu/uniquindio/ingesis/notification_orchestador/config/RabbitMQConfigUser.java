package co.edu.uniquindio.ingesis.notification_orchestador.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfigUser {

    public static final String USER_EVENTS_EXCHANGE = "user.events.exchange";
    public static final String USER_EVENTS_QUEUE = "user.events.queue";
    public static final String USER_EVENTS_ROUTING_KEY = "user.events.routingkey";

    @Bean
    public DirectExchange userEventsExchange() {
        return new DirectExchange(USER_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue userEventsQueue() {
        return new Queue(USER_EVENTS_QUEUE);
    }

    @Bean
    public Binding bindingUserEvents(Queue userEventsQueue, DirectExchange userEventsExchange) {
        return BindingBuilder.bind(userEventsQueue).to(userEventsExchange).with(USER_EVENTS_ROUTING_KEY);
    }
}
