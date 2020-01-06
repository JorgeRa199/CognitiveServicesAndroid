package com.example.examenchido2;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Mahadi on 3/7/2018.
 */

public class FrmtContact extends Fragment {

    View v;
    RecyclerView recyclerView;
    public List<Contact> listCont;
    private SQLiteDatabase db;
    private NotaSqliteHelper noteHelper;


    public FrmtContact() {

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.contact_frmt,container,false);
        recyclerView = (RecyclerView) v.findViewById(R.id.contact_recycleView);
        RecycleViewAdapter viewAdapter = new RecycleViewAdapter(getContext(), listCont);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(viewAdapter);

        return v;
    }

     @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listCont = new ArrayList<>();

         noteHelper = new NotaSqliteHelper(getContext(), "DBTest1", null, 1);
         db = noteHelper.getWritableDatabase();
         update();
        Date currentTime = Calendar.getInstance().getTime();
        listCont.add(new Contact("Tarea matematicas", currentTime.toString(), R.drawable.ic_calculator));


    }

    public List<Contact> getAllNotes() {
        // Seleccionamos todos los registros de la tabla Cars
        Cursor cursor = db.rawQuery("select * from Note", null);
        List<Contact> list = new ArrayList<Contact>();

        if (cursor.moveToFirst()) {
            // iteramos sobre el cursor de resultados,
            // y vamos rellenando el array que posteriormente devolveremos
            while (cursor.isAfterLast() == false) {

                int VIN = cursor.getInt(cursor.getColumnIndex("VIN"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String date = cursor.getString(cursor.getColumnIndex("phn"));

                list.add(new Contact(name, date, R.drawable.ic_calculator));
                cursor.moveToNext();
            }
        }
        return list;
    }


    public void update() {
        // borramos todos los elementos
        listCont.clear();
        // cargamos todos los elementos
        listCont.addAll(getAllNotes());
        // refrescamos el adaptador

    }


}
