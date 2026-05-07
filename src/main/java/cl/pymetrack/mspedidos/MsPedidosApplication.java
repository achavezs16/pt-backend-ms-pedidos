package cl.pymetrack.mspedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRabbit
@EnableScheduling
public class MsPedidosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsPedidosApplication.class, args);
    }

}
