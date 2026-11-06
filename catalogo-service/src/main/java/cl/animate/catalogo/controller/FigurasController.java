package cl.animate.catalogo.controller;

import cl.animate.catalogo.dto.FiguraRequest;
import cl.animate.catalogo.dto.FiguraResponse;
import cl.animate.catalogo.dto.ImagenRequest;
import cl.animate.catalogo.dto.PrecioStockRequest;
import cl.animate.catalogo.service.FigurasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Figuras", description = "CRUD de figuras, galería y papelera")
@RestController
@RequestMapping("/api/figuras")
public class FigurasController {
  private final FigurasService service;

  public FigurasController(FigurasService service) {
    this.service = service;
  }

  @Operation(summary = "Lista las figuras del catálogo (público).")
  @GetMapping
  public List<FiguraResponse> listar() {
    return service.listar();
  }

  @Operation(summary = "Lista las figuras en la papelera (ADMIN).")
  @GetMapping("/papelera")
  public List<FiguraResponse> papelera() {
    return service.papelera();
  }

  @Operation(summary = "Obtiene una figura (público).")
  @GetMapping("/{id}")
  public FiguraResponse obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @Operation(summary = "Crea una figura (ADMIN).")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public FiguraResponse crear(@Valid @RequestBody FiguraRequest r) {
    return service.guardar(null, r);
  }

  @Operation(summary = "Edita todos los datos de una figura (ADMIN).")
  @PutMapping("/{id}")
  public FiguraResponse editar(@PathVariable Long id, @Valid @RequestBody FiguraRequest r) {
    return service.guardar(id, r);
  }

  @Operation(summary = "Cambia solo precio y stock (ADMIN).")
  @PutMapping("/{id}/precio-stock")
  public FiguraResponse precioStock(
      @PathVariable Long id, @Valid @RequestBody PrecioStockRequest r) {
    return service.cambiarPrecioYStock(id, r);
  }

  @Operation(summary = "Envía la figura a la papelera (ADMIN).")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id) {
    service.eliminar(id);
  }

  @Operation(summary = "Restaura una figura desde la papelera (ADMIN).")
  @PutMapping("/{id}/restaurar")
  public FiguraResponse restaurar(@PathVariable Long id) {
    return service.restaurar(id);
  }

  @Operation(summary = "Agrega una imagen a la galería (ADMIN).")
  @PostMapping("/{id}/galeria")
  @ResponseStatus(HttpStatus.CREATED)
  public FiguraResponse agregarImagen(
      @PathVariable Long id, @Valid @RequestBody ImagenRequest r) {
    return service.agregarImagen(id, r.ruta());
  }

  @Operation(summary = "Quita una imagen de la galería; la ruta va como ?ruta=... (ADMIN).")
  @DeleteMapping("/{id}/galeria")
  public FiguraResponse eliminarImagen(@PathVariable Long id, @RequestParam String ruta) {
    return service.eliminarImagen(id, ruta);
  }
}
