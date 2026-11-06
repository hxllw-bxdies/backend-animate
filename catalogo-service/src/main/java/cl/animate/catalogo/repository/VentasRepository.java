package cl.animate.catalogo.repository;

import cl.animate.catalogo.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentasRepository extends JpaRepository<Venta, String> {}
