package rrfeisen.com.github;

public class GatewayPagamento implements AutoCloseable {

    public void conectar() {
        System.out.println("Conectando ao servidor de cartões...");
    }

    @Override
    public void close() {
        System.out.println("Conexão com o gateway encerrada com segurança.");
    }
}
