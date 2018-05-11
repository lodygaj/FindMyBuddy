package com.lodygaj.findmybuddy;

import android.content.Context;
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

/**
 * Created on 6/29/2017.
 */
public class MenuFragment extends Fragment {
    private Context context;
    private FragmentManager fm;

    public String[] contacts;
    public ContactAdapter cAdapter;

    public ListView contactListView;
    public EditText edtTxtFriend;
    public Button btnAddFriend;
    public String user, friend;

    private DDBHelper ddbHelper;

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
        ddbHelper = new DDBHelper();

        // Create contact list adapter
        cAdapter = new ContactAdapter(context, contacts);

        // Retrieve list of friends from database
        cAdapter.contacts = ddbHelper.getFriendsList(user);;
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
                        ddbHelper.deleteFriend(user, friend);

                        // Toast user to show successful deletion
                        Toast.makeText(context, "Friend deleted!", Toast.LENGTH_LONG).show();

                        // Refresh friends list to remove deleted user
                        cAdapter.contacts = ddbHelper.getFriendsList(user);;
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

    public void addFriend(String friend) {
        // Add friendship to friends table
        ddbHelper.addFriend(user, friend);

        Toast.makeText(context, "Friend added!", Toast.LENGTH_LONG).show();
        contactListView.setVisibility(View.VISIBLE);

        // Update list of friends from database
        cAdapter.contacts = ddbHelper.getFriendsList(user);;
        // Refresh list view to show new friend
        cAdapter.notifyDataSetChanged();
    }

    // Method called to upgrade fragment
    public void setFragment(Fragment fragment) {
        fm.beginTransaction().replace(R.id.fl_content, fragment).addToBackStack(null).commit();
        fm.executePendingTransactions();
    }
}