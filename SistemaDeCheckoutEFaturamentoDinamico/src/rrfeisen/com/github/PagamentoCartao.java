package rrfeisen.com.github;

public class PagamentoCartao extends FormaPagamento implements EmitenteNotaFiscal {
    private final String numeroCartao;
    private final int parcelas;

    public PagamentoCartao(double valorTotal, String numeroCartao, int parcelas) {
        super(valorTotal);
        this.numeroCartao = numeroCartao;
        this.parcelas = parcelas;
    }

    @Override
    public void processar() {
        // Implementação do Try-with-Resources exigido no desafio
        try (GatewayPagamento gateway = new GatewayPagamento()) {
            gateway.conectar();

            // Regra de negócio
            if (this.numeroCartao != null && this.numeroCartao.startsWith("4444")) {
                throw new SaldoInsuficienteException("Cartão recusado por falta de limite.");
            }

            this.status = "APROVADO";
        }
        // O metodo close() de GatewayPagamento é chamado automaticamente aqui, mesmo se a exceção for lançada
    }

    @Override
    public void emitirNFe() {
        String cartaoMascarado = "**** **** **** " + numeroCartao.substring(Math.max(0, numeroCartao.length() - 4));

        double imposto = this.valorTotal * 0.15;

        System.out.println("\n=== NOTA FISCAL ELETRÔNICA ===");
        System.out.println("Forma de Pagamento: Cartão de Crédito");
        System.out.println("Cartão: " + cartaoMascarado);
        System.out.println("Parcelas: " + this.parcelas + "x");
        System.out.printf("Valor Total: R$ %.2f\n", this.valorTotal);
        System.out.printf("Imposto Retido (15%%): R$ %.2f\n", imposto);
        System.out.println("==============================\n");
    }
}
