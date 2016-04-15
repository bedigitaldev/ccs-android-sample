package com.android.ccssample.messaging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.ccssample.BaseActivity;
import com.android.ccssample.R;
import com.ccs.android.client.ErrorType;
import com.ccs.android.client.messaging.common.data.Chat;
import com.ccs.android.client.messaging.common.data.Connection;
import com.ccs.android.client.messaging.common.data.MessageChat;
import com.ccs.android.client.messaging.common.data.MessageChatState;
import com.ccs.android.client.messaging.common.data.MessageFailure;
import com.ccs.android.client.messaging.common.data.MessageReceipt;
import com.ccs.android.client.messaging.common.data.MessageSeen;
import com.ccs.android.client.messaging.common.data.MessageSent;
import com.ccs.android.client.messaging.common.data.MessageStatus;
import com.ccs.android.client.messaging.common.listener.ConnectionChangedListener;
import com.ccs.android.client.messaging.common.listener.ErrorListener;
import com.ccs.android.client.messaging.common.listener.MessageListener;
import com.ccs.android.client.messaging.internal.connection.ConnectionState;
import com.ccs.android.client.utils.SegmentIntentBuilder;

import org.jivesoftware.smackx.ChatState;

import java.util.Collection;
import java.util.Date;

/**
 * Created by Mustofa on 10/2/2015.
 */
public class ChatRoomActivity extends BaseActivity implements MessageListener, ConnectionChangedListener, ErrorListener {

    private static final String TAG = "[[ChatRoomActivity]]";
    private String user = null;
    private ViewHolder holder;
    private ChatMessageAdapter adapter;

    @Override
    public void onReceiveMessage(Chat chat, MessageChat message){
        String user = chat.getUser();
        String text = message.getText().toString();
        String time = message.getTime().toString();

        MessageItem item = newItemAdapter(message);

        /* Send seen message to sender */
        String from = getService().getCcsClient().getConfiguration().getUserId();
        MessageSeen messageSeen = new MessageSeen(item.getMessageID(), from, user);
        ccsClient.getMessageClient().send(messageSeen);
        item.setSeen(true);

    }

    @Override
    public void onReceiveMessageStatus(Chat chat, MessageStatus message){

        String user = chat.getUser();
        String log = "s: ";
        MessageItem item = ChatMessageCollection.getMessageItem(user, message.getMessageStatusId());

        if(item == null) return;

        if(message.getStatus() == MessageStatus.SENT){
            MessageSent messageSent = (MessageSent) message;
            item.setSent(true);
            log += "[sent] "+messageSent.getMessageStatusId();
        }

        if(message.getStatus() == MessageStatus.DELIVERED){
            MessageReceipt messageReceipt = (MessageReceipt) message;
            item.setDelivered(true);
            log += "[receipt] "+messageReceipt.getMessageStatusId();
        }

        if(message.getStatus() == MessageStatus.SEEN){
            MessageSeen messageSeen = (MessageSeen) message;
            item.setSeen(true);
            log += "[seen] "+messageSeen.getMessageStatusId();
        }

        if(message.getStatus() == MessageStatus.FAILURE){
            MessageFailure messageFailure = (MessageFailure) message;
            item.setError(true);
            log += "[failure] "+messageFailure.getMessageStatusId();
        }

        adapter.notifyDataSetChanged();

    }

    class MyMessageListener implements MessageListener {
        @Override
        public void onReceiveMessage(Chat chat, MessageChat message) {

        }

        @Override
        public void onReceiveMessageStatus(Chat chat, MessageStatus message) {

        }

        @Override
        public void onReceiveMessageChatState(Chat chat, MessageChatState message) {

        }
    }

    @Override
    public void onReceiveMessageChatState(Chat chat, MessageChatState message){
        String user = chat.getUser();
        ChatState state = message.getState();

        CharSequence statusText = "";
        if (state == ChatState.composing)
            statusText = "Composing...";
        else if (state == ChatState.paused)
            statusText = "Paused...";
        else if(state == ChatState.gone)
            statusText = "Gone...";
        else if(state == ChatState.active)
            statusText = "";

        if(statusText.length()!=0) {
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setText(statusText);
        }else{
            holder.status.setVisibility(View.GONE);
        }

        holder.status.invalidate();
    }

