package rrfeisen.com.github;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<FormaPagamento> carrinho = new ArrayList<>();

        // Adicionando os 4 pagamentos
        carrinho.add(new PagamentoPix(150.00, "123.456.789-00")); // Pix válido
        carrinho.add(new PagamentoPix(200.00, "")); // Pix inválido (chave vazia)
        carrinho.add(new PagamentoCartao(500.00, "1234567890123456", 10)); // Cartão válido
        carrinho.add(new PagamentoCartao(999.00, "4444000011112222", 12)); // Cartão com saldo insuficiente

        System.out.println("Iniciando processamento da fila de pagamentos...\n");

        for (FormaPagamento p : carrinho) {
            try {
                p.processar();
                System.out.println("Pagamento processado! Status: " + p.getStatus());

                //Se implementar EmitenteNotaFiscal, pode emitir NFe
                if (p instanceof EmitenteNotaFiscal) {
                    ((EmitenteNotaFiscal) p).emitirNFe();
                }

            } catch (SaldoInsuficienteException e) {
                System.out.println("Operação Recusada: " + e.getMessage());
            } catch (ChavePixInvalidaException e) {
                System.out.println("Falha no Pix: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Erro de Valor: " + e.getMessage());
            }
            System.out.println("-----------------------------------------");
        }

        System.out.println("Fila de processamento concluída!");
    }
}
