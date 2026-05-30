-- ============================================================
-- Script de criação das tabelas para o projeto LP2-G2
-- Base de dados: SQL Server
-- ============================================================

CREATE TABLE Departamentos (
    sigla       VARCHAR(20)  PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL
);

CREATE TABLE Docentes (
    nif             INT          PRIMARY KEY,
    nome            VARCHAR(100) NOT NULL,
    morada          VARCHAR(200),
    data_nascimento DATE,
    email           VARCHAR(100),
    hash_senha      VARCHAR(255),
    sigla           VARCHAR(20)  NOT NULL UNIQUE
);

CREATE TABLE UnidadesCurriculares (
    id                  INT          PRIMARY KEY IDENTITY(1,1),
    nome                VARCHAR(100) NOT NULL,
    ano_curricular      INT          NOT NULL,
    semestre            INT          NOT NULL,
    sigla_docente       VARCHAR(20),
    momentos_avaliacao  VARCHAR(500)
);

CREATE TABLE Cursos (
    nome            VARCHAR(100) PRIMARY KEY,
    duracao         INT          NOT NULL,
    sigla_dep       VARCHAR(20)  REFERENCES Departamentos(sigla),
    preco_anual     DECIMAL(10,2),
    anos_iniciados  VARCHAR(200),
    ucs             VARCHAR(1000)
);

CREATE TABLE Gestores (
    id              INT          PRIMARY KEY IDENTITY(1,1),
    nome            VARCHAR(100) NOT NULL,
    morada          VARCHAR(200),
    nif             INT          NOT NULL UNIQUE,
    data_nascimento DATE,
    email           VARCHAR(100),
    hash_senha      VARCHAR(255),
    cargo           VARCHAR(100)
);

CREATE TABLE Estudantes (
    numero_mec      INT          PRIMARY KEY,
    nome            VARCHAR(100) NOT NULL,
    morada          VARCHAR(200),
    nif             INT          NOT NULL UNIQUE,
    data_nascimento DATE,
    email           VARCHAR(100),
    hash_senha      VARCHAR(255),
    curso           VARCHAR(100),
    ativo           BIT          NOT NULL DEFAULT 1
);

CREATE TABLE Propinas (
    numero_mec  INT            NOT NULL REFERENCES Estudantes(numero_mec),
    ano_letivo  INT            NOT NULL,
    valor_total DECIMAL(10,2)  NOT NULL,
    valor_pago  DECIMAL(10,2)  NOT NULL DEFAULT 0,
    PRIMARY KEY (numero_mec, ano_letivo)
);

CREATE TABLE Avaliacoes (
    momento     VARCHAR(100)   NOT NULL,
    nome_uc     VARCHAR(100)   NOT NULL,
    numero_mec  INT            NOT NULL REFERENCES Estudantes(numero_mec),
    nota        DECIMAL(5,2),
    PRIMARY KEY (momento, nome_uc, numero_mec)
);
