package com.sloths.speedy.shortsounds.view;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.sloths.speedy.shortsounds.R;
import com.sloths.speedy.shortsounds.controller.EQEffectController;
import com.sloths.speedy.shortsounds.controller.EffectController;
import com.sloths.speedy.shortsounds.controller.ModelControl;
import com.sloths.speedy.shortsounds.controller.ReverbEffectController;
import com.sloths.speedy.shortsounds.model.AudioPlayer;
import com.sloths.speedy.shortsounds.model.AudioRecorder;
import com.sloths.speedy.shortsounds.model.Effect;
import com.sloths.speedy.shortsounds.model.EqEffect;
import com.sloths.speedy.shortsounds.model.ReverbEffect;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The MainActivity for the application. Contains setup for the framework of the UI.
 */
public class MainActivity extends FragmentActivity
                          implements RenameShortSoundDialog.NoticeDialogListener,
                                     RenameShortSoundTrackDialog.RenameShortSoundTrackDialogListener {
    public static final String TAG = "MainActivity";
    public static final String EQ = "EQ";
    public static final String REVERB = "Reverb";
    public static final String TRACKS = "tracks";
    public static final int SLIDE_DURATION = 400;
    public static final String UNTITLED = "Untitled";

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
    private ModelControl modelControl;
    private EffectController effectController;
    private Timer mRecordTimer;
    private int mElapsedTime;

    /**
     * Sets up MainActivity
     * @param savedInstanceState the Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        effectController = null;
        super.onCreate(savedInstanceState);
        Log.d("DB_TEST", "MainActivity:onCreate()");
        sounds = ShortSound.getAll();
        Log.d("DB_TEST", sounds.toString());
        mElapsedTime = 0;
        modelControl = ModelControl.instance();
        final AudioRecorder mAudioRecorder = new AudioRecorder( getCacheDir() );
        modelControl.setmAudioRecorder(mAudioRecorder);
        setContentView(R.layout.activity_main);
        setUpControllerView();
        setUpLibraryDrawer();
        enableActionBarLibraryToggleButton();
        setUpAnimatorViews();
        if (sounds.size() == 0)
            createNew();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setUpFloatingActionButton();
        } else {
            setUpRecordButton();
        }
    }

    /**
     * This sets up the Global SeekBar. Note that when no ShortSound is loaded, this SeekBar should
     * be invisible
     * This sets up the Global Play button and attaches the default click
     * handler. Note that when no ShortSound is loaded, this button should
     * be invisible
     * Now that we have a selected ShortSound in focus we need to update the Global Play
     * button's click handler to play all tracks associated with this ShortSound.
     */
    private void setUpControllerView() {
        mGlobalSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mGlobalSeekBar.setMax(100);  // Set the max value (0-100)

        mGlobalSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return modelControl.isRecording();
            }
        });

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // The current progress level. This will be in the range 0..max
                // where max was set by setMax(int). (The default value for max is 100.)
                // TODO Auto-generated method stub

                if(fromUser) { //!modelControl.isRecording()
                    Log.d("DB_TEST", "SeekBar Progress Changed By User to " + progress);
                    modelControl.updateCurrentPosition(progress);
                    drawPlayButton();
                } else {
                    if(progress == 100) {
                        drawPlayButton();
                        mGlobalSeekBar.setProgress(0);
                        modelControl.updateCurrentPosition(0);
                    }
                }
            }
        };

        mGlobalSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        modelControl.setGlobalSeekBar(mGlobalSeekBar);
        mGlobalPlayButton = (ImageButton)findViewById(R.id.imageButtonPlay);
        drawPlayButton();
        Log.d("DEBUG", "Found the global play button! " + mGlobalPlayButton);
        mGlobalPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!modelControl.onPlayToggle()) {
                    mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
                    mGlobalPlayButton.setColorFilter(getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                } else {
                    drawPlayButton();
                }
            }
        });
    }

    /**
     * Shows and hides the global seek bar and play button
     */
    private void setPlayerVisibility(int value) {
        mGlobalPlayButton.setVisibility(value);
        mGlobalSeekBar.setVisibility(value);
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
                        setPlayerVisibility(View.VISIBLE);
                        mGlobalSeekBar.setEnabled(true);
                        stopTimer();
                        resetSeekBarToZero();
                    } else {
                        mGlobalPlayButton.setEnabled(false);
                        modelControl.onRecordStart();
                        mGlobalSeekBar.setEnabled(false);
                        startTimer();
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
                        setPlayerVisibility(View.VISIBLE);
                        mGlobalSeekBar.setEnabled(true);
                        resetSeekBarToZero();
                    } else {
                        mGlobalPlayButton.setEnabled(false);
                        modelControl.onRecordStart();
                        mGlobalSeekBar.setEnabled(false);;
                    }
                }
            });
        }
    }
    
    /**
     * Retrieve the currently selected ShortSound track names.
     * @return the name of the current track
    */
    public String getCurrentTrackNameAt(int track) {
        Log.i(TAG, "getcurrentShortSoundNames()");
        return mActiveShortSound.getTrackName(track);
    }

    /**
     * Retrieve the currently selected ShortSound track names.
     * @return volume level 0.0 <= level <= 1.0
     */
    public float getShortSoundVolume(int track) {
        if (mActiveShortSound != null)
            return mActiveShortSound.getTracks().get(track).getVolume();
        return 0.0f;
    }

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
     * @return string[] of titles
     */
    private String[] getShortSoundTitles() {
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
     * Provides logic for setting up the library draweFr.
     */
    private void setUpLibraryDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        //  ==> This connects the listview to the actual sounds
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, getShortSoundTitles()));
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

    /**
     * saves the given short sound track to the database
     * @param track, the position of the given track
     */
    public void saveShortSoundTrack(int track) {
        mActiveShortSound.getTracks().get(track).saveShortSoundTrack();
    }

    /**
     *
     * @return the size of the current short sound
     */
    public int getCurrentShortSoundSize() {
        if (mActiveShortSound == null)
            return 0;
        return mActiveShortSound.getSize();
    }

    
    /**
     * The click listener for ListView in the navigation drawer
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mActiveShortSound.equals(sounds.get(position)))
                mDrawerLayout.closeDrawer(mDrawerList);
            else
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
        animator = (ViewAnimator) findViewById(R.id.view_animator);
        animator.setInAnimation(inFromRightAnimation());
        animator.setOutAnimation(outToLeftAnimation());

    }

    /**
     * Helper method for the the DrawerItemClickListener. When a drawer item is clicked
     * its position is passed in as a parameter which determines the short sound to load.
     * @param position int the position of the drawer item clicked
     */
    private void selectShortSoundFromDrawer(int position) {
        if (position < 0 || (sounds != null && position >= sounds.size()))
            throw new IllegalArgumentException("invalid position: " + position + ", sounds.size = " + sounds.size());

        modelControl.stopAllFromPlaying();
        resetSeekBarToZero();
        mActiveShortSound = sounds.get(position);// Set the currently active ShortSound.
        Log.d("SHORT_SOUNDS", "Selected ShortSound ["+mActiveShortSound.getId()+"] from the sidebar.");
        //TODO double check this is not causing bugs
        modelControl.release();
        modelControl.setmAudioPlayer(new AudioPlayer(mActiveShortSound));  // Setup the new AudioPlayer for this SS.
        currentView = TRACKS;
        // Highlight item, update title, close drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mActiveShortSound.getTitle());

        updateCurrentTrackView();
        updateViewStateBasedOnTrackCount();
        invalidateOptionsMenu();
        resetSeekBarToZero();
        // selected mix is loaded so close the drawer
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /**
     * Resets the seek bar to 0
     */
    private void resetSeekBarToZero() {
        modelControl.updateCurrentPosition(0);
        mGlobalSeekBar.setProgress(0);
    }

    /**
     * handles event when back is pressed on android device
     */
    @Override
    public void onBackPressed() {
        if (currentView.equals(TRACKS)) {
            super.onBackPressed();
        } else {
            animateToTrack();
        }
        // Controller is global because this method needs knowledge of
        // the last stored values, where model values are always up to date
        if (effectController != null) {
            // Resets model to default values
            effectController.resetModel();
            effectController = null;
        }
    }

    /**
     * Animates the track
     */
    private void animateToTrack() {
        Animation in = animator.getInAnimation();
        Animation out = animator.getOutAnimation();
        animator.setInAnimation(inFromLeftAnimation());
        animator.setOutAnimation(outToRightAnimation());
        animator.setDisplayedChild(viewMap.get(TRACKS));
        animator.setInAnimation(in);
        animator.setOutAnimation(out);
        currentView = TRACKS;
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

        // Setups the effect to be shown (connects model-controller-view)
        setupEffect(track, effect);

        // Change the view to the effect
        animator.setDisplayedChild(viewMap.get(effect));
        currentView = effect;
    }

    /**
     * This method sets up the effect views.  Its main two purposes are currently
     * for the EQ & Reverb effect.  It gets the view, attaches the controller to it,
     * attaches the model to the controller, and then sets the click listeners for cancel
     * and save.
     * @param track The track to set up the effect view on
     * @param effect The name of the effect being set up
     */
    private void setupEffect(int track, String effect) {
        PointF[] values = mActiveShortSound.getTracks().get(track).getEffectVals(effect);
        if (effect.equals(EQ)) {
            // Set saved values -- if null it defaults
            Fx_EQCanvas eqCanvas = (Fx_EQCanvas) findViewById(R.id.eq_canvas);
            eqCanvas.setValues(values);

            // Attach the EQ model to the EQ controller & controller to view
            EqEffect eqEffect = mActiveShortSound.getTracks().get(track).getmEqEffect();
            EQEffectController eqController = new EQEffectController(eqEffect, values);
            eqCanvas.setController(eqController);

            // Set current controller
            effectController = eqController;

            findViewById(R.id.saveEQButton).setOnClickListener(new SaveButtonListener(effect, track));
            findViewById(R.id.cancelEQButton).setOnClickListener(new CancelButtonListener(effect));
        } else if (effect.equals(REVERB)) {
            // Set saved point value (if null it defaults)
            Fx_ReverbCanvas reverbCanvas = (Fx_ReverbCanvas) findViewById(R.id.reverb_canvas);
            reverbCanvas.setValue(values[0]);

            // Attach the model to controller & controller to view
            ReverbEffect reverbEffect = mActiveShortSound.getTracks().get(track).getmReverbEffect();
            ReverbEffectController reverbController = new ReverbEffectController(reverbEffect, values[0]);
            reverbCanvas.setController(reverbController);

            // Set current controller
            effectController = reverbController;

            findViewById(R.id.saveReverbButton).setOnClickListener(new SaveButtonListener(effect, track));
            findViewById(R.id.cancelReverbButton).setOnClickListener(new CancelButtonListener(effect));
        }
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
     * Start a timer that times the how long a track is recording for
     */
    private void startTimer() {
        mRecordTimer = new Timer();
        TextView timerTextView = (TextView)findViewById(R.id.timerView);
        timerTextView.setVisibility(View.VISIBLE);
        mRecordTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                mElapsedTime += 1; //increase every sec
                mTimeHandler.obtainMessage(1).sendToTarget();

            }
        }, 0, 1000);
    }

    /**
     * Used by startTimer(). Updates the textview associated with the record timer.
     */
    private Handler mTimeHandler = new Handler() {
        public void handleMessage(Message msg) {
            TextView timerTextView = (TextView)findViewById(R.id.timerView);
            int minutes = mElapsedTime / 60;
            int seconds = mElapsedTime - (60 * minutes);
            String z = "";
            if (seconds < 10) z = "0";
            String time = "0" + minutes + ":" + z + seconds;
            timerTextView.setText(time);
        }
    };

    /**
     * stops the timer associated with track recording.
     */
    private void stopTimer() {
        mRecordTimer.cancel();
        TextView timerTextView = (TextView)findViewById(R.id.timerView);
        timerTextView.setVisibility(View.INVISIBLE);
        timerTextView.setText("00:00");
        mElapsedTime = 0;
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

        mShareActionProvider.setOnShareTargetSelectedListener(
                new ShareActionProvider.OnShareTargetSelectedListener() {
            @Override
            public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
                Log.i(TAG, "share target selected clicked");
                source.setShareIntent(createShareIntent());
                //return type doesn't matter, api says return false for consistency
                return false;
            }
        });

        return true;
    }

    /**
     * Specifies the share intent
     * @return the share Intent
     */
    private Intent createShareIntent() {
        if (mActiveShortSound == null) {
            return null;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        try {
            File absolutePath = mActiveShortSound.generateAudioFile();

            Uri contentURI = FileProvider.getUriForFile(MainActivity.this, "com.sloths.speedy.shortsounds.fileprovider", absolutePath);

            if (contentURI != null) {
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentURI);
                shareIntent.setType("audio/wav");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                renameShortSound();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
 
    }

    /**
     * Resumes state on reopening the app
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mActiveShortSound != null && sounds.contains(mActiveShortSound))
            selectShortSoundFromDrawer(sounds.indexOf(mActiveShortSound));
        else if (!sounds.isEmpty())
            selectShortSoundFromDrawer(0);
        else
            createNew();

    }

    /**
     * Deletes the current ShortSound from the library.
     * And returns the user to a blank start recording screen.
     */
    private void deleteShortSound() {
        if (mActiveShortSound != null) {
            int index = sounds.indexOf(mActiveShortSound);
            mDrawerList.setItemChecked(index, false);
            sounds.remove(mActiveShortSound);
            mActiveShortSound.removeShortSound();
            mDrawerList.setAdapter(new ArrayAdapter<>(this,
                    R.layout.drawer_list_item, getShortSoundTitles()));
            if (!sounds.isEmpty()) {
                selectShortSoundFromDrawer(0);
            } else {
                createNew();
//                updateCurrentTrackView();
//                invalidateOptionsMenu();
//                resetSeekBarToZero();
            }
        }
    }

    /**
     * returns the id of the current ShortSound
     * @return int the id of the current ShortSound
     */
    public int getCurrentShortSoundId() {
        return (int) mActiveShortSound.getId();
    }

    /**
     * Removes a track from the ShortSound
     * @param track The track to remove
     */
    public void removeShortSoundTrack(int track) {
        modelControl.removeTrack(track);
        mActiveShortSound.removeTrack(mActiveShortSound.getTracks().get(track));
        updateViewStateBasedOnTrackCount();
    }



    /**
     * Creates a new ShortSound and takes it to an empty screen with no
     * current tracks for this ShortSound
     */
    private void createNew() {
        ShortSound newSound = new ShortSound();
        setTitle(newSound.getTitle());
        sounds.add(newSound);
        modelControl.release();
        modelControl.setmAudioPlayer(new AudioPlayer(newSound));
        mActiveShortSound = newSound;
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, getShortSoundTitles()));
        selectShortSoundFromDrawer(sounds.indexOf(mActiveShortSound));
        ActionBar ab = getActionBar();
        if (ab != null)
            ab.setTitle("ShortSounds");
    }


    /**
     * Set the view when we have a populated ShortSound
     */
    private void updateCurrentTrackView() {
        RelativeLayout tvp = (RelativeLayout) findViewById(R.id.track_list_parent);
        View add = getLayoutInflater().inflate(R.layout.empty_tracks, tvp, false);
        animator.addView(add, viewMap.get(TRACKS) + 1);
        animator.setDisplayedChild(viewMap.get(TRACKS) + 1);
        updateViewStateBasedOnTrackCount();
        animator.removeViewAt(viewMap.get(TRACKS));
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
    }

    /**
     * Renames a ShortSound
     */
    private void renameShortSound() {
        RenameShortSoundDialog inputNameDialog = new RenameShortSoundDialog();
        inputNameDialog.show(getFragmentManager(), "Input Dialog");
    }

    /**
     * Handles event okay selected from NoticeDialogFragment
     * to rename a ShortSound
     * @param inputText the text inputted for the new title
     */
    @Override
    public void onRenameShortSound(String inputText) {
        if (!inputText.matches("\\s+") && !inputText.equals("")) {
            mActiveShortSound.setTitle(inputText);
            setTitle(inputText);
            mDrawerList.setAdapter(new ArrayAdapter<>(this,
                    R.layout.drawer_list_item, getShortSoundTitles()));
        }
    }

    /**
     * Renames a track
     * @param position the position of the track in the ShortSound to
     *                 rename
     */
    public void renameTrack(int position) {
        RenameShortSoundTrackDialog dialog = new RenameShortSoundTrackDialog();
        dialog.setTrack(position);
        dialog.show(getFragmentManager(), "Input Dialog");
    }

    /**
     * Handles event okay selected from NoticeDialogFragment
     * @param inputText the input text from the user to rename the track title to
     */
    @Override
    public void onRenameShortSoundTrack(String inputText, int track) {
        if (!inputText.matches("\\s+") && !inputText.equals("")) {
            mActiveShortSound.getTracks().get(track).saveTrackName(inputText);
            ((TrackList) animator.getChildAt(viewMap.get(TRACKS))
                    .findViewById(R.id.track_list))
                    .notifyTrackNameChanged();
        }
    }

    /**
     * Sets the int representation of a Color of the ShortSoundTrack
     * at Position track
     * @param track int the position of the track
     * @param color int the Inteer representing the Color
     */
    public void setTrackColor(int track, int color) {
        mActiveShortSound.getTracks().get(track).setColor(color);
    }

    /**
     * Returns the int representation of the Color of the ShortSoundTrack at
     * position track
     * @param track int the position of the ShortSoundTrack
     * @return int the int representaiton of the Color of the ShortSoundTrack
     */
    public int getTrackColor(int track) {
        return mActiveShortSound.getTracks().get(track).getColor();
    }

    /**
     * Returns the next available int representation for a
     * ShortSoundTrack color
     * @return int representation of a Color
     */
    public int getNextColorNum() {
        return mActiveShortSound.getNextTrackNumber();
    }

    /**
     * Ends the recording process.
     * @throws IllegalStateException if mActiveShortSound is null
     */
    private void endRecording() {
        mGlobalPlayButton.setEnabled(true);
        drawPlayButton();
        if (mActiveShortSound == null)
            throw new IllegalStateException("mActiveShortSound was null");
        modelControl.onRecordStop( mActiveShortSound );
        // Update the existing fragment manager to add new track to list
        ((TrackList) animator.getChildAt(viewMap.get(TRACKS))
                .findViewById(R.id.track_list))
                .notifyTrackAdded(mActiveShortSound.getSize());
        updateViewStateBasedOnTrackCount();
    }

    /**
     * Update the view state based on the track count.
     */
    public void updateViewStateBasedOnTrackCount() {
        TextView recordSound =
                (TextView) animator.getChildAt(viewMap.get(TRACKS))
                        .findViewById(R.id.recordSoundText);
        if (recordSound != null) {
            if (mActiveShortSound == null || mActiveShortSound.getSize() == 0) {
                recordSound.setVisibility(View.VISIBLE);
                setPlayerVisibility(View.INVISIBLE);
            } else {
                recordSound.setVisibility(View.INVISIBLE);
                setPlayerVisibility(View.VISIBLE);
            }
        }
        resetSeekBarToZero();
    }

    /**
     * draws the play button
     */
    private void drawPlayButton() {
        mGlobalPlayButton.setImageDrawable(getResources()
                .getDrawable(R.drawable.ic_action_play));//TODO
        mGlobalPlayButton.setColorFilter(getResources()
                .getColor(R.color.green_500), android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    /**
     * Cleans up resources and saves track state to the database
     */
    // TODO: Clean up resources & Save track state to DB
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Cancels button class used for listening on cancel button clicked
     * on the effect UI
     */
    public class CancelButtonListener implements View.OnClickListener {
        private String effect;

        public CancelButtonListener(String effect) {
            this.effect = effect;
        }

        /**
         * Switch view back w/o saving anything to backend
         * @param v the View associated with button
         */
        @Override
        public void onClick(View v) {
            // Show message
            ((ShortSoundsApplication)getApplicationContext()).showToast("Canceled " + effect);
            // Got back to track view
            onBackPressed();
        }
    }

    /**
     * Class used for saving an effect on the UI. It loads the track view when clicked
     */
    public class SaveButtonListener implements View.OnClickListener {
        private String effect;
        private int track;

        public SaveButtonListener(String effect, int track) {
            this.effect = effect;
            this.track = track;
        }

        /**
         * handles the click events
         * @param v View associated with the click events
         */
        @Override
        public void onClick(View v) {
            // Show message
            ((ShortSoundsApplication)getApplicationContext()).showToast("Saved " + this.effect);

            // Switch view back
            animateToTrack();
            effectController = null;

            Log.d("MAIN", "Saving current state of track");
            ShortSoundTrack currTrack = mActiveShortSound.getTracks().get(track);
            currTrack.saveShortSoundTrack();
        }
    }

    /**
     * Checks to see if an effect is on
     * @param effect The effect type to check
     * @param position The position of the track that we are checking to see
     *                 if it has the effect on
     * @return true if effect is on in specified track, false otherwise
     */
    public boolean isEffectOn(Effect.Type effect, int position) {
        return mActiveShortSound.isEffectOn(effect, position);
    }
}