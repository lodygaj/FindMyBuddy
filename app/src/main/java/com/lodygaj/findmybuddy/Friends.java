package com.lodygaj.findmybuddy;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

/**
 * Created by Joey Laptop on 8/12/2017.
 */

@DynamoDBTable(tableName = "friends")
public class Friends {
    private String user;
    private String friend;

    @DynamoDBHashKey(attributeName = "user")
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    @DynamoDBRangeKey(attributeName = "friend")
    public String getFriend() {
        return friend;
    }
    public void setFriend(String friend) {
        this.friend = friend;
    }
}
