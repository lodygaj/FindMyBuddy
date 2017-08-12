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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import static android.R.id.list;

public class MenuFragment extends Fragment {
    private Context context;
    private FragmentManager fm;
    public String[] contacts;
    public ContactAdapter cAdapter;
    public ListView contactListView;
    public EditText edtTxtFriend;
    public Button btnAddFriend;
    public String user, friend;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        context = getActivity().getApplicationContext();
        fm = getFragmentManager();

        // Initialize objects from layout
        contactListView = (ListView) view.findViewById(R.id.listViewContacts);
        edtTxtFriend = (EditText) view.findViewById(R.id.editTextFriend);
        btnAddFriend = (Button) view.findViewById(R.id.buttonAddFriend);

        // Retrieve current user from LoginActivity
        user = SaveSharedPreference.getUserName(context);

        contacts = new String[] {"Loading..."};

        // Create contact list adapter
        cAdapter = new ContactAdapter(context, contacts);
        // Attach adapter to list view
        contactListView.setAdapter(cAdapter);
        // Set listener to determine what happens when item in list is clicked
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get selected user
                friend = (String) contactListView.getItemAtPosition(position);

                if(!friend.equals("Loading...")) {
                    // Add friend data to bundle
                    Bundle b = new Bundle();
                    b.putString("Friend", friend);

                    // Load User Fragment
                    UserFragment userFragment = new UserFragment();
                    userFragment.setArguments(b); // set bundle
                    setFragment(userFragment);
                }
            }
        });

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
                        new AsyncDeleteFriend(context, user, friend).execute();

                        return true;
                    }
                });
                pm.show();
                return true;
            }
        });

        // Get and update list of contacts to display in list view
        new AsyncGetFriends(context, user).execute();

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
                    new AsyncAddFriend(context, user, friend).execute();
                    edtTxtFriend.setText("");
                } else {
                    Toast.makeText(context, "Must enter a name!", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    // Method called to upgrade fragment
    public void setFragment(Fragment fragment) {
        fm.beginTransaction().replace(R.id.fl_content, fragment).addToBackStack(null).commit();
        fm.executePendingTransactions();
    }

    /**
     * Class used for retrieving friends from database and storing them in arraylist
     */
    public class AsyncGetFriends extends AsyncTask<String, Void, String> {
        private Context context;
        private String user;
        private String parameters;
        private final String serverURL = "http://jlodyga.com/server/getFriends.php";

        public AsyncGetFriends(Context context, String user) {
            this.context = context;
            this.user = user;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... arg0) {
            //android.os.Debug.waitForDebugger();
            parameters = "user=" + user;
            try {
                URL url = new URL(serverURL);
                URLConnection con = url.openConnection();

                con.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());

                wr.write(parameters);
                wr.flush();
                wr.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";

                while((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                String result = sb.toString();
                return result;
            } catch(Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        public void onPostExecute(String array) {
            //android.os.Debug.waitForDebugger();
            try {
                // Create JSON array from input value
                JSONArray jArray = new JSONArray(array);
                // Create final array to be returned
                String[] friends = new String[jArray.length()];
                // Add each object from JSON Array to final array
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject object = jArray.getJSONObject(i);
                    friends[i] = object.getString(Integer.toString(i));
                }

                if(friends[0].equals("0")) {
                    contactListView.setVisibility(View.GONE);
                } else {
                    // Set contacts
                    cAdapter.contacts = friends;
                    cAdapter.notifyDataSetChanged();
                }
                // Set contacts
                cAdapter.contacts = friends;
                cAdapter.notifyDataSetChanged();
            } catch(Exception e) {
                System.out.println("Exception: " + e.getMessage());
                cAdapter.contacts[0] = "server error";
                cAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Class used for adding new friends to database
     */
    public class AsyncAddFriend extends AsyncTask<String, Void, String> {
        private String parameters;
        private Context context;
        private String user, friend;
        private final String serverURL = "http://jlodyga.com/server/addFriend.php";

        public AsyncAddFriend(Context context, String user, String friend) {
            this.context = context;
            this.user = user;
            this.friend = friend;
        }

        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... arg0) {
            //android.os.Debug.waitForDebugger();
            parameters = "user=" + user + "&friend=" + friend;
            try {
                URL url = new URL(serverURL);
                URLConnection con = url.openConnection();

                con.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());

                wr.write(parameters);
                wr.flush();
                wr.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";

                while((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                String result = sb.toString();
                return result;
            } catch(Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        public void onPostExecute(String value) {
            //android.os.Debug.waitForDebugger();
            if(value.equals("2")) {
                Toast.makeText(context, "Friend does not exist!", Toast.LENGTH_LONG).show();
            } else if(value.equals("1")) {
                Toast.makeText(context, "Friend added successfully!", Toast.LENGTH_LONG).show();
                new AsyncGetFriends(context, user).execute(); // refresh friends list
                contactListView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(context, "Friend already exists!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Class used for deleting friends from database
     */
    public class AsyncDeleteFriend extends AsyncTask<String, Void, String> {
        private String parameters;
        private Context context;
        private String user, friend;
        private final String serverURL = "http://jlodyga.com/server/deleteFriend.php";

        public AsyncDeleteFriend(Context context, String user, String friend) {
            this.context = context;
            this.user = user;
            this.friend = friend;
        }

        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... arg0) {
            //android.os.Debug.waitForDebugger();
            parameters = "user=" + user + "&friend=" + friend;
            try {
                URL url = new URL(serverURL);
                URLConnection con = url.openConnection();

                con.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());

                wr.write(parameters);
                wr.flush();
                wr.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";

                while((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                String result = sb.toString();
                return result;
            } catch(Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        public void onPostExecute(String value) {
            //android.os.Debug.waitForDebugger();
            if(value.equals("1")) {
                Toast.makeText(context, "Friend deleted!", Toast.LENGTH_LONG).show();
                new AsyncGetFriends(context, user).execute(); // refresh friends list
                contactListView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(context, "Unable to delete!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
