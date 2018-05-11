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
import java.util.List;
import java.util.Random;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

/**
 * Created on 6/29/2017.
 */
public class UserFragment extends Fragment implements RoomListener {
    private Context context;
    private FragmentManager fm;
    private Button btnLastKnown, btnSendRequest;
    private ImageButton btnSendMessage;
    private EditText chatEdtTxt;
    private ListView chatView;
    private String user, friend;
    private TextView txtUser;

    private User selectedUser;

    private DDBHelper ddbHelper;

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
        btnSendMessage = (ImageButton) view.findViewById(R.id.btnSendMessage);
        btnLastKnown = (Button) view.findViewById(R.id.btnLastKnown);
        btnSendRequest = (Button) view.findViewById(R.id.btnLocRequest);

        // Initialize Amazon DynamoDB
        ddbHelper = new DDBHelper();

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

        // Create room name for ScaleDrone chat
        roomName = ddbHelper.getRoomName(user, friend);

        // Populate chat list view from DynamoDB message table
        getMessages();

        // Create user data for ScaleDrone chat
        MemberData data = new MemberData(user, getRandomColor());

        // Initialize ScaleDrone chat
        scaledrone = new Scaledrone(channelID, data);
        scaledrone.connect(new Listener() {
            @Override
            public void onOpen() {
                System.out.println("ScaleDrone connection open");
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
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get message text from text field
                String message = chatEdtTxt.getText().toString();
                if (message.length() > 0) {
                    // Publish message to ScaleDrone API
                    scaledrone.publish(roomName, message);
                    chatEdtTxt.getText().clear();

                    // Submit message to DynamoDB database
                    Message m = new Message(user, friend, message, getTimestamp());
                    ddbHelper.addMessage(m);
                }
            }
        });

        // Called when add friend button is clicked
        btnLastKnown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new AsyncGetLocation(context, friend).execute();

                // Get user info from database
                selectedUser = ddbHelper.getUser(friend);

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
        // Perform user message query
        List<Message> userMessageList = ddbHelper.queryMessages(user, friend);
        // Perform friend message query
        List<Message> friendMessageList = ddbHelper.queryMessages(friend, user);

        // Add paginated lists into one ArrayList
        ArrayList<Message> messageList = new ArrayList<>();
        for(Message m: userMessageList) {
            messageList.add(m);
        }
        for(Message m: friendMessageList) {
            messageList.add(m);
        }

        // Sort messages by timestamp
        if(messageList.size() > 0) {
            Collections.sort(messageList, new Comparator<Message>() {
                @Override
                public int compare(final Message object1, final Message object2) {
                    return object1.getTimestamp().compareTo(object2.getTimestamp());
                }
            });
        }

        // Create user and friend member data
        final MemberData userData = new MemberData(user, getRandomColor());
        final MemberData friendData = new MemberData(friend, getRandomColor());
        MessageData message;

        // Add messages to message adapter and display in listview
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

    // Successfully connected to ScaleDrone room
    @Override
    public void onOpen(Room room) {
        System.out.println("Connected to room");
    }

    // Connecting to ScaleDrone room failed
    @Override
    public void onOpenFailure(Room room, Exception e) {
        System.err.println(e);
    }

    // Received a message from ScaleDrone room
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
}

// Class structure used to hold user data for ScaleDrone chat API
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