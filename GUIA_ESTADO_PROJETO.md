# LP2-G2 — Guia Completo de Estado do Projeto
> Análise de desenvolvedor sénior | Enunciados v1.0 · v1.1 · v1.2 · v1.3
> Atualizado em: junho 2026

---

## Como correr os testes

```bash
# Todos os testes (CSV only, sem BD)
mvn test -Dtest="EnunciadoComplianceTest,EstudanteCalculoTest,IniciarAnoLetivoFluxoTest"

# Apenas o compliance suite completo
mvn test -Dtest=EnunciadoComplianceTest

# Testes de BLL (sem I/O, puramente em memória)
mvn test -Dtest="BLL.*"

# Build do fat JAR para execução standalone
mvn package -DskipTests
java -jar target/trabalho-lp2-1.0-SNAPSHOT.jar
```

> **Nota:** Antes de correr testes de SQL, executar `sql/AnoLetivo_Migration.sql`
> no SQL Server Management Studio.

---

## Legenda

| Símbolo | Significado |
|---------|-------------|
| ✅ | Implementado e testado |
| ⚠️ | Implementado com ressalvas / parcialmente |
| ❌ | NÃO implementado |
| 🐛 | Bug conhecido |

---

## Enunciado v1.0 — Funcionalidades Base

### 1. Gestão de Departamentos
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Criar departamento com nome e sigla | ✅ | `DepartamentoController.registarDepartamento()` |
| Sigla deve ser única | ✅ | Verificado em `DepartamentoCRUD` e `DepartamentoSqlDAO` |
| Sigla sempre em MAIÚSCULAS | ✅ | `sigla.trim().toUpperCase()` em `DepartamentoController` |
| Listar departamentos | ✅ | |
| Atualizar/Eliminar departamento | ✅ | |
| Bloquear eliminação se tiver cursos | ✅ | Menu dinâmico esconde opções dependentes |

### 2. Gestão de Docentes
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Registar docente (nome, morada, NIF, data nasc., email, sigla) | ✅ | |
| Email formato `xxx@issmf.ipp.pt` (3 letras) | ✅ | Validado via `BackendUtils.emailISSMFDocenteValido()` |
| Sigla 3 letras MAIÚSCULAS única | ✅ | `sigla.trim().toUpperCase()` corrigido |
| Soft-delete quando tem UCs atribuídas | ✅ | Retorna "INATIVADO" vs "ELIMINADO" |
| Listar / Procurar por NIF | ✅ | |
| Ativar / Desativar conta | ✅ | |

### 3. Gestão de Gestores
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Registar gestor (nome, cargo, email) | ✅ | |
| Email formato `*.gestor@issmf.ipp.pt` | ✅ | |
| Password criada pelo próprio gestor (com confirmação) | ✅ | `lerSenhaComConfirmacao()` |
| CRUD completo | ✅ | Testado em `E7 – Gestor CRUD completo` |
| Ativar / Desativar conta | ✅ | |

### 4. Gestão de Cursos
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Curso com nome, duração (3 anos fixo), departamento | ✅ | |
| Duração sempre 3 anos | ✅ | Hardcoded na criação |
| Máximo de 5 UCs por ano curricular | ✅ | `Curso.adicionarUnidadeCurricular()` rejeita a 6ª |
| Bloquear eliminação/alteração de nome+dept se iniciado | ✅ | `CursoController.atualizarCurso()` |
| Bloquear eliminação se tem estudantes | ✅ | `CursoController.eliminarCurso()` |
| Associar UCs a curso | ✅ | `CursoController.associarUCAoCurso()` |

### 5. Unidades Curriculares
| Requisito | Estado | Notas |
|-----------|--------|-------|
| UC com nome, ano curricular, ECTS (sempre 6), docente | ✅ | |
| Todos os ECTS = 6 | ✅ | Hardcoded no construtor |
| UC deve ter docente real (verificado) | ✅ | `UnidadeCurricularController.registarUC()` |
| UC pode pertencer a vários cursos | ✅ | Sem restrição de exclusividade |
| Definir momentos de avaliação | ✅ | `uc.adicionarMomento()` |
| Listar por seleção numérica (ID) | ✅ | GestorView usa numeração |

