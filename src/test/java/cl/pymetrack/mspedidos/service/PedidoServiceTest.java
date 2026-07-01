package cl.pymetrack.mspedidos.service;

import cl.pymetrack.mspedidos.dto.ActualizarEstadoPedidoRequest;
import cl.pymetrack.mspedidos.dto.CrearPedidoItemRequest;
import cl.pymetrack.mspedidos.dto.CrearPedidoRequest;
import cl.pymetrack.mspedidos.entity.Pedido;
import cl.pymetrack.mspedidos.entity.PedidoItem;
import cl.pymetrack.mspedidos.event.PedidoEstadoEvent;
import cl.pymetrack.mspedidos.messaging.PedidoEventPublisher;
import cl.pymetrack.mspedidos.model.EstadoPedido;
import cl.pymetrack.mspedidos.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoEventPublisher pedidoEventPublisher;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedidoFicticio;

    @BeforeEach
    void setUp() {
        // Preparamos un pedido base para reutilizar en varias pruebas
        pedidoFicticio = new Pedido();
        pedidoFicticio.setId(1L);
        pedidoFicticio.setIdPyme(100L);
        
        // Asumiendo que CREADO es un valor válido en tu enum EstadoPedido
        // Usamos values()[0] como fallback por si acaso, pero lo normal sería EstadoPedido.CREADO
        pedidoFicticio.setEstadoPedidoPyme(EstadoPedido.values()[0]); 
        
        PedidoItem item = new PedidoItem();
        item.setProductoId(5L);
        item.setNombreProducto("Producto Test");
        item.setCantidad(2);
        
        List<PedidoItem> items = new ArrayList<>();
        items.add(item);
        pedidoFicticio.setItems(items);
    }

    @Test
    void testFindAll_DebeRetornarListaDePedidos() {
        Mockito.when(this.pedidoRepository.findAllByOrderByCreadoEnDesc()).thenReturn(List.of(this.pedidoFicticio));
        List<Pedido> resultado = pedidoService.findAll();
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    void testFindByPyme_DebeRetornarPedidosDeLaPyme() {
        when(pedidoRepository.findByIdPymeOrderByCreadoEnDesc(100L)).thenReturn(List.of(pedidoFicticio));
        List<Pedido> resultado = pedidoService.findByPyme(100L);
        assertEquals(100L, resultado.get(0).getIdPyme());
    }

    @Test
    void testFindById_CuandoExiste_DebeRetornarPedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoFicticio));
        Pedido resultado = pedidoService.findById(1L);
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void testFindById_CuandoNoExiste_DebeLanzarExcepcion() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.findById(99L);
        });
        assertTrue(ex.getMessage().contains("Pedido no encontrado"));
    }

    @Test
    void testCrearPedido_SinItems_DebeLanzarExcepcion() {
        CrearPedidoRequest request = new CrearPedidoRequest();
        request.setItems(new ArrayList<>()); // Lista vacía

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.crearPedido(request);
        });
        assertEquals("El pedido debe tener al menos un producto", ex.getMessage());
    }

    @Test
    void testCrearPedido_CantidadCero_DebeLanzarExcepcion() {
        CrearPedidoRequest request = new CrearPedidoRequest();
        CrearPedidoItemRequest item = new CrearPedidoItemRequest();
        item.setCantidad(0); // Cantidad inválida
        request.setItems(List.of(item));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.crearPedido(request);
        });
        assertEquals("La cantidad del producto debe ser mayor a cero", ex.getMessage());
    }

    @Test
    void testCrearPedido_Exitoso() {
        // Arrange
        CrearPedidoRequest request = new CrearPedidoRequest();
        request.setIdPyme(100L);
        request.setCostoDespachoChile(BigDecimal.valueOf(2500));
        
        CrearPedidoItemRequest item = new CrearPedidoItemRequest();
        item.setProductoId(1L);
        item.setCantidad(2);
        item.setPrecioUnitario(BigDecimal.valueOf(5000));
        request.setItems(List.of(item));

        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pedido guardado = pedidoService.crearPedido(request);

        // Assert
        assertNotNull(guardado);
        assertEquals(100L, guardado.getIdPyme());
        assertEquals(1, guardado.getItems().size());
        assertEquals(BigDecimal.valueOf(2500), guardado.getCostoDespachoChile());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

@Test
    void testActualizarEstado_PublicaEventoExitosamente() {
        // Arrange
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoFicticio));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActualizarEstadoPedidoRequest request = new ActualizarEstadoPedidoRequest();
        request.setEstado("ASIGNADO"); // Estado válido en tu Enum
        request.setRepartidorId(50L);
        request.setObservacion("Preparando envío");

        // Act
        Pedido actualizado = pedidoService.actualizarEstado(1L, request);

        // Assert
        assertEquals("ASIGNADO", actualizado.getEstadoPedidoPyme().name());
        
        // Capturar y verificar el evento publicado a RabbitMQ
        ArgumentCaptor<PedidoEstadoEvent> captor = ArgumentCaptor.forClass(PedidoEstadoEvent.class);
        verify(pedidoEventPublisher, times(1)).publicarCambioEstado(captor.capture());
        
        PedidoEstadoEvent eventoPublicado = captor.getValue();
        assertEquals(1L, eventoPublicado.getPedidoId());
        assertEquals("ASIGNADO", eventoPublicado.getEstadoNuevo());
        assertEquals(50L, eventoPublicado.getRepartidorId());
    }

    @Test
    void testAceptarPedido_LlamaActualizarEstadoConAsignado() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoFicticio));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido actualizado = pedidoService.aceptarPedido(1L, 99L);

        assertEquals("ASIGNADO", actualizado.getEstadoPedidoPyme().name());
        verify(pedidoEventPublisher, times(1)).publicarCambioEstado(any(PedidoEstadoEvent.class));
    }

    @Test
    void testRechazarPedido_LlamaActualizarEstadoConRechazado() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoFicticio));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido actualizado = pedidoService.rechazarPedido(1L, 99L);

        assertEquals("RECHAZADO", actualizado.getEstadoPedidoPyme().name());
        verify(pedidoEventPublisher, times(1)).publicarCambioEstado(any(PedidoEstadoEvent.class));
    }
}