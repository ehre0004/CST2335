package com.example.rae.androidlabs;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.rae.androidlabs.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ChatWindow extends Activity {
    private ArrayList<String> chatHistory = new ArrayList<>();
    private ListView chatbox;
    private EditText chattext;
    private Button sendbutt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        chatbox = findViewById(R.id.chatbox);
        chattext = findViewById(R.id.chattext);
        sendbutt = findViewById(R.id.sendbutt);


        final ChatAdapter messageAdapter = new ChatAdapter(this);
        chatbox.setAdapter(messageAdapter);

        sendbutt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatHistory.add(chattext.getText().toString());
                messageAdapter.notifyDataSetChanged(); // restarts the process of getCount & getView()
                chattext.setText("");
            }
        });


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
