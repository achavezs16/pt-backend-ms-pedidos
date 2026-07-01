package cl.pymetrack.mspedidos.controller;

import cl.pymetrack.mspedidos.dto.ActualizarEstadoPedidoRequest;
import cl.pymetrack.mspedidos.entity.Pedido;
import cl.pymetrack.mspedidos.service.PedidoService;
import cl.pymetrack.mspedidos.dto.CrearPedidoRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> findAll() {
        return ResponseEntity.ok(pedidoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> findById(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.findById(id));
    }

    @GetMapping("/pyme/{pymeId}")
    public ResponseEntity<List<Pedido>> findByPyme(@PathVariable Long pymeId) {
        return ResponseEntity.ok(pedidoService.findByPyme(pymeId));
    }

    @PostMapping
    public ResponseEntity<Pedido> create(@RequestBody CrearPedidoRequest request) {
        return ResponseEntity.ok(pedidoService.crearPedido(request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Pedido> actualizarEstado(
            @PathVariable Long id,
            @RequestBody ActualizarEstadoPedidoRequest request
    ) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, request));
    }

    @PostMapping("/{id}/aceptar")
    public ResponseEntity<Pedido> aceptarPedido(
            @PathVariable Long id,
            @RequestParam Long repartidorId
    ) {
        return ResponseEntity.ok(pedidoService.aceptarPedido(id, repartidorId));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<Pedido> rechazarPedido(
            @PathVariable Long id,
            @RequestParam Long repartidorId
    ) {
        return ResponseEntity.ok(pedidoService.rechazarPedido(id, repartidorId));
    }
}
