package cl.animate.catalogo.config;

import cl.animate.catalogo.model.Figura;
import cl.animate.catalogo.model.Manga;
import cl.animate.catalogo.model.Tomo;
import cl.animate.catalogo.repository.FigurasRepository;
import cl.animate.catalogo.repository.MangasRepository;
import cl.animate.catalogo.service.Textos;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * Datos iniciales: los mangas y figuras que antes estaban en src/data/mangas.js y figuras.js del
 * frontend (copiados a resources/seed/*.json). Solo se cargan si las tablas están vacías.
 */
@Configuration
public class Seed {
  @JsonIgnoreProperties(ignoreUnknown = true)
  record TomoSeed(int tomo, int precio, int stock, String img) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record MangaSeed(
      String titulo,
      String genero,
      int vistas,
      String fechaAgregado,
      String sinopsis,
      String imgPortada,
      List<TomoSeed> volumenes) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record FiguraSeed(
      String nombre,
      String proveniencia,
      String tipoFigura,
      int stock,
      int precio,
      int vistas,
      String fechaAgregado,
      String imgPortada,
      List<String> galeria) {}

  @Bean
  CommandLineRunner cargarCatalogo(
      MangasRepository mangas, FigurasRepository figuras, ObjectMapper json) {
    return args -> {
      if (mangas.count() == 0) {
        for (MangaSeed s : leer(json, "seed/mangas.json", new TypeReference<List<MangaSeed>>() {})) {
          Manga m = new Manga();
          m.titulo = s.titulo();
          m.tituloClave = Textos.normalizar(s.titulo());
          m.genero = s.genero();
          m.vistas = s.vistas();
          m.fechaAgregado = LocalDate.parse(s.fechaAgregado());
          m.sinopsis = s.sinopsis();
          m.imgPortada = s.imgPortada();
          if (s.volumenes() != null)
            s.volumenes()
                .forEach(v -> m.tomos.add(new Tomo(m, v.tomo(), v.precio(), v.stock(), v.img())));
          mangas.save(m);
        }
      }
      if (figuras.count() == 0) {
        for (FiguraSeed s :
            leer(json, "seed/figuras.json", new TypeReference<List<FiguraSeed>>() {})) {
          Figura f = new Figura();
          f.nombre = s.nombre();
          f.nombreClave = Textos.normalizar(s.nombre());
          f.proveniencia = s.proveniencia();
          f.tipoFigura = s.tipoFigura();
          f.stock = s.stock();
          f.precio = s.precio();
          f.vistas = s.vistas();
          f.fechaAgregado = LocalDate.parse(s.fechaAgregado());
          f.imgPortada = s.imgPortada();
          if (s.galeria() != null) f.galeria.addAll(s.galeria());
          figuras.save(f);
        }
      }
    };
  }

  private <T> List<T> leer(ObjectMapper json, String ruta, TypeReference<List<T>> tipo)
      throws Exception {
    try (InputStream in = new ClassPathResource(ruta).getInputStream()) {
      return json.readValue(in, tipo);
    }
  }
}
