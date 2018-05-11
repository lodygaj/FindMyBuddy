package com.lodygaj.findmybuddy;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 5/10/2018.
 */
public class DDBHelper {

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper mapper;

    private User user;
    private String roomName;
    private List<Message> messageList;
    private PaginatedQueryList<Friends> result;

    public DDBHelper() {
        // Initialize Amazon DynamoDB client
        dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.mapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
    }

    /**********************************************
     * MESSAGES TABLE
     *********************************************/

    // Submit new message to Message Table
    public void addMessage(Message param1) {
        final Message message = param1;
        Runnable runnable = new Runnable() {
            public void run() {
                mapper.save(message);
            }
        };
        Thread myThread = new Thread(runnable);
        myThread.start();

        // Wait for thread to complete
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Query for messages by user and friend
    public List<Message> queryMessages(String param1, String param2) {
        final String user = param1;
        final String friend = param2;

        // Query messages from database
        Runnable runnable = new Runnable() {
            public void run() {
                // Build query to get friend messages sent to user
                Message message = new Message();
                message.setUser(user);

                Map<String, AttributeValue> attributeValue = new HashMap<>();
                attributeValue.put(":friend", new AttributeValue(friend));

                final DynamoDBQueryExpression<Message> queryExpression = new DynamoDBQueryExpression<Message>()
                        .withHashKeyValues(message)
                        .withFilterExpression("friend = :friend")
                        .withExpressionAttributeValues(attributeValue);

                messageList = mapper.query(Message.class, queryExpression);

            }
        };
        Thread myThread = new Thread(runnable);
        myThread.start();

        // Wait for thread to complete
        try {
        myThread.join();
        } catch (InterruptedException e) {
        e.printStackTrace();
        }

        return messageList;
    }

    /**********************************************
     * USERS TABLE
     *********************************************/

    // Add new user to users table
    public void addUser(User param1) {
        user = param1;

        Runnable runnable = new Runnable() {
            public void run() {
                mapper.save(user);
            }
        };
        Thread myThread = new Thread(runnable);
        myThread.start();

        // Wait for thread to complete
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Load user from users table
    public User getUser(String param1) {
        final String username = param1;

        Runnable runnable = new Runnable() {
            public void run() {
                user = mapper.load(User.class, username);
            }
        };
        Thread myThread = new Thread(runnable);
        myThread.start();

        // Wait for thread to complete
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return user;
    }

    // Update users location in users table
    public void setUserLocation(User param1) {
        final User newUser = param1;

        // Update user in database
        Runnable runnable = new Runnable() {
            public void run() {
                user = mapper.load(User.class, newUser.getUsername());
                user.setLatitude(newUser.getLatitude());
                user.setLongitude(newUser.getLongitude());
                user.setTimestamp(newUser.getTimestamp());
                mapper.save(user);
            }
        };
        Thread myThread = new Thread(runnable);
        myThread.start();

        // Wait for thread to complete
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**********************************************
     * FRIENDS TABLE
     *********************************************/

    // Create unique ScaleDrone chat room name for user and friend
    public String getRoomName(String param1, String param2) {
        final String user = param1;
        final String friend = param2;

        // Get user/friend relationship info from friends table
        Runnable runnable = new Runnable() {
            public void run() {
                Friends friendship = mapper.load(Friends.class, user, friend);
                if(friendship.getAdded()) {
                    roomName = "observable-" + user + friend;
                } else {
                    roomName = "observable-" + friend + user;
                }
            }
        };
        Thread myThread = new Thread(runnable);
        myThread.start();

        // Wait for thread to complete
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return roomName;
    }

    public void deleteFriend(String param1, String param2) {
        final String username = param1;
        final String friend = param2;

        Runnable runnable = new Runnable() {
            public void run() {
                Friends friendship = new Friends();

                // Delete user/friend record from database
                friendship.setUser(username);
                friendship.setFriend(friend);
                mapper.delete(friendship);

                // Delete friend/user record from database
                friendship.setUser(friend);
                friendship.setFriend(username);
                mapper.delete(friendship);
            }
        };
        Thread myThread = new Thread(runnable);
        myThread.start();

        // Wait for thread to complete
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addFriend(String param1, String param2) {
        final String username = param1;
        final String friend = param2;

        // Add friendship to database
        Runnable runnable = new Runnable() {
            public void run() {
                Friends friends = new Friends();

                // Add user/friend item
                friends.setUser(username);
                friends.setFriend(friend);
                friends.setAdded(true);
                mapper.save(friends);

                // Add friend/user item
                friends.setUser(friend);
                friends.setFriend(username);
                friends.setAdded(false);
                mapper.save(friends);
            }
        };
        Thread myThread = new Thread(runnable);
        myThread.start();

        // Wait for thread to complete
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String[] getFriendsList(String param1) {
        // Set hash key values
        Friends friendship = new Friends();
        friendship.setUser(param1);

        // Create query with hash key values
        final DynamoDBQueryExpression query = new DynamoDBQueryExpression()
                .withHashKeyValues(friendship)
                .withConsistentRead(false);

        // Run query and get result from database
        Runnable runnable = new Runnable() {
            public void run() {
                result = mapper.query(Friends.class, query);
            }
        };
        Thread myThread = new Thread(runnable);
        myThread.start();

        // Wait for thread to complete
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Add query results to array list of friends
        String[] friends = new String[result.size()];
        int index = 0;
        for(Friends friend: result) {
            friends[index++] = friend.getFriend();
        }

        return friends;
    }
}
