package cl.animate.usuarios.exception;

import cl.animate.common.exception.ApiErrors;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;

/** @ControllerAdvice aplica los @ExceptionHandler heredados de ApiErrors a todos los controladores. */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler extends ApiErrors {}
