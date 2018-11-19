package com.example.rae.androidlabs;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MessageFragment extends Fragment {
    private TextView id;
    private TextView message;
    private Button button;
    private boolean isPhone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View messageview = inflater.inflate(R.layout.message_details_fields, container, false);

        message = messageview.findViewById(R.id.details_message);
        id = messageview.findViewById(R.id.details_id);
        button = messageview.findViewById(R.id.details_button);

        id.setBackgroundColor(getResources().getColor(R.color.white));

        Bundle extras = getArguments();
        message.setText(extras.getString("message", "Message loads here"));
        id.setText(getString(R.string.detailsID)+extras.getLong("id"));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ChatWindow.isPhone()) {
                    Intent intent = new Intent();
                    String idno = id.getText().toString();
                    String putID = idno.substring(idno.indexOf("=")+1);
                    intent.putExtra("id", putID);
                    Activity parentActivityPhone = (MessageDetails) getActivity();
                    parentActivityPhone.setResult(100, intent);
                    parentActivityPhone.finish();
                } else {
                    Activity parentActivityTablet = (ChatWindow) getActivity();
                    String idno = id.getText().toString();
                    String putID = idno.substring(idno.indexOf("=")+1);
                    ((ChatWindow) parentActivityTablet).deleteMessage(Long.parseLong(putID));
                    getFragmentManager().popBackStack();
                }
            }
        });

        return messageview;
    }
}
