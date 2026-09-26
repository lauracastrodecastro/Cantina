package com.example.cantina;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PedidoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido);

        Spinner horario = findViewById(R.id.spinnerHorario);
        RadioGroup pagamento = findViewById(R.id.radioGroupPagamento);

        findViewById(R.id.btnConfirmarPedido).setOnClickListener(view -> {
            int pagamentoSelecionado = pagamento.getCheckedRadioButtonId();
            if (horario.getSelectedItemPosition() == 0 || pagamentoSelecionado == -1) {
                Toast.makeText(this, "Escolha o horário e a forma de pagamento.", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton opcao = findViewById(pagamentoSelecionado);
            String mensagem = "Pedido confirmado para " + horario.getSelectedItem()
                    + " — pagamento: " + opcao.getText();
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
        });
    }
}
