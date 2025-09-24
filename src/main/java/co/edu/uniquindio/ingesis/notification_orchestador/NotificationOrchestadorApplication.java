package co.edu.uniquindio.ingesis.notification_orchestador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.amqp.rabbit.annotation.EnableRabbit
public class NotificationOrchestadorApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationOrchestadorApplication.class, args);
	}

}
