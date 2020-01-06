package com.example.examenchido2;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class NotaSqliteHelper extends SQLiteOpenHelper {

    //Sentencia SQL para crear la tabla Cars
    String sqlCreate = "CREATE TABLE Note (VIN INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, phn TEXT)";


    public NotaSqliteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int prevVersion, int newVersion) {

        //Se elimina la versión anterior de la tabla
        db.execSQL("DROP TABLE IF EXISTS Note");

        //Se crea la nueva versión de la tabla
        db.execSQL(sqlCreate);
    }
}