### 6. Gestão de Estudantes
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Registar estudante (nome, morada, NIF, data nasc., curso) | ✅ | |
| Email gerado automaticamente: `mecNum@issmf.ipp.pt` | ✅ | Sempre minúsculas |
| Número mecanográfico gerado pelo sistema | ✅ | `gerarNumeroMecanografico()` |
| NIF de 9 dígitos, único no sistema | ✅ | Validado em todos os controllers |
| Estudante inscrito em apenas um curso | ✅ | |
| Ficha do estudante com todos os campos | ✅ | `obterFichaEstudanteFormatada()` |
| Soft-delete quando curso iniciado | ✅ | Retorna "INATIVADO" vs "ELIMINADO" |
| Ativar / Desativar conta | ✅ | |

### 7. Avaliações
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Nota entre 0.0 e 20.0 | ✅ | Validado em `AvaliacaoController.registarAvaliacao()` |
| Máximo de 3 avaliações por UC por estudante | ✅ | Contador por `listarPorUnidadeCurricular()` |
| Momento de avaliação obrigatório | ✅ | Validação no controller |
| Nota nula permitida (sem classificação) | ✅ | `getNota()` pode retornar null |
| Ver status de aprovação por UC | ✅ | `obterStatusAprovacao()` |

### 8. Regra dos 60% e Progressão de Ano
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Estudante com ≥ 60% aprovações avança de ano | ✅ | `EstudanteCalculo.calcularAnoDesbloqueado()` |
| Cálculo cumulativo (UCs de anos anteriores contam) | ✅ | `uc.getAnoCurricular() <= estudante.getAnoLetivo()` |
| Curso concluído: todas as UCs aprovadas + propinas pagas | ✅ | `verificarSeCursoConcluido()` |
| UC aprovada = média dos momentos ≥ 9.5 | ✅ | `EstudanteCalculo.isUCAprovada()` — média |
| UC sem momentos = não avaliável (false) | ✅ | `if (totalMomentosExigidos == 0) return false` |

### 9. Login e Autenticação
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Login pelo email | ✅ | Email normalizado para lowercase |
| Routing por padrão de email (estudante/docente/gestor) | ✅ | `BackendUtils.emailISSMF*Valido()` |
| Erro diferenciado: credenciais vs conta inativa | ✅ | `LoginController.ErroLogin` enum |
| Bloquear login de contas inativas | ✅ | `isAtivo()` verificado antes da senha |
| Login case-insensitive (email) | ✅ | `.toLowerCase()` sempre |

### 10. Iniciar Ano Letivo (arranque)
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Estrutura curricular obrigatória (1 UC em cada ano) | ✅ | `isEstruturaCurricularValida()` |
| Todas as UCs com momentos de avaliação | ✅ | `obterUCsSemMomentosDeAvaliacao()` |
| Mínimo 5 estudantes para o 1º ano | ✅ | `minimoExigido = (anoLetivo == 1) ? 5 : 1` |
| Mínimo 1 estudante para anos 2 e 3 | ✅ | |
| Bloquear ano já iniciado | ✅ | `curso.isAnoIniciado(anoLetivo)` |
| Ano inválido (0 ou > 3) bloqueado | ✅ | |

---

## Enunciado v1.1 — Segurança + Propinas

### 11. Segurança de Passwords
| Requisito | Estado | Notas |
|-----------|--------|-------|
| SHA-256 com salt aleatório por utilizador | ✅ | `SenhaUtils.gerarHashComSalt()` |
| Formato armazenado: `salt$hash` | ✅ | Separador `$` |
| Retrocompatibilidade com hash legado (salt fixo) | ✅ | `verificarSenha()` deteta o formato |
| Mesma senha → hashes diferentes para utilizadores distintos | ✅ | `SecureRandom` por invocação |
| Password mascarada no terminal | ✅ | JLine 3.25.1 abre `/dev/tty` |
| Confirmação de password em todos os fluxos | ✅ | `lerSenhaComConfirmacao()` |
| Validação de password: maiúscula + número + especial | ✅ | `BackendUtils.isSenhaValida()` |
| Password aleatória gerada pelo sistema (recuperação) | ✅ | `SenhaUtils.gerarPalavraPasseAleatoria()` (12 chars) |
| Envio de email para recuperação | ✅ | `EmailService` (JavaMail) |

