package main.common.exceptions;

public class CancelarRegistoException extends RuntimeException {
    public CancelarRegistoException(String mensagem) {
        super(mensagem);
    }
}
