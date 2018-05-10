package com.lodygaj.findmybuddy;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Member;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

/**
 * Created by Joey Laptop on 6/29/2017.
 */
public class UserFragment extends Fragment implements RoomListener {
    private Context context;
    private FragmentManager fm;
    private Button btnLastKnown, btnSendRequest;
    private ImageButton btnSendmessage;
    private EditText chatEdtTxt;
    private ListView chatView;
    private String user, friend;
    private TextView txtUser;

    private User selectedUser;

    private AmazonDynamoDBClient dynamoDBClient;
    private DynamoDBMapper mapper;

    private MessageAdapter messageAdapter;
    private String channelID = "RAr1NhVD6XnYNC95";
    private String roomName;
    private Scaledrone scaledrone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        context = getActivity().getApplicationContext();
        fm = getFragmentManager();

        // Get objects from layout
        txtUser = (TextView) view.findViewById(R.id.txtUsername);
        chatView = (ListView) view.findViewById(R.id.chat_view);
        chatEdtTxt = (EditText) view.findViewById(R.id.chatEdtTxt);
        btnSendmessage = (ImageButton) view.findViewById(R.id.btnSendMessage);
        btnLastKnown = (Button) view.findViewById(R.id.btnLastKnown);
        btnSendRequest = (Button) view.findViewById(R.id.btnLocRequest);

        // Initialize Amazon DynamoDB client
        dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.mapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        // Get current user from shared preferences
        user = SaveSharedPreference.getUserName(context);

        // Get friend data from bundle
        Bundle b = this.getArguments();
        if(b != null){
            friend = b.getString("Friend");
        }

        //  Set friend title
        txtUser.setText(friend);

        // Set up chat list view adapter
        messageAdapter = new MessageAdapter(context);
        chatView.setAdapter(messageAdapter);

        // Create room name for Scaledrone chat
        getRoomName();

        // Populate chat list view from DynamoDB message table
        getMessages();

        // Create user data for Scaledrone chat
        MemberData data = new MemberData(user, getRandomColor());

        // Initialize Scaledrone chat
        scaledrone = new Scaledrone(channelID, data);
        scaledrone.connect(new Listener() {
            @Override
            public void onOpen() {
                System.out.println("Scaledrone connection open");
                scaledrone.subscribe(roomName, UserFragment.this);
            }

            @Override
            public void onOpenFailure(Exception ex) {
                System.err.println(ex);
            }

            @Override
            public void onFailure(Exception ex) {
                System.err.println(ex);
            }

            @Override
            public void onClosed(String reason) {
                System.err.println(reason);
            }
        });

        // Called when send message button is clicked
        btnSendmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = chatEdtTxt.getText().toString();
                if (message.length() > 0) {
                    scaledrone.publish(roomName, message);
                    chatEdtTxt.getText().clear();
                }

