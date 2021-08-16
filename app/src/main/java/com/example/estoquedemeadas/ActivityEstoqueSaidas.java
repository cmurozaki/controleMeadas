package com.example.estoquedemeadas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ActivityEstoqueSaidas extends AppCompatActivity {

    //atributo da classe.
    public AlertDialog alerta;

    SQLiteDatabase db_estoque;

    Boolean blnExisteProd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estoque_saidas);

        db_estoque = openOrCreateDatabase("estoque_meadas.db", Context.MODE_PRIVATE, null);

        TextView txtEstoqueSaiNomeMarca = (TextView)findViewById(R.id.txtEstoqueSaiNomeMarca);
        TextView txtEstoqueSaiIdMarca = (TextView)findViewById(R.id.txtEstoqueSaiIdMarca);
        EditText edtEstoqueSaiCodRef = (EditText)findViewById(R.id.edtEstoqueSaiCodRef);
        EditText edtEstoqueSaiCodBarras = (EditText)findViewById(R.id.edtEstoqueSaiCodBarras);
        ImageButton ibtnEstoqueSaiCodBarras = (ImageButton)findViewById(R.id.ibtnEstoqueSaiCodBarras);
        ImageButton ibtnEstoqueSaiBuscarCodBarras = (ImageButton)findViewById(R.id.ibtnEstoqueSaiBuscarCodBarras);
        EditText edtEstoqueSaiQuant = (EditText)findViewById(R.id.edtEstoqueSaiQuant);
        Button btnEstoqueEntSair = (Button)findViewById(R.id.btnEstoqueEntSair);
        Button btnEstoqueEntGravar = (Button)findViewById(R.id.btnEstoqueEntGravar);

        // Recebe os valores enviados pelo outro formulário
        Intent valores = getIntent();
        txtEstoqueSaiNomeMarca.setText(valores.getStringExtra("txtEstoqueSaiNomeMarca"));
        txtEstoqueSaiIdMarca.setText(valores.getStringExtra("txtEstoqueSaiIdMarca"));

        // Botão GRAVAR.
        btnEstoqueEntGravar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int intQuantSai = 0;
                int intQuantTotal = 0;
                int intEstoqueAtual = 0;

                // Se não informou o código de referência e nem o código de barras.
                if(edtEstoqueSaiCodRef.getText().length()==0 && edtEstoqueSaiCodBarras.getText().length()==0) {
                    exibe_msg( "Código de Referência é de preenchimento obrigatório." );
                    return;
                }

                // Verifica se informou uma quantidade.
                if(edtEstoqueSaiQuant.getText().length()==0) {
                    caixa_dialogo_ok("QUANTIDADE INVÁLIDA", "Informe uma quantidade para a baixa do estoque");
                    edtEstoqueSaiQuant.requestFocus();
                    return;
                }

                // Quantidade de Saída
                intQuantSai = Integer.parseInt(edtEstoqueSaiQuant.getText().toString());

                String strSqlFields;
                String strSqlData;
                String strSqlUpdate;
                Boolean blnTemCodBarras = false;

                blnExisteProd = false;

                // Identifica se o usuário informou o Código de Barras
                if(edtEstoqueSaiCodBarras.getText().length()==0) {
                    blnTemCodBarras = false;
                }
                else {
                    blnTemCodBarras = true;
                }

                // Busca o produto pelo código de referência.
                if(edtEstoqueSaiCodRef.getText().length()>0) {
                    Cursor cursor = db_estoque.query("db_estoque", new String[]{}, "id_marca=? AND cod_ref=?",
                            new String[]{txtEstoqueSaiIdMarca.getText().toString(), edtEstoqueSaiCodRef.getText().toString()},
                            null, null, null);
                    try {
                        if(cursor.moveToFirst()) {
                            blnExisteProd = true;
                        } else {
                            blnExisteProd = false;
                        }
                    } catch(Exception ex) {
                        exibe_msg("Erro - " + ex.getMessage());
                        return;
                    }
                }

                ////
                // Verifica se o produto já existe.
                if(blnTemCodBarras && blnExisteProd==false) {
                    // Busca pelo Código de Barras
                    Cursor cursor = db_estoque.query("db_estoque", new String[]{"_id", "estoq_atual", "cod_ref"}, "id_marca=? AND cod_barras=?",
                            new String[]{txtEstoqueSaiIdMarca.getText().toString(), edtEstoqueSaiCodBarras.getText().toString()},
                            null, null, null);
                    try {
                        if(cursor.moveToFirst()) {
                            blnExisteProd = true;
                            intEstoqueAtual = Integer.parseInt(cursor.getString(1));
                            edtEstoqueSaiCodRef.setText(cursor.getString(2));
                        } else {
                            blnExisteProd = false;
                        }
                    }
                    catch(Exception ex) {
                        exibe_msg("Erro - " + ex.getMessage());
                        return;
                    }
                    if (intEstoqueAtual <= 0 || intEstoqueAtual-intQuantSai < 0) {
                        exibe_msg( getString( R.string.msg_produto_sem_estoque ) );
                        return;
                    }
                } else {
                    // Busca pelo Código de Referência.
                    Cursor cursor = db_estoque.query("db_estoque", new String[]{"_id", "estoq_atual"}, "id_marca=? AND cod_ref=?",
                            new String[]{txtEstoqueSaiIdMarca.getText().toString(), edtEstoqueSaiCodRef.getText().toString()},
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
                    if (intEstoqueAtual <= 0 || intEstoqueAtual-intQuantSai < 0) {
                        exibe_msg( getString(R.string.msg_produto_sem_estoque ));
                        return;
                    }
                }
                // Verifica se o produto já existe.
                ////

                // Se o produto não existe.
                if(edtEstoqueSaiCodRef.getText().length()==0 && blnExisteProd==false) {
                    caixa_dialogo_ok("PRODUTO NÃO LOCALIZADO", "Produto não localizado no estoque para a realização da baixa");
                    return;
                }

                ////
                // Cria variáveis para realizar um INSERT
                if(edtEstoqueSaiCodBarras.getText().length()==0) {
                    strSqlFields = "id_marca, cod_ref, estoq_atual";
                    strSqlData = txtEstoqueSaiIdMarca.getText().toString() + "," + edtEstoqueSaiCodRef.getText().toString() + ", " + intQuantSai;
                }
                else {
                    strSqlFields = "id_marca, cod_ref, cod_barras, estoq_atual";
                    strSqlData = txtEstoqueSaiIdMarca.getText().toString() + "," + edtEstoqueSaiCodRef.getText().toString() + ", " +
                            edtEstoqueSaiCodBarras.getText().toString() + ", " + intQuantSai;
                }
                StringBuilder sql_estoque_insert = new StringBuilder();
                sql_estoque_insert.append("INSERT INTO db_estoque (" + strSqlFields + ") VALUES (" + strSqlData + ")");
                // Cria variáveis para realizar um INSERT
                ////

                ////
                // Cria variáveis para realizar um UPDATE
                if(blnExisteProd) {
                    intQuantTotal = intEstoqueAtual - intQuantSai;
                    StringBuilder sql_estoque_update = new StringBuilder();
                    if (blnTemCodBarras) {
                        strSqlUpdate = " estoq_atual=" + intQuantTotal + ", cod_barras=" + edtEstoqueSaiCodBarras.getText().toString() +
                                " WHERE id_marca=" + txtEstoqueSaiIdMarca.getText().toString() + " AND cod_ref=" + edtEstoqueSaiCodRef.getText().toString();
                        sql_estoque_update.append("UPDATE db_estoque SET " + strSqlUpdate);
                    } else {
                        strSqlUpdate = " estoq_atual=" + intQuantTotal +
                                " WHERE id_marca=" + txtEstoqueSaiIdMarca.getText().toString() + " AND cod_ref=" + edtEstoqueSaiCodRef.getText().toString();
                        sql_estoque_update.append("UPDATE db_estoque SET " + strSqlUpdate);
                    }
                    // Cria variáveis para realizar um UPDATE
                    ////

                    try {
                        db_estoque.execSQL(sql_estoque_update.toString());
                        caixa_dialogo_ok("ESTOQUE ATUAL", "Estoque Atual do Produto: " + intQuantTotal);
                        restaura_campos();
                    } catch (Exception ex) {
                        caixa_dialogo_ok("Erro", ex.getMessage());
                    }
                } else {
                    caixa_dialogo_ok("PRODUTO NÃO LOCALIZADO", "Código do produto não encontrado para a realização da baixa");
                }

            }
        });

        // Botão para consulta o código de barras informado.
        ibtnEstoqueSaiBuscarCodBarras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtEstoqueSaiCodBarras.getText().length()==0) {
                    caixa_dialogo_ok("BUSCA POR CÓDIGO DE BARRAS", "Digite ou faça a leitura do Código de Barras do produto");
                    return;
                }
                Cursor cursor = db_estoque.query("db_estoque", new String[]{"_id", "cod_ref"}, "id_marca=? AND cod_barras=?",
                        new String[]{txtEstoqueSaiIdMarca.getText().toString(), edtEstoqueSaiCodBarras.getText().toString()},
                        null, null, null);
                try {
                    if(cursor.moveToFirst()) {
                        // Achou o Código de Barras
                        edtEstoqueSaiCodRef.setText(cursor.getString(1));
                        edtEstoqueSaiQuant.requestFocus();
                    } else {
                        caixa_dialogo_ok("CÓDIGO DE REFERÊNCIA", "Informe o Código de Referência.");
                        edtEstoqueSaiCodRef.requestFocus();
                    }
                }
                catch(Exception ex) {
                    exibe_msg("Erro - " + ex.getMessage());
                    return;
                }
            }
        });

        // Botão SAIR
        btnEstoqueEntSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityEstoqueSaidas.this, MainActivity.class);
                ActivityEstoqueSaidas.this.startActivity(intent);
                finish();
            }
        });
    }

    public void exibe_msg(String msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
    }

    // Restaura os campos após a gravação de uma saída.
    public void restaura_campos() {
        EditText edtEstoqueSaiCodRef = (EditText)findViewById(R.id.edtEstoqueSaiCodRef);
        EditText edtEstoqueSaiCodBarras = (EditText)findViewById(R.id.edtEstoqueSaiCodBarras);
        EditText edtEstoqueSaiQuant = (EditText)findViewById(R.id.edtEstoqueSaiQuant);

        edtEstoqueSaiCodBarras.setText("");
        edtEstoqueSaiCodRef.setText("");
        edtEstoqueSaiQuant.setText("1");
        edtEstoqueSaiCodRef.requestFocus();

        // Exibe o teclado virtual automaticamente
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edtEstoqueSaiCodRef, InputMethodManager.SHOW_IMPLICIT);

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
    public void ibtnEstoqueSaiCodBarrasOnClick(View v) {
        final Activity activity = this;
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats((IntentIntegrator.PRODUCT_CODE_TYPES));
        integrator.setPrompt("Aproxime o leitor no código de barras");
        integrator.setCameraId(0);      // 0 - Traseira ou 1 - Frontal
        integrator.initiateScan();
    }

    // Trata o que foi lido pela câmera.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        EditText edtEstoqueSaiCodBarras = (EditText)findViewById(R.id.edtEstoqueSaiCodBarras);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                edtEstoqueSaiCodBarras.setText(result.getContents());
            } else {
                exibe_msg("Digite o código de barras");
                edtEstoqueSaiCodBarras.requestFocus();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    // Aciona a câmera.
    //////
}