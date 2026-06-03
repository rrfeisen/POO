package rrfeisen.com.github;

public abstract class FormaPagamento {
    protected double valorTotal;
    protected String status = "PENDENTE";

    public FormaPagamento(double valorTotal) {
        if (valorTotal <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero.");
        }
        this.valorTotal = valorTotal;
    }

    // Metodo abstrato que as classes filhas são obrigadas a implementar
    public abstract void processar();

    //<editor-fold defaultstate="collapsed" desc="getters">

    public String getStatus() {
        return status;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    //</editor-fold>
}