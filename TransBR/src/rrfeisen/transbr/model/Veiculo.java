package rrfeisen.transbr.model;

import java.io.Serializable;

/**
 * Classe abstrata que representa um veículo da frota da TransBR.
 * Atributos comuns ficam aqui e são herdados pelas especializações.
 */
public abstract class Veiculo implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String placa;
    protected double capacidadeMaximaToneladas;

    public Veiculo(String placa, double capacidadeMaximaToneladas) {
        this.placa = placa;
        this.capacidadeMaximaToneladas = capacidadeMaximaToneladas;
    }

    public String getPlaca() {
        return placa;
    }

    public double getCapacidadeMaximaToneladas() {
        return capacidadeMaximaToneladas;
    }

    /** Cada veículo concreto informa seu tipo para exibição e regras de negócio. */
    public abstract String getTipo();

    @Override
    public String toString() {
        return String.format("%s [Placa: %s, Capacidade: %.2f t]", getTipo(), placa, capacidadeMaximaToneladas);
    }
}
