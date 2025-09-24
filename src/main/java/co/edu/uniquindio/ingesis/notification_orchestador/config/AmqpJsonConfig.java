package co.edu.uniquindio.ingesis.notification_orchestador.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AmqpJsonConfig {

    @Bean
    public ClassMapper classMapper() {
        DefaultClassMapper mapper = new DefaultClassMapper();
        Map<String, Class<?>> idMap = new HashMap<>();

        // Mapea el __TypeId__ que manda auth-app → a tus clases locales del orquestador
        idMap.put("com.example.usermanagement.events.UserLoginEvent",
                co.edu.uniquindio.ingesis.notification_orchestador.events.UserLoginEvent.class);
        idMap.put("com.example.usermanagement.events.UserCreatedEvent",
                co.edu.uniquindio.ingesis.notification_orchestador.events.UserCreatedEvent.class);
        idMap.put("com.example.usermanagement.events.PasswordChangedEvent",
                co.edu.uniquindio.ingesis.notification_orchestador.events.PasswordChangedEvent.class);
        idMap.put("com.example.usermanagement.events.PasswordRecoveryRequestedEvent",
                co.edu.uniquindio.ingesis.notification_orchestador.events.PasswordRecoveryRequestedEvent.class);

        mapper.setIdClassMapping(idMap);
        mapper.setTrustedPackages("*"); // opcional: acepta tipos de cualquier paquete
        return mapper;
    }

    @Bean
    public MessageConverter messageConverter(ClassMapper classMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter mc) {
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(mc); // publica JSON
        return rt;
    }

    // Importante: el nombre "rabbitListenerContainerFactory" es el default que usará @RabbitListener
    @Bean(name = "rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf, MessageConverter mc) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(mc); // consume JSON → objetos
        return f;
    }
}
