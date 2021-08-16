package com.example.estoquedemeadas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ActivityEstoqueEstatistica extends AppCompatActivity {

    //atributo da classe.
    public AlertDialog alerta;

    private Button btnVoltar;

    SQLiteDatabase db_estoque;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estoque_estatistica);

        TextView txtTotalEstoque = (TextView)findViewById(R.id.txtTotalEstoque);

        Integer nTotEstoque = 0;

        db_estoque = openOrCreateDatabase("estoque_meadas.db", Context.MODE_PRIVATE, null);

        Cursor cursor = db_estoque.query("db_estoque", null, null, null, null, null, null);

        if(cursor.moveToFirst()) {
            do{
                int id = cursor.getInt(6);
                nTotEstoque = nTotEstoque + id;
            }while (cursor.moveToNext());
        }
        db_estoque.close();
        txtTotalEstoque.setText(String.valueOf(nTotEstoque));

        // Botão VOLTAR
        btnVoltar = (Button)findViewById(R.id.btnEstoqEstatisticaVoltar);
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivityEstoqueEstatistica.this, ActivityEstoque.class);
                ActivityEstoqueEstatistica.this.startActivity(i);
                finish();
            }
        });
    }

}