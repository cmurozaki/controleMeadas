package com.example.estoquedemeadas;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.os.Process;

public class MainActivity extends AppCompatActivity {

    //atributo da classe.
    public AlertDialog alerta;

    SQLiteDatabase db_marcas;
    SQLiteDatabase db_estoque;

    // Deixa uma solicitação na fila para o Android realizar a cópia ao Google Drive.
    private void backupBancoNuvem() {
        BackupManager backupManager = new BackupManager(this);
        backupManager.dataChanged();
        Log.d("Backup", "solicitado");
    }

    //// MENU
    // Este bloco cria um menu. No arquivo "menu.xml" estão as opções.
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    // Trata a opção selecionada.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.menuOpcMarcas) {
            Intent intent = new Intent(MainActivity.this, ActivityMarcas.class);
            MainActivity.this.startActivity(intent);
        }
        else if (item.getItemId()==R.id.menuOpcEstoques) {
            Intent intent = new Intent(MainActivity.this, ActivityEstoque.class);
            MainActivity.this.startActivity(intent);
        }
        else if (item.getItemId()==R.id.menuOpcReiniciar) {
            caixa_dialogo_sim_nao();
        }
        else if (item.getItemId()==R.id.menuBackup) {
            caixa_dialogo_ok("Backup", "A cópia dos seus dados será realizada automaticamente no Google Drive.");
            backupBancoNuvem();
        }
        else if (item.getItemId()==R.id.menuSair) {
            // Outra forma de carregar uma outra Activity:
            // startActivity(new Intent(getBaseContext(), SplashActivity.class));
            // finishAndRemoveTask();
            this.finishAffinity();
        }
        return false;
    }
    // MENU
    ////

    public void reinicia_tabelas() {

    }

    ////
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
        //define um botão como negativo.
        /*
        builder.setNegativeButton("Negativo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(MainActivity.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
            }
        });
        */
        //cria o AlertDialog
        alerta = builder.create();
        //Exibe
        alerta.show();
    }

    public void caixa_dialogo_sim_nao() {
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //define o titulo
        builder.setTitle("REINICIAR TABELAS");
        //define a mensagem
        builder.setMessage("Confirma ZERAR todos os registros?");
        //define um botão como OK
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                // Exclusão - dm_marcas
                StringBuilder sql_drop_marcas = new StringBuilder();
                sql_drop_marcas.append("DROP TABLE IF EXISTS db_marcas");
                try {
                    db_marcas.execSQL(sql_drop_marcas.toString());
                    caixa_dialogo_ok("DROP TABLE", "Tabela db_marcas excluída com sucesso");
                } catch (Exception ex) {
                    caixa_dialogo_ok("Erro", "Exclusão da tabela db_marcas. " + ex.getMessage());
                }

                // Exclusão - db_estoques
                StringBuilder sql_drop_estoques = new StringBuilder();
                sql_drop_estoques.append("DROP TABLE IF EXISTS db_estoque");
                try {
                    db_estoque.execSQL(sql_drop_estoques.toString());
                    caixa_dialogo_ok("DROP TABLE", "Tabela db_estoques excluída com sucesso");
                    // finish();
                } catch (Exception ex) {
                    caixa_dialogo_ok("Erro", "Exclusão da tabela db_estoques. " + ex.getMessage());
                }
                finish();
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
    //
    ////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ////
        // Trata sobre a criação do banco de dados.
        db_marcas = openOrCreateDatabase("marcas_meadas.db", Context.MODE_PRIVATE, null);
        db_estoque = openOrCreateDatabase("estoque_meadas.db", Context.MODE_PRIVATE, null);

        StringBuilder sql_marcas = new StringBuilder();
        StringBuilder sql_estoque = new StringBuilder();
        StringBuilder sql_marcas_ins = new StringBuilder();

        // Cria a estrutura para controle das marcas.
        sql_marcas.append("CREATE TABLE IF NOT EXISTS db_marcas (");
        sql_marcas.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,");
        sql_marcas.append("nome_marca VARCHAR(20) )");
        try {
            db_marcas.execSQL(sql_marcas.toString());
            // caixa_dialogo_ok("Sucesso", "Tabela MARCAS criada com sucesso.");
        }
        catch(Exception ex) {
            caixa_dialogo_ok("Erro", "Falha na criação do Banco de Dados. " + ex.getMessage());
        }

        // Teste
        /*
        sql_marcas_ins.append("INSERT INTO db_marcas (");
        sql_marcas_ins.append("nome_marca");
        sql_marcas_ins.append(") VALUES (");
        sql_marcas_ins.append("'TESTE')");
        try {
            db_marcas.execSQL(sql_marcas_ins.toString());
            // caixa_dialogo_ok("Sucesso", "Tabela MARCAS criada com sucesso.");
        }
        catch(Exception ex) {
            caixa_dialogo_ok("Erro", "Falha na inserção de dados. " + ex.getMessage());
        }
        */

        // Cria a estrutura para controle de estoque das meadas.
        sql_estoque.append("CREATE TABLE IF NOT EXISTS db_estoque (");
        sql_estoque.append("_id          INTEGER PRIMARY KEY AUTOINCREMENT,");
        sql_estoque.append("id_marca     INTEGER,");
        // sql_estoque.append("cod_ref      CHAR(4),");
        sql_estoque.append("cod_ref      INTEGER,");
        sql_estoque.append("cod_barras   VARCHAR(20) DEFAULT ' ',");
        sql_estoque.append("desc_produto VARCHAR(30),");
        sql_estoque.append("estoq_min    INTEGER,");
        sql_estoque.append("estoq_atual  INTEGER)");
        try {
            db_estoque.execSQL(sql_estoque.toString());
            // caixa_dialogo_ok("Sucesso", "Tabela ESTOQUE criada com sucesso.");
        }
        catch(Exception ex) {
            caixa_dialogo_ok("Erro", "Falha na criação do Banco de Dados. " + ex.getMessage());
        }
        // Trata sobre a criação do banco de dados.
        ////

        ////
        // Clicar no botão MARCAS
        Button btnMainMarcas = (Button)findViewById(R.id.btnMainMarcas);
        btnMainMarcas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ActivityMarcas.class);
                MainActivity.this.startActivity(i);
            }
        });
        // Clicar no botão MARCAS
        ////

        ////
        // Clicar no botão ESTOQUES
        Button btnMainEstoques = (Button)findViewById(R.id.btnMainEstoques);
        btnMainEstoques.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ActivityEstoque.class);
                MainActivity.this.startActivity(i);
            }
        });
        // Clicar no botão ESTOQUES
        ////

    }

    protected void onDestroyed() {
        super.onDestroy();
        int idProcessoAtual = Process.myPid();
        Process.killProcess(idProcessoAtual);
    }
}