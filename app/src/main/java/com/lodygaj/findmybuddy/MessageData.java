package com.lodygaj.findmybuddy;

public class MessageData {
    private String text;
    private MemberData data;
    private boolean belongsToCurrentUser;

    public MessageData(String text, MemberData data, boolean belongsToCurrentUser) {
        this.text = text;
        this.data = data;
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public String getText() {
        return text;
    }

    public MemberData getData() {
        return data;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }
}
