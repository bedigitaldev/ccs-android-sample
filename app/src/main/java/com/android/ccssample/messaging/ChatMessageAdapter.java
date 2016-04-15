package com.android.ccssample.messaging;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.ccssample.R;
import com.ccs.android.client.utils.Emoji;
import com.ccs.android.client.utils.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class ChatMessageAdapter extends BaseAdapter {

    private static final String TAG = ChatMessageAdapter.class.getSimpleName();
    private static final int TYPE_MESSAGE = 0;
    private static final int TYPE_HINT = 1;
    private static final int TYPE_EMPTY = 2;

    private final Activity activity;
    private String user;
    private ArrayList<MessageItem> messages = new ArrayList<MessageItem>();
    private final int appearanceStyle;
    private String hint;

    public ChatMessageAdapter(Activity activity, String user) {
        this.activity = activity;
        messages = new ArrayList<MessageItem>();
        this.user = user;
        hint = null;
        appearanceStyle = R.style.ChatText_Normal;
        refresh();
    }

    @Override
    public int getCount() {
        return messages.size() + 2;
    }

    @Override
    public Object getItem(int position) {
        if(position == 0) return null;
        else if (position < (messages.size()+1))
            return messages.get(position-1);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 7;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return TYPE_EMPTY;
        } else if (position < (messages.size()+1)) {
            MessageItem item = messages.get(position-1);
            return TYPE_MESSAGE;
        } else {
            return hint == null ? TYPE_EMPTY : TYPE_HINT;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int type = getItemViewType(position);
        View view = null;
        ViewHolderMessage holderMessage = null;
        if (convertView == null) {
            int resource;
            if (type == TYPE_MESSAGE) {
                resource = R.layout.chat_room_message;
            } else if (type == TYPE_HINT) {
                resource = R.layout.chat_room_info;
            } else if (type == TYPE_EMPTY) {
                resource = R.layout.chat_room_empty;
            } else {
                throw new IllegalStateException();
            }

            view = activity.getLayoutInflater().inflate(resource, parent, false);
            if(type == TYPE_MESSAGE){
                holderMessage = new ViewHolderMessage(view);
                convertView = view;
                convertView.setTag(holderMessage);
            }
        } else {
            view = convertView;
            if (type == TYPE_MESSAGE)
                holderMessage = (ViewHolderMessage) convertView.getTag();
        }

        if (type == TYPE_EMPTY)
            return view;

        if (type == TYPE_HINT) {
            TextView textView = ((TextView) view.findViewById(R.id.info));
            textView.setText(hint);
            textView.setTextAppearance(activity, R.style.ChatInfo);
            textView.setVisibility(View.GONE);
            return view;
        }

        String name = "";
        final MessageItem messageItem = (MessageItem) getItem(position);
        final boolean incoming = messageItem.isIncoming();
        Spannable text = messageItem.getText();

        LinearLayout bubble = holderMessage.bubble;
        TextView textView = holderMessage.content;
        textView.setTextAppearance(activity, appearanceStyle);

        String time = Helper.formatSmartTime(messageItem.getTime());
        int messageResource = 0;
        SpannableStringBuilder builder = new SpannableStringBuilder();

        if (!incoming) {
            messageResource = R.drawable.ic_action_done_all_unseen_grey;
            if (messageItem.isError())
                messageResource = R.drawable.ic_alert_error;
            else if (!messageItem.isSent())
                messageResource = R.drawable.ic_action_schedule_grey;
            else if (!messageItem.isDelivered())
                messageResource = R.drawable.ic_action_not_delivered_grey;
            else if(messageItem.isSeen())
                messageResource = R.drawable.ic_action_done_all;
        }

        text = (Spannable) Emoji.replaceEmoji(text);

        if (incoming) {
            name = user;
            if(!name.equals("")) {
                SpannableString nameFormat = new SpannableString(name + "\n");
                nameFormat.setSpan(new UnderlineSpan(), 0, nameFormat.length(), 0);
                append(builder, nameFormat, new TextAppearanceSpan(activity, R.style.ChatInfo));
            }
        }

        Date timeStamp = messageItem.getTime();
        if (timeStamp != null) {
            time = Helper.formatSmartTime(timeStamp);
        }

        if (type == TYPE_MESSAGE) {
            append(builder, text, new TextAppearanceSpan(activity,R.style.ChatRead));
            append(builder, "     ", new TextAppearanceSpan(activity, R.style.ChatTime));
            append(builder, time, new TextAppearanceSpan(activity, R.style.ChatTime));
            if (messageResource != 0) {
                append(builder, " ", new TextAppearanceSpan(activity, R.style.ChatTime));
                int ascent = (int) (-textView.getPaint().ascent());
                Drawable drawable = activity.getResources().getDrawable(messageResource);
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                Drawable d = new BitmapDrawable(Bitmap.createScaledBitmap(bitmap, ascent, ascent, false));
                d.setBounds(0, 0, ascent, ascent);
                ImageSpan imageSpan = new ImageSpan(d, DynamicDrawableSpan.ALIGN_BASELINE);
                append(builder, " ", imageSpan);
            }

            textView.setText(builder);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (incoming) {
            bubble.setBackgroundResource(R.drawable.ic_msg_in);
            textView.setGravity(Gravity.LEFT);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(20, 3, 20, 3);
            bubble.setLayoutParams(params);

            textView.setTextColor(activity.getResources().getColor(R.color.material_grey_800));
            textView.setLinkTextColor(Color.BLACK);
        } else {
            bubble.setBackgroundResource(R.drawable.ic_msg_out);
            textView.setLinkTextColor(Color.WHITE);
            textView.setGravity(Gravity.LEFT);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.setMargins(20, 3, 20, 3);
            bubble.setLayoutParams(params);
        }

        return view;
    }

    public void refresh(){
        Collection<MessageItem> items = ChatMessageCollection.getAllMessageFor(user);
        if(items.size()!=0){
            messages.clear();
            for(MessageItem item: items){
                messages.add(item);
            }
            Collections.sort(messages);
            notifyDataSetChanged();
        }
    }

    private void append(SpannableStringBuilder builder, CharSequence text, CharacterStyle span) {
        int start = builder.length();
        builder.append(text);
        builder.setSpan(span, start, start + text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static class ViewHolderMessage{
        final LinearLayout bubble;
        final TextView content;

        public ViewHolderMessage(View v){
            bubble = (LinearLayout) v.findViewById(R.id.bubble);
            content = (TextView) v.findViewById(R.id.text);
        }
    }

}