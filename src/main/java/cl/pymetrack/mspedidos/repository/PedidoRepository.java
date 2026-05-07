package cl.pymetrack.mspedidos.repository;

import cl.pymetrack.mspedidos.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    Optional<Pedido> findByNumeroOrdenPyme(String numeroOrdenPyme);
    
    List<Pedido> findByIdPymeOrderByCreadoEnDesc(Long idPyme);
    
    List<Pedido> findByEstadoPedidoPymeOrderByCreadoEnDesc(Pedido.EstadoPedidoPyme estado);
    
    List<Pedido> findByIdPymeAndEstadoPedidoPymeOrderByCreadoEnDesc(Long idPyme, Pedido.EstadoPedidoPyme estado);
}