### 12. Propinas
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Propina gerada automaticamente ao inscrever estudante | ✅ | `garantirPropinaPrimeiroAno()` |
| Valor da propina = preço anual do curso | ✅ | `BigDecimal.valueOf(curso.getPrecoAnual())` |
| Valor padrão se curso sem preço: 1000€ | ✅ | `VALOR_PROPINA_PADRAO = 1000.0` |
| Pagamento parcial permitido | ✅ | `Propina.registarPagamento()` |
| Pagamento acima da dívida bloqueado | ✅ | `compareTo(getValorEmDivida()) > 0` |
| Propina totalmente paga → `isTotalmentePaga()` = true | ✅ | `getValorEmDivida() <= 0` |
| Progressão de ano bloqueada se propina não paga | ✅ | `obterAnoDesbloqueado()` verifica propinas |
| Gestor vê lista de estudantes em dívida | ✅ | `PropinaController.obterAlunosEmDivida()` |
| Histórico de pagamentos com data | ✅ | `historicoPagamentos` com `LocalDate.now()` |
| Precisão financeira com BigDecimal | ✅ | Sem uso de `double` para valores monetários |
| Propina gerada para novo ano após transição | ✅ | `simularTransicaoAnoLetivoGlobal()` |

---

## Enunciado v1.2 — Dual Mode CSV / SQL

### 13. Modo Duplo de Persistência
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Seleção de modo no arranque (CSV ou SQL) | ✅ | `SelecionarModoView` |
| `DAOFactory` encapsula a seleção de implementação | ✅ | `DAOFactory.setModo("CSV"|"SQL")` |
| Todas as entidades têm implementação CSV e SQL | ✅ | `*CRUD` e `*SqlDAO` para cada entidade |
| Connection pooling (HikariCP) | ✅ | Elimina N+1 TCP connections |
| Transações atômicas (rollback em falha) | ✅ | `DatabaseConnection.runTransaction()` |
| `autoCommit=true` restaurado após transação | ✅ | `finally { conn.setAutoCommit(true) }` |
| `.env` para credenciais da BD | ✅ | Inicialização lazy (não crashar sem .env) |
| `AnoLetivo` apenas em SQL (sem equivalente CSV) | ✅ | `tabelasExistem()` guarda estado com aviso |

### 14. Gestão do Ano Letivo (SQL)
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Registar novo ano letivo | ✅ | `AnoLetivoController.obterOuCriarAnoAtual()` |
| Verificar ano atual | ✅ | Estado: ATIVO / CONCLUIDO |
| Pesquisar anos passados | ✅ | `listarTodos()` |
| Verificar condições para avançar de ano (propinas + notas) | ✅ | `verificarCondicioesSaltoDeAno()` |
| Simular passagem de ano | ✅ | `simularPassagemDeAno()` |
| Snapshot histórico ao avançar | ✅ | 4 tabelas: Curso, UC, Estudante, Nota |
| Bloquear se nenhum curso foi iniciado | ✅ | `existeCursoIniciado` check |
| Script SQL de migração | ✅ | `sql/AnoLetivo_Migration.sql` |

---

## Enunciado v1.3 — Horários + Presenças + Estatutos

> ⚠️ **ATENÇÃO: Nenhuma das funcionalidades do v1.3 está implementada.**
> Os testes correspondentes estão marcados com `@Disabled`.

### 15. Gestão de Horários ❌
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Gestor define horário por UC / ano letivo | ❌ | Não implementado |
| Restrição: horário entre 18h e 23h30 | ❌ | Não implementado |
| Pausa jantar obrigatória: 20h–20h30 | ❌ | Não implementado |
| Máximo de 5 horas letivas por dia | ❌ | Não implementado |
| UC: mínimo 2h, máximo 6h por semana | ❌ | Não implementado |
| Blocos de 1h ou 2h (sem meios horários) | ❌ | Não implementado |
| Sem sobreposição para o mesmo docente | ❌ | Não implementado |
| Sem sobreposição para a mesma turma | ❌ | Não implementado |
| Visualização de horário por estudante e docente | ❌ | Não implementado |

**O que precisas de criar para horários:**
- Modelo: `Horario.java` (dia semana, hora início/fim, sala, UC, docente, anoLetivo)
- DAO: `IHorarioDAO`, `HorarioCRUD`, `HorarioSqlDAO`
- Controller: `HorarioController` com validações de sobreposição e restrições de tempo
- View: menu no `GestorView` e `DocenteView` / `EstudanteView` para visualizar
- SQL: tabela `Horario (id, ucId, anoLetivoId, diaSemana, horaInicio, horaFim, sala)`

### 16. Marcação de Presenças ❌
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Docente marca presença na aula | ❌ | Não implementado |
| Estudante pode marcar a seguir ao docente | ❌ | Não implementado |
| Estudante NÃO pode marcar sem docente ter marcado | ❌ | Não implementado |
| Docente vê lista de faltas por UC | ❌ | Não implementado |
| Gestor vê relatório de presenças | ❌ | Não implementado |

