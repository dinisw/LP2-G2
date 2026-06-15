-- ============================================================
--  MIGRAÇÃO COMPLETA: Gestão de Ano Letivo
--  LP2-G2  |  SQL Server
--  Executar UMA VEZ na base de dados de produção
-- ============================================================

-- ── 1. Tabela principal: registo de anos letivos ───────────────
CREATE TABLE AnoLetivo (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    anoCalendario INT         NOT NULL,        -- ex: 2024 → ano 2024/2025
    dataInicio    DATE        NOT NULL,
    dataFim       DATE        NULL,            -- NULL enquanto ATIVO
    estado        VARCHAR(20) NOT NULL DEFAULT 'ATIVO'
        CONSTRAINT CK_AnoLetivo_Estado CHECK (estado IN ('ATIVO','CONCLUIDO')),
    CONSTRAINT UQ_AnoLetivo_Ano UNIQUE (anoCalendario)
);
GO

-- ── 2. Snapshot dos cursos por ano letivo ──────────────────────
--  Guarda o estado de cada curso no momento em que o ano terminou.
CREATE TABLE AnoLetivoCurso (
    id              INT IDENTITY(1,1) PRIMARY KEY,
    anoLetivoId     INT          NOT NULL
        CONSTRAINT FK_ALC_AnoLetivo REFERENCES AnoLetivo(id) ON DELETE CASCADE,
    cursoId         INT          NULL              -- pode ficar NULL se o curso for apagado
        CONSTRAINT FK_ALC_Curso    REFERENCES Curso(id) ON DELETE SET NULL,
    nomeSnapshot    VARCHAR(100) NOT NULL,          -- cópia do nome na altura
    estadoCurso     VARCHAR(30)  NOT NULL           -- 'INICIADO' | 'NAO_INICIADO'
);
GO

-- ── 3. Snapshot das UCs por curso por ano ──────────────────────
CREATE TABLE AnoLetivoUC (
    id                INT IDENTITY(1,1) PRIMARY KEY,
    anoLetivoCursoId  INT          NOT NULL
        CONSTRAINT FK_ALUC_Curso REFERENCES AnoLetivoCurso(id) ON DELETE CASCADE,
    nomeUC            VARCHAR(100) NOT NULL,
    anoCurricular     INT          NOT NULL,
    docenteNome       VARCHAR(100) NULL,
    docenteSigla      VARCHAR(20)  NULL,
    momentos          VARCHAR(300) NULL             -- ex: 'Frequência,Exame'
);
GO

-- ── 4. Snapshot dos estudantes por curso por ano ───────────────
CREATE TABLE AnoLetivoEstudante (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    anoLetivoCursoId     INT           NOT NULL
        CONSTRAINT FK_ALE_Curso REFERENCES AnoLetivoCurso(id) ON DELETE CASCADE,
    numeroMec            INT           NOT NULL,    -- sem FK: mantém histórico mesmo se aluno for apagado
    nomeSnapshot         VARCHAR(100)  NOT NULL,
    anoCurricularInicio  INT           NOT NULL,    -- ano curricular no início do período
    anoCurricularFim     INT           NOT NULL,    -- ano curricular no fim (após transição)
    propinaTotal         DECIMAL(10,2) NOT NULL DEFAULT 0,
    propinaPaga          DECIMAL(10,2) NOT NULL DEFAULT 0,
    resultado            VARCHAR(30)   NOT NULL     -- 'TRANSICAO' | 'RETIDO' | 'CONCLUIDO_CURSO'
);
GO

-- ── 5. Snapshot das notas por estudante por ano ────────────────
CREATE TABLE AnoLetivoNota (
    id                     INT IDENTITY(1,1) PRIMARY KEY,
    anoLetivoEstudanteId   INT           NOT NULL
        CONSTRAINT FK_ALN_Estudante REFERENCES AnoLetivoEstudante(id) ON DELETE CASCADE,
    nomeUC                 VARCHAR(100)  NOT NULL,
    momento                VARCHAR(50)   NOT NULL,
    nota                   DECIMAL(4,2)  NULL
);
GO

-- ── 6. Registo inicial automático ─────────────────────────────
DECLARE @ano  INT = YEAR(GETDATE());
DECLARE @mes  INT = MONTH(GETDATE());
DECLARE @ini  INT = CASE WHEN @mes >= 9 THEN @ano ELSE @ano - 1 END;

IF NOT EXISTS (SELECT 1 FROM AnoLetivo WHERE anoCalendario = @ini)
    INSERT INTO AnoLetivo (anoCalendario, dataInicio, dataFim, estado)
    VALUES (@ini, GETDATE(), NULL, 'ATIVO');
GO

-- ============================================================
--  DIAGRAMA DE RELAÇÕES
-- ============================================================
--
--  Departamento ──< Curso >─── CursoAnoIniciado
--                    │  └───── CursoUnidadeCurricular >── UnidadeCurricular ──< UnidadeCurricularMomento
--                    │                                           └── Docente
--                    └─── Estudante ──< Avaliacao
--                              └────── Propina
--
--  AnoLetivo ──< AnoLetivoCurso ──< AnoLetivoUC
--                     └──────────< AnoLetivoEstudante ──< AnoLetivoNota
--
--  Relação lógica (não FK):
--    AnoLetivo.anoCalendario  →  ano civil de início (ex: 2024 = 2024/2025)
--    Estudante.anoLetivo      →  ano curricular do aluno (1, 2 ou 3)
--    Propina.anoLetivo        →  ano curricular da propina (1, 2 ou 3)
--
--  Fluxo "Avançar Ano Letivo":
--    1. Validar: todas as notas lançadas + propinas pagas
--    2. Gravar snapshot em AnoLetivoCurso / AnoLetivoEstudante / AnoLetivoNota
--    3. Marcar AnoLetivo.estado = 'CONCLUIDO', definir dataFim
--    4. Inserir novo AnoLetivo ATIVO com anoCalendario+1
--    5. EstudanteController.simularTransicaoAnoLetivoGlobal() atualiza anoLetivo dos alunos
-- ============================================================
