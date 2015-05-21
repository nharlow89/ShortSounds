package com.sloths.speedy.shortsounds.view;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import android.widget.SeekBar;
import android.widget.ShareActionProvider;
import com.sloths.speedy.shortsounds.controller.ModelControl;
import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.model.AudioPlayer;
import com.sloths.speedy.shortsounds.model.AudioRecorder;
import com.sloths.speedy.shortsounds.model.EqEffect;
import com.sloths.speedy.shortsounds.model.ReverbEffect;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The MainActivity for the application. Contains setup for the framework of the UI.
 */
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
    private ShareActionProvider mShareActionProvider;
    private ShortSound mActiveShortSound;
    private FloatingActionButtonBasicFragment mActionBarFragment;
    private SeekBar mGlobalSeekBar;
    private int position;
    private ModelControl modelControl;

    /**
     * Sets up MainActivity
     * @param savedInstanceState the Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DB_TEST", "MainActivity:onCreate()");
        sounds = ShortSound.getAll();
        Log.d("DB_TEST", sounds.toString());
        mShortSoundsTitles = getShortSoundTitles(sounds);
        modelControl = ModelControl.instance();
        final AudioRecorder mAudioRecorder = new AudioRecorder( getCacheDir() );
        modelControl.setmAudioRecorder(mAudioRecorder);
        setContentView(R.layout.activity_main);
        setUpGlobalPlayButton();
        setUpGlobalSeekBar();
        setUpLibraryDrawer();
        enableActionBarLibraryToggleButton();
        setUpAnimatorViews();
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
        mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
        Log.d("DEBUG", "Found the global play button! " + mGlobalPlayButton);
        mGlobalPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !modelControl.onPlayToggle() )
                    mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
                else
                    mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
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
        mGlobalSeekBar.setMax(100);  // Set the max value (0-100)
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
                        showSeekBarAndPlayButton();
                    } else {
                        mGlobalPlayButton.setEnabled(false);
                        modelControl.onRecordStart();
                    }
                }
            });
        } else {
            ImageButton recordButton = (ImageButton) findViewById(R.id.imageButtonRecord);
            recordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (modelControl.isRecording()) {
                        endRecording();
                        showSeekBarAndPlayButton();
                    } else {
                        mGlobalPlayButton.setEnabled(false);
                        modelControl.onRecordStart();
                    }
                }
            });
        }
    }

    /**
     * Shows the global seek bar and play button
     */
    private void showSeekBarAndPlayButton() {
        if (mGlobalPlayButton.getVisibility() == View.INVISIBLE) {
            mGlobalPlayButton.setVisibility(View.VISIBLE);
        }
        if (mGlobalSeekBar.getVisibility() == View.INVISIBLE) {
            mGlobalSeekBar.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Retrieve the currently selected ShortSound track names.
     * @return
    */
    public List<String> getCurShortSoundNames() {
        List<String> list = new ArrayList<>();
        if (mActiveShortSound != null) {
            for (ShortSoundTrack track : mActiveShortSound.getTracks())
                list.add(track.getTitle());
        }
        return list;
    }

    /**
     * Retrieve the currently selected ShortSound track names.
     * @return volume level 0.0 <= level <= 1.0
     */
    public float getShortSoundVolume(int track) {
        return mActiveShortSound.getTracks().get(track).getVolume();
    }

//    /**
//     * Retrieve the current AudioPlayer.
//     * @return the current AudioPlayer.
//     */
//    public ModelControl getModelControl() {
//        return modelControl;
//    }

    /**
     * Sets up the floating action button used as record button. Will
     * only be called for Android SDK >= LOLLIPOP
     */
    private void setUpFloatingActionButton() {
        FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        mActionBarFragment = new FloatingActionButtonBasicFragment();
        mActionBarFragment.setOnLoadListener(new FloatingActionButtonBasicFragment.OnFragmentLoadedListener() {
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

    /**
     * onPostCreate setup
     * @param savedInstanceState The Bundle
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    /**
     * onConfigurationChanged
     * @param newConfig the Configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Called whenever we call invalidateOptionsMenu()
     * @param menu The drawer menu
     * @return boolean true if prepared successfully, false else
     */
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

    public void saveShortSoundTrack(int track) {
        mActiveShortSound.getTracks().get(track).saveShortSoundTrack();
    }


    /**
     * The click listener for ListView in the navigation drawer
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectShortSoundFromDrawer(position);
        }
    }

    /**
     * sets up Views for the animator
     */
    private void setUpAnimatorViews() {
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
        modelControl.setmAudioPlayer(new AudioPlayer(mActiveShortSound));  // Setup the new AudioPlayer for this SS.
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

            // TODO: Set a listener to update the SeekBar based on play position.
        } else {
            // selected mix is already loaded so close the drawer
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    /**
     * handles event when back is pressed on android device
     */
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

    /**
     * Animation for sliding views in from the right
     * @return the Animation
     */
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


    /**
     * Animation for sliding views out to right
     * @return the Animation
     */
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

    /**
     * Animation for sliding views in from left
     * @return the Animation
     */
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

    /**
     * Animation for sliding views out to left
     * @return the Animation
     */
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

    /**
     * This is used for loading the popup when clicking a specific effect
     * @param track the track to load the effect on
     * @param effect the effect to load on the track
     */
    public void effectEditSelected(int track, String effect) {
        // Set effect view with values pulled from model
        PointF[] values = mActiveShortSound.getTracks().get(track).getEffectVals(effect);
        if (effect.equals(EQ)) {
            // EQ
            Fx_EQCanvas eqCanvas = (Fx_EQCanvas) findViewById(R.id.eq_canvas);
            if (values != null) {
                // Set saved values
                eqCanvas = (Fx_EQCanvas) findViewById(R.id.eq_canvas);
                eqCanvas.setValues(values);
            } else {
                // Set default values for EQ
                eqCanvas.resetPoints();
            }
//            // Attach the EQ effect controller to the view
//            EqEffect eqEffect = mActiveShortSound.getTracks().get(track).getmEqEffect();
//            eqCanvas.setController(new EQEffectController(eqEffect));
            // Set button listeners on save & cancel on EQ
            findViewById(R.id.saveEQButton).setOnClickListener(new SaveButtonListener(track, effect));
            findViewById(R.id.cancelEQButton).setOnClickListener(new CancelButtonListener(effect));
        } else if (effect.equals(REVERB)) {
            //REVERB
            Fx_ReverbCanvas reverbCanvas = (Fx_ReverbCanvas) findViewById(R.id.reverb_canvas);
            if (values != null) {
                reverbCanvas.setValue(values[0]);
            } else {
                // Set default values for Reverb
                reverbCanvas.resetPoint();
            }
//            // Attach the EQ effect controller to the view
//            ReverbEffect reverbEffect = mActiveShortSound.getTracks().get(track).getmReverbEffect();
//            reverbCanvas.setController(new ReverbEffectController(reverbEffect));
            // Set button listeners on save & cancel on Reverb
            findViewById(R.id.saveReverbButton).setOnClickListener(new SaveButtonListener(track, effect));
            findViewById(R.id.cancelReverbButton).setOnClickListener(new CancelButtonListener(effect));
        } else {
            // Set cancel and save for other effects
            findViewById(R.id.saveReverbButton).setOnClickListener(new SaveButtonListener(track, effect));
            findViewById(R.id.cancelReverbButton).setOnClickListener(new CancelButtonListener(effect));
        }

        // Change the view to the effect
        animator.setDisplayedChild(viewMap.get(effect));
        currentView = effect;
        mActiveShortSound = sounds.get( position );  // Set the currently active ShortSound.
        // Highlight item, update title, close drawer
        mDrawerList.setItemChecked(position, true);

        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(mShortSoundsTitles[position]);
        invalidateOptionsMenu();
    }

    /**
     * Sets the Title on the action bar to the parameter title
     * @param title the title of a shortSound
     */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * Creates the Action Bar Options Menu
     * @param menu The Action Bar menu
     * @return true if menu created, false else
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

    /**
     * Specifies the share intent
     * @return the share Intent
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

    /**
     * Determines what to do when a button is pressed on the menu bar
     * based on what the MenuItem that is passed in as a parameter is
     * @param item The MenuItem selected
     * @return true if selected, false else
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
                deleteShortSound();
                return true;
            case R.id.action_new:
                createNew();
                return true;
            case R.id.action_rename:
                rename();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
 
    }

    /**
     * Deletes the current ShortSound from the library.
     * If there are other ShortSounds in the library, opens the first
     * ShortSound.
     * If there are no ShortSounds in the library, creates and opens a new
     * ShortSound
     */
    private void deleteShortSound() {
        if (mActiveShortSound != null) {
            this.position = -1;
            Log.d("CHECK", "" + sounds.size());
            sounds.remove(mActiveShortSound);
            Log.d("CHECK", "" + sounds.size());
            mActiveShortSound.delete();
            if (sounds.size() > 0) {
                mShortSoundsTitles = getShortSoundTitles(ShortSound.getAll());
                Log.d("CHECK", "" + mShortSoundsTitles.length);
                mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                        R.layout.drawer_list_item, mShortSoundsTitles));
                Log.d("CHECK", sounds.get(0).getTitle());
                selectShortSoundFromDrawer(0);
                // hides seek bar and play button
                setUpGlobalSeekBar();
                setUpGlobalPlayButton();
            } else {
                createNew();
            }
        }
    }

    /**
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

    /**
     * Renames a ShortSound
     */
    private void rename() {
        FragmentManager fragmentManager = getFragmentManager();
        NoticeDialogFragment inputNameDialog = new NoticeDialogFragment();
        inputNameDialog.show(fragmentManager, "Input Dialog");
    }

    /**
     * Handles event okay selected from NoticeDialogFragment
     * @param inputText
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

    /**
     * Ends the recording process.
     */
    private void endRecording() {
        mGlobalPlayButton.setEnabled(true);
        mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));//TODO
        ShortSound newShortSound = modelControl.onRecordStop( mActiveShortSound );
        if ( newShortSound != null ) {
            // Update the sidebar with the new ShortSound.
            sounds.add(newShortSound);
            mShortSoundsTitles = getShortSoundTitles(sounds);
            mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                    R.layout.drawer_list_item, mShortSoundsTitles));
            // Select the new ShortSound to be active.
            selectShortSoundFromDrawer(sounds.size() - 1);

        } else {
            // Update the existing fragment manager to add new track to list
            ((TrackView) findViewById(R.id.track_list)).notifyTrackAdded(mActiveShortSound.getTracks().size() - 1);
        }

        // Activate GlobalPlayButton so that tracks are playable
        // If there is only one track, this must've been an empty ShortSound before
        if (mActiveShortSound.getTracks().size() == 1) {
            setGlobalPlayButtonClickHandler();
        }
    }

    // TODO: Clean up resources & Save track state to DB
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Cancels button listeners
     */
    public class CancelButtonListener implements View.OnClickListener {
        private String effect;

        /**
         * Constructor for a CancelButtonListener
         * @param effect The effect to cancel the b utton on
         */
        public CancelButtonListener(String effect) {
            this.effect = effect;
        }

        /**
         * Switch view back w/o saving anything to backend
         * @param v the View associated with button
         */
        @Override
        public void onClick(View v) {
            Log.d("Main", "Cancel clicked");
            // Show message
            showToast("Canceled " + effect);
            // Got back to track view
            onBackPressed();
        }
    }

    /**
     * Class used for saving an effect on the UI.
     * Should save the values to the model
     */
    public class SaveButtonListener implements View.OnClickListener {
        private int track;
        private String effect;

        /**
         * Constructor for a SaveButtonListener
         * @param track the track associated with the listener
         * @param effect the effect associated with the track
         */
        public SaveButtonListener(int track, String effect) {
            this.track = track;
            this.effect = effect;
            String trackName = mActiveShortSound.getTracks().get(track).toString();
        }

        /**
         * handles the click events
         * @param v View associated with the click events
         */
        @Override
        public void onClick(View v) {
//            String trackName = mActiveShortSound.getTracks().get(track).toString();

            // Save values to backend effect (EQ/Reverb) & change view
            if (effect.equals(EQ)) {
                Fx_EQCanvas eqCanvas = (Fx_EQCanvas) findViewById(R.id.eq_canvas);
                PointF lo = eqCanvas.getBandA();
                PointF hi = eqCanvas.getBandB();
                EqEffect effect = mActiveShortSound.getTracks().get(track).getmEqEffect();
                PointF[] newVals = new PointF[]{lo, hi};
                effect.setPointVals(newVals);
            } else if (effect.equals(REVERB)) {
                Fx_ReverbCanvas reverbCanvas = (Fx_ReverbCanvas) findViewById(R.id.reverb_canvas);
                PointF point = reverbCanvas.getValue();
                ReverbEffect effect = mActiveShortSound.getTracks().get(track).getmReverbEffect();
                effect.setPointVal(point);
            } else {
                // Don't do anything for other effects
            }

            // Show message
            showToast("Saved " + effect);

            // Switch view back
            onBackPressed();
        }
    }

    /**
     * shows toast
     * @param text The String text associated with the toast
     */
    private void showToast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        LinearLayout layout =(LinearLayout)toast.getView();
        TextView textView = ((TextView)layout.getChildAt(0));
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        toast.show();
    }
}