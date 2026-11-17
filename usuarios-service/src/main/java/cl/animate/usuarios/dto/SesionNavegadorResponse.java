package cl.animate.usuarios.dto;

/**
 * Respuesta de login/refresh para el navegador: además del JWT entrega el refresh token.
 * En AWS el front (S3) y la API (EC2) están en dominios distintos y la cookie no viaja,
 * así que el front guarda el refresh y lo envía en la cabecera X-Refresh-Token.
 */
public record SesionNavegadorResponse(
        String accessToken, long expiresIn, UsuarioResponse usuario, String refreshToken) {

    public static SesionNavegadorResponse de(AuthResultado r) {
        SesionResponse s = r.sesion();
        return new SesionNavegadorResponse(s.accessToken(), s.expiresIn(), s.usuario(), r.refresh());
    }
}