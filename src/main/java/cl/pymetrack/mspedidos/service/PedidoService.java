package cl.pymetrack.mspedidos.service;

import cl.pymetrack.mspedidos.dto.ActualizarEstadoPedidoRequest;
import cl.pymetrack.mspedidos.entity.Pedido;
import cl.pymetrack.mspedidos.event.PedidoEstadoEvent;
import cl.pymetrack.mspedidos.messaging.PedidoEventPublisher;
import cl.pymetrack.mspedidos.model.EstadoPedido;
import cl.pymetrack.mspedidos.repository.PedidoRepository;
import cl.pymetrack.mspedidos.dto.CrearPedidoItemRequest;
import cl.pymetrack.mspedidos.dto.CrearPedidoRequest;
import cl.pymetrack.mspedidos.entity.PedidoItem;
import cl.pymetrack.mspedidos.event.PedidoItemEvent;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
    public Pedido crearPedido(CrearPedidoRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        }

        Pedido pedido = new Pedido();

        pedido.setIdPyme(request.getIdPyme());
        pedido.setNumeroOrdenPyme(request.getNumeroOrdenPyme());
        pedido.setNombreCliente(request.getNombreCliente());
        pedido.setEmailCliente(request.getEmailCliente());
        pedido.setTelefonoCliente(request.getTelefonoCliente());
        pedido.setDireccionEntregaChile(request.getDireccionEntregaChile());
        pedido.setComunaEntregaChile(request.getComunaEntregaChile());
        pedido.setRegionEntregaChile(request.getRegionEntregaChile());

        pedido.setSubtotal(request.getSubtotal());
        pedido.setCostoDespachoChile(
                request.getCostoDespachoChile() != null
                        ? request.getCostoDespachoChile()
                        : BigDecimal.ZERO
        );
        pedido.setTotalPedido(request.getTotalPedido());

        pedido.setEtiquetaDespachoPyme(request.getEtiquetaDespachoPyme());
        pedido.setNotasPedido(request.getNotasPedido());

        pedido.setItems(new ArrayList<>());

        for (CrearPedidoItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getCantidad() == null || itemRequest.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad del producto debe ser mayor a cero");
            }

            PedidoItem item = new PedidoItem();
            item.setPedido(pedido);
            item.setProductoId(itemRequest.getProductoId());
            item.setNombreProducto(itemRequest.getNombreProducto());
            item.setCantidad(itemRequest.getCantidad());
            item.setPrecioUnitario(itemRequest.getPrecioUnitario());

            pedido.getItems().add(item);
        }

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido actualizarEstado(Long pedidoId, ActualizarEstadoPedidoRequest request) {
        Pedido pedido = findById(pedidoId);

        String estadoAnterior = pedido.getEstadoPedidoPyme().name();
        EstadoPedido nuevoEstado = EstadoPedido.valueOf(request.getEstado());

        pedido.setEstadoPedidoPyme(nuevoEstado);
        pedido.setActualizadoEn(LocalDateTime.now());

        List<PedidoItemEvent> items = pedido.getItems()
                .stream()
                .map(item -> new PedidoItemEvent(
                        item.getProductoId(),
                        item.getNombreProducto(),
                        item.getCantidad()
                ))
                .collect(Collectors.toList());

        Pedido pedidoActualizado = pedidoRepository.save(pedido);

        PedidoEstadoEvent event = new PedidoEstadoEvent(
                pedidoActualizado.getId(),
                pedidoActualizado.getIdPyme(),
                estadoAnterior,
                nuevoEstado.name(),
                request.getRepartidorId(),
                request.getObservacion(),
                items
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
