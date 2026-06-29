package rrfeisen.transbr.exceptions;

/** Lançada quando o peso da carga excede a capacidade máxima do veículo. */
public class ExcessoPesoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ExcessoPesoException(String mensagem) {
        super(mensagem);
    }
}
