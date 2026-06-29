package rrfeisen.transbr.model;

/**
 * Carga Regular: e-commerce, eletrônicos, alimentos não perecíveis.
 * Implementa Seguravel, mas só calcula seguro de fato quando o valor de
 * mercado supera o limiar de alto valor (R$ 100.000,00).
 */
public class CargaRegular extends Carga implements Seguravel {
    private static final long serialVersionUID = 1L;

    public static final double LIMIAR_ALTO_VALOR = 100_000.0;
    private static final double TAXA_SEGURO_ALTO_VALOR = 0.015; // 1,5% do valor de mercado

    public CargaRegular(String descricao, double pesoToneladas, double valorMercado) {
        super(descricao, pesoToneladas, valorMercado);
    }

    public boolean isAltoValor() {
        return valorMercado > LIMIAR_ALTO_VALOR;
    }

    @Override
    public double calcularSeguro() {
        if (!isAltoValor()) {
            return 0.0;
        }
        return valorMercado * TAXA_SEGURO_ALTO_VALOR;
    }

    @Override
    public String getTipo() {
        return "Carga Regular";
    }
}
