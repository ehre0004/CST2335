package com.example.rae.androidlabs;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChatWindow extends Activity {
    protected static final String ACTIVITY_NAME = "ChatWindow";
    private ArrayList<String> chatHistory = new ArrayList<>();
    private ListView chatbox;
    private EditText chattext;
    private Button sendbutt;
    private FrameLayout frame;
    private Fragment mFragment;
    protected static boolean phoneLayout = true;
    private ChatDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor c;
    ChatAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        Log.i(ACTIVITY_NAME, "in onCreate()");

        // initializing variables
        chatbox = findViewById(R.id.chatbox);
        chattext = findViewById(R.id.chattext);
        sendbutt = findViewById(R.id.sendbutt);
        frame = findViewById(R.id.chat_frame);
        phoneLayout = (frame == null) ? true : false;

        // DATABASE & CURSOR
//        this.deleteDatabase(ChatDatabaseHelper.DATABASE_NAME); // delete database if needed
        dbHelper = new ChatDatabaseHelper(this.getApplicationContext());
        db = dbHelper.getWritableDatabase();
        queryDB();

        // Log messages about cursor
        Log.i(ACTIVITY_NAME, "Cursor's column count = " + c.getColumnCount());
        for (int i = 0; i < c.getColumnCount(); i++) {
            Log.i(ACTIVITY_NAME, "Column name: " + c.getColumnName(i));
        }

        // chatadapter, chat button
        messageAdapter = new ChatAdapter(this);
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
                queryDB();
                messageAdapter.notifyDataSetChanged(); // restarts the process of getCount & getView()
                chattext.setText("");
            }
        });

        // load fragment when list item / chat item clicked on
        chatbox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // save message and id to pass to fragment
                Bundle saveDetails = new Bundle();
                Log.i(ACTIVITY_NAME, "in setOnItemClickListener... long id="+id);
                saveDetails.putLong("id", id);
                String message = messageAdapter.getItem(position);
                saveDetails.putString("message", message);

                if (phoneLayout) {
                    // if phone mode, start activity MessageDetails and pass bundle
                    Intent intent = new Intent(ChatWindow.this, MessageDetails.class);
                    intent.putExtras(saveDetails);
                    startActivityForResult(intent, 0);
                } else {
                    // create fragment and transaction
                    mFragment = new MessageFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();

                    // pass bundle and fragment
                    getFragmentManager().popBackStack();
                    mFragment.setArguments(saveDetails);
                    transaction.add(R.id.chat_frame, mFragment).addToBackStack(null).commit();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        dbHelper.close();
        c.close();
    }

    protected void queryDB() {
        String query = "select " + ChatDatabaseHelper.KEY_ID + ", " + ChatDatabaseHelper.KEY_MESSAGE + " from " + ChatDatabaseHelper.TABLE_NAME;
        c = db.rawQuery(query, null);
        // or alternatively:
        // Cursor b = db.query(true, ChatDatabaseHelper.TABLE_NAME, new String[]{ChatDatabaseHelper.KEY_MESSAGE}, "where ? not null", new String[]{ChatDatabaseHelper.KEY_MESSAGE}, null, null, null, null)

        // Cursor moving through messages
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Log.i(ACTIVITY_NAME, "SQL MESSAGE: " + c.getString(c.getColumnIndex(ChatDatabaseHelper.KEY_MESSAGE)));
            String msg = c.getString(c.getColumnIndex(ChatDatabaseHelper.KEY_MESSAGE));
            chatHistory.add(msg);
            c.moveToNext();
        }
    }

    protected void deleteMessage(long id) {
        db.delete("Chat", ChatDatabaseHelper.KEY_ID+" = ?", new String[] {id+""});
        chatHistory.clear();

        Log.i(ACTIVITY_NAME, "in deleteMessage(): deleted message");
        Toast.makeText(this, "Successfully deleted message", Toast.LENGTH_LONG).show();

        // new cursor
        queryDB();

        messageAdapter.notifyDataSetChanged();
    }

    public static boolean isPhone() {
        return phoneLayout;
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent data) {
        Log.i(ACTIVITY_NAME, "in onActivityResult");
        if (responseCode == 100) {
            Bundle extras = data.getExtras();

            Long theID = Long.parseLong(extras.getString("id"));
            Log.i(ACTIVITY_NAME, "id="+theID);

            deleteMessage(theID);
        }
    }

    // inner class ChatWindow
    private class ChatAdapter extends ArrayAdapter<String> {
        public ChatAdapter(Context context) {
            super(context, 0);
        }

        public int getCount(){
            return c.getCount();
        }

        public String getItem(int position) {
            c.moveToPosition(position);
            return c.getString(c.getColumnIndex(ChatDatabaseHelper.KEY_MESSAGE));
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
            c.moveToPosition(position);
            return c.getLong(c.getColumnIndex(ChatDatabaseHelper.KEY_ID));
        }
    }
}
