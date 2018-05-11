package com.lodygaj.findmybuddy;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created on 3/12/2018.
 */
@DynamoDBTable(tableName = "findmybuddy-mobilehub-1813168738-messages")
public class Message {
    private String user;
    private String friend;
    private String text;
    private String timestamp;

    public Message() {}

    public Message(String user, String friend, String text, String timestamp) {
        this.user = user;
        this.friend = friend;
        this.text = text;
        this.timestamp = timestamp;
    }

    @DynamoDBHashKey(attributeName = "user")
    public String getUser() {
        return user;
    }

    public void setUser(final String _user) {
        this.user = _user;
    }

    @DynamoDBAttribute(attributeName = "friend")
    public String getFriend() {
        return friend;
    }

    public void setFriend(final String _friend) {
        this.friend = _friend;
    }

    @DynamoDBRangeKey(attributeName = "timestamp")
    @DynamoDBAttribute(attributeName = "timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final String _timestamp) {
        this.timestamp = _timestamp;
    }

    @DynamoDBAttribute(attributeName = "text")
    public String getText() {
        return text;
    }

    public void setText(final String _text) {
        this.text = _text;
    }
}