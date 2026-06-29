package rrfeisen.transbr.exceptions;

/** Lançada quando uma carga perigosa não possui licença ambiental válida para transporte. */
public class LicencaAmbientalInvalidaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LicencaAmbientalInvalidaException(String mensagem) {
        super(mensagem);
    }
}
