package rrfeisen.transbr.model;

import java.io.Serializable;

/**
 * Representa o despacho: associação entre um Motorista, um Veículo e uma Carga.
 */
public class Viagem implements Serializable {
    private static final long serialVersionUID = 1L;

    // Contador estático para gerar IDs sequenciais. Não é persistido pela
    // serialização da instância de Viagem, por isso é restaurado manualmente
    // pelo GerenciadorLogistica ao carregar os dados (ver atualizarContador).
    private static int contador = 0;

    private int id;
    private Motorista motorista;
    private Veiculo veiculo;
    private Carga carga;
    private double valorSeguroCalculado;
    private String status;

    public Viagem(Motorista motorista, Veiculo veiculo, Carga carga) {
        this.id = ++contador;
        this.motorista = motorista;
        this.veiculo = veiculo;
        this.carga = carga;
        this.valorSeguroCalculado = 0.0;
        this.status = "PENDENTE";
    }

    public int getId() {
        return id;
    }

    public Motorista getMotorista() {
        return motorista;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public Carga getCarga() {
        return carga;
    }

    public double getValorSeguroCalculado() {
        return valorSeguroCalculado;
    }

    public void setValorSeguroCalculado(double valorSeguroCalculado) {
        this.valorSeguroCalculado = valorSeguroCalculado;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /** Garante que novas viagens continuem a numeração após um carregamento do .dat */
    public static void atualizarContador(int maiorIdConhecido) {
        if (maiorIdConhecido > contador) {
            contador = maiorIdConhecido;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Viagem #%d [Status: %s]%n  Motorista: %s%n  Veículo: %s%n  Carga: %s%n  Seguro calculado: R$ %.2f",
                id, status, motorista.getNome(), veiculo.toString(), carga.toString(), valorSeguroCalculado);
    }
}
