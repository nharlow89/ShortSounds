package com.sloths.speedy.shortsounds.view;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.support.v4.view.MenuItemCompat;
import android.content.Intent;
import android.widget.SeekBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.AudioRecorder;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

import java.io.File;
import java.util.List;



public class MainActivity extends FragmentActivity implements NoticeDialogFragment.NoticeDialogListener{
    private String[] mShortSoundsTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private List<ShortSound> sounds;
    private ImageButton mGlobalPlayButton;
    private ShareActionProvider mShareActionProvider;
    private int position;
    private ShortSound mActiveShortSound;
    private AudioRecorder mAudioRecorder;
    private FloatingActionButtonBasicFragment mActionBarFragment;
    private RecyclerViewFragment mMainFragment;
    private SeekBar mGlobalSeekBar;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setUpFloatingActionButton();
        } else {
            setUpRecordButton();
        }
        position = -1;
    }

    /**
     * This sets up the Global Play button and attaches the default click
     * handler. Note that when no ShortSound is loaded, this button should
     * be invisible
     */
    private void setUpGlobalPlayButton() {
        mGlobalPlayButton = (ImageButton) findViewById(R.id.imageButtonPlay);
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
                        hideRecordATrackTextView();
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
                        hideRecordATrackTextView();
                    } else {
                        beginRecording();
                    }
                }
            });
        }
    }

    /**
     * Checks if there are currently any tracks, if so will hide hide the record a
     * sound textview
     */
    private void hideRecordATrackTextView() {
        if (mActiveShortSound.getTracks().size() > 0) {
            TextView recordASound = (TextView) findViewById(R.id.textRecordASound);
            if (recordASound != null) recordASound.setVisibility(View.INVISIBLE);
        }
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
        if (position == -1) {
            menu.findItem(R.id.action_rename).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_share).setVisible(false);
        } else {
            menu.findItem(R.id.action_rename).setVisible(!drawerOpen);
            menu.findItem(R.id.action_delete).setVisible(!drawerOpen);
            menu.findItem(R.id.action_share).setVisible(!drawerOpen);
        }
        menu.findItem(R.id.action_new).setVisible(!drawerOpen);

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
        mActiveShortSound = sounds.get( position );  // Set the currently active ShortSound.
        // Grabs the ShortSound and populates the screen with it
        mMainFragment = new RecyclerViewFragment();
        // Sets it to the correct ShortSound
        mMainFragment.setDataSource( mActiveShortSound );
//        Bundle args = new Bundle();
//        long targetShortSoundId = sounds.get( position ).getId();
//        args.putLong(RecyclerViewFragment.ARG_SOUND_ID, targetShortSoundId);
//        mMainFragment.setArguments(args);

        // Replaces the main content screen w/ Short sound
        FragmentManager fragmentManager = this.getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.track_list, mMainFragment).commit();

        // Highlight item, update title, close drawer
        mDrawerList.setItemChecked(position, true);

        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(mShortSoundsTitles[position]);
        this.position = position;
        invalidateOptionsMenu();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    // ---------------------------------------------------------------
    // Don't know if this stuff is needed, it's copied code
    // ---------------------------------------------------------------
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
                return true;
            case R.id.action_new:
                //TODO: Add new shortsound functionality
                return true;
            case R.id.action_rename:
                //TODO: Add renaming functionality
                rename();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void rename() {
        FragmentManager fragmentManager = getFragmentManager();
        NoticeDialogFragment inputNameDialog = new NoticeDialogFragment();
        inputNameDialog.show(fragmentManager, "Input Dialog");
    }

    @Override
    public void onOkay(String inputText) {
        sounds.get(position).setTitle(inputText);
        mShortSoundsTitles[position] = inputText;
        setTitle(inputText);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mShortSoundsTitles));
    }

    /** Begins the recording process. */
    private void beginRecording() {
        // Play back other tracks if there are other tracks
        if (mActiveShortSound != null) {
            mActiveShortSound.playAllTracks();
        }

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
            ArrayAdapter drawerListAdapter = (ArrayAdapter) mDrawerList.getAdapter();
            drawerListAdapter.notifyDataSetChanged();
        } else {
            mActiveShortSound.stopAllTracks();
        }
        Log.d("DEBUG", "Finished Recording new track to ShortSound["+mActiveShortSound.getId()+"]");
        // Create the new ShortSoundTrack (that this will record to)
        ShortSoundTrack newTrack = new ShortSoundTrack( recordedFile, mActiveShortSound.getId() );
        mActiveShortSound.addTrack( newTrack );

        if ( isNewShortSound ) {
            // Select the new ShortSound to be active.
            selectShortSoundFromDrawer(sounds.size() - 1);
        } else {
            // Update the existing fragment manager to add new track to list
            mMainFragment.notifyTrackAdded( mActiveShortSound.getTracks().size() - 1 );
        }
        mAudioRecorder.reset();  // Have to reset for the next recording
    }
}