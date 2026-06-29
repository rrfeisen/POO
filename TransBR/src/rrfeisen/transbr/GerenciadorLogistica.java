package rrfeisen.transbr;

import rrfeisen.transbr.exceptions.CarroceriaIncompativelException;
import rrfeisen.transbr.exceptions.ExcessoPesoException;
import rrfeisen.transbr.exceptions.InspecaoInmetroException;
import rrfeisen.transbr.exceptions.LicencaAmbientalInvalidaException;
import rrfeisen.transbr.exceptions.MoppNaoCertificadoException;
import rrfeisen.transbr.model.CargaPerigosa;
import rrfeisen.transbr.model.CarretaTanque;
import rrfeisen.transbr.model.Carga;
import rrfeisen.transbr.model.Motorista;
import rrfeisen.transbr.model.Seguravel;
import rrfeisen.transbr.model.Veiculo;
import rrfeisen.transbr.model.Viagem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe centralizadora do sistema. Guarda as listas de veículos, motoristas,
 * cargas e viagens, valida todas as regras de negócio na criação de uma
 * Viagem e cuida da persistência binária do estado completo da empresa.
 */
public class GerenciadorLogistica implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String ARQUIVO_DADOS = "transbr_dados.dat";

    private List<Veiculo> veiculos;
    private List<Motorista> motoristas;
    private List<Carga> cargas;
    private List<Viagem> viagens;

    public GerenciadorLogistica() {
        this.veiculos = new ArrayList<>();
        this.motoristas = new ArrayList<>();
        this.cargas = new ArrayList<>();
        this.viagens = new ArrayList<>();
    }

    // ---------------- Cadastros ----------------

    public void cadastrarVeiculo(Veiculo veiculo) {
        veiculos.add(veiculo);
    }

    public void cadastrarMotorista(Motorista motorista) {
        motoristas.add(motorista);
    }

    public void cadastrarCarga(Carga carga) {
        cargas.add(carga);
    }

    public List<Veiculo> getVeiculos() {
        return veiculos;
    }

    public List<Motorista> getMotoristas() {
        return motoristas;
    }

    public List<Carga> getCargas() {
        return cargas;
    }

    public List<Viagem> getViagens() {
        return viagens;
    }

    // ---------------- Remoção de cadastros ----------------

    /** Remove o veículo na posição informada. Lança IndexOutOfBoundsException se o índice for inválido. */
    public Veiculo removerVeiculo(int index) {
        return veiculos.remove(index);
    }

    /** Remove o motorista na posição informada. Lança IndexOutOfBoundsException se o índice for inválido. */
    public Motorista removerMotorista(int index) {
        return motoristas.remove(index);
    }

    /** Remove a carga na posição informada. Lança IndexOutOfBoundsException se o índice for inválido. */
    public Carga removerCarga(int index) {
        return cargas.remove(index);
    }

    /** Remove a viagem na posição informada. Lança IndexOutOfBoundsException se o índice for inválido. */
    public Viagem removerViagem(int index) {
        return viagens.remove(index);
    }

    /**
     * Verifica se um veículo, motorista ou carga já está vinculado a alguma
     * viagem registrada. Usado para alertar o operador antes de uma remoção,
     * já que a Viagem mantém a referência ao objeto mesmo após o cadastro
     * original ser removido das listas (o histórico não é afetado).
     */
    public boolean estaEmUso(Object cadastro) {
        for (Viagem v : viagens) {
            if (v.getVeiculo() == cadastro || v.getMotorista() == cadastro || v.getCarga() == cadastro) {
                return true;
            }
        }
        return false;
    }

    // ---------------- Regras de negócio ----------------

    /**
     * Valida todas as regras de negócio e, se tudo estiver correto, cria e
     * autoriza a viagem. Lança exceções customizadas (RuntimeException) em
     * caso de qualquer violação, para serem tratadas em um bloco try-catch
     * no menu principal.
     */
    public Viagem criarViagem(Motorista motorista, Veiculo veiculo, Carga carga) {

        // Regra 1: Excesso de carga
        if (carga.getPesoToneladas() > veiculo.getCapacidadeMaximaToneladas()) {
            throw new ExcessoPesoException(String.format(
                    "Excesso de carga: a carga pesa %.2f t, mas o veículo %s suporta no máximo %.2f t.",
                    carga.getPesoToneladas(), veiculo.getPlaca(), veiculo.getCapacidadeMaximaToneladas()));
        }

        if (carga instanceof CargaPerigosa) {
            CargaPerigosa perigosa = (CargaPerigosa) carga;

            // Regra 2: MOPP obrigatório para carga perigosa
            if (!motorista.isMoppAtivo()) {
                throw new MoppNaoCertificadoException(String.format(
                        "Motorista %s não possui certificação MOPP ativa para transportar a carga perigosa (ONU %s).",
                        motorista.getNome(), perigosa.getNumeroONU()));
            }

            // Regra 3: carroceria incompatível com carga inflamável/líquida
            if (perigosa.isInflamavelOuLiquida() && !(veiculo instanceof CarretaTanque)) {
                throw new CarroceriaIncompativelException(String.format(
                        "A carga perigosa inflamável/líquida (ONU %s) só pode ser transportada em uma Carreta Tanque, e não em um(a) %s.",
                        perigosa.getNumeroONU(), veiculo.getTipo()));
            }

            // Licença ambiental obrigatória para toda carga perigosa
            if (!perigosa.isLicencaAmbientalValida()) {
                throw new LicencaAmbientalInvalidaException(String.format(
                        "A carga perigosa (ONU %s) não possui licença ambiental válida para transporte.",
                        perigosa.getNumeroONU()));
            }
        }

        // Regra adicional: Carreta Tanque exige inspeção Inmetro ativa
        if (veiculo instanceof CarretaTanque) {
            CarretaTanque tanque = (CarretaTanque) veiculo;
            if (!tanque.isInspecaoInmetroAtiva()) {
                throw new InspecaoInmetroException(String.format(
                        "A Carreta Tanque %s está com a inspeção do Inmetro vencida ou inativa.", tanque.getPlaca()));
            }
        }

        Viagem viagem = new Viagem(motorista, veiculo, carga);

        // Apólice de seguro obrigatória: toda Carga Perigosa, e Carga Regular de
        // alto valor (>R$100.000), passam pelo cálculo automático via Seguravel.
        double seguro = 0.0;
        if (carga instanceof Seguravel) {
            Seguravel seguravel = (Seguravel) carga;
            seguro = seguravel.calcularSeguro();
        }
        viagem.setValorSeguroCalculado(seguro);

        viagem.setStatus("AUTORIZADA");
        viagens.add(viagem);
        return viagem;
    }

    // ---------------- Persistência binária ----------------

    public void salvar() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO_DADOS))) {
            oos.writeObject(this);
        }
    }

    public static GerenciadorLogistica carregar() {
        File arquivo = new File(ARQUIVO_DADOS);
        if (!arquivo.exists()) {
            return new GerenciadorLogistica();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            GerenciadorLogistica gerenciador = (GerenciadorLogistica) ois.readObject();

            // O contador estático de IDs de Viagem não é persistido junto com
            // cada instância, então é resincronizado aqui após o carregamento.
            int maiorId = 0;
            for (Viagem v : gerenciador.getViagens()) {
                if (v.getId() > maiorId) {
                    maiorId = v.getId();
                }
            }
            Viagem.atualizarContador(maiorId);

            return gerenciador;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Aviso: não foi possível carregar o arquivo de dados (" + e.getMessage()
                    + "). Iniciando o sistema vazio.");
            return new GerenciadorLogistica();
        }
    }
}
