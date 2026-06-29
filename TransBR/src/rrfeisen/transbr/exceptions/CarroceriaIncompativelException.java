package rrfeisen.transbr.exceptions;

/** Lançada quando uma carga perigosa inflamável/líquida é alocada fora de uma Carreta Tanque. */
public class CarroceriaIncompativelException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CarroceriaIncompativelException(String mensagem) {
        super(mensagem);
    }
}
