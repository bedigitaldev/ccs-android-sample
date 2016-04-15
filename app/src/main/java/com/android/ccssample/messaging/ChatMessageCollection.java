package com.android.ccssample.messaging;

import com.ccs.android.client.utils.NestedMap;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Mustofa on 10/7/2015.
 */
public class ChatMessageCollection {

    private static NestedMap<MessageItem> maps = new NestedMap<MessageItem>();

    public static void addMessageItem(String user, MessageItem item){
        maps.put(user, item.getMessageID(), item);
    }

    public static void removeMessageItem(String user, String messageId){
        maps.remove(user, messageId);
    }

    public static MessageItem getMessageItem(String user, String messageId){
        return maps.get(user, messageId);
    }

    public static Collection<MessageItem> getAllMessageFor(String user){
        Map<String, MessageItem> items = maps.getNested(user);
        return items.values();
    }

}
