package cl.pymetrack.mspedidos.repository;

import cl.pymetrack.mspedidos.entity.Pedido;
import cl.pymetrack.mspedidos.model.EstadoPedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Override
    @EntityGraph(attributePaths = "items")
    List<Pedido> findAll();

    @EntityGraph(attributePaths = "items")
    List<Pedido> findAllByOrderByCreadoEnDesc();

    Optional<Pedido> findByNumeroOrdenPyme(String numeroOrdenPyme);

    @EntityGraph(attributePaths = "items")
    List<Pedido> findByIdPymeOrderByCreadoEnDesc(Long idPyme);

    @EntityGraph(attributePaths = "items")
    List<Pedido> findByEstadoPedidoPymeOrderByCreadoEnDesc(EstadoPedido estadoPedidoPyme);

    @EntityGraph(attributePaths = "items")
    List<Pedido> findByIdPymeAndEstadoPedidoPymeOrderByCreadoEnDesc(Long idPyme, EstadoPedido estadoPedidoPyme);
}