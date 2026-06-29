package rrfeisen.transbr.model;

import java.io.Serializable;

/**
 * Classe abstrata que representa uma mercadoria a ser transportada.
 */
public abstract class Carga implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String descricao;
    protected double pesoToneladas;
    protected double valorMercado;

    public Carga(String descricao, double pesoToneladas, double valorMercado) {
        this.descricao = descricao;
        this.pesoToneladas = pesoToneladas;
        this.valorMercado = valorMercado;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getPesoToneladas() {
        return pesoToneladas;
    }

    public double getValorMercado() {
        return valorMercado;
    }

    public abstract String getTipo();

    @Override
    public String toString() {
        return String.format("%s [%s, Peso: %.2f t, Valor: R$ %.2f]", getTipo(), descricao, pesoToneladas, valorMercado);
    }
}
