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
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Member;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;

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
    private String user, friend;
    private TextView txtUser;
    private DynamoDBMapper mapper;
    private User selectedUser;

    private MessageAdapter messageAdapter;
    private ListView chatView;

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
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
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

        // Create user data for Scaledrone chat
        MemberData data = new MemberData(user, getRandomColor());

        //TODO
        // Create room name for Scaledrone chat
        //roomName = "observable-" + user + "_" + friend;
        roomName = "observable-room";

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
            final Message message = new Message(json.asText(), data, belongsToCurrentUser);
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

    // Method called to upgrade fragment
    public void setFragment(Fragment fragment) {
        fm.beginTransaction().replace(R.id.fl_content, fragment).addToBackStack(null).commit();
        fm.executePendingTransactions();
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