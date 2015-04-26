package com.sloths.speedy.shortsounds;

import android.app.Activity;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.content.res.Configuration;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


// TODO: 1) Populate pieces for skeleton (String names etc.)
//       2) Run application and get a good look on what's happening
//            -Populate other aspects as see fit
//       3) Add ExpandableListView to FrameLayout in activity_main.xml
//       4) Populate in java code the expandable list view w/ tracks
//       5) Possibly figure out how to expand track w/ a fake effect (just a String)

public class MainActivity extends FragmentActivity {
    private String[] mShortSounds;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpLibraryDrawer();
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        //setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        RecyclerView recList = (RecyclerView) findViewById(R.id.track_list);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
    }

    private void setUpLibraryDrawer() {
        mShortSounds = getResources().getStringArray(R.array.shortsounds_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        //  ==> This connects the listview to the actual sounds
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mShortSounds));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mTitle = mDrawerTitle = getTitle();

        // ActionBarDrawerToggle ties together drawer to action bar
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        // Grabs the ShortSound and populates the screen with it
        RecyclerViewFragment fragment = new RecyclerViewFragment();

        // Sets it to the correct ShortSound
        Bundle args = new Bundle();
        args.putInt(RecyclerViewFragment.ARG_SOUND_NUMBER, position);
        fragment.setArguments(args);

        // Replaces the main content screen w/ Short sound
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // Highlight item, update title, close drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mShortSounds[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }


    /**
     * Fragment that appears in the "content_frame", shows a current ShortSound
     * This will display
     */
    public static class ShortSoundFragment extends Fragment {
        public static final String ARG_SOUND_NUMBER = "sound_number";

        public ShortSoundFragment() {
            // Empty constructor required for fragment subclasses
        }

        // Creates the view to put in to the main screen
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            /*
            ExpandableListView rootView = (ExpandableListView) inflater.inflate(R.layout.short_sound_exp_view, container, false);
            // Set title
            int i = getArguments().getInt(ARG_SOUND_NUMBER);
            String sound = getResources().getStringArray(R.array.shortsounds_array)[i];
            getActivity().setTitle(sound);
            // Populate array of tracks
            List<String> listDataHeader =
                    Arrays.asList(getResources().getStringArray(R.array.track_array));
            HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();
            for (String s : listDataHeader) {
                List<String> list = new ArrayList<String>();
                list.add(getResources().getString(R.string.fake_track));
                listDataChild.put(s, list);
            }

            ExpandableListAdapter listAdapter = new ExpandableListAdapter(this.getActivity(), listDataHeader, listDataChild);
            rootView.setAdapter(listAdapter);*/
            return null;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
}
