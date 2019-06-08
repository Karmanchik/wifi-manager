package com.alt.karman.wifimanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import com.alt.karman.wifimanager.activity.NetworkPOJO;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class MyDbWorker {

    private static final String SETTING_DB_NAME = "setting.db";

    static public void addNetwork(Context main, String SSID, String password){

        SQLiteDatabase db = createConnect(main);
        Cursor cursor = db.rawQuery("SELECT * FROM AC WHERE ssid='" + SSID + "';", null);
        if (cursor.getCount() > 0)
            db.execSQL("UPDATE AC SET password='" + password + "' WHERE ssid='" + SSID + "';");
        else
            db.execSQL("INSERT INTO AC (ssid, password) VALUES('" + SSID + "', '" + password + "');");
        db.close();
    }

    static public List<NetworkPOJO> getAll(Context c) {
        SQLiteDatabase db = createConnect(c);
        Cursor cursor = db.rawQuery("select * from AC;", null);
        List<NetworkPOJO> result = Collections.EMPTY_LIST;
        while (cursor.moveToNext()) {
            result.add(new NetworkPOJO(
                    cursor.getString(0),
                    cursor.getString(1),
                    ""
            ));
        }
        cursor.close();
        db.close();
        return result;
    }

    static public String getPasswordNetwork(Context main, String SSID){
        SQLiteDatabase db = createConnect(main);
        String passDB = "";
        Cursor myCursor = db.rawQuery("select * from AC where ssid='" + SSID + "';", null);
        while (myCursor.moveToNext()) {
            passDB = myCursor.getString(1);
        }
        myCursor.close();
        db.close();
        if (passDB.equals("")) {
            return "";
        }
        else{
            Toast.makeText(main, passDB, Toast.LENGTH_LONG).show();
            return passDB;
        }
    }

    static private SQLiteDatabase createConnect(Context main){
        return main.openOrCreateDatabase(SETTING_DB_NAME, MODE_PRIVATE, null);
    }

    static public void initDB(Context main){
        SQLiteDatabase db = createConnect(main);
        db.execSQL("CREATE TABLE IF NOT EXISTS AC (ssid VARCHAR(255), password VARCHAR(255));");
        db.close();
    }

}
