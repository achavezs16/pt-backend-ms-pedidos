package cl.pymetrack.mspedidos.controller;

import cl.pymetrack.mspedidos.entity.Pedido;
import cl.pymetrack.mspedidos.repository.PedidoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {
    
    private final PedidoRepository pedidoRepository;
    
    public PedidoController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }
    
    @GetMapping
    public ResponseEntity<List<Pedido>> getAllPedidos() {
        return ResponseEntity.ok(pedidoRepository.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> getPedidoById(@PathVariable Long id) {
        Optional<Pedido> pedido = pedidoRepository.findById(id);
        return pedido.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/pyme/{pymeId}")
    public ResponseEntity<List<Pedido>> getPedidosByPyme(@PathVariable Long pymeId) {
        List<Pedido> pedidos = pedidoRepository.findByIdPymeOrderByCreadoEnDesc(pymeId);
        return ResponseEntity.ok(pedidos);
    }
    
    @GetMapping("/numero-orden/{numeroOrden}")
    public ResponseEntity<Pedido> getPedidoByNumeroOrden(@PathVariable String numeroOrden) {
        Optional<Pedido> pedido = pedidoRepository.findByNumeroOrdenPyme(numeroOrden);
        return pedido.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Pedido> createPedido(@RequestBody Pedido pedido) {
        Pedido savedPedido = pedidoRepository.save(pedido);
        return ResponseEntity.ok(savedPedido);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Pedido> updatePedido(@PathVariable Long id, @RequestBody Pedido pedido) {
        if (!pedidoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        pedido.setId(id);
        Pedido updatedPedido = pedidoRepository.save(pedido);
        return ResponseEntity.ok(updatedPedido);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePedido(@PathVariable Long id) {
        if (!pedidoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        pedidoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
