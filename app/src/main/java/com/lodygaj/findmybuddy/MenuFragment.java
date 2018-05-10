package com.lodygaj.findmybuddy;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {
    private Context context;
    private FragmentManager fm;
    public String[] contacts;
    public ContactAdapter cAdapter;
    public ListView contactListView;
    public EditText edtTxtFriend;
    public Button btnAddFriend;
    public String user, friend;
    private DynamoDBMapper mapper;
    private Friends selectedFriend;
    private PaginatedQueryList<Friends> result;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        context = getActivity().getApplicationContext();
        fm = getFragmentManager();

        // Get objects from layout
        contactListView = (ListView) view.findViewById(R.id.listViewContacts);
        edtTxtFriend = (EditText) view.findViewById(R.id.editTextFriend);
        btnAddFriend = (Button) view.findViewById(R.id.buttonAddFriend);

        // Get current user from SharedPreferences
        user = SaveSharedPreference.getUserName(context);

        // Initialize Amazon DynamoDB client
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.mapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        // Create contact list adapter
        cAdapter = new ContactAdapter(context, contacts);

        // Retrieve list of friends from database
        cAdapter.contacts = getUserFriends(user);
        //new AsyncGetFriends(context, user).execute();

        // Attach adapter to list view
        contactListView.setAdapter(cAdapter);

        // Set listener to determine what happens when item in list is clicked
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get selected user from list view
                friend = (String) contactListView.getItemAtPosition(position);

                // Add friend data to bundle
                Bundle b = new Bundle();
                b.putString("Friend", friend);

                // Load User Fragment
                UserFragment userFragment = new UserFragment();
                userFragment.setArguments(b); // set bundle
                setFragment(userFragment);
            }
        });

        // Set listener to determine what happens when item in list is long clicked
        contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                PopupMenu pm = new PopupMenu(getContext(), view);
                Menu menu = pm.getMenu();
                menu.add("Delete");
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Get selected user
                        friend = (String) contactListView.getItemAtPosition(position);

                        // Delete from database
                        //new AsyncDeleteFriend(context, user, friend).execute();
                        selectedFriend = new Friends();

                        Runnable runnable = new Runnable() {
                            public void run() {
                                // Delete user/friend record from database
                                selectedFriend.setUser(user);
                                selectedFriend.setFriend(friend);
                                mapper.delete(selectedFriend);

                                // Delete friend/user record from database
                                selectedFriend.setUser(friend);
                                selectedFriend.setFriend(user);
                                mapper.delete(selectedFriend);
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

                        // Toast user to show successful deletion
                        Toast.makeText(context, "Friend deleted!", Toast.LENGTH_LONG).show();

                        // Refresh friends list to remove deleted user
                        cAdapter.contacts = getUserFriends(user);
                        cAdapter.notifyDataSetChanged();

                        return true;
                    }
                });
                pm.show();
                return true;
            }
        });

        // Called when add friend button is clicked
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close keyboard
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                String friend = edtTxtFriend.getText().toString();
                if(!friend.equals("")) {
                    //new AsyncAddFriend(context, user, friend).execute();
                    addFriend(friend);
                    edtTxtFriend.setText("");
                } else {
                    Toast.makeText(context, "Must enter a name!", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    public String[] getUserFriends(final String user) {
        // Set hash key values
        Friends friendsToFind = new Friends();
        friendsToFind.setUser(user);

        // Create query with hash key values
        final DynamoDBQueryExpression query = new DynamoDBQueryExpression()
                .withHashKeyValues(friendsToFind)
                .withConsistentRead(false);

        // Run query and get result from database
        Runnable runnable = new Runnable() {
            public void run() {
                result = mapper.query(Friends.class, query);
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

        // Add query results to array list of friends
        String[] friends = new String[result.size()];
        int index = 0;
        for(Friends friend: result) {
            friends[index++] = friend.getFriend();
        }

        return friends;
    }

    public void addFriend(final String friend) {
        // Add friendship to database
        Runnable runnable = new Runnable() {
            public void run() {
                Friends friends = new Friends();
                // Add user/friend item
                friends.setUser(user);
                friends.setFriend(friend);
                friends.setAdded(true);
                mapper.save(friends);
                // Add friend/user item
                friends.setUser(friend);
                friends.setFriend(user);
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

        Toast.makeText(context, "Friend added!", Toast.LENGTH_LONG).show();
        contactListView.setVisibility(View.VISIBLE);

        // Update list of friends from database
        cAdapter.contacts = getUserFriends(user);
        // Refresh list view to show new friend
        cAdapter.notifyDataSetChanged();
    }

    // Method called to upgrade fragment
    public void setFragment(Fragment fragment) {
        fm.beginTransaction().replace(R.id.fl_content, fragment).addToBackStack(null).commit();
        fm.executePendingTransactions();
    }
}