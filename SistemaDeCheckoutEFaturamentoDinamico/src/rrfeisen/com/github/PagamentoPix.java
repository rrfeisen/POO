package rrfeisen.com.github;

public class PagamentoPix extends FormaPagamento {
    private final String chavePix;

    public PagamentoPix(double valorTotal, String chavePix) {
        super(valorTotal);
        this.chavePix = chavePix;
    }

    @Override
    public void processar() {
        if (this.chavePix == null || this.chavePix.trim().isEmpty()) {
            throw new ChavePixInvalidaException("A chave Pix não pode estar vazia.");
        }
        this.status = "APROVADO";
    }
}
