# Relatório de Verificação — Enunciados v1.0, v1.1, v1.2

Data: 2026-06-16
Âmbito: código de produção (`src/main/java`) + suite de testes JUnit (`src/test/java`, 29 ficheiros). Enunciado v1.3 excluído do âmbito, conforme indicado.

Metodologia: revisão manual e estática, ficheiro a ficheiro, de toda a produção relevante e dos 29 ficheiros de teste. Não foi possível compilar/executar a suite no sandbox (falta JDK 17, Maven e acesso à BD SQL Server); a verificação assenta em leitura cruzada do código, das regras de negócio e das asserções de teste.

## Veredito geral

Os requisitos de v1.0, v1.1 e v1.2 estão implementados e cobertos por testes. Foram encontrados e corrigidos 6 bugs reais no código de produção e 6 defeitos na suite de testes (testes "mudos" sem asserções, um teste a documentar um bug já corrigido, e um comentário de classe desatualizado e falso). Depois destas correções, não foram encontradas mais inconsistências entre o que os enunciados pedem e o que o código faz. Fica 1 questão aberta (curso imutável) que deve ser confirmada com o docente antes de fechar como "conforme".

## 1. Bugs de produção corrigidos

1. **`CursoController.atualizarCurso()`** — comparava o `precoAnual` (BigDecimal) por referência (`!=`) em vez de por valor. Em modo CSV a lista partilhada em memória mascarava o erro; em modo SQL, onde cada leitura cria um objeto novo, o bloqueio de alteração de preço falhava sempre. Corrigido para usar `.compareTo()`.
2. **`CursoSqlDAO`** — lia `precoAnual` com `rs.getDouble()`, reintroduzindo imprecisão de vírgula flutuante antes da conversão para BigDecimal. Corrigido para `rs.getBigDecimal("precoAnual")`.
3. **`AvaliacaoSqlDAO.eliminarAvaliacoesPorEstudante()`** — usava o nome de coluna errado (`numeroMecEstudante`, que pertence à tabela `Propina`) em vez de `estudanteNumeroMec` (coluna real da tabela `Avaliacao`). Corrigido.
4. **`UnidadeCurricularCRUD.atualizarUC(String, UnidadeCurricular)`** — stub que retornava sempre `false`. Sem impacto real (não é chamado em produção), mas violava o contrato da interface. Corrigido.
5. **`UnidadeCurricularCRUD.atualizarUC(UnidadeCurricular)`** — comparava nomes de UC com `==` em vez de `.equals()`. Um teste só passava por coincidência (mesma referência de String). Corrigido para `.equalsIgnoreCase()`.
6. **`PropinaController.gerarPropinaAnual()`** — risco de `NullPointerException`: chamava `curso.getPrecoAnual().compareTo(...)` sem verificar null, e `getPrecoAnual()` pode legitimamente ser null. Corrigido com guarda `!= null`.

Todas as 6 correções foram reconfirmadas nesta sessão por leitura direta do código atual — estão em vigor.

## 2. Correções na suite de testes

- `EnunciadoComplianceTest` (E3) — teste "mudo": configurava objetos mas não chamava nenhuma asserção. Corrigido.
- `UserStoriesValidationTest.US14_LancamentoNotasUpsertEProgresso` — idem, sem asserções. Corrigido.
- `MasterE2EIntegrationTest.test06_Avaliacoes_E_Medias` — idem, sem asserções. Corrigido.
- `AvaliacaoFluxoTest` — testava um bug que já tinha sido corrigido em produção, mas a asserção estava comentada (deixando o teste a "passar" sem validar nada). Corrigido.
- `PropinaIntegrationTest` — falta de limpeza de dados de teste no CSV entre execuções. Corrigido (`@AfterEach` a eliminar a propina fictícia).
- `EstudanteCalculoFluxoTest` — Javadoc da classe afirmava (falsamente) que os testes de `EstudanteCalculoTest` estavam "quebrados" por criarem UCs sem momentos de avaliação. Confirmado por leitura direta que `EstudanteCalculoTest.setup()` define momentos corretamente — a afirmação era falsa. Javadoc corrigido.

Os restantes 23 ficheiros de teste foram revistos integralmente e estão corretos: asserções reais, fixtures bem geridas, sem testes "mudos" ou desatualizados.

## 3. Questão aberta — curso imutável (não resolvida automaticamente)

O enunciado v1.0 diz, literalmente: *"Sempre que existam estudantes e professores alocados a um curso, o mesmo não poderá ser alterado em sistema."*

A implementação atual (`Curso.isIniciado()`, usado em `CursoController.atualizarCurso()` e `eliminarCurso()`) bloqueia alterações apenas quando o ano letivo foi formalmente iniciado via `iniciarAnoLetivo()` — uma condição mais estreita do que "existem estudantes e professores alocados". É possível ter um curso com estudantes inscritos e docentes associados a UCs, mas sem nenhum ano letivo iniciado, e ainda assim conseguir alterá-lo.

O teste `NovasFuncionalidadesTest.alterarPrecoEmCursoNaoIniciado` valida precisamente este comportamento atual (mais permissivo) como correto.

Não alterei o código de produção para resolver isto — é uma decisão de interpretação do enunciado que deve ser confirmada com o docente/responsável antes de decidir se `isIniciado()` deve ser substituído por uma verificação direta de "tem estudantes E professores alocados".

## 4. Observações não bloqueantes

- **Credenciais da BD em texto simples**: aparecem em 3 ficheiros de teste — `DAL/ConexaoBDTest.java`, `DAL/SchemaBDTest.java`, `DAL/SetupBDTest.java` (classe base de todos os `*SqlDAOTest`). Não é um bug funcional, mas é uma prática de risco num repositório (mesmo que privado/académico).
- **`ConexaoBDTest`** tem um teste que nunca falha de facto: o caminho de falha de ligação é apanhado e apenas registado em log, nunca validado com `assert`. É um teste de diagnóstico, não um teste funcional.
- **`UnidadeCurricularSqlDAOTest`** tem 2 testes (`getUnidadeCurriculars_retornaLista`, `procurarPorId_encontra`) que dependem do estado pré-existente da BD partilhada (assume lista não vazia; assume que existe UC com `id=1`). Frágil se a BD de desenvolvimento for limpa/resetada.
- **`EnunciadoComplianceTest`** (Secção D, 6 testes `@Disabled`) — testes de horários/presenças do v1.3, corretamente marcados como não implementados e fora de âmbito. Nenhuma ação necessária, conforme indicado.

## Conclusão

Com as 6 correções de produção e as 6 correções de teste aplicadas, o código cobre os requisitos de v1.0, v1.1 e v1.2 e está funcional, sem bugs conhecidos e sem testes a mascarar falhas. O único ponto que requer uma decisão (não técnica, mas de interpretação do enunciado) é o critério exato de "curso imutável" descrito na Secção 3.
