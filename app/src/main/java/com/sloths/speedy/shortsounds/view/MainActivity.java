package com.sloths.speedy.shortsounds.view;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;
import android.content.Intent;
import android.widget.SeekBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.AudioRecorder;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends FragmentActivity implements NoticeDialogFragment.NoticeDialogListener {
    public static final String EQ = "EQ";
    public static final String REVERB = "Reverb";
    public static final String BIT = "Bit Crush";
    public static final String DIST = "Distortion";
    public static final String TRACKS = "tracks";

    public static final int SLIDE_DURATION = 400;

    private String[] mShortSoundsTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private List<ShortSound> sounds;
    private ImageButton mGlobalPlayButton;
    private Map<String, Integer> viewMap;
    private String currentView;
    private ViewAnimator animator;
    private Animation slideLeft;
    private Animation slideRight;
    private ShareActionProvider mShareActionProvider;
    private ShortSound mActiveShortSound;
    private AudioRecorder mAudioRecorder;
    private FloatingActionButtonBasicFragment mActionBarFragment;
    private SeekBar mGlobalSeekBar;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("DB_TEST", "MainActivity:onCreate()");
        sounds = ShortSound.getAll();
        Log.d("DB_TEST", sounds.toString());
        mShortSoundsTitles = getShortSoundTitles(sounds);
        mAudioRecorder = new AudioRecorder( getCacheDir() );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpGlobalPlayButton();
        setUpGlobalSeekBar();
        setUpLibraryDrawer();
        enableActionBarLibraryToggleButton();
        setUpViews();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setUpFloatingActionButton();
        } else {
            setUpRecordButton();
        }
        position = -1;
    }

    /**
     * Now that we have a selected ShortSound in focus we need to update the Global Play
     * button's click handler to play all tracks associated with this ShortSound.
     */
    private void setGlobalPlayButtonClickHandler() {
        mGlobalPlayButton = (ImageButton)findViewById(R.id.imageButtonPlay);
        mGlobalPlayButton.setVisibility(View.VISIBLE);
        Log.d("DEBUG", "Found the global play button! " + mGlobalPlayButton);
        mGlobalPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActiveShortSound != null) {
                    // TODO: we need to handle the case when the ShortSound finishes playing!
                    if (mActiveShortSound.isPlaying()) {
                        // The ShortSound is already playing, stop it.
                        mActiveShortSound.pauseAllTracks();
                        mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
                    } else {
                        if (mActiveShortSound.isPaused()) {
                            // The ShortSound was previously playing, unpause it.
                            mActiveShortSound.unPauseAllTracks();
                        } else {
                            // The ShortSound is not playing yet, play it.
                            mActiveShortSound.playAllTracks();
                        }
                        mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
                    }
                }
            }
        });
    }

    /**
     * This sets up the Global Play button and attaches the default click
     * handler. Note that when no ShortSound is loaded, this button should
     * be invisible
     */
    private void setUpGlobalPlayButton() {
        mGlobalPlayButton = (ImageButton) findViewById(R.id.imageButtonPlay);
        Log.e("DEBUG", mGlobalPlayButton.toString());
        mGlobalPlayButton.setVisibility(View.INVISIBLE);  // Default to invisible when ShortSound has not been clicked.
    }

    /**
     * This sets up the Global SeekBar. Note that when no ShortSound is loaded, this SeekBar should
     * be invisible
     */
    private void setUpGlobalSeekBar() {
        mGlobalSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mGlobalSeekBar.setVisibility(View.INVISIBLE);  // Default to invisible when ShortSound has not been clicked.
    }

    /**
     * Now that we have a selected ShortSound in focus we need to update the global seek bar to be
     * visible.
     */
    private void enableFunctionalityOfGlobalSeekBar() {
        mGlobalSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mGlobalSeekBar.setVisibility(View.VISIBLE);
    }

    /**
     * This sets up the Record button and attaches the click handler which gives it the record
     * functionality.
     */
    private void setUpRecordButton() {
        // Looks a little ugly, but we have to account for the FAB because it uses a different view
        // element.
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            FloatingActionButton button = mActionBarFragment.getActionButton();
            button.setOnCheckedChangeListener(new FloatingActionButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(FloatingActionButton fabView, boolean isChecked) {
                    if ( !isChecked ) {
                        endRecording();
                    } else {
                        beginRecording();
                    }
                }
            });
        } else {
            ImageButton recordButton = (ImageButton) findViewById(R.id.imageButtonRecord);
            recordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAudioRecorder.isRecording()) {
                        endRecording();
                    } else {
                        beginRecording();
                    }
                }
            });
        }
    }

    public ShortSound getCurShortSound() {
        return mActiveShortSound;
    }

    /**
     * Sets up the floating action button used as record button. Will
     * only be called for Android SDK >= LOLLIPOP
     */
    private void setUpFloatingActionButton() {
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        mActionBarFragment = new FloatingActionButtonBasicFragment();
        mActionBarFragment.setOnLoadListener( new FloatingActionButtonBasicFragment.OnFragmentLoadedListener() {
            @Override
            public void didLoad() {
                setUpRecordButton();
            }
        });
        transaction.replace(R.id.sample_content_fragment, mActionBarFragment);
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
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
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
        boolean shortSoundSelected = mActiveShortSound != null;
        menu.findItem(R.id.action_rename).setVisible(!drawerOpen && shortSoundSelected);
        menu.findItem(R.id.action_delete).setVisible(!drawerOpen && shortSoundSelected);
        menu.findItem(R.id.action_share).setVisible(!drawerOpen && shortSoundSelected);
        menu.findItem(R.id.action_new).setVisible(!drawerOpen && shortSoundSelected);

        return super.onPrepareOptionsMenu(menu);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectShortSoundFromDrawer(position);
        }
    }

    private void setUpViews() {
        viewMap = new HashMap<>();
        viewMap.put(TRACKS, 0);
        viewMap.put(EQ, 1);
        viewMap.put(REVERB, 2);
        viewMap.put(BIT, 3);
        viewMap.put(DIST, 4);

        animator = (ViewAnimator) findViewById(R.id.view_animator);
//        slideLeft = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
//        slideRight = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
//        slideLeft.setDuration(SLIDE_DURATION);
//        slideRight.setDuration(SLIDE_DURATION);
        animator.setInAnimation(inFromRightAnimation());
        animator.setOutAnimation(outToLeftAnimation());

        ((TextView)animator.getChildAt(viewMap.get(BIT)).findViewById(R.id.effectNameTitle)).setText(BIT);
        ((TextView)animator.getChildAt(viewMap.get(DIST)).findViewById(R.id.effectNameTitle)).setText(DIST);
    }

    /**
     * Helper method for the the DrawerItemClickListener. When a drawer item is clicked
     * its position is passed in as a parameter which determines the short sound to load
     * into a recyclerViewFragment, which is then inflated into the UI.
     * @param position int the position of the drawer item clicked
     */
    private void selectShortSoundFromDrawer(int position) {
        mActiveShortSound = sounds.get(position);  // Set the currently active ShortSound.
        setGlobalPlayButtonClickHandler();
        enableFunctionalityOfGlobalSeekBar();
        if (this.position != position) {
            currentView = TRACKS;

            // Highlight item, update title, close drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
            setTitle(mShortSoundsTitles[position]);
            this.position = position;

            // load the mix into a view and replace it in the animator
            TrackView tv = (TrackView) findViewById(R.id.track_list);
            View add = getLayoutInflater().inflate(R.layout.track_list_xml, tv, false);
            animator.addView(add, viewMap.get(TRACKS) + 1);
            animator.setDisplayedChild(viewMap.get(TRACKS) + 1);
            animator.removeViewAt(viewMap.get(TRACKS));
            invalidateOptionsMenu();
        } else {
            // selected mix is already loaded so cloase the drawer
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    public void onBackPressed() {

        if (currentView.equals(TRACKS)) {
            super.onBackPressed();
        } else {
            Animation in = animator.getInAnimation();
            Animation out = animator.getOutAnimation();
            animator.setInAnimation(inFromLeftAnimation());
            animator.setOutAnimation(outToRightAnimation());
            animator.setDisplayedChild(viewMap.get(TRACKS));
            animator.setInAnimation(in);
            animator.setOutAnimation(out);
            currentView = TRACKS;
        }
    }

    private Animation inFromRightAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(SLIDE_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }


    private Animation outToRightAnimation() {
        Animation outtoRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoRight.setDuration(SLIDE_DURATION);
        outtoRight.setInterpolator(new AccelerateInterpolator());
        return outtoRight;
    }
    private Animation inFromLeftAnimation() {
        Animation inFromLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromLeft.setDuration(SLIDE_DURATION);
        inFromLeft.setInterpolator(new AccelerateInterpolator());
        return inFromLeft;
    }

    private Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoLeft.setDuration(SLIDE_DURATION);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }

    // This is used for loading the popup when clicking a specific effect
    public void effectEditSelected(int track, String effect) {
        animator.setDisplayedChild(viewMap.get(effect));
        currentView = effect;
        mActiveShortSound = sounds.get( position );  // Set the currently active ShortSound.
        // Highlight item, update title, close drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(mShortSoundsTitles[position]);
        invalidateOptionsMenu();
    }

    /*
     * Sets the Title on the action bar to the parameter title
     */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /*
     * Creates the Action Bar Options Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem shareItem = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) shareItem.getActionProvider();

        mShareActionProvider.setShareIntent(createShareIntent());

        return true;
    }

    /*
     * Specifies the share intent
     */
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        File absolutePath = new File(getFilesDir(), "ss1-track1.mp3");
        Uri contentURI = FileProvider.getUriForFile(MainActivity.this, "com.sloths.speedy.shortsounds.fileprovider", absolutePath);

        if (contentURI != null) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentURI);
            shareIntent.setType("audio/mpeg3");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        return shareIntent;
    }

    /*
     * Determines what to do when a button is pressed on the menu bar
     * based on what the MenuItem that is passed in as a parameter is
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_delete:
                //TODO: Add delete functionality
                deleteShortSound();
                return true;
            case R.id.action_new:
                //TODO: Add new shortsound functionality
                createNew();
                return true;
            case R.id.action_rename:
                //TODO: Don't allow only white space
                rename();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
 
    }

    /*
     * Deletes a ShortSound from the library.
     */
    private void deleteShortSound() {
        if (mActiveShortSound != null) {
            // Delete sound from database.
            // Reset library
            // Return to start screen
        }
    }

    /*
     * Creates a new ShortSound and takes it to an empty screen with no
     * current tracks for this ShortSound
     */
    private void createNew() {
        ShortSound newSound = new ShortSound();
        setTitle(newSound.getTitle());
        sounds.add(newSound);
        mActiveShortSound = newSound;
        mShortSoundsTitles = getShortSoundTitles(ShortSound.getAll());
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mShortSoundsTitles));
        selectShortSoundFromDrawer(sounds.size() - 1);
        // hides seek bar and play button
        setUpGlobalSeekBar();
        setUpGlobalPlayButton();
        // TODO: made record a sound text view visible
    }

    /*
     * Renames a ShortSound
     */
    private void rename() {
        FragmentManager fragmentManager = getFragmentManager();
        NoticeDialogFragment inputNameDialog = new NoticeDialogFragment();
        inputNameDialog.show(fragmentManager, "Input Dialog");
    }

    /*
     * Changes the ShortSound name to inputText if
     * inputText is not only whitespace or the empty string
     */
    @Override
    public void onOkay(String inputText) {
        if (!inputText.matches("\\s+") && !inputText.equals("")) {
            mActiveShortSound.setTitle(inputText);
            mShortSoundsTitles = getShortSoundTitles(ShortSound.getAll());
            setTitle(inputText);
            mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                    R.layout.drawer_list_item, mShortSoundsTitles));
        }
    }

    /** Begins the recording process. */
    private void beginRecording() {
        // Play back other tracks if there are other tracks
        if (mActiveShortSound != null)
            mActiveShortSound.playAllTracks();
        // Setup the MediaRecorder
        mAudioRecorder.start();
    }

    /** Ends the recording process. */
    private void endRecording() {
        File recordedFile = mAudioRecorder.end();
        Log.d("DEBUG", "endRecording() recordedFile: " + recordedFile.getAbsolutePath());

        boolean isNewShortSound = mActiveShortSound == null;
        if ( isNewShortSound ) {
            // Case 1. There is no active ShortSound, create one and continue.
            // Create the new ShortSound and add it the list.
            mActiveShortSound = new ShortSound();
            sounds.add( mActiveShortSound );
            // Update the sidebar with the new ShortSound.
            mShortSoundsTitles = getShortSoundTitles(sounds);
            mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                    R.layout.drawer_list_item, mShortSoundsTitles));
        } else {
            mActiveShortSound.stopAllTracks();
        }
        Log.d("DEBUG", "Finished Recording new track to ShortSound[" + mActiveShortSound.getId() + "]");
        // Create the new ShortSoundTrack (that this will record to)
        ShortSoundTrack newTrack = new ShortSoundTrack( recordedFile, mActiveShortSound.getId() );
        mActiveShortSound.addTrack(newTrack);

        if ( isNewShortSound ) {
            // Select the new ShortSound to be active.
            selectShortSoundFromDrawer(sounds.size() - 1);
        } else {
            // Update the existing fragment manager to add new track to list
            ((TrackView) findViewById(R.id.track_list)).notifyTrackAdded(mActiveShortSound.getTracks().size() - 1);
        }
        mAudioRecorder.reset();  // Have to reset for the next recording
    }
}