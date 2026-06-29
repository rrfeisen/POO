package rrfeisen.transbr.model;

/**
 * Caminhão Baú: entregas urbanas e cargas regulares.
 * Não pode transportar cargas perigosas inflamáveis/líquidas (exige Carreta Tanque).
 */
public class CaminhaoBau extends Veiculo {
    private static final long serialVersionUID = 1L;

    public CaminhaoBau(String placa, double capacidadeMaximaToneladas) {
        super(placa, capacidadeMaximaToneladas);
    }

    @Override
    public String getTipo() {
        return "Caminhão Baú";
    }
}
