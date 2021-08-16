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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ActivityConsultas extends AppCompatActivity {

    //atributo da classe.
    public AlertDialog alerta;

    SQLiteDatabase db_estoque;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultas);

        db_estoque = openOrCreateDatabase("estoque_meadas.db", Context.MODE_PRIVATE, null);

        ListView lstConsultasEstoques = (ListView)findViewById(R.id.lstConsultasEstoques);
        TextView txtConsultasNomeMarca = (TextView)findViewById(R.id.txtConsultasNomeMarca);
        TextView txtConsultasIdMarca = (TextView)findViewById(R.id.txtConsultasIdMarca);
        ImageButton ibtnConsultasCodRef = (ImageButton)findViewById(R.id.ibtnConsultasCodRef);
        Button btnConsultasSair = (Button)findViewById(R.id.btnConsultasSair);

        // Recebe os valores enviados pelo outro formulário
        Intent valores = getIntent();
        txtConsultasNomeMarca.setText(valores.getStringExtra("txtConsultasNomeMarca"));
        txtConsultasIdMarca.setText(valores.getStringExtra("txtConsultasIdMarca"));

        busca_todo_estoque();

        // Botão SAIR
        btnConsultasSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });

        // Pressionar em um item da lista de produtos.
        lstConsultasEstoques.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txtCodRef = (TextView) view.findViewById(R.id.txtListaEstoquesCodRef);
                caixa_dialogo_sim_nao( txtConsultasIdMarca.getText().toString(), Integer.parseInt(txtCodRef.getText().toString()) );
            }
        });

        // Botão CONSULTA DE ESTOQUE POR CÓDIGO DE REFERÊNCIA
        ibtnConsultasCodRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView txtConsultasIdMarca = (TextView)findViewById(R.id.txtConsultasIdMarca);
                EditText edtConsultasCodRef = (EditText)findViewById(R.id.edtConsultasCodRef);
                StringBuilder sql_estoque = new StringBuilder();
                sql_estoque.append("SELECT * FROM db_estoque WHERE id_marca=" + txtConsultasIdMarca.getText().toString() );
                sql_estoque.append(" AND cod_ref=" + edtConsultasCodRef.getText().toString());
                sql_estoque.append(" ORDER BY cod_ref");

                /*Cursor cursor = db_estoque.query("db_estoque", new String[] {"cod_ref", "quant_atual"}, "id_marca=? and cod_ref=?",
                        new String[] {txtConsultasIdMarca.getText().toString(), edtConsultasCodRef.getText().toString()},
                        null, null, null);
                */
                try {
                    Cursor cursor = db_estoque.rawQuery(sql_estoque.toString(), null);
                    String[] nomeCampos = {"cod_ref", "estoq_atual"};
                    int[] idViews = {R.id.txtListaEstoquesCodRef, R.id.txtListaEstoquesQuant};
                    SimpleCursorAdapter adaptador = new SimpleCursorAdapter(getBaseContext(),
                            R.layout.lista_estoques,cursor,nomeCampos,idViews, 0);
                    ListView lstConsultasEstoques = (ListView)findViewById(R.id.lstConsultasEstoques);
                    lstConsultasEstoques.setAdapter(adaptador);
                    if(cursor.moveToFirst()) {
                        // Encontrou o código de referência.
                    } else {
                        caixa_dialogo_ok("Código de Referência", "O código de referência não foi cadastrado.");
                        return;
                    }
                } catch(Exception ex) {

                }
            }
        });
    }

    private void busca_todo_estoque() {
        TextView txtConsultasIdMarca = (TextView)findViewById(R.id.txtConsultasIdMarca);
        StringBuilder sql_estoque = new StringBuilder();
        sql_estoque.append("SELECT * FROM db_estoque WHERE id_marca=" + txtConsultasIdMarca.getText().toString() );
        sql_estoque.append(" ORDER BY cod_ref");
        try {
            Cursor cursor = db_estoque.rawQuery(sql_estoque.toString(),null);
            String[] nomeCampos = {"cod_ref", "estoq_atual"};
            int[] idViews = {R.id.txtListaEstoquesCodRef, R.id.txtListaEstoquesQuant};
            SimpleCursorAdapter adaptador = new SimpleCursorAdapter(getBaseContext(),
                    R.layout.lista_estoques,cursor,nomeCampos,idViews, 0);
            ListView lstConsultasEstoques = (ListView)findViewById(R.id.lstConsultasEstoques);
            lstConsultasEstoques.setAdapter(adaptador);
        } catch(Exception ex) {
            caixa_dialogo_ok("Erro", "Falha " + ex.getMessage());
        }

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

    public void caixa_dialogo_sim_nao(String strIdMarca, int intCodRef) {
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //define o titulo
        builder.setTitle("EXCLUIR PRODUTO");
        //define a mensagem
        builder.setMessage("Confirma excluir este produto do estoque? Código: " + intCodRef);
        //define um botão como OK
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                // Exclusão do produto da tabela de estoque
                StringBuilder sql_delete_estoque = new StringBuilder();
                sql_delete_estoque.append("DELETE FROM db_estoque WHERE id_marca=" + strIdMarca);
                sql_delete_estoque.append(" AND cod_ref=" + intCodRef);
                try {
                    db_estoque.execSQL(sql_delete_estoque.toString());
                    caixa_dialogo_ok("EXCLUSÃO DE PRODUTO", "Produto excluído do estoque");
                    busca_todo_estoque();
                } catch (Exception ex) {
                    caixa_dialogo_ok("Erro", "Exclusão de produto. " + ex.getMessage());
                }
                //finish();
            }
        });
        //define um botão como negativo.
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                // Toast.makeText(MainActivity.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
            }
        });
        //cria o AlertDialog
        alerta = builder.create();
        //Exibe
        alerta.show();
    }
}