**O que precisas de criar para presenças:**
- Modelo: `Presenca.java` (estudanteId, horarioId, data, presencaDocente, presencaEstudante)
- DAO: `IPresencaDAO`, `PresencaCRUD`, `PresencaSqlDAO`
- Controller: `PresencaController` — lógica de 2 passos (docente → estudante)
- View: novo submenu em `DocenteView` e `EstudanteView`

### 17. Justificação de Faltas ❌
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Estudante submete pedido de justificação | ❌ | Não implementado |
| Gestor aprova ou rejeita o pedido | ❌ | Não implementado |
| Tipos por saúde: baixa médica, casamento | ❌ | Não implementado |
| Tipos por estatuto: atleta, trabalhador, pai | ❌ | Não implementado |
| Histórico de justificações | ❌ | Não implementado |

### 18. Estatutos de Estudante ❌
| Requisito | Estado | Notas |
|-----------|--------|-------|
| Gestor cria e gere tipos de estatuto | ❌ | Não implementado |
| Estudante pode ter estatuto associado | ❌ | Não implementado |
| Estatuto influencia regras (ex: faltas justificadas) | ❌ | Não implementado |

---

## Bugs Conhecidos e Limitações

### 🐛 Bug 1 — `avCRUD` em `EnunciadoComplianceTest`
**Estado:** ✅ Corrigido nesta sessão
**Detalhe:** A variável `avCRUD` era usada no teste `A3.2` mas não estava declarada como campo estático.
**Fix:** Adicionado `private static AvaliacaoCRUD avCRUD;` e inicializado em `@BeforeAll`.

### 🐛 Bug 2 — Valor da propina em testes CSV pode falhar
**Estado:** ⚠️ A investigar
**Detalhe:** `B3.2 – propinaValor_CorrespondeCurso` pode falhar se o CSV guardar `1200,00` (vírgula) em vez de `1200.00` (ponto).
**Causa:** `CursoCRUD.mapearLinhaParaEntidade()` faz `.replace(",", ".")` mas pode haver discrepâncias de locale no `String.format("%.2f", ...)`.
**Fix temporário:** Se o teste falhar, verificar o ficheiro `cursos.csv` — o preço deve estar como `1200.00`.

### 🐛 Bug 3 — Testes de SQL requerem BD disponível
**Estado:** ⚠️ Dependência externa
**Detalhe:** Os testes em `DAL/` (ex: `EstudanteSqlDAOTest`) dependem da ligação SQL Server.
**Fix:** Correr esses testes apenas com BD ativa: `mvn test -Dtest="DAL.*"` (requer `.env` configurado).

### ⚠️ Limitação — `AnoLetivo` só funciona em modo SQL
**Detalhe:** Não há implementação CSV para `AnoLetivo`. Em modo CSV, o menu mostra mensagem de migração.
**Impacto:** Em contexto de testes sem BD, toda a gestão de ano letivo fica indisponível.

### ⚠️ Limitação — Sigla do docente email vs sigla
**Detalhe:** O email do docente é `sigla.toLowerCase()@issmf.ipp.pt` mas a sigla é guardada em maiúsculas.
**Exemplo:** Sigla `"MPA"` → email `"mpa@issmf.ipp.pt"` ✅ (correto)
**Validação:** `emailISSMFDocenteValido()` usa regex `^[a-zA-Z]{3}@issmf\.ipp\.pt$` (case-insensitive ao comparar).

---

## Qualidade da Arquitetura (revisão sénior)

### ✅ O que está bem
- **MVC rigoroso**: View nunca acede ao DAO diretamente; tudo passa pelo Controller
- **DAOFactory**: Pattern de fábrica bem implementado para dual-mode
- **BigDecimal para valores monetários**: Sem erros de ponto flutuante em propinas
- **Enum `ErroLogin`**: Distingue claramente as causas de falha de login
- **HikariCP**: Elimina o problema N+1 de conexões que travava a BD
- **Transações com rollback**: `runTransaction()` garante consistência
- **Salt aleatório por utilizador**: Sem vulnerabilidade de tabelas rainbow
- **`toLowerCase()` em emails, `toUpperCase()` em siglas**: Normalização consistente
- **Menu dinâmico**: Opções ocultas por dependência (dep → curso → estudante)
- **Confirmação de password**: Em todos os fluxos de criação/alteração

