package com.android.ccssample.messaging;

import android.text.Spannable;

import java.util.Date;

/**
 * Created by Mustofa on 10/6/2015.
 */
public class MessageItem implements Comparable<MessageItem>{
    private String messageID;
    private Spannable text;
    private Date time;
    private Date delay;
    private boolean incoming;
    private boolean read;
    private boolean sent;
    private boolean delivered;
    private boolean error;
    private boolean seen;

    public MessageItem(String messageID, Spannable text, boolean incoming){
        this.messageID = messageID;
        this.text = text;
        this.incoming = incoming;
    }

    public String getMessageID() {
        return messageID;
    }

    public Spannable getText() {
        return text;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getDelay() {
        return delay;
    }

    public void setDelay(Date delay) {
        this.delay = delay;
    }

    @Override
    public int compareTo(MessageItem another) {
        return time.compareTo(another.time);
    }
}