                // Submit message to DynamoDB database
                final Message newMessage = new Message(user, friend, message, getTimestamp());
                Runnable runnable = new Runnable() {
                    public void run() {
                        mapper.save(newMessage);
                    }
                };
                Thread myThread = new Thread(runnable);
                myThread.start();
            }
        });

        // Called when add friend button is clicked
        btnLastKnown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new AsyncGetLocation(context, friend).execute();

                // Get user info from database
                Runnable runnable = new Runnable() {
                    public void run() {
                        selectedUser = mapper.load(User.class, friend);
                    }
                };
                Thread mythread = new Thread(runnable);
                mythread.start();
                // Wait for thread to complete
                try {
                    mythread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Add user data to bundle
                Bundle b = new Bundle();
                b.putString("Friend", selectedUser.getUsername());
                b.putDouble("Latitude", selectedUser.getLatitude());
                b.putDouble("Longitude", selectedUser.getLongitude());
                b.putString("Time", selectedUser.getTimestamp());

                // Set map fragment
                MapFragment mapFragment = new MapFragment();
                mapFragment.setArguments(b); // set bundle
                setFragment(mapFragment);
            }
        });

        // Called when send location request button is clicked
        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO




            }
        });

        return view;
    }

    // Retrieves chat messages from DynamoDb and populates list view
    public void getMessages() {
        // Build query to get user messages sent to friend
        Message userMessage = new Message();
        userMessage.setUser(user);

        Map<String, AttributeValue> friendAttribute = new HashMap<>();
        friendAttribute.put(":friend", new AttributeValue(friend));

        final DynamoDBQueryExpression<Message> userQueryExpression = new DynamoDBQueryExpression<Message>()
                .withHashKeyValues(userMessage)
                .withFilterExpression("friend = :friend")
                .withExpressionAttributeValues(friendAttribute);

        // Build query to get friend messages sent to user
        Message friendMessage = new Message();
        friendMessage.setUser(friend);

        Map<String, AttributeValue> userAttribute = new HashMap<>();
        userAttribute.put(":friend", new AttributeValue(user));

        final DynamoDBQueryExpression<Message> friendQueryExpression = new DynamoDBQueryExpression<Message>()
                .withHashKeyValues(friendMessage)
                .withFilterExpression("friend = :friend")
                .withExpressionAttributeValues(userAttribute);

        // Query messages from database
        Runnable runnable = new Runnable() {
            public void run() {
                // Perform user message query
                List<Message> userMessageList = mapper.query(Message.class, userQueryExpression);
                // Perform friend message query
                List<Message> friendMessageList = mapper.query(Message.class, friendQueryExpression);

                // Add to lists together and sort by timestamp
                ArrayList<Message> messageList = new ArrayList<>();
                for(Message m: userMessageList) {
                    messageList.add(m);
                }
                for(Message m: friendMessageList) {
                    messageList.add(m);
                }

                if(messageList.size() > 0) {
                    Collections.sort(messageList, new Comparator<Message>() {
                        @Override
                        public int compare(final Message object1, final Message object2) {
                            return object1.getTimestamp().compareTo(object2.getTimestamp());
                        }
                    });
                }

                final MemberData userData = new MemberData(user, getRandomColor());
                final MemberData friendData = new MemberData(friend, getRandomColor());
                MessageData message;

                for(Message m: messageList) {
                    if(m.getUser().equals(user)) {
                        message = new MessageData(m.getText(), userData, true);
                    } else {
                        message = new MessageData(m.getText(), friendData, false);
                    }

                    messageAdapter.add(message);
                    chatView.setSelection(chatView.getCount() - 1);
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
    }

    // Get current timestamp
    public String getTimestamp() {
        Long tsLong = System.currentTimeMillis() / 1000;
        return tsLong.toString();
    }

    // Method called to upgrade fragment
    public void setFragment(Fragment fragment) {
        fm.beginTransaction().replace(R.id.fl_content, fragment).addToBackStack(null).commit();
        fm.executePendingTransactions();
    }

    // Successfully connected to Scaledrone room
    @Override
    public void onOpen(Room room) {
        System.out.println("Connected to room");
    }

    // Connecting to Scaledrone room failed
    @Override
    public void onOpenFailure(Room room, Exception e) {
        System.err.println(e);
    }

    // Received a message from Scaledrone room
    @Override
    public void onMessage(Room room, final JsonNode json, final Member member) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final MemberData data = mapper.treeToValue(member.getClientData(), MemberData.class);
            boolean belongsToCurrentUser = member.getId().equals(scaledrone.getClientID());
            final MessageData message = new MessageData(json.asText(), data, belongsToCurrentUser);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageAdapter.add(message);
                    chatView.setSelection(chatView.getCount() - 1);
                }
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // Function used to return a random hex color value
    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }

    // Create unique ScaleDrone chat room name for user and friend
    public void getRoomName() {
        // Get room info from database
        Runnable runnable = new Runnable() {
            public void run() {
                Friends friendToFind = mapper.load(Friends.class, user, friend);
                if(friendToFind.getAdded()) {
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
    }
}

// Class structure used to hold user data for Scaledrone chat API
class MemberData {
    private String name;
    private String color;

    public MemberData(String name, String color) {
        this.name = name;
        this.color = color;
    }

    // Add an empty constructor so we can later parse JSON into MemberData using Jackson
    public MemberData() {
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "MemberData{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}