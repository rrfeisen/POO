# TransBR Logística — Sistema Unificado de Logística e Despacho

Projeto em Java puro (sem dependências externas) implementando o sistema
descrito no edital.

## Estrutura

```
src/rrfeisen/transbr/
├── Main.java                       -> menu interativo de console
├── GerenciadorLogistica.java       -> regras de negócio + persistência binária
├── model/
│   ├── Veiculo.java                (abstract)
│   ├── CaminhaoBau.java
│   ├── CarretaTanque.java
│   ├── Motorista.java
│   ├── Carga.java                  (abstract)
│   ├── CargaRegular.java
│   ├── CargaPerigosa.java
│   ├── Seguravel.java               (interface)
│   └── Viagem.java
└── exceptions/
    ├── ExcessoPesoException.java
    ├── MoppNaoCertificadoException.java
    ├── CarroceriaIncompativelException.java
    ├── LicencaAmbientalInvalidaException.java
    └── InspecaoInmetroException.java
```

## Como compilar e executar (linha de comando)

```bash
# a partir da raiz do projeto
javac -d bin -encoding UTF-8 $(find src -name "*.java")
cd bin
java rrfeisen.transbr.Main
```

O arquivo `transbr_dados.dat` é criado/lido no diretório onde o programa é
executado (no exemplo acima, dentro de `bin/`). Para reiniciar do zero, basta
apagar esse arquivo.

## Como importar em uma IDE (NetBeans / Eclipse / IntelliJ)

1. Crie um novo projeto Java vazio.
2. Copie a pasta `src/rrfeisen` inteira para dentro do `src` do projeto da IDE.
3. Defina `rrfeisen.transbr.Main` como classe principal (main class).
4. Rode normalmente — a IDE compila tudo automaticamente.

## Menu do sistema

```
1 - Cadastrar Veículo
2 - Cadastrar Motorista
3 - Cadastrar Carga
4 - Criar Viagem (Despacho)
5 - Listar Todos os Cadastros   <- mostra veículos, motoristas, cargas e viagens já salvos
6 - Listar Viagens Ativas
7 - Remover Cadastro            <- remove veículo, motorista, carga ou viagem
8 - Salvar e Sair
```

A opção **5** existe especificamente para visualizar o conteúdo do
`transbr_dados.dat` em texto legível: como esse arquivo é binário
(serialização via `ObjectOutputStream`), não é possível abri-lo num editor
de texto comum — a forma correta de "acessar" os dados é rodar o programa
(que já desserializa tudo automaticamente ao iniciar) e usar essa opção do
menu para ver tudo o que está armazenado.

A opção **7** permite remover qualquer veículo, motorista, carga ou viagem
cadastrados. Antes de remover, o sistema:
1. Lista os itens existentes para o operador escolher;
2. Avisa se o item já está vinculado a uma viagem registrada (a viagem
   mantém o histórico intacto mesmo após a remoção do cadastro original,
   já que `Viagem` guarda a referência direta ao objeto, não um índice);
3. Pede confirmação explícita (s/n) antes de remover de fato.

## Cobertura das regras de negócio (todas validadas em `criarViagem`)

| Regra do edital                                   | Exceção lançada                       |
|-----------------------------------------------------|----------------------------------------|
| Peso da carga > capacidade do veículo               | `ExcessoPesoException`                 |
| Carga perigosa + motorista sem MOPP ativo           | `MoppNaoCertificadoException`          |
| Carga perigosa inflamável/líquida fora de Carreta Tanque | `CarroceriaIncompativelException` |
| Carga perigosa sem licença ambiental válida          | `LicencaAmbientalInvalidaException`    |
| Carreta Tanque com inspeção Inmetro vencida/inativa  | `InspecaoInmetroException`             |
| Apólice de seguro obrigatória                        | Calculada automaticamente via `Seguravel.calcularSeguro()` (toda Carga Perigosa, e Carga Regular com valor > R$ 100.000,00) |
| Perda de dados ao reiniciar                          | Persistência binária via `ObjectInputStream`/`ObjectOutputStream` em `GerenciadorLogistica` |

Todas as exceções estendem `RuntimeException` e são capturadas em um único
bloco `try-catch` no menu "Criar Viagem", exibindo um alerta amigável sem
travar o programa, conforme exigido no edital.

## Decisões de implementação

- **Herança**: atributos comuns (`placa`, `capacidadeMaximaToneladas` em
  `Veiculo`; `descricao`, `pesoToneladas`, `valorMercado` em `Carga`) ficam
  `protected` na classe-mãe.
- **Interface `Seguravel`**: tanto `CargaPerigosa` quanto `CargaRegular` a
  implementam. `CargaPerigosa.calcularSeguro()` sempre retorna um valor
  (4% do valor de mercado); `CargaRegular.calcularSeguro()` só retorna valor
  maior que zero (1,5%) quando o valor de mercado supera R$ 100.000,00 —
  caso contrário retorna `0.0`.
- **Inspeção Inmetro**: tratada como regra adicional específica de
  `CarretaTanque`, já que o edital a lista como exigência do veículo.
- **IDs de Viagem**: gerados por um contador estático sequencial. Como esse
  contador não é parte do estado serializado de cada `Viagem`, o
  `GerenciadorLogistica.carregar()` resincroniza o contador com o maior ID
  encontrado no arquivo `.dat` ao abrir o programa, evitando IDs duplicados.
- **UTF-8 forçado** na entrada (`Scanner`) e saída (`PrintStream`) do console
  para garantir que acentuação seja exibida corretamente em qualquer terminal.
