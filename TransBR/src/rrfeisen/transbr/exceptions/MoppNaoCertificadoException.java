package rrfeisen.transbr.exceptions;

/** Lançada quando o motorista não possui certificação MOPP ativa para carga perigosa. */
public class MoppNaoCertificadoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MoppNaoCertificadoException(String mensagem) {
        super(mensagem);
    }
}
