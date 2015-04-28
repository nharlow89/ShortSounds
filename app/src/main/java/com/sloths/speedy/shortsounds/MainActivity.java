package com.sloths.speedy.shortsounds;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {
    private String[] mShortSounds;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private List<ShortSound> sounds;
    // TODO: Current sound could be implemented differntly, mockup done this way
    private ShortSound currSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //jbuscher git test comment
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("DB_TEST", "MainActivity:onCreate()");
        sounds = ShortSound.getAll();
        Log.d("DB_TEST", sounds.toString());

        // Drawer Layout Stuff:
        // This array can later be an array of actual short sounds (or connection from string->obj)
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
        // TODO: change to v7, currently deprecated
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

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
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
            // TODO: Before integration w/ Joel's code this while suffice for currSound
            currSound = sounds.get(position);
            selectItem(position);
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        // Grabs the ShortSound and populates the screen with it
        Fragment fragment = new ShortSoundFragment();

        // Sets it to the correct ShortSound
        Bundle args = new Bundle();
        args.putInt(ShortSoundFragment.ARG_SOUND_NUMBER, position);
        fragment.setArguments(args);

        // Replaces the main content screen w/ Short sound
        FragmentManager fragmentManager = getFragmentManager();
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

            ExpandableListView rootView = (ExpandableListView) inflater.inflate(R.layout.short_sound_exp_view, container, false);
            // Set title
            int i = getArguments().getInt(ARG_SOUND_NUMBER);
            String sound = getResources().getStringArray(R.array.shortsounds_array)[i];
            getActivity().setTitle(sound);
            // Populate array of tracks
            // TODO: This will be updated based upon shortSound, but for now it is static
            List<String> listDataHeader =
                    Arrays.asList(getResources().getStringArray(R.array.track_array));
            HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();
            for (String s : listDataHeader) {
                List<String> list = new ArrayList<String>();
                list.add(getResources().getString(R.string.fake_track));
                listDataChild.put(s, list);
            }

            ExpandableListAdapter listAdapter = new ExpandableListAdapter(this.getActivity(), listDataHeader, listDataChild);
            rootView.setAdapter(listAdapter);
            return rootView;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * Swaps card view w/ our track content
     */
    private void selectTrack(int position) {
        // Grabs the ShortSound and populates the screen with it

        // Create fragment for tracks
        Fragment trackFragment = new TrackEffectsPanelFragment(currSound.getTracks().get(position));

        // Replaces the main content screen w/ Short sound
        FragmentManager fragmentManager = getFragmentManager();
        // TODO: replace current track item with this fragment
        fragmentManager.beginTransaction().replace(R.id.content_frame, trackFragment).commit();
    }


    // ---------------------------------------------------------------
    // Don't know if this stuff is needed, it's copied code
    // ---------------------------------------------------------------
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
