# Guia Técnico — Sistema de Gestão Académica ISSMF
> LP2-G2 · Versão documentada após auditoria completa (Junho 2026)

---

## Índice

1. [Visão Geral da Arquitetura](#1-visão-geral-da-arquitetura)
2. [Camada Model](#2-camada-model)
3. [Camada DAL — Acesso a Dados](#3-camada-dal--acesso-a-dados)
4. [A Base de Dados: HikariCP e DatabaseConnection](#4-a-base-de-dados-hikaricp-e-databaseconnection)
5. [DAOFactory — O Padrão de Fábrica](#5-daofactory--o-padrão-de-fábrica)
6. [Camada BLL — Lógica de Negócio](#6-camada-bll--lógica-de-negócio)
7. [Camada Controller](#7-camada-controller)
8. [Camada View](#8-camada-view)
9. [Java Avançado: Lambdas, Streams e Genéricos](#9-java-avançado-lambdas-streams-e-genéricos)
10. [Padrões de Design Utilizados](#10-padrões-de-design-utilizados)
11. [Estado dos Testes e Bugs Conhecidos](#11-estado-dos-testes-e-bugs-conhecidos)
12. [Enunciados: Cobertura e Lacunas](#12-enunciados-cobertura-e-lacunas)

---

## 1. Visão Geral da Arquitetura

O projeto segue uma arquitetura **MVC em 4 camadas**:

```
┌──────────────────────────────────────────────────────────┐
│  VIEW  (view/)          Console UI, menus, tabelas       │
├──────────────────────────────────────────────────────────┤
│  CONTROLLER (controller/)  Orquestração, validação       │
├──────────────────────────────────────────────────────────┤
│  BLL (BLL/)             Regras de negócio puras          │
├──────────────────────────────────────────────────────────┤
│  DAL (DAL/)             Acesso a dados (CSV ou SQL)      │
│    ├─ CSV (AbstractCsvCRUD)                              │
│    └─ SQL (DatabaseConnection + HikariCP)                │
└──────────────────────────────────────────────────────────┘
```

**Dual Persistence** — o mesmo código de negócio funciona com dois motores de armazenamento, controlado por `config.properties`:
- `armazenamento.tipo=CSV` → ficheiros em `src/main/java/CSVs/`
- `armazenamento.tipo=SQL` → SQL Server via JDBC + HikariCP

A escolha é feita em runtime através de `DAOFactory`.

---

## 2. Camada Model

Os models são POJOs (Plain Old Java Objects) sem lógica de negócio — apenas dados e getters/setters.

### Hierarquia de utilizadores

```
Utilizador (abstrato)
 ├── Gestor
 ├── Docente
 └── Estudante
```

`Utilizador` contém os campos comuns: nome, morada, NIF, dataNascimento, email, hash (password), ativo.

### Campos-chave que causam confusão

**`Estudante.listaAvaliacoes`** — *não* é uma lista de UCs. É uma lista de `Avaliacao`. Para aceder às UCs de um estudante:
```java
// ERRADO:
estudante.getListaAvaliacoes(); // pode estar vazia!

// CORRETO: carregar primeiro, depois ler
estudante.setListaAvaliacoes(avaliacaoDAO.listarPorEstudante(mec));
estudante.getListaAvaliacoes().stream()
    .map(Avaliacao::getUnidadeCurricular)
    .collect(toList());
```
`listaAvaliacoes` começa sempre como `new ArrayList<>()` vazio. Tem de ser explicitamente preenchida antes de usar.

**`Resultado<T>`** — wrapper genérico para retornos de controller:
```java
public class Resultado<T> {
    public T dados;         // resultado em caso de sucesso
    public boolean sucesso;
    public String mensagemErro;
}
```
Exemplo de uso num controller:
```java
// Falha:
return new Resultado<>(false, "Estudante não encontrado.");
// Sucesso com dados:
return new Resultado<>(estudante, true);
```

**`Curso.precoAnual`** — pode ser `null` se o Curso for criado com o construtor `new Curso(nome, duracao, departamento)` sem definir preço. Isto causava um NullPointerException silencioso em `garantirPropinaPrimeiroAno` (bug corrigido em junho 2026).

---

## 3. Camada DAL — Acesso a Dados

### 3.1 CSV: AbstractCsvCRUD

```
AbstractCsvCRUD<T>
 └─ Guarda dados em src/main/java/CSVs/<entidade>.csv
 └─ Carrega para memória (List<T> dados) na 1.ª instanciação
 └─ Escritas: atualizam a memória E o ficheiro em simultâneo
```

Cada subclasse implementa dois métodos:
- `mapearLinhaParaEntidade(String linha)` → transforma uma linha CSV no objeto Java
- `mapearEntidadeParaLinha(T entidade)` → transforma o objeto numa linha CSV

**Atenção — bug de design em AvaliacaoCRUD:** o `mapearLinhaParaEntidade` instancia directamente `new UnidadeCurricularCRUD()` e `new EstudanteCRUD()`. Isto significa que cada linha do CSV de avaliações faz uma leitura completa dos CSVs de UCs e estudantes. Em produção com muitas avaliações, isto é O(n²). A versão SQL resolve este problema com JOINs.

### 3.2 SQL: SQLDAOs e RowMapper

Cada entidade com suporte SQL tem um `XxxSqlDAO` que usa `DatabaseConnection`:

```java
// Exemplo: UnidadeCurricularSqlDAO.getUnidadeCurriculars()
public List<UnidadeCurricular> getUnidadeCurriculars() {
    return db.select(SELECT_UC, ucMapper(), (Object[]) null);
}
```

O `SELECT_UC` usa LEFT JOIN para trazer o docente em UMA query, eliminando o N+1:
```sql
SELECT uc.id, uc.nome, uc.anoCurricular, uc.semestre, uc.docenteId,
       d.nome AS docNome, d.morada AS docMorada, ...
FROM UnidadeCurricular uc
LEFT JOIN Docente d ON uc.docenteId = d.id
```

O `ucMapper()` é um **RowMapper** — uma interface funcional que recebe um `ResultSet` e devolve um objeto:
```java
private RowMapper<UnidadeCurricular> ucMapper() {
    return rs -> {                          // ← lambda
        Docente docente = null;
        String docNome = rs.getString("docNome");
        if (docNome != null) {             // docente pode não existir (LEFT JOIN)
            docente = new Docente(docNome, ...);
        }
        return new UnidadeCurricular(rs.getString("nome"), rs.getInt("id"), ...);
    };
}
```

### 3.3 Interfaces IxxxDAO

Cada entidade tem uma interface (ex: `IEstudanteDAO`) que ambas as implementações (CSV e SQL) cumprem. Isto permite trocar a implementação sem alterar os controllers.

```
IEstudanteDAO
 ├── EstudanteCRUD    (CSV)
 └── EstudanteSqlDAO  (SQL)
```

---

## 4. A Base de Dados: HikariCP e DatabaseConnection

### O que é HikariCP?

HikariCP é um **connection pool** — em vez de abrir uma nova ligação TCP ao SQL Server em cada query (o que demora ~100ms), mantém um conjunto de ligações pré-abertas e reutilizáveis.

```
Sem pool:                    Com HikariCP:
query 1 → abrir ligação      query 1 → pegar ligação do pool
         → executar                   → executar
         → fechar ligação             → devolver ao pool
query 2 → abrir ligação      query 2 → pegar ligação do pool (já pronta)
  (100ms de overhead!)               → executar
                                     → devolver ao pool
                                       (~1ms de overhead)
```

### Configuração (DatabaseConnection.java)

```java
// Pool criado UMA ÚNICA VEZ para toda a JVM (padrão Singleton)
private static volatile HikariDataSource pool;
private static volatile boolean inicializado = false;

public DatabaseConnection() {
    // Double-checked locking: thread-safe, sem synchronize desnecessário
    if (!inicializado) {
        synchronized (DatabaseConnection.class) {
            if (!inicializado) {
                inicializarPool();
                inicializado = true;
            }
        }
    }
}
```

O `volatile` garante que a thread vê o valor mais recente da variável (sem cache de CPU).  
O `synchronized` garante que só uma thread inicializa o pool mesmo em acesso concorrente.

Configuração do pool:
```java
cfg.setMaximumPoolSize(10);    // até 10 ligações em paralelo
cfg.setMinimumIdle(2);         // sempre 2 prontas
cfg.setConnectionTimeout(30_000);   // 30s para obter ligação
cfg.setIdleTimeout(600_000);        // 10min inativa → fecha
cfg.setMaxLifetime(1_800_000);      // 30min máximo de vida
cfg.setAutoCommit(true);            // cada operação é auto-confirmada
```

### Os 4 métodos de DatabaseConnection

| Método | Quando usar | Retorna |
|--------|-------------|---------|
| `select(sql, mapper, params)` | Consultas (SELECT) | `ArrayList<T>` |
| `create(sql, params)` | INSERT com chave gerada | `int` (ID gerado) |
| `execute(sql, params)` | UPDATE / DELETE / INSERT simples | `int` (linhas afetadas) |
| `runTransaction(work)` | Múltiplas operações ACID | `boolean` (sucesso) |

**Exemplo de SELECT com lambda:**
```java
// Busca todos os gestores com nif > 100000
ArrayList<Gestor> gestores = db.select(
    "SELECT * FROM Gestor WHERE nif > ?",
    rs -> new Gestor(rs.getString("nome"), rs.getInt("nif"), ...),  // RowMapper como lambda
    100000  // parâmetro (evita SQL injection)
);
```

**Exemplo de transação:**
```java
// Transferência: debita A e credita B atomicamente
boolean ok = db.runTransaction(conn -> {
    try (PreparedStatement s1 = conn.prepareStatement("UPDATE Conta SET saldo=saldo-? WHERE id=?")) {
        s1.setDouble(1, valor); s1.setInt(2, idA);
        s1.executeUpdate();
    }
    try (PreparedStatement s2 = conn.prepareStatement("UPDATE Conta SET saldo=saldo+? WHERE id=?")) {
        s2.setDouble(1, valor); s2.setInt(2, idB);
        s2.executeUpdate();
    }
});
// Se qualquer linha falhar → ROLLBACK automático
```

O `TransactionConsumer` é uma **interface funcional** anotada com `@FunctionalInterface`:
```java
@FunctionalInterface
public interface TransactionConsumer {
    void execute(Connection conn) throws SQLException;
}
```
Qualquer lambda `conn -> { ... }` que lança `SQLException` implementa esta interface.

### Gestão automática de recursos (try-with-resources)

```java
// ANTES (problemático — ligação não fechada se exceção):
Connection conn = openConnection();
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.executeUpdate();
stmt.close();
conn.close();

// DEPOIS (correto — Java fecha automaticamente ao sair do bloco):
try (conn; PreparedStatement stmt = conn.prepareStatement(sql)) {
    stmt.executeUpdate();
} // ← conn e stmt fechados/devolvidos ao pool aqui, mesmo com exceção
```

`try (conn; stmt)` chama `conn.close()` e `stmt.close()` automaticamente porque ambos implementam `AutoCloseable`. No caso da ligação HikariCP, `close()` **não fecha a ligação** — devolve-a ao pool para reutilização.

### SQL injection e PreparedStatement

**NUNCA concatenar strings em SQL:**
```java
// PERIGOSO (SQL Injection):
String sql = "SELECT * FROM Gestor WHERE email='" + email + "'";

// SEGURO (PreparedStatement):
String sql = "SELECT * FROM Gestor WHERE email=?";
stmt.setString(1, email);  // valor escapado automaticamente
```
O `?` é um parâmetro de substituição. O driver JDBC escapa todos os caracteres especiais antes de enviar ao servidor.

---

## 5. DAOFactory — O Padrão de Fábrica

`DAOFactory` implementa o padrão **Abstract Factory + Singleton**:

```java
public class DAOFactory {
    private static String tipoArmazenamento; // "CSV" ou "SQL"
    private static IEstudanteDAO estudanteDAO; // singleton em cache

    public static IEstudanteDAO getEstudanteDAO() {
        if (estudanteDAO == null)
            estudanteDAO = isSql() ? new EstudanteSqlDAO() : new EstudanteCRUD();
        return estudanteDAO;
    }
}
```

**Como funciona:**
1. Na primeira chamada a `getEstudanteDAO()`, a instância é null → cria-se a instância correta
2. Nas chamadas seguintes, retorna a mesma instância (singleton)
3. `resetarInstancias()` descarta todos os singletons (útil em testes)
4. `setModo("SQL")` muda o modo E descarta os singletons

**O modo é lido de `config.properties`:**
```properties
armazenamento.tipo=CSV   # ou SQL
```

**Atenção em testes:** o `DAOFactory` usa estado estático. Se dois testes mudarem o modo, podem interferir entre si. A solução é chamar `DAOFactory.setModo("CSV")` no `@BeforeAll` de cada suite de testes que usa CSV.

---

## 6. Camada BLL — Lógica de Negócio

A BLL (Business Logic Layer) contém cálculos puros, sem dependência de DAOs. Isto permite testá-los de forma isolada.

### EstudanteCalculo.isUCAprovada()

```java
public static boolean isUCAprovada(List<Avaliacao> avaliacoes, String nomeUC) {
    // 1. Filtrar avaliações desta UC que têm nota
    List<Avaliacao> avaliacoesDestaUC = avaliacoes.stream()
        .filter(a -> a.getUnidadeCurricular().getNome().equalsIgnoreCase(nomeUC)
                  && a.getNota() != null)
        .toList();  // Java 16+: cria lista imutável

    if (avaliacoesDestaUC.isEmpty()) return false;

    // 2. Verificar se TODOS os momentos da UC têm nota
    List<String> momentosValidos = avaliacoesDestaUC.get(0)
        .getUnidadeCurricular().getMomentosAvaliacao();
    
    boolean todosOsMomentosTêmNota = momentosValidos.stream()
        .allMatch(momento -> avaliacoesDestaUC.stream()
            .anyMatch(a -> a.getMomento().equalsIgnoreCase(momento)));
    
    if (!todosOsMomentosTêmNota) return false;

    // 3. Calcular média e verificar >= 9.5
    double soma = avaliacoesDestaUC.stream()
        .filter(a -> momentosValidos.stream()
            .anyMatch(m -> m.equalsIgnoreCase(a.getMomento())))
        .mapToDouble(Avaliacao::getNota)
        .sum();
    
    return (soma / momentosValidos.size()) >= 9.5;
}
```

**Pontos críticos:**
- Uma UC só é "aprovada" se TODOS os momentos definidos tiverem nota lançada
- A média é calculada sobre os momentos DEFINIDOS (não sobre os momentos com nota)
- Se `momentosValidos` estiver vazio (UC sem momentos configurados), retorna `false`

### EstudanteCalculo.calcularAnoDesbloqueado()

```java
public static int calcularAnoDesbloqueado(Estudante estudante, Curso curso) {
    List<Avaliacao> avaliacoes = estudante.getListaAvaliacoes();
    
    // UCs do ano atual ou anterior (denominador da taxa de aproveitamento)
    long totalInscritas = curso.getUnidadeCurriculars().stream()
        .filter(uc -> uc.getAnoCurricular() <= estudante.getAnoLetivo())
        .count();
    
    if (totalInscritas == 0) return 1;
    
    // UCs aprovadas em TODO o curso (numerador)
    long aprovadasGlobais = curso.getUnidadeCurriculars().stream()
        .filter(u -> isUCAprovada(avaliacoes, u.getNome()))
        .count();
    
    double aproveitamento = (double) aprovadasGlobais / totalInscritas;
    
    // Regra dos 60%: avança de ano se >= 60% das UCs do ano atual aprovadas
    if (aproveitamento >= 0.60) {
        return Math.min(estudante.getAnoLetivo() + 1, curso.getDuracao());
    }
    return estudante.getAnoLetivo();
}
```

**Assimetria intencional:** `totalInscritas` conta só UCs do ano atual, mas `aprovadasGlobais` conta aprovações em TODO o curso. Isto permite que aprovações de UCs de anos superiores (feitas antecipadamente) contribuam para a progressão.

### NotasCalculo.calcularMedia()

```java
public static double calcularMedia(List<Avaliacao> avaliacoes) {
    // Agrupa notas por UC
    Map<String, List<Double>> notasPorUC = new HashMap<>();
    for (Avaliacao av : avaliacoes) {
        if (av.getNota() != null && av.getUnidadeCurricular() != null) {
            notasPorUC
                .computeIfAbsent(av.getUnidadeCurricular().getNome(), k -> new ArrayList<>())
                .add(av.getNota());
        }
    }
    // computeIfAbsent: se a chave não existir, cria uma nova ArrayList e coloca no mapa
    
    // Média das médias por UC (não média global de todas as notas)
    double somaMediasUC = notasPorUC.values().stream()
        .mapToDouble(notas -> notas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0))
        .sum();
    
    return Math.round((somaMediasUC / notasPorUC.size()) * 100.0) / 100.0;
}
```

`computeIfAbsent` é equivalente a:
```java
if (!map.containsKey(key)) map.put(key, new ArrayList<>());
map.get(key).add(value);
```
mas mais eficiente (só faz o lookup uma vez).

---

## 7. Camada Controller

Os controllers orquestram a lógica: validam inputs, chamam DAOs e BLL, devolvem `Resultado<T>`.

### Fluxo típico de um controller

```java
public Resultado<Integer> registarEstudante(String nome, String morada, int nif, ...) {
    // 1. Validar inputs
    if (!BackendUtils.isNomeValido(nome))
        return new Resultado<>(false, "Nome inválido.");
    
    // 2. Verificar regras de negócio
    if (estudanteDAO.procurarPorNif(nif) != null)
        return new Resultado<>(false, "Já existe um estudante com esse NIF.");
    
    // 3. Criar entidade
    Estudante estudante = new Estudante(nome, morada, nif, ...);
    
    // 4. Persistir
    Resultado<Estudante> res = estudanteDAO.registarEstudante(estudante);
    
    // 5. Operações adicionais em caso de sucesso
    if (res.sucesso) {
        garantirPropinaPrimeiroAno(res.dados.getNumeroMec(), curso);
    }
    
    return res.sucesso
        ? new Resultado<>(res.dados.getNumeroMec(), true)
        : new Resultado<>(false, res.mensagemErro);
}
```

### Dependências entre controllers

**EstudanteController** instancia `PropinaController` internamente (em `obterAnoDesbloqueado`, `verificarSeCursoConcluido`, `simularTransicaoAnoLetivoGlobal`). Isto é um acoplamento implícito — idealmente seria injeção de dependência, mas é o padrão atual do projeto.

### CursoController.iniciarAnoLetivo()

Este método é o mais complexo do sistema. Quando um docente inicia o ano letivo:
1. Verifica se o curso existe e se o ano não foi já iniciado
2. Marca o ano como iniciado no curso
3. Para cada estudante do curso cujo `obterAnoDesbloqueado() == anoLetivo`, cria registos de `Avaliacao` nulos (um por momento, por UC, por estudante)
4. Isto permite que os estudantes vejam as UCs inscritas antes das notas serem lançadas

---

## 8. Camada View

### Padrão dos menus com while loop

Todos os menus persistentes seguem este padrão:
```java
while (true) {
    try {
        System.out.print(GetWhiteBold() + "Opção: " + GetReset());
        String input = ler.nextLine().trim();
        int opcao = Integer.parseInt(input);
        if (opcao < 1 || opcao > lista.size()) {
            System.out.println(GetRed() + "Opção inválida." + GetReset());
            continue;  // ← volta ao início do while, não sai
        }
        // processar opcao
        break;  // ← sai do while depois de processar com sucesso
    } catch (NumberFormatException e) {
        System.out.println(GetRed() + "Digite apenas números." + GetReset());
        // continua o while automaticamente
    }
}
```

### DesignUtils — cores e formatação

```java
// Cores ANSI para terminal
GetCyanBold()   → "\033[1;36m"   // cabeçalhos de tabela
GetWhiteBold()  → "\033[1;37m"   // prompts
GetGreen()      → "\033[0;32m"   // sucesso / aprovado
GetRed()        → "\033[0;31m"   // erro / reprovado
GetYellow()     → "\033[0;33m"   // aviso / pendente
GetReset()      → "\033[0m"      // reset (obrigatório no final!)

// Bordas de caixa
GetBordaSuperior()  → "╔══...══╗"
GetBordaMeio()      → "╠══...══╣"
GetBordaInferior()  → "╚══...══╝"
```

**Regra importante:** sempre terminar strings coloridas com `GetReset()`. Se não o fizer, a cor "vaza" para as linhas seguintes.

```java
// Correto:
System.out.println(GetGreen() + "Aprovado" + GetReset());
// Errado (cor fica ativa):
System.out.println(GetGreen() + "Aprovado");
```

### BackendUtils.parseHoraFlexivel()

Aceita múltiplos formatos de hora para facilitar a entrada:
```java
parseHoraFlexivel("18")    → LocalTime.of(18, 0)  // "18" → 18:00
parseHoraFlexivel("18:30") → LocalTime.of(18, 30)
parseHoraFlexivel("1830")  → LocalTime.of(18, 30)  // formato compacto
parseHoraFlexivel("9")     → LocalTime.of(9, 0)    // hora única
```

---

## 9. Java Avançado: Lambdas, Streams e Genéricos

### 9.1 Lambdas

Uma lambda é uma função anónima. Em Java, só funciona onde existe uma **interface funcional** (interface com um único método abstrato).

```java
// Interface funcional:
@FunctionalInterface
interface RowMapper<T> {
    T mapRow(ResultSet rs) throws SQLException;
}

// Implementação tradicional (classe anónima):
RowMapper<Estudante> mapper = new RowMapper<Estudante>() {
    @Override
    public Estudante mapRow(ResultSet rs) throws SQLException {
        return new Estudante(rs.getString("nome"), ...);
    }
};

// Implementação com lambda (equivalente, muito mais conciso):
RowMapper<Estudante> mapper = rs -> new Estudante(rs.getString("nome"), ...);
```

**Sintaxe da lambda:**
```java
// Zero parâmetros:
Runnable r = () -> System.out.println("Hello");

// Um parâmetro (sem parênteses se tipo inferido):
Consumer<String> c = nome -> System.out.println(nome);

// Vários parâmetros:
BiFunction<Integer, Integer, Integer> soma = (a, b) -> a + b;

// Corpo com múltiplas linhas:
Function<String, Integer> f = nome -> {
    int tamanho = nome.length();
    return tamanho * 2;
};
```

### 9.2 Method References

Alternativa mais concisa à lambda quando a lambda apenas chama um método:

```java
// Lambda:
list.stream().map(e -> e.getNome()).collect(toList());

// Method reference equivalente:
list.stream().map(Estudante::getNome).collect(toList());

// Tipos de method reference:
Classe::metodoEstatico       // Integer::parseInt
instancia::metodoDeInstancia // System.out::println
Classe::metodoDeInstancia    // Estudante::getNome (instância implícita)
Classe::new                  // ArrayList::new (construtor)
```

### 9.3 Streams

Streams são sequências de operações sobre coleções, sem modificar a coleção original:

```java
List<Estudante> estudantes = ...;

// Exemplo complexo: nomes dos estudantes ativos, ordenados, sem duplicados
List<String> nomes = estudantes.stream()
    .filter(e -> e.isAtivo())              // só ativos
    .filter(e -> e.getAnoLetivo() == 2)    // só do 2º ano
    .map(Estudante::getNome)               // extrai nome
    .sorted()                              // ordena alfabeticamente
    .distinct()                            // remove duplicados
    .collect(Collectors.toList());         // materializa em List

// Contagem com condição:
long total = avaliacoes.stream()
    .filter(a -> a.getNota() != null && a.getNota() >= 9.5)
    .count();

// Soma:
double media = avaliacoes.stream()
    .filter(a -> a.getNota() != null)
    .mapToDouble(Avaliacao::getNota)    // DoubleStream
    .average()                          // OptionalDouble
    .orElse(0.0);                       // valor padrão se vazio

// Verificação:
boolean todosAprovados = avaliacoes.stream()
    .allMatch(a -> a.getNota() != null && a.getNota() >= 9.5);

boolean algumReprovado = avaliacoes.stream()
    .anyMatch(a -> a.getNota() != null && a.getNota() < 9.5);
```

**Streams são lazy:** as operações intermediárias (`filter`, `map`, `sorted`) só executam quando uma operação terminal (`collect`, `count`, `findFirst`) é chamada.

### 9.4 Optional

`Optional<T>` evita NullPointerException ao representar um valor que pode não existir:

```java
// Em vez de: Estudante e = dao.procurar(mec); // pode ser null → NPE
// Usar:
Optional<Estudante> opt = dao.procurar(mec);
opt.ifPresent(e -> System.out.println(e.getNome())); // só executa se presente

// Com valor padrão:
String nome = opt.map(Estudante::getNome).orElse("Desconhecido");

// Em streams:
Propina propina = propinas.stream()
    .filter(p -> p.getAnoLetivo() == 1)
    .findFirst()     // → Optional<Propina>
    .orElse(null);   // null se não encontrar
```

### 9.5 Genéricos

```java
// Resultado<T> é genérico: T pode ser qualquer tipo
public class Resultado<T> {
    public T dados;  // tipo concreto determinado na criação
}

// Uso:
Resultado<Integer> r1 = new Resultado<>(42, true);       // T = Integer
Resultado<Estudante> r2 = new Resultado<>(estudante, true); // T = Estudante
Resultado<List<String>> r3 = new Resultado<>(lista, true);  // T = List<String>

// Wildcard (qualquer tipo):
Resultado<?> resultado = fazerAlgo(); // não sei o tipo, mas posso ler sucesso/mensagem
```

### 9.6 Text Blocks (Java 15+)

```java
// Em EstudanteController.obterFichaEstudanteFormatada():
return """
    --- FICHA DE ESTUDANTE ---
    Nome: %s
    N. Mecanográfico: %s
    Email: %s
    """.formatted(nome, mec, email);
// Sem escapes de \n, indentação removida automaticamente
```

### 9.7 Double-Checked Locking

Padrão para criar singletons thread-safe:
```java
private static volatile HikariDataSource pool; // volatile: visível entre threads

// Verificação dupla para minimizar sincronização:
if (!inicializado) {                           // check 1: rápido, sem lock
    synchronized (DatabaseConnection.class) { // lock: garante exclusividade
        if (!inicializado) {                   // check 2: dentro do lock
            inicializarPool();
            inicializado = true;
        }
    }
}
// 99.9% das vezes, o check 1 é suficiente (já inicializado)
// Só sincroniza na primeira chamada
```

---

## 10. Padrões de Design Utilizados

| Padrão | Onde | Descrição |
|--------|------|-----------|
| **MVC** | Toda a aplicação | Model-View-Controller |
| **Abstract Factory** | `DAOFactory` | Cria famílias de objetos (CSV ou SQL) |
| **Strategy** | `IEstudanteDAO` + impls | Comportamento intercambiável |
| **Singleton** | `DAOFactory` singletons, HikariCP pool | Uma única instância |
| **Double-Checked Locking** | `DatabaseConnection` | Singleton thread-safe |
| **Template Method** | `AbstractCsvCRUD` | Esqueleto do algoritmo, detalhes nas subclasses |
| **Result Object** | `Resultado<T>` | Encapsula sucesso/erro/dados |
| **Functional Interface** | `RowMapper<T>`, `TransactionConsumer` | Permite lambdas |

---

## 11. Estado dos Testes e Bugs Conhecidos

### Resultados dos testes (junho 2026)

```
Total: 231 testes, 6 ignorados (v1.3 @Disabled)

Com VPN ISEP (DB acessível):  ~210 PASS, ~17 FAIL (limpeza de dados de testes anteriores)
Sem VPN ISEP (sem BD):        ~207 PASS (CSV), 17 FAIL + 3 ERROR (todos SQL, esperado)
```

### Falhas SQL (apenas sem VPN — ambientais)

Os testes SQL (`DocenteSqlDAOTest`, `GestorSqlDAOTest`, etc.) requerem ligação ao servidor `CTESPBD.DEI.ISEP.IPP.PT`. Sem VPN ISEP, o pool HikariCP falha ao inicializar e todos os DAOs SQL retornam `false`/`empty` em vez de lançar exceção.

### Bugs corrigidos nesta sessão

| Bug | Ficheiro | Descrição |
|-----|----------|-----------|
| NullPointerException silencioso | `EstudanteController:338` | `getPrecoAnual()` null quando Curso criado sem preço → propina nunca criada |
| listaAvaliacoes vazia nos menus | `EstudanteView:42` | `verMeuHorario` e `marcarMinhaPresenca` recebiam estudante sem avaliacoes |
| UCs não mostradas no Gestor | `GestorView:1370` | `procurarEstudante` não carregava nem mostrava UCs |
| SSL raw JDBC em testes | `SetupBDTest` | Mudado para usar pool HikariCP via `DatabaseConnection.getPooledConnection()` |
| SchemaBDTest SSL | `SchemaBDTest` | Mesmo fix |

### Bugs conhecidos (não corrigidos)

**BUG-1 (MÉDIO) — AvaliacaoController.obterStatusAprovacao inconsistente com isUCAprovada:**
- `obterStatusAprovacao` calcula média dividindo pelo número de momentos COM nota
- `isUCAprovada` exige TODOS os momentos definidos, divide pelo número TOTAL de momentos
- Um estudante pode aparecer "APROVADO" em `obterStatusAprovacao` mas falhar em `isUCAprovada`
- Ficheiro: `AvaliacaoController.java` linhas 77-91

**BUG-2 (BAIXO) — AvaliacaoCRUD.mapearLinhaParaEntidade cria instâncias fora do DAOFactory:**
- `new UnidadeCurricularCRUD()` e `new EstudanteCRUD()` criados diretamente
- Em modo SQL, isto usa CSV em vez do SQL DAO
- Causa: avaliacoes em modo SQL não mapeiam corretamente UC/Estudante quando lidos do CSV

**BUG-3 (BAIXO) — CSVs em src/main/java:**
- Dados de persistência misturados com código fonte
- Impede empacotamento correto em JAR
- Solução: mover para `src/main/resources/CSVs/`

---

## 12. Enunciados: Cobertura e Lacunas

### Enunciado 1.0 — Gestão Académica Base ✅ IMPLEMENTADO

| Funcionalidade | Estado |
|----------------|--------|
| CRUD Departamento | ✅ |
| CRUD Docente | ✅ |
| CRUD Estudante | ✅ |
| CRUD UC com momentos | ✅ |
| CRUD Curso com UCs | ✅ |
| Login com hash+salt | ✅ |
| Ficha de estudante | ✅ |
| Regra 60% progressão | ✅ (EstudanteCalculo) |
| Máx 5 UCs por ano | ✅ (Curso.adicionarUnidadeCurricular) |
| Máx 3 momentos por UC por estudante | ✅ (AvaliacaoController) |
| Notas 0-20 | ✅ |
| Email automático `mec@issmf.ipp.pt` | ✅ |

### Enunciado 1.1 — Segurança + Propinas ✅ IMPLEMENTADO

| Funcionalidade | Estado |
|----------------|--------|
| Hash com salt (SenhaUtils) | ✅ |
| Recuperar senha por email | ✅ (RecuperarSenhaController) |
| Propina criada no registo | ✅ (garantirPropinaPrimeiroAno) |
| Pagamento parcial/total | ✅ |
| Bloqueio progressão por dívida | ✅ |
| Reposição propina ao reprovar | ✅ |
| Lista alunos em dívida | ✅ |
| Simulação passagem de ano | ✅ |

### Enunciado 1.2 — Dual Persistence CSV+SQL ✅ IMPLEMENTADO

| Funcionalidade | Estado |
|----------------|--------|
| DAOFactory com CSV/SQL | ✅ |
| HikariCP connection pool | ✅ |
| SQLDAOs para todas as entidades base | ✅ |
| JOINs otimizados (sem N+1) | ✅ (corrigido nesta sessão) |
| Modo configurável via config.properties | ✅ |

### Enunciado 1.3 — Horários + Presenças + Justificações + Estatutos ⚠️ PARCIAL

| Funcionalidade | Estado |
|----------------|--------|
| Models (Horario, Presenca, JustificacaoFalta, EstatutoEstudante) | ✅ |
| CSV CRUDs para v1.3 | ✅ |
| HorarioController, PresencaController | ✅ (implementado) |
| JustificacaoFaltaController, EstatutoController | ✅ (implementado) |
| Views (GestorView menus v1.3) | ✅ |
| Views (DocenteView marcar presenças) | ✅ |
| Views (EstudanteView ver horário) | ✅ |
| SQL DAOs para v1.3 | ❌ (CSV apenas) |
| Regras de horário (18h-23h30, pausas, blocos) | ❌ NÃO VALIDADAS na UI |
| Testes para v1.3 | ❌ todos @Disabled |

**Nota:** As funcionalidades v1.3 funcionam com CSV. Não há SQL DAO para Horario, Presenca, JustificacaoFalta, TipoEstatuto, EstatutoEstudante — o `DAOFactory` retorna sempre a implementação CSV para estas entidades, independentemente do modo.

---

*Documento gerado após auditoria completa em junho 2026.*
*Para questões técnicas, consultar os ficheiros referenciados ou abrir uma issue no repositório.*
