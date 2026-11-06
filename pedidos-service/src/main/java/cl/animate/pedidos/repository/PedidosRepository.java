package cl.animate.pedidos.repository;

import cl.animate.pedidos.model.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidosRepository extends JpaRepository<Pedido, String> {
  /** Historial de un usuario, de la compra más nueva a la más antigua. */
  List<Pedido> findByUsuarioIdOrderByCreadoDesc(Long usuarioId);

  /** Todas las ventas (panel admin). */
  List<Pedido> findAllByOrderByCreadoDesc();
}
