package com.example.rae.androidlabs;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    public final static String CLASSNAME = "ChatDatabaseHelper";
    public final static String DATABASE_NAME = "ChatLog";
    public final static String TABLE_NAME = "Chat";
    private static int VERSION_NUM = 1;
    public final static String KEY_ID = "_id";
    public final static String KEY_MESSAGE = "MESSAGE";

    public ChatDatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(CLASSNAME, "Calling onCreate");
        db.execSQL( "CREATE TABLE " + TABLE_NAME
                + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_MESSAGE + " text);" );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(CLASSNAME, "Calling onUpgrade, oldVersion=" + oldVersion + ", newVersion=" + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public int getVersion() {return VERSION_NUM;}
}
