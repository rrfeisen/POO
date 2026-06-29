package rrfeisen.transbr.model;

/**
 * Carreta Tanque: uso exclusivo para fluidos e combustíveis.
 * Exige inspeção ativa do Inmetro para circular.
 */
public class CarretaTanque extends Veiculo {
    private static final long serialVersionUID = 1L;

    private boolean inspecaoInmetroAtiva;

    public CarretaTanque(String placa, double capacidadeMaximaToneladas, boolean inspecaoInmetroAtiva) {
        super(placa, capacidadeMaximaToneladas);
        this.inspecaoInmetroAtiva = inspecaoInmetroAtiva;
    }

    public boolean isInspecaoInmetroAtiva() {
        return inspecaoInmetroAtiva;
    }

    public void setInspecaoInmetroAtiva(boolean inspecaoInmetroAtiva) {
        this.inspecaoInmetroAtiva = inspecaoInmetroAtiva;
    }

    @Override
    public String getTipo() {
        return "Carreta Tanque";
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" [Inspeção Inmetro: %s]",
                inspecaoInmetroAtiva ? "ATIVA" : "VENCIDA/INATIVA");
    }
}
