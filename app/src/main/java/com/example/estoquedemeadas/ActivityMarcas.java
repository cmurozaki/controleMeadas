package com.example.estoquedemeadas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

//
// DICA:
// - Para ajustar a tela quand o teclado virtual é acionado:
//   No arquivo AndroidManifest.xml adicionar estas tag's:
//         <activity android:name=".ActivityMarcas"
//            android:windowSoftInputMode="adjustPan"/>
//
public class ActivityMarcas extends AppCompatActivity {

    //atributo da classe.
    public AlertDialog alerta;

    SQLiteDatabase db_marcas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcas);

        db_marcas = openOrCreateDatabase("marcas_meadas.db", Context.MODE_PRIVATE, null);

        LinearLayout linearLayMarcasNovo = (LinearLayout)findViewById(R.id.linearLayMarcasNovo);
        Button btnMarcasNovo = (Button)findViewById(R.id.btnMarcasNovo);
        Button btnMarcasSair = (Button)findViewById(R.id.btnMarcasSair);
        ImageButton ibtnMarcasGravarNovo = (ImageButton)findViewById(R.id.ibtnMarcasGravarNovo);
        Button btnMarcasCancelar = (Button)findViewById(R.id.btnMarcasCancelar);
        EditText edtMarcasNova = (EditText)findViewById(R.id.edtMarcasNova);

        ListView lstMarcasDados = (ListView)findViewById(R.id.lstMarcasDados);

        // Exemplo para inserir dados na lista
        /*
        ListView lstDados = (ListView)findViewById(R.id.lstMarcasDados);
        String[] dados = {"TESTE 1", "TESTE 2"};
        ArrayAdapter<String> adpter = new ArrayAdapter<>(this, R.layout.lista_marcas, dados);
        lstDados.setAdapter(adpter);

        */
        /*
        StringBuilder sql_marcas = new StringBuilder();
        sql_marcas.append("SELECT * FROM db_marcas ORDER BY nome_marca ASC");
        try {
            Cursor cursor = db_marcas.rawQuery(sql_marcas.toString(),null);
            String[] nomeCampos = {"_id", "nome_marca"};
            int[] idViews = {R.id.txtIdMarca, R.id.txtNomeMarca};
            SimpleCursorAdapter adaptador = new SimpleCursorAdapter(getBaseContext(),
                    R.layout.lista_marcas,cursor,nomeCampos,idViews, 0);
            ListView lstMarcasDados = (ListView)findViewById(R.id.lstMarcasDados);
            lstMarcasDados.setAdapter(adaptador);
        }
        catch(Exception ex) {
            caixa_dialogo_ok("Erro", "Falha " + ex.getMessage());

        }
        */

        busca_todas_marcas();

        // Botão NOVO
        btnMarcasNovo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayMarcasNovo.setVisibility(View.VISIBLE);
            }
        });

        // Botão SAIR
        btnMarcasSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayMarcasNovo.setVisibility(View.INVISIBLE);
                finish();
            }
        });

        // Botão GRAVAR NOVA MARCA
        ibtnMarcasGravarNovo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sql_nova_marca = new StringBuilder();
                sql_nova_marca.append("INSERT INTO db_marcas (");
                sql_nova_marca.append("nome_marca) VALUES (");
                sql_nova_marca.append("'" + edtMarcasNova.getText().toString() + "')");
                try {
                    db_marcas.execSQL(sql_nova_marca.toString());
                    caixa_dialogo_ok("NOVA MARCA", "Registro salvo com sucesso.");
                    edtMarcasNova.setText("");
                    linearLayMarcasNovo.setVisibility(View.INVISIBLE);
                    busca_todas_marcas();
                }
                catch(Exception ex)
                {
                    caixa_dialogo_ok("Erro", "Falha na inserção de dados. " + ex.getMessage());
                }
            }
        });

        // Selecionar um item da lista de dados
        lstMarcasDados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // caixa_dialogo_ok("TESTE", Long.toString(id));
                Cursor cursor = db_marcas.query("db_marcas", new String[]{"nome_marca", "_id"},
                        "_id=?", new String[]{Long.toString(id)},
                        null, null, null);
                if (cursor.moveToFirst()) {
                    Intent i = new Intent(ActivityMarcas.this, ActivityMarcasEditarExcluir.class);
                    i.putExtra("edtMarcasEdExNome", cursor.getString(0));
                    i.putExtra("txtMarcasEdExIdMarca", cursor.getString(1));
                    ActivityMarcas.this.startActivity(i);
                    finish();
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
            ListView lstMarcasDados = (ListView)findViewById(R.id.lstMarcasDados);
            lstMarcasDados.setAdapter(adaptador);
        }
        catch(Exception ex) {
            caixa_dialogo_ok("Erro", "Falha " + ex.getMessage());
        }

    }

}