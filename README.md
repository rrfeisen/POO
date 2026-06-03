# Documentação — Sistema de Checkout e Faturamento Dinâmico

**Projeto:** Motor de Pagamentos — Plataforma de E-commerce  
**Pacote:** `rrfeisen.com.github`  
**Linguagem:** Java  

---

## Sumário

1. [Visão Geral da Arquitetura](#1-visão-geral-da-arquitetura)
2. [EmitenteNotaFiscal.java — A Interface](#2-emitentenotafiscaljava--a-interface)
3. [FormaPagamento.java — A Classe Abstrata](#3-formapagamentojava--a-classe-abstrata)
4. [SaldoInsuficienteException.java](#4-saldoinsuficienteexceptionjava)
5. [ChavePixInvalidaException.java](#5-chavepixinvalidaexceptionjava)
6. [GatewayPagamento.java — O Desafio Extra](#6-gatewaypagamentojava--o-desafio-extra)
7. [PagamentoCartao.java](#7-pagamentocartaojava)
8. [PagamentoPix.java](#8-pagamentopixjava)
9. [Main.java — O Fluxo de Teste](#9-mainjava--o-fluxo-de-teste)
10. [Decisões de Design e Por Quê](#10-decisões-de-design-e-por-quê)
11. [Saída Esperada no Console](#11-saída-esperada-no-console)

---

## 1. Visão Geral da Arquitetura

O sistema é composto por **8 arquivos** organizados em três camadas:

```
┌─────────────────────────────────────────────────────────┐
│                     CONTRATOS                           │
│   EmitenteNotaFiscal (interface)                        │
└─────────────────────────┬───────────────────────────────┘
                          │ implementa
┌─────────────────────────▼───────────────────────────────┐
│                  MOLDE GENÉRICO                         │
│   FormaPagamento (classe abstrata)                      │
└──────────────┬──────────────────────────┬───────────────┘
               │ herda                    │ herda
┌──────────────▼──────────┐  ┌────────────▼───────────────┐
│   PagamentoCartao       │  │   PagamentoPix             │
│   (usa GatewayPagamento)│  │                            │
└─────────────────────────┘  └────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                   EXCEÇÕES CUSTOMIZADAS                 │
│   SaldoInsuficienteException  ChavePixInvalidaException │
└─────────────────────────────────────────────────────────┘
```

**Princípios aplicados:**

| Conceito | Onde aparece |
|---|---|
| Abstração | `FormaPagamento` não pode ser instanciada diretamente |
| Herança | `PagamentoCartao` e `PagamentoPix` herdam de `FormaPagamento` |
| Polimorfismo | O laço em `Main` trata todos os objetos como `FormaPagamento` |
| Interface | `EmitenteNotaFiscal` define um contrato independente da hierarquia |
| Try-with-Resources | `GatewayPagamento` é aberto e fechado automaticamente |
| Exceções customizadas | Erros de negócio têm tipos próprios e significativos |

---

## 2. EmitenteNotaFiscal.java — A Interface

```java
public interface EmitenteNotaFiscal {
    void emitirNFe();
}
```

### O que é e por que existe

Uma **interface** define um contrato: qualquer classe que a implemente promete ter o método `emitirNFe()`. Ela não diz *como* fazer, só *o que* deve ser feito.

### Por que não colocar `emitirNFe()` na própria `FormaPagamento`?

Porque **nem toda forma de pagamento emite nota fiscal**. O Pix, neste sistema, não emite. Se `emitirNFe()` estivesse em `FormaPagamento`, `PagamentoPix` seria obrigado a implementar um método que não faz sentido para ele — violando o princípio da segregação de interfaces.

A solução foi criar uma interface separada e aplicá-la **somente** em `PagamentoCartao`.

---

## 3. FormaPagamento.java — A Classe Abstrata

```java
public abstract class FormaPagamento {
    protected double valorTotal;
    protected String status = "PENDENTE";

    public FormaPagamento(double valorTotal) {
        if (valorTotal <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero.");
        }
        this.valorTotal = valorTotal;
    }

    public abstract void processar();

    public String getStatus() { return status; }
    public double getValorTotal() { return valorTotal; }
}
```

### Por que `abstract`?

A palavra-chave `abstract` na classe impede que alguém escreva `new FormaPagamento(100)` diretamente. Faz sentido: um "pagamento genérico" não existe no mundo real — sempre é um Pix, um cartão ou outro método concreto.

### Por que os atributos são `protected` e não `private`?

Com `private`, as subclasses (`PagamentoCartao`, `PagamentoPix`) não teriam acesso a `status` e `valorTotal`. Precisariam de getters/setters para tudo. O modificador `protected` permite que classes filhas acessem e modifiquem esses atributos diretamente, o que é necessário para que `processar()` possa alterar `this.status = "APROVADO"`.

### Por que `status` é inicializado na declaração e não no construtor?

```java
protected String status = "PENDENTE"; // inicialização na declaração
```

É uma questão de clareza. O valor padrão fica visível exatamente onde o atributo é declarado, sem precisar procurar no construtor. O resultado é o mesmo, mas a leitura é mais direta.

### A validação de segurança no construtor

```java
if (valorTotal <= 0) {
    throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero.");
}
```

Essa verificação está no construtor da classe **pai**, o que significa que ela é executada para **qualquer** forma de pagamento, independentemente de qual subclasse está sendo criada. Não é possível criar um `PagamentoPix` ou `PagamentoCartao` com valor inválido sem passar por essa regra.

### Por que `processar()` é abstrato?

```java
public abstract void processar();
```

Cada forma de pagamento tem uma lógica de processamento completamente diferente. Tornar o método abstrato garante que toda subclasse concreta seja obrigada a implementar sua própria versão — o compilador não deixa compilar o código se isso não for feito.

---

## 4. SaldoInsuficienteException.java

```java
public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
```

### Por que `RuntimeException` e não `Exception`?

Esta é uma das decisões mais importantes do projeto. Existem dois tipos de exceções em Java:

| Tipo | Comportamento | Exemplo |
|---|---|---|
| **Checked** (`extends Exception`) | Compilador exige `try-catch` ou `throws` na assinatura | `IOException` |
| **Unchecked** (`extends RuntimeException`) | Não exige declaração explícita | `NullPointerException`, `IllegalArgumentException` |

Usar `RuntimeException` permite que `processar()` em `FormaPagamento` seja declarado simplesmente como:

```java
public abstract void processar();
```

Se fosse `Exception`, seria necessário:

```java
public abstract void processar() throws SaldoInsuficienteException, ChavePixInvalidaException;
```

Isso criaria um acoplamento forte: a classe abstrata passaria a conhecer os detalhes de cada subclasse. Além disso, o `IllegalArgumentException` (já usado no construtor) também é `RuntimeException` — manter a consistência facilita o tratamento no `Main`.

---

## 5. ChavePixInvalidaException.java

```java
public class ChavePixInvalidaException extends RuntimeException {
    public ChavePixInvalidaException(String mensagem) {
        super(mensagem);
    }
}
```

Mesma decisão de design da `SaldoInsuficienteException`. Representa um erro de negócio específico do canal Pix, com nome descritivo que torna o código autoexplicativo.

### Por que criar exceções customizadas em vez de usar `IllegalArgumentException` para tudo?

Porque exceções diferentes permitem tratamentos diferentes no `Main`. Com tipos distintos, é possível exibir mensagens específicas para cada situação de erro, e o `catch` captura exatamente o que espera — sem ambiguidade.

---

## 6. GatewayPagamento.java — O Desafio Extra

```java
public class GatewayPagamento implements AutoCloseable {

    public void conectar() {
        System.out.println("Conectando ao servidor de cartões...");
    }

    @Override
    public void close() {
        System.out.println("Conexão com o gateway encerrada com segurança.");
    }
}
```

### O que é `AutoCloseable`?

É uma interface nativa do Java com um único método: `close()`. Qualquer classe que a implemente pode ser usada em um bloco `try-with-resources`.

### O que o try-with-resources garante?

```java
try (GatewayPagamento gateway = new GatewayPagamento()) {
    gateway.conectar();
    // ... lógica de pagamento
}
// close() é chamado aqui AUTOMATICAMENTE
```

O `close()` é chamado ao final do bloco `try` **sempre**, seja a execução normal ou com exceção. Isso elimina o risco de deixar uma conexão aberta por esquecimento — problema comum e crítico em sistemas financeiros.

Sem o try-with-resources, seria necessário:

```java
GatewayPagamento gateway = new GatewayPagamento();
try {
    gateway.conectar();
} finally {
    gateway.close(); // esquecível e verboso
}
```

---

## 7. PagamentoCartao.java

```java
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
        try (GatewayPagamento gateway = new GatewayPagamento()) {
            gateway.conectar();

            if (this.numeroCartao != null && this.numeroCartao.startsWith("4444")) {
                throw new SaldoInsuficienteException("Cartão recusado por falta de limite.");
            }

            this.status = "APROVADO";
        }
    }

    @Override
    public void emitirNFe() {
        String cartaoMascarado = "**** **** **** " +
            numeroCartao.substring(Math.max(0, numeroCartao.length() - 4));
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
```

### Por que `extends FormaPagamento implements EmitenteNotaFiscal`?

Java não permite herança múltipla de classes, mas permite implementar múltiplas interfaces. Aqui, `PagamentoCartao` faz as duas coisas:

- **Herda** o comportamento e os atributos de `FormaPagamento`
- **Implementa** o contrato de `EmitenteNotaFiscal`

### Por que `super(valorTotal)` no construtor?

O construtor da classe filha deve chamar o construtor da classe pai explicitamente quando o pai não tem um construtor sem argumentos. O `super(valorTotal)` garante que a validação definida em `FormaPagamento` seja executada antes de qualquer coisa.

### Por que os atributos são `private final`?

`private` porque `numeroCartao` e `parcelas` são dados sensíveis e específicos desta classe — nenhuma outra deve acessá-los diretamente. `final` porque o número do cartão e as parcelas de uma transação não mudam após a criação do objeto — é uma boa prática imutabilizar o que não precisa mudar.

### A simulação de saldo insuficiente

```java
if (this.numeroCartao != null && this.numeroCartao.startsWith("4444")) {
    throw new SaldoInsuficienteException("Cartão recusado por falta de limite.");
}
```

O `null check` antes de `startsWith` evita um `NullPointerException` caso o número seja nulo. É uma defesa preventiva.

### O mascaramento do cartão na NFe

```java
numeroCartao.substring(Math.max(0, numeroCartao.length() - 4))
```

`Math.max(0, ...)` é uma proteção: se o número tiver menos de 4 dígitos por algum motivo, `substring` não lançará `StringIndexOutOfBoundsException`. Retorna o que houver.

---

## 8. PagamentoPix.java

```java
public class PagamentoPix extends FormaPagamento {
    private final String chavePix;

    public PagamentoPix(double valorTotal, String chavePix) {
        super(valorTotal);
        this.chavePix = chavePix;
    }

    @Override
    public void processar() {
        if (this.chavePix == null || this.chavePix.trim().isEmpty()) {
            throw new ChavePixInvalidaException("A chave Pix não pode estar vazia.");
        }
        this.status = "APROVADO";
    }
}
```

### Por que não implementa `EmitenteNotaFiscal`?

Por decisão de negócio: o canal Pix, neste sistema, não gera nota fiscal automática. A interface não é aplicada aqui, o que demonstra que a hierarquia de herança e o contrato de interface são **independentes** — uma classe pode herdar sem implementar, e pode implementar sem herdar.

### O `.trim().isEmpty()` em vez de `.isEmpty()`

```java
this.chavePix.trim().isEmpty()
```

`.trim()` remove espaços em branco antes e depois da string. Sem ele, uma chave `"   "` (só espaços) passaria pela validação e seria considerada válida. O `.trim()` torna a validação mais robusta.

A ordem das verificações também importa:

```java
this.chavePix == null || this.chavePix.trim().isEmpty()
```

O `null` é verificado primeiro. Se `chavePix` for `null` e a ordem fosse invertida, `null.trim()` causaria `NullPointerException`. O operador `||` em Java usa avaliação de curto-circuito: se o lado esquerdo for `true`, o lado direito não é avaliado.

---

## 9. Main.java — O Fluxo de Teste

```java
public class Main {
    public static void main(String[] args) {
        ArrayList<FormaPagamento> carrinho = new ArrayList<>();

        carrinho.add(new PagamentoPix(150.00, "123.456.789-00"));
        carrinho.add(new PagamentoPix(200.00, ""));
        carrinho.add(new PagamentoCartao(500.00, "1234567890123456", 10));
        carrinho.add(new PagamentoCartao(999.00, "4444000011112222", 12));

        for (FormaPagamento p : carrinho) {
            try {
                p.processar();
                System.out.println("Pagamento processado! Status: " + p.getStatus());

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
    }
}
```

### O polimorfismo na prática

```java
ArrayList<FormaPagamento> carrinho = new ArrayList<>();
```

A lista armazena objetos do tipo `FormaPagamento`, mas na prática contém instâncias de `PagamentoPix` e `PagamentoCartao`. O laço `for` não sabe e não precisa saber qual tipo concreto é cada objeto — chama `p.processar()` e cada objeto executa a sua própria versão do método. Isso é polimorfismo.

### O `instanceof` e o casting dinâmico

```java
if (p instanceof EmitenteNotaFiscal) {
    ((EmitenteNotaFiscal) p).emitirNFe();
}
```

Antes de chamar `emitirNFe()`, o código verifica se o objeto atual implementa a interface. Só `PagamentoCartao` passa nessa verificação. O `(EmitenteNotaFiscal) p` faz o casting — diz ao compilador "trate esse objeto como um `EmitenteNotaFiscal` agora". Sem o `instanceof`, um `PagamentoPix` causaria `ClassCastException` em tempo de execução.

### A resiliência do sistema — por que `try-catch` está dentro do laço

O `try-catch` está **dentro** do `for`, não fora. Se estivesse fora, uma exceção no segundo pagamento interromperia todo o processamento e os pagamentos 3 e 4 nunca seriam executados. Dentro do laço, cada erro é capturado, a mensagem é exibida, e o próximo pagamento é processado normalmente.

### Por que há três `catch` separados?

Cada exceção representa um tipo diferente de problema e merece uma mensagem diferente para o usuário:

- `SaldoInsuficienteException` → problema com o cartão
- `ChavePixInvalidaException` → problema com a chave Pix
- `IllegalArgumentException` → problema com o valor informado

Usar um único `catch (Exception e)` funcionaria tecnicamente, mas perderia toda a especificidade — e a mensagem de erro seria a mesma para situações completamente diferentes.

---

## 10. Decisões de Design e Por Quê

### Resumo das escolhas mais importantes

| Decisão | Alternativa descartada | Motivo da escolha |
|---|---|---|
| Exceções como `RuntimeException` | `Exception` (checked) | Evita poluir assinaturas de métodos com `throws`; mantém coerência com `IllegalArgumentException` |
| `status` inicializado na declaração | Inicializar no construtor | Mais legível; o valor padrão fica visível junto ao atributo |
| Atributos `private final` em subclasses | Só `private` | `final` comunica a imutabilidade intencional da transação |
| `trim()` antes de `isEmpty()` | Só `isEmpty()` | Captura casos com espaços em branco que passariam como válidos |
| `Math.max(0, length - 4)` no mascaramento | `substring(length - 4)` direto | Evita `StringIndexOutOfBoundsException` para strings curtas |
| `null check` antes de `startsWith` | Confiar que nunca será nulo | Defensive programming; evita NPE em dados inesperados |
| `try-catch` dentro do laço | `try-catch` fora do laço | Garante que um erro não interrompa o processamento dos demais itens |
| `instanceof` antes do cast | Cast direto | Evita `ClassCastException` em tempo de execução |

---

## 11. Saída Esperada no Console

```
Iniciando processamento da fila de pagamentos...

Conectando ao servidor de cartões... [não aparece para Pix]
Pagamento processado! Status: APROVADO
-----------------------------------------
Falha no Pix: A chave Pix não pode estar vazia.
-----------------------------------------
Conectando ao servidor de cartões...
Conexão com o gateway encerrada com segurança.
Pagamento processado! Status: APROVADO

=== NOTA FISCAL ELETRÔNICA ===
Forma de Pagamento: Cartão de Crédito
Cartão: **** **** **** 3456
Parcelas: 10x
Valor Total: R$ 500,00
Imposto Retido (15%): R$ 75,00
==============================

-----------------------------------------
Conectando ao servidor de cartões...
Conexão com o gateway encerrada com segurança.
Operação Recusada: Cartão recusado por falta de limite.
-----------------------------------------
Fila de processamento concluída!
```

> **Observação sobre o cartão recusado:** o `close()` do `GatewayPagamento` aparece **antes** da mensagem de erro. Isso demonstra o try-with-resources funcionando: mesmo quando a exceção é lançada, a conexão é encerrada antes de a exceção se propagar para o `catch` do `Main`.
