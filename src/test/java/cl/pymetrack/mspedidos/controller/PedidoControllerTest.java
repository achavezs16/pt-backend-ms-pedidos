package cl.pymetrack.mspedidos.controller;

import cl.pymetrack.mspedidos.entity.Pedido;
import cl.pymetrack.mspedidos.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private PedidoController pedidoController;

    @Test
    void findAll_DeberiaRetornarListaDePedidos() {
        // Preparar
        List<Pedido> pedidosFalsos = new ArrayList<>();
        Pedido p = new Pedido();
        p.setId(1L);
        pedidosFalsos.add(p);

        when(pedidoService.findAll()).thenReturn(pedidosFalsos);

        // Actuar
        ResponseEntity<List<Pedido>> response = pedidoController.findAll();

        // Afirmar
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value()); // Ajuste técnico para Spring moderno
        assertEquals(1, response.getBody().size());
    }
}