package com.stuhorner.drawingsample;

/**
 * Created by Stu on 6/22/2016.
 */
public class ChatRow {
    String UID;
    String name;
    String subtitle;
    boolean newMessage;
    int chatIcon;

    public ChatRow(String UID) {
        this.UID = UID;
        this.name = "Artery User";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setNewMessage(boolean newMessage) {
        this.newMessage = newMessage;
    }

    public boolean isNewMessage() {
        return newMessage;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public int getChatIcon() {
        return chatIcon;
    }

    public void setChatIcon(int chatIcon) {
        this.chatIcon = chatIcon;
    }
}
