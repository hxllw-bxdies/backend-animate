package cl.animate.catalogo.controller;

import cl.animate.catalogo.dto.MangaRequest;
import cl.animate.catalogo.dto.MangaResponse;
import cl.animate.catalogo.dto.MangaUpdateRequest;
import cl.animate.catalogo.dto.PrecioStockRequest;
import cl.animate.catalogo.dto.TomoRequest;
import cl.animate.catalogo.service.MangasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Lectura pública; el resto requiere ADMIN (ver SecurityConfig). */
@Tag(name = "Mangas", description = "CRUD de mangas, tomos y papelera")
@RestController
@RequestMapping("/api/mangas")
public class MangasController {
  private final MangasService service;

  public MangasController(MangasService service) {
    this.service = service;
  }

  @Operation(summary = "Lista los mangas del catálogo (público).")
  @GetMapping
  public List<MangaResponse> listar() {
    return service.listar();
  }

  @Operation(summary = "Lista los mangas en la papelera (ADMIN).")
  @GetMapping("/papelera")
  public List<MangaResponse> papelera() {
    return service.papelera();
  }

  @Operation(summary = "Obtiene un manga con sus tomos (público).")
  @GetMapping("/{id}")
  public MangaResponse obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @Operation(summary = "Crea un manga con su tomo 1 (ADMIN).")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MangaResponse crear(@Valid @RequestBody MangaRequest r) {
    return service.crear(r);
  }

  @Operation(summary = "Edita título, género, sinopsis y portada (ADMIN).")
  @PutMapping("/{id}")
  public MangaResponse editar(@PathVariable Long id, @Valid @RequestBody MangaUpdateRequest r) {
    return service.editar(id, r);
  }

  @Operation(summary = "Envía el manga a la papelera (ADMIN).")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id) {
    service.eliminar(id);
  }

  @Operation(summary = "Restaura un manga desde la papelera (ADMIN).")
  @PutMapping("/{id}/restaurar")
  public MangaResponse restaurar(@PathVariable Long id) {
    return service.restaurar(id);
  }

  @Operation(summary = "Agrega el siguiente tomo al manga (ADMIN).")
  @PostMapping("/{id}/tomos")
  @ResponseStatus(HttpStatus.CREATED)
  public MangaResponse agregarTomo(@PathVariable Long id, @Valid @RequestBody TomoRequest r) {
    return service.agregarTomo(id, r);
  }

  @Operation(summary = "Cambia precio y stock de un tomo (ADMIN).")
  @PutMapping("/{id}/tomos/{tomo}")
  public MangaResponse cambiarTomo(
      @PathVariable Long id, @PathVariable int tomo, @Valid @RequestBody PrecioStockRequest r) {
    return service.cambiarPrecioYStock(id, tomo, r);
  }

  @Operation(summary = "Elimina un tomo del manga (ADMIN).")
  @DeleteMapping("/{id}/tomos/{tomo}")
  public MangaResponse eliminarTomo(@PathVariable Long id, @PathVariable int tomo) {
    return service.eliminarTomo(id, tomo);
  }
}
