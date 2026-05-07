package cl.pymetrack.mspedidos.service;

import cl.pymetrack.mspedidos.dto.ActualizarEstadoPedidoRequest;
import cl.pymetrack.mspedidos.entity.Pedido;
import cl.pymetrack.mspedidos.event.PedidoEstadoEvent;
import cl.pymetrack.mspedidos.messaging.PedidoEventPublisher;
import cl.pymetrack.mspedidos.model.EstadoPedido;
import cl.pymetrack.mspedidos.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher pedidoEventPublisher;

    public PedidoService(PedidoRepository pedidoRepository, PedidoEventPublisher pedidoEventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoEventPublisher = pedidoEventPublisher;
    }

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> findByPyme(Long pymeId) {
        return pedidoRepository.findByIdPymeOrderByCreadoEnDesc(pymeId);
    }

    public Pedido findById(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + id));
    }

    public Pedido save(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido actualizarEstado(Long pedidoId, ActualizarEstadoPedidoRequest request) {
        Pedido pedido = findById(pedidoId);

        String estadoAnterior = pedido.getEstadoPedidoPyme().name();
        EstadoPedido nuevoEstado = EstadoPedido.valueOf(request.getEstado());

        pedido.setEstadoPedidoPyme(nuevoEstado);
        pedido.setActualizadoEn(LocalDateTime.now());

        Pedido pedidoActualizado = pedidoRepository.save(pedido);

        PedidoEstadoEvent event = new PedidoEstadoEvent(
                pedidoActualizado.getId(),
                pedidoActualizado.getIdPyme(),
                estadoAnterior,
                nuevoEstado.name(),
                request.getRepartidorId(),
                request.getObservacion()
        );

        pedidoEventPublisher.publicarCambioEstado(event);

        return pedidoActualizado;
    }

    @Transactional
    public Pedido aceptarPedido(Long pedidoId, Long repartidorId) {
        ActualizarEstadoPedidoRequest request = new ActualizarEstadoPedidoRequest();
        request.setEstado("ASIGNADO");
        request.setRepartidorId(repartidorId);
        request.setObservacion("Pedido aceptado por repartidor");

        return actualizarEstado(pedidoId, request);
    }

    @Transactional
    public Pedido rechazarPedido(Long pedidoId, Long repartidorId) {
        ActualizarEstadoPedidoRequest request = new ActualizarEstadoPedidoRequest();
        request.setEstado("RECHAZADO");
        request.setRepartidorId(repartidorId);
        request.setObservacion("Pedido rechazado por repartidor");

        return actualizarEstado(pedidoId, request);
    }
}
