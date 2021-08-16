package com.example.estoquedemeadas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityMarcasEditarExcluir extends AppCompatActivity {

    //atributo da classe.
    public AlertDialog alerta;

    public boolean result;

    SQLiteDatabase db_marcas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcas_editar_excluir);

        inicializa();

        db_marcas = openOrCreateDatabase("marcas_meadas.db", Context.MODE_PRIVATE, null);

        // Recebe os valores enviados pelo outro formulário
        Intent valores = getIntent();
        // txtIdMarca.setText(valores.getStringExtra("txtIdMarca"));

        Button btnMarcasEdExSair = (Button)findViewById(R.id.btnMarcasEdExSair);
        Button btnMarcasEdExEditar = (Button)findViewById(R.id.btnMarcasEdExEditar);
        Button btnMarcasEdExCancelar = (Button)findViewById(R.id.btnMarcasEdExCancelar);
        Button btnMarcasEdExExcluir = (Button)findViewById(R.id.btnMarcasEdExExcluir);
        Button btnMarcasEdExAlterar = (Button)findViewById(R.id.btnMarcasEdExAlterar);
        EditText edtMarcasEdExNome = (EditText)findViewById(R.id.edtMarcasEdExNome);
        LinearLayout lstMarcasEdExBotoes = (LinearLayout)findViewById(R.id.lstMarcasEdExBotoes);
        LinearLayout lstMarcasEdExAlterar = (LinearLayout)findViewById(R.id.lstMarcasEdExAlterar);
        TextView txtMarcasEdExIdMarca = (TextView)findViewById(R.id.txtMarcasEdExIdMarca);

        edtMarcasEdExNome.setText(valores.getStringExtra("edtMarcasEdExNome"));
        txtMarcasEdExIdMarca.setText(valores.getStringExtra("txtMarcasEdExIdMarca"));

        btnMarcasEdExSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnMarcasEdExEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lstMarcasEdExBotoes.setVisibility(View.INVISIBLE);
                lstMarcasEdExAlterar.setVisibility(View.VISIBLE);
                edtMarcasEdExNome.setEnabled(true);
                edtMarcasEdExNome.requestFocus();
            }
        });

        btnMarcasEdExCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               inicializa();
            }
        });

        btnMarcasEdExExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleta_marca();
            }

        });

        btnMarcasEdExAlterar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView txtMarcasEdExIdMarca = (TextView)findViewById(R.id.txtMarcasEdExIdMarca);
                EditText edtMarcasEdExNome = (EditText)findViewById(R.id.edtMarcasEdExNome);
                StringBuilder sql_update_marca = new StringBuilder();
                String strSql;
                strSql = "UPDATE db_marcas ";
                strSql = strSql + " SET nome_marca=";
                strSql = strSql + "'" + edtMarcasEdExNome.getText().toString() + "'";
                strSql = strSql + " WHERE _id=" + txtMarcasEdExIdMarca.getText().toString();
                try {
                    db_marcas.execSQL(strSql);
                    exibe_msg("Registro alterado com sucesso.");
                }
                catch(Exception ex) {
                    exibe_msg("Erro - " + ex.getMessage());
                }
            }
        });

    }

    public void inicializa() {
        Button btnMarcasEdExExcluir = (Button)findViewById(R.id.btnMarcasEdExExcluir);
        Button btnMarcasEdExSair = (Button)findViewById(R.id.btnMarcasEdExSair);
        Button btnMarcasEdExEditar = (Button)findViewById(R.id.btnMarcasEdExEditar);
        LinearLayout lstMarcasEdExBotoes = (LinearLayout)findViewById(R.id.lstMarcasEdExBotoes);
        LinearLayout lstMarcasEdExAlterar = (LinearLayout)findViewById(R.id.lstMarcasEdExAlterar);
        EditText edtMarcasEdExNome = (EditText)findViewById(R.id.edtMarcasEdExNome);

        // Habilita todos os botões ao entrar
        btnMarcasEdExSair.setEnabled(true);
        btnMarcasEdExEditar.setEnabled(true);
        btnMarcasEdExExcluir.setEnabled(true);
        lstMarcasEdExBotoes.setVisibility(View.VISIBLE);

        // Desabilita a caixa de texto
        edtMarcasEdExNome.setEnabled(false);

        // Esconde os botões ALTERAR e CANCELAR
        lstMarcasEdExAlterar.setVisibility(View.INVISIBLE);
    }

    public void exibe_msg(String msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void deleta_marca() {

        TextView txtMarcasEdExIdMarca = (TextView) findViewById(R.id.txtMarcasEdExIdMarca);

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o titulo
        builder.setTitle("EXCLUSÃO DE MARCA");

        //define a mensagem
        builder.setMessage("Confirma a exclusão desta MARCA? Ao excluí-la todo os produtos dela serão PERDIDOS.");

        //define um botão como SIM
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                String _id = txtMarcasEdExIdMarca.getText().toString();
                StringBuilder sql_marca_delete = new StringBuilder();
                sql_marca_delete.append("DELETE FROM db_marcas WHERE _id=" + _id);
                try {
                    db_marcas.execSQL(sql_marca_delete.toString());
                    exibe_msg("Marca excluída com sucesso");
                    finish();
                }
                catch(Exception ex) {
                    exibe_msg("Erro - " + ex.getMessage());
                }
            }
        });

        //define um botão como CANCELAR.
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();
    }

}