package rrfeisen.transbr.exceptions;

/** Lançada quando uma Carreta Tanque está com a inspeção do Inmetro vencida/inativa. */
public class InspecaoInmetroException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InspecaoInmetroException(String mensagem) {
        super(mensagem);
    }
}