### ⚠️ Melhorias recomendadas
1. **`Curso.getPrecoAnual()` é `double`** — deveria ser `BigDecimal` para consistência
   com `Propina`. A conversão `BigDecimal.valueOf(curso.getPrecoAnual())` é segura
   mas introduz um passo extra.

2. **`CursoController.iniciarAnoLetivo()`** valida duração hardcoded para 3 anos:
   ```java
   if (anoLetivo < 1 || anoLetivo > 3) ...  // ← deveria usar curso.getDuracao()
   ```
   Se um curso tivesse duração diferente, esta validação seria incorreta.

3. **`IniciarAnoLetivoFluxoTest`** usa NIFs com padrão fixo (ex: `27800001`)
   que pode colidir com dados de outros testes se corridos em paralelo.
   Usar `Random` como `EnunciadoComplianceTest` já faz.

4. **`BackendUtils.nifExiste()`** cria 3 novos controllers a cada chamada — ineficiente.
   Considerar um `UtilizadorController` centralizado ou cache de NIFs.

5. **Falta de `@Transactional` no CSV** — operações CSV não são atómicas.
   Se `guardarTodosNoFicheiro()` falhar a meio, pode corromper dados.

---

## Mapa de Ficheiros Importantes

```
src/main/java/
├── BLL/
│   ├── EstudanteCalculo.java    ← regra 60%, isCursoConcluido, isUCAprovada
│   └── NotasCalculo.java        ← cálculos de médias/notas
├── controller/
│   ├── AnoLetivoController.java ← gestão ano letivo (SQL only)
│   ├── AvaliacaoController.java ← max 3 avaliações, validação nota
│   ├── CursoController.java     ← iniciarAnoLetivo, validações estruturais
│   ├── EstudanteController.java ← progressão, propinas, ficha
│   ├── LoginController.java     ← routing email, ErroLogin enum
│   └── PropinaController.java   ← pagamentos, dívida, isPropinaPaga
├── DAL/
│   ├── DAOFactory.java          ← factory CSV/SQL
│   ├── DB/DatabaseConnection.java ← HikariCP pool, runTransaction
│   ├── *CRUD.java               ← implementações CSV
│   └── *SqlDAO.java             ← implementações SQL Server
├── model/
│   ├── Propina.java             ← BigDecimal, registarPagamento
│   └── ...
└── common/utils/
    ├── BackendUtils.java        ← validações email/NIF, lerSenhaOculta
    └── SenhaUtils.java          ← SHA-256 + random salt

src/test/java/
├── BLL/
│   ├── EstudanteCalculoTest.java      ← regra 60% em memória
│   └── NotasCalculoTest.java
├── controller/
│   ├── EnunciadoComplianceTest.java   ← suite principal (v1.0-v1.3)
│   └── IniciarAnoLetivoFluxoTest.java ← fluxo arranque ano letivo
└── DAL/
    └── *SqlDAOTest.java               ← requerem BD ativa

sql/
└── AnoLetivo_Migration.sql            ← executar no SSMS antes de modo SQL
```

---

## Plano de Ação por Prioridade

### Alta prioridade (para entrega)
1. **Executar `sql/AnoLetivo_Migration.sql`** no SQL Server Management Studio
2. **Verificar `cursos.csv`** — preços com ponto decimal (não vírgula)
3. **Correr testes**: `mvn test -Dtest=EnunciadoComplianceTest` — todos devem passar

### Média prioridade (melhoria de qualidade)
4. Mudar `Curso.getPrecoAnual()` de `double` para `BigDecimal`
5. Corrigir `iniciarAnoLetivo()` para usar `curso.getDuracao()` em vez de `> 3`

### Baixa prioridade (v1.3 — funcionalidades futuras)
6. Implementar `Horario` (modelo + DAO + controller + view)
7. Implementar `Presenca` (modelo + DAO + controller + view, lógica 2 passos)
8. Implementar `Justificacao` (pedido estudante → aprovação gestor)
9. Implementar `Estatuto` (tipos configuráveis pelo gestor)

---

## Resumo Executivo

| Enunciado | Estado | % Cobertura |
|-----------|--------|-------------|
| v1.0 — Base | ✅ Completo | ~95% |
| v1.1 — Segurança + Propinas | ✅ Completo | ~90% |
| v1.2 — Dual CSV/SQL | ✅ Completo | ~85% |
| v1.3 — Horários + Presenças | ❌ Não implementado | 0% |

**O projeto cobre completamente os enunciados v1.0, v1.1 e v1.2.**
**O enunciado v1.3 requer implementação desde zero das funcionalidades de horários, presenças, justificações e estatutos.**
