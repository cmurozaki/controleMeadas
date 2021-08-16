package com.example.estoquedemeadas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ActivityEstoque extends AppCompatActivity {

    //atributo da classe.
    public AlertDialog alerta;

    public String strOpcao;

    SQLiteDatabase db_marcas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estoque);

        db_marcas = openOrCreateDatabase("marcas_meadas.db", Context.MODE_PRIVATE, null);

        Button btnEstoqueSair = (Button)findViewById(R.id.btnEstoqueSair);
        Button btnEstoqueEntradas = (Button)findViewById(R.id.btnEstoqueEntradas);
        Button btnEstoqueSaidas = (Button)findViewById(R.id.btnEstoqueSaidas);
        Button btnEstoqueConsultas = (Button)findViewById(R.id.btnEstoqueConsultas);
        Button btnEstoqueEstatística = (Button)findViewById(R.id.btnEstoqueEstatística);

        LinearLayout linearEstoqueMarcas = (LinearLayout)findViewById(R.id.linearEstoqueMarcas);
        ListView lstEstoqueMarcas = (ListView)findViewById(R.id.lstEstoqueMarcas);

        linearEstoqueMarcas.setVisibility(View.INVISIBLE);

        busca_todas_marcas();

        btnEstoqueEntradas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearEstoqueMarcas.setVisibility(View.VISIBLE);
                strOpcao = "ENTRADA";
            }
        });

        btnEstoqueSaidas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearEstoqueMarcas.setVisibility(View.VISIBLE);
                strOpcao = "SAIDA";
            }
        });

        btnEstoqueConsultas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearEstoqueMarcas.setVisibility(View.VISIBLE);
                strOpcao = "CONSULTA";
            }
        });

        btnEstoqueEstatística.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearEstoqueMarcas.setVisibility(View.VISIBLE);
                strOpcao = "ESTATISTICA";
            }
        });

        btnEstoqueSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivityEstoque.this, MainActivity.class);
                ActivityEstoque.this.startActivity(i);
            }
        });

        lstEstoqueMarcas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = db_marcas.query("db_marcas", new String[]{"nome_marca", "_id"},
                        "_id=?", new String[]{Long.toString(id)},
                        null, null, null);
                if (cursor.moveToFirst()) {
                    if(strOpcao=="ENTRADA") {
                        Intent i = new Intent(ActivityEstoque.this, ActivityEstoqueEntradas.class);
                        i.putExtra("txtEstoqueEntNomeMarca", cursor.getString(0));
                        i.putExtra("txtEstoqueEntIdMarca", cursor.getString(1));
                        ActivityEstoque.this.startActivity(i);
                        finish();
                    } else if(strOpcao=="CONSULTA") {
                        Intent i = new Intent(ActivityEstoque.this, ActivityConsultas.class);
                        i.putExtra("txtConsultasNomeMarca", cursor.getString(0));
                        i.putExtra("txtConsultasIdMarca", cursor.getString(1));
                        ActivityEstoque.this.startActivity(i);
                        finish();
                    } else if(strOpcao=="SAIDA") {
                        Intent i = new Intent(ActivityEstoque.this, ActivityEstoqueSaidas.class);
                        i.putExtra("txtEstoqueSaiNomeMarca", cursor.getString(0));
                        i.putExtra("txtEstoqueSaiIdMarca", cursor.getString(1));
                        ActivityEstoque.this.startActivity(i);
                        finish();
                    } else if(strOpcao=="ESTATISTICA") {
                        Intent i = new Intent(ActivityEstoque.this, ActivityEstoqueEstatistica.class);
                        ActivityEstoque.this.startActivity(i);
                        finish();
                    }
                }

            }
        });
    }

    // Caixa de diálogo
    public void caixa_dialogo_ok(String strTitulo, String strMensagem) {
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //define o titulo
        builder.setTitle(strTitulo);
        //define a mensagem
        builder.setMessage(strMensagem);
        //define um botão como OK
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        //cria o AlertDialog
        alerta = builder.create();
        //Exibe
        alerta.show();
    }

    // Busca todas as marcas cadastradas
    public void busca_todas_marcas() {
        StringBuilder sql_marcas = new StringBuilder();
        sql_marcas.append("SELECT * FROM db_marcas ORDER BY nome_marca ASC");
        try {
            Cursor cursor = db_marcas.rawQuery(sql_marcas.toString(),null);
            String[] nomeCampos = {"_id", "nome_marca"};
            int[] idViews = {R.id.txtIdMarca, R.id.txtNomeMarca};
            SimpleCursorAdapter adaptador = new SimpleCursorAdapter(getBaseContext(),
                    R.layout.lista_marcas,cursor,nomeCampos,idViews, 0);
            ListView lstEstoqueMarcas = (ListView)findViewById(R.id.lstEstoqueMarcas);
            lstEstoqueMarcas.setAdapter(adaptador);
        }
        catch(Exception ex) {
            caixa_dialogo_ok("Erro", "Falha " + ex.getMessage());
        }

    }

    protected void onDestroyed() {
        super.onDestroy();
        int idProcessoAtual = Process.myPid();
        Process.killProcess(idProcessoAtual);

    }
}