    @Override
    public void onConnectionChanged(Connection connection){

        String message = "";

        if(connection.getState() == ConnectionState.connecting){
            message = "Connecting...";
        }

        if(connection.getState() == ConnectionState.connected){
            message = "Connected";
        }

        if(connection.getState() == ConnectionState.authorized){
            message = "Authorized";
        }

        if(connection.getState() == ConnectionState.disconnected){
            message = "Disconnected";
        }

        holder.alert.setVisibility(View.VISIBLE);
        holder.alertText.setText(message);

    }

    @Override
    public void onError(ErrorType errorType){
        String message = "Unknown error type";
        if(errorType == ErrorType.UNAUTHORIZED){
            message = "Unauthorized account";
        }
        holder.alert.setVisibility(View.VISIBLE);
        holder.alertText.setText(message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = SegmentIntentBuilder.getSegment(getIntent(), 0);

        View view = getLayoutInflater().inflate(R.layout.activity_chat_room, null, false);
        setContentView(view);

        adapter = new ChatMessageAdapter(this, user);
        holder = new ViewHolder(view);
        holder.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send();
                holder.input.setText("");
            }
        });
        holder.name.setText("User " + user);
        holder.input.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String message = s.toString().trim();
                message = message.replaceAll("\n\n+", "\n\n");
                message = message.replaceAll(" +", " ");
                holder.send.setEnabled(message.length() != 0);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (ccsClient != null)
                    ccsClient.getMessageClient().onComposing(user, s);
            }
        });
        holder.alertIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.alert.setVisibility(View.GONE);
            }
        });
        holder.list.setAdapter(adapter);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        /* Register IM listener */
        ccsClient.addListener(ConnectionChangedListener.class, this);
        ccsClient.addListener(MessageListener.class, this);

        sendSeenPacket();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ccsClient.removeListener(ConnectionChangedListener.class, this);
        ccsClient.removeListener(MessageListener.class, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ccsClient.getMessageClient().onGone(user);
    }

    private void send(){
        String from = getService().getCcsClient().getConfiguration().getUserId();
        String to = user;
        String text = holder.input.getText().toString();

        /* Call SDK to send message */
        MessageChat message = new MessageChat(from, to, text);
        getService().getCcsClient().getMessageClient().send(message);
        /* ======================== */

        newItemAdapter(message);
    }

    private void sendSeenPacket(){
        Collection<MessageItem> items = ChatMessageCollection.getAllMessageFor(user);
        String from = getService().getCcsClient().getConfiguration().getUserId();
        if(items.size()!=0){
            for(MessageItem item: items){
                if(!item.isSeen() && item.isIncoming()){
                    MessageSeen messageSeen = new MessageSeen(item.getMessageID(), from, user);
                    ccsClient.getMessageClient().send(messageSeen);
                    item.setSeen(true);
                }
            }
        }
    }

    public static Intent createIntent(Context context, String user){
        SegmentIntentBuilder sintent = new SegmentIntentBuilder<>(context, ChatRoomActivity.class);
        sintent.addSegment(user);
        return sintent.build();
    }

    private MessageItem newItemAdapter(MessageChat message){
        MessageItem item = new MessageItem(message.getId(), message.getText(), message.isIncoming());
        Date now = (message.getTime()==null)?new Date():message.getTime();
        item.setTime(now);
        item.setSent(false);
        item.setDelivered(false);
        item.setSeen(false);
        item.setError(false);
        ChatMessageCollection.addMessageItem(user, item);
        adapter.refresh();
        return item;
    }

    private static class ViewHolder {

        final TextView name;
        final TextView status;
        final ImageView avatar;
        final ImageButton send;
        final EditText input;
        final ListView list;
        final View alert;
        final TextView alertText;
        final ImageView alertIcon;

        public ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.name);
            status = (TextView) view.findViewById(R.id.status_text);
            avatar = (ImageView) view.findViewById(R.id.avatar);
            send = (ImageButton) view.findViewById(R.id.chat_send);
            input = (EditText) view.findViewById(R.id.chat_input);
            list = (ListView) view.findViewById(R.id.chat_list);
            alert = view.findViewById(R.id.alert_panel);
            alertText = (TextView) view.findViewById(R.id.alert_text);
            alertIcon = (ImageView) view.findViewById(R.id.alert_icon);
        }

    }
}
