package com.sloths.speedy.shortsounds.view;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ViewAnimator;

import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.ShortSound;

import java.util.List;
import java.util.Map;


public class MainActivity extends FragmentActivity {
    public static final String EQ = "EQ";
    public static final String REVERB = "Reverb";
    public static final String BIT = "Bit Crush";
    public static final String DIST = "Distortion";
    public static final String TRACKS = "tracks";

    private String[] mShortSoundsTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private List<ShortSound> sounds;
    private ImageButton mGlobalPlayButton;
    private Map<String, View> fragMap;
    private String currentFragment;
    private ViewAnimator animator;
    Animation slideLeft, slideRight;
    private ShortSound mActiveShortSound;


    private EffectFragment eqFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("DB_TEST", "MainActivity:onCreate()");
        sounds = ShortSound.getAll();
        Log.d("DB_TEST", sounds.toString());
        mShortSoundsTitles = getShortSoundTitles(sounds);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        setUpGlobalPlayButton();
        setUpLibraryDrawer();
        enableActionBarLibraryToggleButton();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setUpFloatingActionButton();
        }
//        setUpFragments();


        animator = (ViewAnimator) findViewById(R.id.view_animator);
        slideLeft = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideRight = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        animator.setInAnimation(slideLeft);
        animator.setOutAnimation(slideRight);

    }

    public ShortSound getCurShortSound() {
        return mActiveShortSound;
    }

//    private void setUpFragments() {
//        fragMap = new HashMap<>();
//        String[] arr = {EQ, DIST, REVERB, BIT};
//        for (String s : arr) {
//            EffectFragment f = new EffectFragment();
//            f.setName(s);
//            fragMap.put(s, f);
//        }
//    }

    /**
     * This sets up the Global Play button and attaches the default click
     * handler. Note that when no ShortSound is loaded, this button should
     * be disabled.
     */
    private void setUpGlobalPlayButton() {
        mGlobalPlayButton = (ImageButton) findViewById(R.id.imageButtonPlay);
        Log.e("DEBUG", mGlobalPlayButton.toString());
        mGlobalPlayButton.setEnabled(false);  // Default to disabled when ShortSound has not been clicked.
    }

    /**
     * Sets up the floating action button used as record button. Will
     * only be called for Android SDK >= LOLLIPOP
     */
    private void setUpFloatingActionButton() {
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        FloatingActionButtonBasicFragment fragment = new FloatingActionButtonBasicFragment();
        transaction.replace(R.id.sample_content_fragment, fragment);
        transaction.commit();
    }

    /**
     * Takes a list of short sounds and creates an String array of the titles
     * @param sounds list of shortsounds
     * @return string[] of titles
     */
    private String[] getShortSoundTitles(List<ShortSound> sounds) {
        String[] titles = new String[sounds.size()];
        for(int i = 0; i < sounds.size(); i++)
            titles[i] = sounds.get(i).getTitle();
        return titles;
    }

    /**
     * Enables the action bar icon for the nav drawer that opens the library.
     */
    private void enableActionBarLibraryToggleButton() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    /**
     * Provides logic for setting up the library drawer.
     */
    private void setUpLibraryDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        //  ==> This connects the listview to the actual sounds
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mShortSoundsTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mTitle = mDrawerTitle = getTitle();

        // ActionBarDrawerToggle ties together drawer to action bar
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
//                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
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
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectShortSoundFromDrawer(position);
        }
    }

    /**
     * Helper method for the the DrawerItemClickListener. When a drawer item is clicked
     * its position is passed in as a parameter which determines the short sound to load
     * into a recyclerViewFragment, which is then inflated into the UI.
     * @param position int the position of the drawer item clicked
     */
    private void selectShortSoundFromDrawer(int position) {
        // Grabs the ShortSound and populates the screen with it
//        View fragment = new RecyclerView();
        // Sets it to the correct ShortSound
//        Bundle args = new Bundle();
//        long targetShortSoundId = sounds.get( position ).getId();
//        args.putLong(TrackView.ARG_SOUND_ID, targetShortSoundId);
//        fragment.setArguments(args);

        // Replaces the main content screen w/ Short sound
//        FragmentManager fragmentManager = this.getSupportFragmentManager();
//        fragmentManager.beginTransaction().replace(R.id.animator, fragment).commit();
//        fragMap.put(TRACKS, fragment);
        mActiveShortSound = sounds.get(position);
        animator.setDisplayedChild(0);
//        ((TrackView) findViewById(R.id.track_view)).setShortSound(sounds.get(position).getId());

        currentFragment = TRACKS;

        // Highlight item, update title, close drawer
        mDrawerList.setItemChecked(position, true);

        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(mShortSoundsTitles[position]);
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

    // This is used for loading the popup when clicking a specific effect
    public void effectEditSelected(int track, String effect) {
//        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        if (effect.equals(EQ)) {
            animator.setDisplayedChild(1);
//            tr.replace(R.id.animator, fragMap.get(EQ)).commit();
//            currentFragment = EQ;
        } else if (effect.equals("Reverb")) {
            animator.setDisplayedChild(2);
//            loadReverbEffectDialog(track, effect);
        } else {
//            loadGeneralEffectDialog(track, effect);
        }
    }
}