package com.example.cantina;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Cadastro extends AppCompatActivity {

    private EditText et_nome, et_senha, et_repetir_senha, et_turma, et_aluno;
    private Button btn_cadastrar;
    private Spinner sp_usuario;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        iniciarComponentes();
        configurarSpinner();

        btn_cadastrar.setOnClickListener(v -> validarCampos());
    }

    private void iniciarComponentes() {
        et_nome = findViewById(R.id.et_nome);
        et_senha = findViewById(R.id.et_senha);
        et_repetir_senha = findViewById(R.id.et_repetir_senha);
        et_turma = findViewById(R.id.et_turma);
        et_aluno = findViewById(R.id.et_aluno);
        btn_cadastrar = findViewById(R.id.btn_cadastrar);
        sp_usuario = findViewById(R.id.sp_usuario);
    }

    private void configurarSpinner() {
        String[] opcoes = {"Aluno", "Responsável", "Cantina"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcoes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_usuario.setAdapter(adapter);

        sp_usuario.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipo = opcoes[position];
                if (tipo.equals("Aluno")) {
                    et_aluno.setVisibility(View.GONE);
                    et_turma.setVisibility(View.VISIBLE);
                } else if (tipo.equals("Responsável")) {
                    et_aluno.setVisibility(View.VISIBLE);
                    et_turma.setVisibility(View.VISIBLE);
                } else if (tipo.equals("Cantina")) {
                    et_aluno.setVisibility(View.GONE);
                    et_turma.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void validarCampos() {
        String nome = et_nome.getText().toString().trim();
        String senha = et_senha.getText().toString().trim();
        String repetirSenha = et_repetir_senha.getText().toString().trim();
        String turma = et_turma.getText().toString().trim();
        String nomeAluno = et_aluno.getText().toString().trim();
        String tipoUsuario = sp_usuario.getSelectedItem().toString();

        if (nome.isEmpty() || senha.isEmpty() || repetirSenha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tipoUsuario.equals("Aluno") && turma.isEmpty()) {
            Toast.makeText(this, "Informe a turma!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tipoUsuario.equals("Responsável") && (turma.isEmpty() || nomeAluno.isEmpty())) {
            Toast.makeText(this, "Preencha a turma e o nome do aluno!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(repetirSenha)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.length() < 6) {
            Toast.makeText(this, "A senha precisa ter no mínimo 6 caracteres!", Toast.LENGTH_SHORT).show();
            return;
        }

        String emailTecnico = nome.toLowerCase().replaceAll("\\s+", ".") + "@cantina.com";

        cadastrarUsuarioAuth(emailTecnico, senha, nome, turma, nomeAluno, tipoUsuario);
    }

    private void cadastrarUsuarioAuth(String email, String senha, String nome, String turma, String nomeAluno, String tipoUsuario) {
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            salvarNoFirestore(uid, email, nome, turma, nomeAluno, tipoUsuario);
                        }
                    } else {
                        String erro = task.getException() != null ? task.getException().getMessage() : "Erro ao cadastrar";
                        Toast.makeText(Cadastro.this, "Erro: " + erro, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void salvarNoFirestore(String uid, String email, String nome, String turma, String nomeAluno, String tipoUsuario) {
        Object objetoUsuario;

        if (tipoUsuario.equals("Aluno")) {
            objetoUsuario = new Aluno(uid, nome, email, turma);
        } else if (tipoUsuario.equals("Responsável")) {
            objetoUsuario = new Responsavel(uid, nome, email, turma, nomeAluno);
        } else {
            objetoUsuario = new Cantina(uid, nome, email);
        }

        db.collection("usuarios")
                .document(uid)
                .set(objetoUsuario)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Cadastro.this, "Cadastro de " + tipoUsuario + " realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Cadastro.this, "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}