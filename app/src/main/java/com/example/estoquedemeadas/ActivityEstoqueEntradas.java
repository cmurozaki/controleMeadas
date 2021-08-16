package com.example.estoquedemeadas;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.common.StringUtils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ActivityEstoqueEntradas extends AppCompatActivity {

    //atributo da classe.
    public AlertDialog alerta;

    SQLiteDatabase db_estoque;

    Boolean blnExisteProd = false;

    // Deixa uma solicitação na fila para o Android realizar a cópia ao Google Drive.
    private void backupBancoNuvem() {
        BackupManager backupManager = new BackupManager(this);
        backupManager.dataChanged();
        Log.d("Backup", "entrada solicitado");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estoque_entradas);

        db_estoque = openOrCreateDatabase("estoque_meadas.db", Context.MODE_PRIVATE, null);

        ImageButton ibtnEstoqueEntBuscarCodBarras = (ImageButton)findViewById(R.id.ibtnEstoqueEntBuscarCodBarras);
        Button btnEstoqueEntGravar = (Button)findViewById(R.id.btnEstoqueEntGravar);
        Button btnEstoqueEntSair = (Button)findViewById(R.id.btnEstoqueEntSair);
        TextView txtEstoqueEntNomeMarca = (TextView)findViewById(R.id.txtEstoqueEntNomeMarca);
        TextView txtEstoqueEntIdMarca = (TextView)findViewById(R.id.txtEstoqueEntIdMarca);

        EditText edtEstoqueEntCodRef = (EditText)findViewById(R.id.edtEstoqueSaiCodRef);
        EditText edtEstoqueEntCodBarras = (EditText)findViewById(R.id.edtEstoqueSaiCodBarras);
        EditText edtEstoqueEntQuant = (EditText)findViewById(R.id.edtEstoqueSaiQuant);

        // Recebe os valores enviados pelo outro formulário
        Intent valores = getIntent();
        txtEstoqueEntNomeMarca.setText(valores.getStringExtra("txtEstoqueEntNomeMarca"));
        txtEstoqueEntIdMarca.setText(valores.getStringExtra("txtEstoqueEntIdMarca"));

        edtEstoqueEntQuant.setText("1");

        // Gravar a ENTRADA
        btnEstoqueEntGravar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int intQuantEnt = 0;
                int intQuantTotal = 0;
                int intEstoqueAtual = 0;

                // Quantidade de Entrada
                intQuantEnt = Integer.parseInt(edtEstoqueEntQuant.getText().toString());

                String strSqlFields;
                String strSqlData;
                String strSqlUpdate;
                Boolean blnTemCodBarras = false;

                blnExisteProd = false;

                // Identifica se o usuário informou o Código de Barras
                if(edtEstoqueEntCodBarras.getText().length()==0) {
                    blnTemCodBarras = false;
                }
                else {
                    blnTemCodBarras = true;
                }

                // Busca o produto pelo código de referência.
                if(edtEstoqueEntCodRef.getText().length()>0) {
                    Cursor cursor = db_estoque.query("db_estoque", new String[]{}, "id_marca=? AND cod_ref=?",
                            new String[]{txtEstoqueEntIdMarca.getText().toString(), edtEstoqueEntCodRef.getText().toString()},
                            null, null, null);
                    try {
                        if(cursor.moveToFirst()) {
                            blnExisteProd = true;
                        } else {
                            blnExisteProd = false;
                        }
                    } catch(Exception ex) {

                    }
                }

                ////
                // Verifica se o produto já existe.
                if(blnTemCodBarras && blnExisteProd==false) {
                    // Busca pelo Código de Barras, desde que não tenha localizado pelo código de referência acima.
                    Cursor cursor = db_estoque.query("db_estoque", new String[]{"_id", "estoq_atual", "cod_ref"}, "id_marca=? AND cod_barras=?",
                            new String[]{txtEstoqueEntIdMarca.getText().toString(), edtEstoqueEntCodBarras.getText().toString()},
                            null, null, null);
                    try {
                        if(cursor.moveToFirst()) {
                            blnExisteProd = true;
                            intEstoqueAtual = Integer.parseInt(cursor.getString(1));
                            edtEstoqueEntCodRef.setText(cursor.getString(2));
                        } else {
                            blnExisteProd = false;
                        }
                    }
                    catch(Exception ex) {
                        exibe_msg("Erro - " + ex.getMessage());
                        return;
                    }
                } else {
                    // Busca pelo Código de Referência.
                    if(edtEstoqueEntCodRef.getText().length()==0) {
                        exibe_msg( "Código de Referência é de preenchimento obrigatório." );
                        return;
                    }
                    Cursor cursor = db_estoque.query("db_estoque", new String[]{"_id", "estoq_atual"}, "id_marca=? AND cod_ref=?",
                            new String[]{txtEstoqueEntIdMarca.getText().toString(), edtEstoqueEntCodRef.getText().toString()},
                            null, null, null);
                    try {
                        if(cursor.moveToFirst()) {
                            blnExisteProd = true;
                            intEstoqueAtual = Integer.parseInt(cursor.getString(1));
                        } else {
                            blnExisteProd = false;
                        }
                    }
                    catch(Exception ex) {
                        exibe_msg("Erro - " + ex.getMessage());
                        return;
                    }
                }
                // Verifica se o produto já existe.
                ////

                // Usuário não informou o código de referência e não localizou pelo código de barras.
                if(edtEstoqueEntCodRef.getText().length()==0 && blnExisteProd==false) {
                    exibe_msg( "Produto não encontrado. Informe o Código de Referência." );
                    return;
                }

                String strEstoqueEntCodRef = edtEstoqueEntCodRef.getText().toString();

                ////
                // Cria variáveis para realizar um INSERT
                if(edtEstoqueEntCodBarras.getText().length()==0) {
                    strSqlFields = "id_marca, cod_ref, estoq_atual";
                    strSqlData = txtEstoqueEntIdMarca.getText().toString() + "," + strEstoqueEntCodRef + ", " + intQuantEnt;
                }
                else {
                    strSqlFields = "id_marca, cod_ref, cod_barras, estoq_atual";
                    strSqlData = txtEstoqueEntIdMarca.getText().toString() + "," + strEstoqueEntCodRef + ", " +
                            edtEstoqueEntCodBarras.getText().toString() + ", " + intQuantEnt;
                }
                StringBuilder sql_estoque_insert = new StringBuilder();
                sql_estoque_insert.append("INSERT INTO db_estoque (" + strSqlFields + ") VALUES (" + strSqlData + ")");
                // Cria variáveis para realizar um INSERT
                ////

                ////
                // Cria variáveis para realizar um UPDATE
                intQuantTotal = intQuantEnt + intEstoqueAtual;
                StringBuilder sql_estoque_update = new StringBuilder();
                if(blnTemCodBarras) {
                    strSqlUpdate = " estoq_atual=" + intQuantTotal + ", cod_barras=" + edtEstoqueEntCodBarras.getText().toString() +
                            " WHERE id_marca=" + txtEstoqueEntIdMarca.getText().toString() + " AND cod_ref=" + strEstoqueEntCodRef;
                    sql_estoque_update.append("UPDATE db_estoque SET " + strSqlUpdate);
                } else {
                    strSqlUpdate = " estoq_atual=" + intQuantTotal +
                            " WHERE id_marca=" + txtEstoqueEntIdMarca.getText().toString() + " AND cod_ref=" + strEstoqueEntCodRef;
                    sql_estoque_update.append("UPDATE db_estoque SET " + strSqlUpdate);
                }
                // Cria variáveis para realizar um UPDATE
                ////

                if(blnExisteProd) {
                    // Produto já existe, realiza atualização.
                    try {
                        db_estoque.execSQL(sql_estoque_update.toString());
                        restaura_campos();
                    } catch(Exception ex) {
                        caixa_dialogo_ok("Erro", ex.getMessage());
                    }
                } else {
                    // Produto não existe, adiciona na base de estoque.
                    try {
                        db_estoque.execSQL(sql_estoque_insert.toString());
                        restaura_campos();
                        exibe_msg("Produto Cadastrado com sucesso.");
                    }
                    catch(Exception ex) {
                        // exibe_msg("Erro - " + ex.getMessage());
                        caixa_dialogo_ok("Erro", ex.getMessage());
                        return;
                    }
                }
                backupBancoNuvem();
                caixa_dialogo_ok("ESTOQUE ATUAL - REFERÊNCIA: " + strEstoqueEntCodRef, "Estoque Atual do Produto: " + intQuantTotal);
            }
        });

        // Botão BUSCAR POR CÓDIGO DE BARRAS
        ibtnEstoqueEntBuscarCodBarras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Busca pelo Código de Barras
                Cursor cursor = db_estoque.query("db_estoque", new String[]{"_id", "cod_ref"}, "id_marca=? AND cod_barras=?",
                        new String[]{txtEstoqueEntIdMarca.getText().toString(), edtEstoqueEntCodBarras.getText().toString()},
                        null, null, null);
                try {
                    if(cursor.moveToFirst()) {
                        // Achou o Código de Barras
                        edtEstoqueEntCodRef.setText(cursor.getString(1));
                        edtEstoqueEntQuant.requestFocus();
                    } else {
                        caixa_dialogo_ok("CÓDIGO DE REFERÊNCIA", "Informe o Código de Referência.");
                        edtEstoqueEntCodRef.requestFocus();
                    }
                }
                catch(Exception ex) {
                    exibe_msg("Erro - " + ex.getMessage());
                    return;
                }
            }
        });

        btnEstoqueEntSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityEstoqueEntradas.this,MainActivity.class);
                ActivityEstoqueEntradas.this.startActivity(intent);
                finish();
            }
        });

    }

    // Restaura os campos após a gravação de uma entrada.
    public void restaura_campos() {
        EditText edtEstoqueEntCodRef = (EditText)findViewById(R.id.edtEstoqueSaiCodRef);
        EditText edtEstoqueEntCodBarras = (EditText)findViewById(R.id.edtEstoqueSaiCodBarras);
        EditText edtEstoqueEntQuant = (EditText)findViewById(R.id.edtEstoqueSaiQuant);

        edtEstoqueEntCodBarras.setText("");
        edtEstoqueEntCodRef.setText("");
        edtEstoqueEntQuant.setText("1");
        edtEstoqueEntCodRef.requestFocus();

        // Exibe o teclado virtual automaticamente
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edtEstoqueEntCodRef, InputMethodManager.SHOW_IMPLICIT);
    }
    public void exibe_msg(String msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
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

    //////
    // Aciona a câmera.
    // IMPORTANTE:
    // Adicionar antes em 'Gradle Scripts -> build.gradle (Module: .app):
    //    implementation 'com.google.zxing:core:3.2.1'
    //    implementation 'com.journeyapps:zxing-android-embedded:3.2.0@aar'
    // e em seguida clicar em 'Sync Now' (canto superior direito)
    public void ibtnEstoqueEntCodBarrasOnClick(View v) {
        final Activity activity = this;
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats((IntentIntegrator.PRODUCT_CODE_TYPES));
        integrator.setPrompt("Aproxime o leitor no código de barras");
        integrator.setCameraId(0);      // 0 - Traseira ou 1 - Frontal
        integrator.initiateScan();
    }

    // Trata o que foi lido pela câmera.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        EditText edtEstoqueEntCodBarras = (EditText)findViewById(R.id.edtEstoqueSaiCodBarras);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                edtEstoqueEntCodBarras.setText(result.getContents());
            } else {
                exibe_msg("Digite o código de barras");
                edtEstoqueEntCodBarras.requestFocus();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    // Aciona a câmera.
    //////

}