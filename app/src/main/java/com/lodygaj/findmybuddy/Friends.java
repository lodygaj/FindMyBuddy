package com.lodygaj.findmybuddy;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "findmybuddy-mobilehub-1813168738-friends")

public class Friends {
    private String _user;
    private String _friend;

    @DynamoDBHashKey(attributeName = "user")
    @DynamoDBAttribute(attributeName = "user")
    public String getUser() {
        return _user;
    }

    public void setUser(final String _user) {
        this._user = _user;
    }
    @DynamoDBRangeKey(attributeName = "friend")
    @DynamoDBAttribute(attributeName = "friend")
    public String getFriend() {
        return _friend;
    }

    public void setFriend(final String _friend) {
        this._friend = _friend;
    }
}