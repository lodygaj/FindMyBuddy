package com.lodygaj.findmybuddy;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created on 3/12/2018.
 */
@DynamoDBTable(tableName = "findmybuddy-mobilehub-1813168738-friends")
public class Friends {
    private String _user;
    private String _friend;
    private Boolean _added;

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

    @DynamoDBAttribute(attributeName = "added")
    public Boolean getAdded() {
        return _added;
    }

    public void setAdded(Boolean _added) {
        this._added = _added;
    }
}