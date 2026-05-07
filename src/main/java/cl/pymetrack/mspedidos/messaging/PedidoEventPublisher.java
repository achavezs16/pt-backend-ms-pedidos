package cl.pymetrack.mspedidos.messaging;

import cl.pymetrack.mspedidos.config.RabbitMQConfig;
import cl.pymetrack.mspedidos.event.PedidoEstadoEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PedidoEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarCambioEstado(PedidoEstadoEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PEDIDOS_EXCHANGE,
                RabbitMQConfig.PEDIDO_ESTADO_ROUTING_KEY,
                event
        );
    }
}