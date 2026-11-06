package cl.animate.catalogo.service;

import java.text.Normalizer;
import java.util.Locale;

/** Utilidades de texto del catálogo. */
public final class Textos {
  private Textos() {}

  /** "Bocchi The Róck " → "bocchi the rock". Sirve para detectar nombres repetidos. */
  public static String normalizar(String s) {
    return Normalizer.normalize(s.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .replaceAll("\\s+", " ");
  }

  /** Clave que se asigna al enviar un producto a la papelera, para liberar el nombre. */
  public static String clavePapelera(String clave, Long id) {
    return clave + "#papelera-" + id;
  }

  /** Recupera la clave original al restaurar desde la papelera. */
  public static String claveOriginal(String clave) {
    int i = clave.indexOf("#papelera-");
    return i < 0 ? clave : clave.substring(0, i);
  }
}
