package com.example.cantina;

    public class Aluno {
        private String idAluno;
        private String nome;
        private String senha;
        private String turma;

        public Aluno() {
        }

        public Aluno(String idAluno, String nome, String senha, String turma) {
            this.idAluno = idAluno;
            this.nome = nome;
            this.senha = senha;
            this.turma = turma;
        }

        public String getIdAluno() { return idAluno; }
        public void setIdAluno(String uid) { this.idAluno = idAluno; }

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }

        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }

        public String getTurma() { return turma; }
        public void setTurma(String turma) { this.turma = turma; }
    }

