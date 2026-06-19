package cl.pymetrack.mspedidos.controller;

import cl.pymetrack.mspedidos.dto.ActualizarEstadoPedidoRequest;
import cl.pymetrack.mspedidos.dto.CrearPedidoRequest;
import cl.pymetrack.mspedidos.entity.Pedido;
import cl.pymetrack.mspedidos.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    private Pedido pedidoFicticio;

    @BeforeEach
    void setUp() {
        pedidoFicticio = new Pedido();
        pedidoFicticio.setId(1L);
        pedidoFicticio.setIdPyme(100L);
        pedidoFicticio.setNombreCliente("Juan Pérez");
    }

    @Test
    void testFindAll_DebeRetornarListaYStatus200() throws Exception {
        when(pedidoService.findAll()).thenReturn(List.of(pedidoFicticio));

        mockMvc.perform(get("/pedidos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].idPyme").value(100L));
    }

    @Test
    void testFindById_DebeRetornarPedidoYStatus200() throws Exception {
        when(pedidoService.findById(1L)).thenReturn(pedidoFicticio);

        mockMvc.perform(get("/pedidos/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombreCliente").value("Juan Pérez"));
    }

    @Test
    void testFindByPyme_DebeRetornarListaPorPymeYStatus200() throws Exception {
        when(pedidoService.findByPyme(100L)).thenReturn(List.of(pedidoFicticio));

        mockMvc.perform(get("/pedidos/pyme/{pymeId}", 100L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPyme").value(100L));
    }

    @Test
    void testCreate_DebeCrearPedidoYStatus200() throws Exception {
        CrearPedidoRequest request = new CrearPedidoRequest();
        request.setIdPyme(100L);
        // El objectMapper convierte nuestro objeto Java en un JSON real
        String jsonRequest = objectMapper.writeValueAsString(request);

        when(pedidoService.crearPedido(any(CrearPedidoRequest.class))).thenReturn(pedidoFicticio);

        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testActualizarEstado_DebeActualizarYStatus200() throws Exception {
        ActualizarEstadoPedidoRequest request = new ActualizarEstadoPedidoRequest();
        request.setEstado("ASIGNADO");
        String jsonRequest = objectMapper.writeValueAsString(request);

        when(pedidoService.actualizarEstado(eq(1L), any(ActualizarEstadoPedidoRequest.class))).thenReturn(pedidoFicticio);

        mockMvc.perform(patch("/pedidos/{id}/estado", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testAceptarPedido_DebeAceptarYStatus200() throws Exception {
        when(pedidoService.aceptarPedido(1L, 50L)).thenReturn(pedidoFicticio);

        mockMvc.perform(post("/pedidos/{id}/aceptar", 1L)
                .param("repartidorId", "50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testRechazarPedido_DebeRechazarYStatus200() throws Exception {
        when(pedidoService.rechazarPedido(1L, 50L)).thenReturn(pedidoFicticio);

        mockMvc.perform(post("/pedidos/{id}/rechazar", 1L)
                .param("repartidorId", "50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}