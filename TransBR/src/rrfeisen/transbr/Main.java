package rrfeisen.transbr;

import rrfeisen.transbr.exceptions.CarroceriaIncompativelException;
import rrfeisen.transbr.exceptions.ExcessoPesoException;
import rrfeisen.transbr.exceptions.InspecaoInmetroException;
import rrfeisen.transbr.exceptions.LicencaAmbientalInvalidaException;
import rrfeisen.transbr.exceptions.MoppNaoCertificadoException;
import rrfeisen.transbr.model.CaminhaoBau;
import rrfeisen.transbr.model.Carga;
import rrfeisen.transbr.model.CargaPerigosa;
import rrfeisen.transbr.model.CargaRegular;
import rrfeisen.transbr.model.CarretaTanque;
import rrfeisen.transbr.model.Motorista;
import rrfeisen.transbr.model.Veiculo;
import rrfeisen.transbr.model.Viagem;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Ponto de entrada do sistema. Implementa o menu interativo de console
 * descrito no edital: cadastros, criação de viagem (com try-catch das
 * exceções de regra de negócio), listagem de viagens ativas e persistência.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    private static GerenciadorLogistica gerenciador;

    public static void main(String[] args) {
        // Força UTF-8 na saída para garantir que acentos sejam exibidos
        // corretamente independente da configuração de locale do sistema
        // operacional/terminal onde o programa for executado.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        gerenciador = GerenciadorLogistica.carregar();

        System.out.println("=== Sistema TransBR Logística ===");
        System.out.println("Dados anteriores carregados: "
                + gerenciador.getVeiculos().size() + " veículo(s), "
                + gerenciador.getMotoristas().size() + " motorista(s), "
                + gerenciador.getCargas().size() + " carga(s), "
                + gerenciador.getViagens().size() + " viagem(ns).");

        boolean rodando = true;
        while (rodando) {
            exibirMenu();
            int opcao = lerInteiro("Escolha uma opção: ");
            switch (opcao) {
                case 1:
                    cadastrarVeiculo();
                    break;
                case 2:
                    cadastrarMotorista();
                    break;
                case 3:
                    cadastrarCarga();
                    break;
                case 4:
                    criarViagem();
                    break;
                case 5:
                    listarTodosCadastros();
                    break;
                case 6:
                    listarViagensAtivas();
                    break;
                case 7:
                    removerCadastro();
                    break;
                case 8:
                    rodando = !salvarESair();
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
        scanner.close();
    }

    private static void exibirMenu() {
        System.out.println();
        System.out.println("------------------------------------");
        System.out.println("1 - Cadastrar Veículo");
        System.out.println("2 - Cadastrar Motorista");
        System.out.println("3 - Cadastrar Carga");
        System.out.println("4 - Criar Viagem (Despacho)");
        System.out.println("5 - Listar Todos os Cadastros");
        System.out.println("6 - Listar Viagens Ativas");
        System.out.println("7 - Remover Cadastro");
        System.out.println("8 - Salvar e Sair");
        System.out.println("------------------------------------");
    }

    // ---------------- Cadastros ----------------

    private static void cadastrarVeiculo() {
        System.out.println("Tipo de veículo: 1 - Caminhão Baú | 2 - Carreta Tanque");
        int tipo = lerInteiro("Escolha: ");
        String placa = lerTexto("Placa: ");
        double capacidade = lerDouble("Capacidade máxima (toneladas): ");

        if (tipo == 1) {
            gerenciador.cadastrarVeiculo(new CaminhaoBau(placa, capacidade));
            System.out.println("Caminhão Baú cadastrado com sucesso.");
        } else if (tipo == 2) {
            boolean inspecao = lerBooleano("Inspeção Inmetro ativa? (s/n): ");
            gerenciador.cadastrarVeiculo(new CarretaTanque(placa, capacidade, inspecao));
            System.out.println("Carreta Tanque cadastrada com sucesso.");
        } else {
            System.out.println("Tipo inválido. Cadastro cancelado.");
        }
    }

    private static void cadastrarMotorista() {
        String id = lerTexto("ID/Matrícula: ");
        String nome = lerTexto("Nome: ");
        String cnh = lerTexto("CNH: ");
        boolean mopp = lerBooleano("Possui certificação MOPP ativa? (s/n): ");
        gerenciador.cadastrarMotorista(new Motorista(id, nome, cnh, mopp));
        System.out.println("Motorista cadastrado com sucesso.");
    }

    private static void cadastrarCarga() {
        System.out.println("Tipo de carga: 1 - Carga Regular | 2 - Carga Perigosa");
        int tipo = lerInteiro("Escolha: ");
        String descricao = lerTexto("Descrição: ");
        double peso = lerDouble("Peso (toneladas): ");
        double valor = lerDouble("Valor de mercado (R$): ");

        if (tipo == 1) {
            gerenciador.cadastrarCarga(new CargaRegular(descricao, peso, valor));
            System.out.println("Carga Regular cadastrada com sucesso.");
        } else if (tipo == 2) {
            String onu = lerTexto("Número ONU: ");
            boolean licenca = lerBooleano("Licença ambiental válida? (s/n): ");
            boolean inflamavel = lerBooleano("Inflamável ou líquida? (s/n): ");
            gerenciador.cadastrarCarga(new CargaPerigosa(descricao, peso, valor, onu, licenca, inflamavel));
            System.out.println("Carga Perigosa cadastrada com sucesso.");
        } else {
            System.out.println("Tipo inválido. Cadastro cancelado.");
        }
    }

    // ---------------- Viagem ----------------

    private static void criarViagem() {
        if (gerenciador.getMotoristas().isEmpty() || gerenciador.getVeiculos().isEmpty()
                || gerenciador.getCargas().isEmpty()) {
            System.out.println("É necessário ter ao menos um motorista, um veículo e uma carga cadastrados.");
            return;
        }

        System.out.println("--- Motoristas ---");
        listar(gerenciador.getMotoristas());
        int idxMotorista = lerInteiro("Escolha o número do motorista: ") - 1;

        System.out.println("--- Veículos ---");
        listar(gerenciador.getVeiculos());
        int idxVeiculo = lerInteiro("Escolha o número do veículo: ") - 1;

        System.out.println("--- Cargas ---");
        listar(gerenciador.getCargas());
        int idxCarga = lerInteiro("Escolha o número da carga: ") - 1;

        try {
            Motorista motorista = gerenciador.getMotoristas().get(idxMotorista);
            Veiculo veiculo = gerenciador.getVeiculos().get(idxVeiculo);
            Carga carga = gerenciador.getCargas().get(idxCarga);

            // Toda a validação das regras de negócio acontece dentro de criarViagem().
            Viagem viagem = gerenciador.criarViagem(motorista, veiculo, carga);

            System.out.println("Viagem autorizada com sucesso!");
            System.out.println(viagem);

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Índice inválido. Operação cancelada.");
        } catch (ExcessoPesoException | MoppNaoCertificadoException | CarroceriaIncompativelException
                | LicencaAmbientalInvalidaException | InspecaoInmetroException e) {
            // Alerta amigável da exceção, sem travar o sistema.
            System.out.println("ALERTA: Viagem recusada -> " + e.getMessage());
        }
    }

    private static void listarTodosCadastros() {
        System.out.println("=== Veículos cadastrados (" + gerenciador.getVeiculos().size() + ") ===");
        if (gerenciador.getVeiculos().isEmpty()) {
            System.out.println("Nenhum veículo cadastrado.");
        } else {
            listar(gerenciador.getVeiculos());
        }

        System.out.println();
        System.out.println("=== Motoristas cadastrados (" + gerenciador.getMotoristas().size() + ") ===");
        if (gerenciador.getMotoristas().isEmpty()) {
            System.out.println("Nenhum motorista cadastrado.");
        } else {
            listar(gerenciador.getMotoristas());
        }

        System.out.println();
        System.out.println("=== Cargas cadastradas (" + gerenciador.getCargas().size() + ") ===");
        if (gerenciador.getCargas().isEmpty()) {
            System.out.println("Nenhuma carga cadastrada.");
        } else {
            listar(gerenciador.getCargas());
        }

        System.out.println();
        System.out.println("=== Viagens registradas (" + gerenciador.getViagens().size() + ") ===");
        if (gerenciador.getViagens().isEmpty()) {
            System.out.println("Nenhuma viagem registrada.");
        } else {
            for (Viagem v : gerenciador.getViagens()) {
                System.out.println(v);
                System.out.println();
            }
        }
    }

    private static void listarViagensAtivas() {
        List<Viagem> viagens = gerenciador.getViagens();
        boolean encontrou = false;
        for (Viagem v : viagens) {
            if ("AUTORIZADA".equals(v.getStatus())) {
                System.out.println(v);
                System.out.println();
                encontrou = true;
            }
        }
        if (!encontrou) {
            System.out.println("Nenhuma viagem ativa no momento.");
        }
    }

    private static void removerCadastro() {
        System.out.println("O que deseja remover?");
        System.out.println("1 - Veículo");
        System.out.println("2 - Motorista");
        System.out.println("3 - Carga");
        System.out.println("4 - Viagem");
        System.out.println("5 - Cancelar");
        int opcao = lerInteiro("Escolha: ");

        switch (opcao) {
            case 1:
                removerItem("veículo", gerenciador.getVeiculos(), gerenciador::removerVeiculo);
                break;
            case 2:
                removerItem("motorista", gerenciador.getMotoristas(), gerenciador::removerMotorista);
                break;
            case 3:
                removerItem("carga", gerenciador.getCargas(), gerenciador::removerCarga);
                break;
            case 4:
                removerItem("viagem", gerenciador.getViagens(), gerenciador::removerViagem);
                break;
            case 5:
                System.out.println("Operação cancelada.");
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    /**
     * Fluxo genérico de remoção: lista os itens, pede o índice, alerta se o
     * item já estiver vinculado a alguma viagem registrada, pede confirmação
     * e só então remove.
     */
    private static <T> void removerItem(String rotulo, List<T> lista, java.util.function.IntFunction<T> removedor) {
        if (lista.isEmpty()) {
            System.out.println("Não há " + rotulo + "(s) cadastrado(s).");
            return;
        }

        listar(lista);
        int idx = lerInteiro("Escolha o número do " + rotulo + " a remover: ") - 1;

        try {
            T item = lista.get(idx);

            if (gerenciador.estaEmUso(item)) {
                System.out.println("Atenção: este " + rotulo + " está vinculado a uma ou mais viagens já registradas.");
                System.out.println("As viagens existentes continuarão com o histórico intacto mesmo após a remoção.");
            }

            boolean confirma = lerBooleano("Confirma a remoção? (s/n): ");
            if (confirma) {
                removedor.apply(idx);
                System.out.println(rotulo.substring(0, 1).toUpperCase() + rotulo.substring(1) + " removido(a) com sucesso.");
            } else {
                System.out.println("Remoção cancelada.");
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Índice inválido. Operação cancelada.");
        }
    }

    private static boolean salvarESair() {
        try {
            gerenciador.salvar();
            System.out.println("Dados salvos com sucesso em transbr_dados.dat. Encerrando o sistema.");
            return true;
        } catch (IOException e) {
            System.out.println("Erro ao salvar dados: " + e.getMessage());
            return false;
        }
    }

    // ---------------- Utilitários de entrada ----------------

    private static <T> void listar(List<T> lista) {
        for (int i = 0; i < lista.size(); i++) {
            System.out.println((i + 1) + " - " + lista.get(i));
        }
    }

    private static int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String linha = scanner.nextLine().trim();
            try {
                return Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    private static double lerDouble(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String linha = scanner.nextLine().trim().replace(",", ".");
            try {
                return Double.parseDouble(linha);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número (use ponto ou vírgula).");
            }
        }
    }

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    private static boolean lerBooleano(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String linha = scanner.nextLine().trim().toLowerCase();
            if (linha.equals("s") || linha.equals("sim")) {
                return true;
            }
            if (linha.equals("n") || linha.equals("nao") || linha.equals("não")) {
                return false;
            }
            System.out.println("Resposta inválida. Digite 's' ou 'n'.");
        }
    }
}
