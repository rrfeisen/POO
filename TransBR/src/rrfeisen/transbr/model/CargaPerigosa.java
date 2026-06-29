package rrfeisen.transbr.model;

/**
 * Carga Perigosa: combustíveis, ácidos, explosivos.
 * Sempre exige licença ambiental válida e assina o contrato Seguravel
 * obrigatoriamente (todo transporte de carga perigosa paga seguro).
 */
public class CargaPerigosa extends Carga implements Seguravel {
    private static final long serialVersionUID = 1L;

    private static final double TAXA_SEGURO_PERIGOSA = 0.04; // 4% do valor de mercado

    private String numeroONU;
    private boolean licencaAmbientalValida;
    private boolean inflamavelOuLiquida;

    public CargaPerigosa(String descricao, double pesoToneladas, double valorMercado,
                          String numeroONU, boolean licencaAmbientalValida, boolean inflamavelOuLiquida) {
        super(descricao, pesoToneladas, valorMercado);
        this.numeroONU = numeroONU;
        this.licencaAmbientalValida = licencaAmbientalValida;
        this.inflamavelOuLiquida = inflamavelOuLiquida;
    }

    public String getNumeroONU() {
        return numeroONU;
    }

    public boolean isLicencaAmbientalValida() {
        return licencaAmbientalValida;
    }

    public boolean isInflamavelOuLiquida() {
        return inflamavelOuLiquida;
    }

    @Override
    public double calcularSeguro() {
        return valorMercado * TAXA_SEGURO_PERIGOSA;
    }

    @Override
    public String getTipo() {
        return "Carga Perigosa";
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" [ONU: %s, Licença Ambiental: %s, Inflamável/Líquida: %s]",
                numeroONU, licencaAmbientalValida ? "VÁLIDA" : "INVÁLIDA", inflamavelOuLiquida ? "SIM" : "NÃO");
    }
}
