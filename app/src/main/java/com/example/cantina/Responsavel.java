package com.example.cantina;

public class Responsavel {
    private String idResponsavel;
    private String nome;
    private String senha;
    private String aluno;
    private String turma;

    public Responsavel() {
    }

    public Responsavel(String idResponsavel, String nome, String senha, String aluno, String turma) {
        this.idResponsavel = idResponsavel;
        this.nome = nome;
        this.senha = senha;
        this.aluno = aluno;
        this.turma = turma;
    }

    public String getIdAluno() { return idResponsavel; }
    public void setIdAluno(String uid) { this.idResponsavel = idResponsavel; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getAluno() { return aluno; }
    public void setAluno(String aluno) { this.aluno = aluno; }
    public String getTurma() { return turma; }
    public void setTurma(String turma) { this.turma = turma; }
}

