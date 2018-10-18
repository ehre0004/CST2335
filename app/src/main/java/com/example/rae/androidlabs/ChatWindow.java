package com.example.rae.androidlabs;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatWindow extends Activity {
    protected static final String ACTIVITY_NAME = "ChatWindow";
    private ArrayList<String> chatHistory = new ArrayList<>();
    private static ListView chatbox;
    private static EditText chattext;
    private static Button sendbutt;
    private ChatDatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        // initializing variables
        chatbox = findViewById(R.id.chatbox);
        chattext = findViewById(R.id.chattext);
        sendbutt = findViewById(R.id.sendbutt);

        // DATABASE & CURSOR
        // this.deleteDatabase(ChatDatabaseHelper.DATABASE_NAME); <- this deletes any former databases if you need it
        dbHelper = new ChatDatabaseHelper(this.getApplicationContext());
        db = dbHelper.getWritableDatabase();
        Cursor c = db.rawQuery("select * from "+ ChatDatabaseHelper.TABLE_NAME +" where ? not null",
                new String[]{ChatDatabaseHelper.KEY_MESSAGE});

        // Cursor moving through messages
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Log.i(ACTIVITY_NAME, "SQL MESSAGE: " + c.getString(c.getColumnIndex(ChatDatabaseHelper.KEY_MESSAGE)));
            String msg = c.getString(c.getColumnIndex(ChatDatabaseHelper.KEY_MESSAGE));
            chatHistory.add(msg);
            c.moveToNext();
        }

        // Log messages about cursor
        Log.i(ACTIVITY_NAME, "Cursor's column count = " + c.getColumnCount());
        for (int i = 0; i < c.getColumnCount(); i++) {
            Log.i(ACTIVITY_NAME, "Column name: " + c.getColumnName(i));
        }

        // chatadapter, chat button
        final ChatAdapter messageAdapter = new ChatAdapter(this);
        final ContentValues cv = new ContentValues();
        chatbox.setAdapter(messageAdapter);
        sendbutt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get chat message and add to array list (chat history)
                String message = chattext.getText().toString();
                chatHistory.add(message);

                // insert into DATABASE. use ContentValues and SQLiteDatabase
                cv.put(ChatDatabaseHelper.KEY_MESSAGE, message);
                db.insert(ChatDatabaseHelper.TABLE_NAME, ChatDatabaseHelper.KEY_MESSAGE, cv);

                // update screens and chattext back to blank
                messageAdapter.notifyDataSetChanged(); // restarts the process of getCount & getView()
                chattext.setText("");
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        dbHelper.close();
    }

    // inner class ChatWindow
    private class ChatAdapter extends ArrayAdapter<String> {
        public ChatAdapter(Context context) {
            super(context, 0);
        }

        public int getCount(){
            return chatHistory.size();
        }

        public String getItem(int position) {
            return chatHistory.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ChatWindow.this.getLayoutInflater();
            View result = null;
            if (position%2 == 0) { // even row
                result = inflater.inflate(R.layout.chat_row_incoming, null);
            } else {
                result = inflater.inflate(R.layout.chat_row_outgoing, null);
            }

            TextView message = (TextView) result.findViewById(R.id.messageText);
            message.setText(getItem(position));  // getting string at position
            return result;
        }

        public long getItemId(int position) {
            return position;
        }
    }
}
