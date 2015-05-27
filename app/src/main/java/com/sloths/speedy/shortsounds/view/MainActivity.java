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
import android.support.v7.widget.RecyclerView;
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
    public static final String BIT = "Bit Crush";
    public static final String DIST = "Distortion";
    public static final String TRACKS = "tracks";
    public static final int SLIDE_DURATION = 400;
    public static final String UNTITLED = "Untitled";

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
    private EffectController effectController;
    private Timer mRecordTimer;
    private boolean mTimerIsRunning;
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
        mTimerIsRunning = false;
        mElapsedTime = 0;
        mShortSoundsTitles = getShortSoundTitles(sounds);
        modelControl = ModelControl.instance();
        final AudioRecorder mAudioRecorder = new AudioRecorder( getCacheDir() );
        modelControl.setmAudioRecorder(mAudioRecorder);
        setContentView(R.layout.activity_main);
        setUpControllerView();
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
                } else {
                    if(progress == 100) {
                        mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
                        mGlobalSeekBar.setProgress(0);
                        modelControl.updateCurrentPosition(0);
                    }
                }
            }

        };

        mGlobalSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        modelControl.setGlobalSeekBar(mGlobalSeekBar);
        mGlobalPlayButton = (ImageButton)findViewById(R.id.imageButtonPlay);

        mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
        Log.d("DEBUG", "Found the global play button! " + mGlobalPlayButton);
        mGlobalPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!modelControl.onPlayToggle())
                    mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
                else
                    mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
            }
        });
        setPlayerVisibility(View.INVISIBLE);
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
     * @return
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

    public int getCurrentShortSoundSize() {
        if (mActiveShortSound == null)
            return 0;
        return mActiveShortSound.getSize();
    }

    /**
     * Toggles the play and pause buttons
     * @return boolean true of toggled, false else
     */
//    @Override
//    public boolean onPlayToggle() {
//        if ( !modelControl.onPlayToggle() )
//            mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
//        else
//            mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
//        return true;
//    }
    
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
        if (viewMap == null) {
            viewMap = new HashMap<>();
            viewMap.put(TRACKS, 0);
            viewMap.put(EQ, 1);
            viewMap.put(REVERB, 2);
            viewMap.put(BIT, 3);
            viewMap.put(DIST, 4);
        }

        animator = (ViewAnimator) findViewById(R.id.view_animator);
        animator.findViewById(R.id.eq_canvas);
        animator.findViewById(R.id.reverb_canvas);
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
     * its position is passed in as a parameter which determines the short sound to load.
     * @param position int the position of the drawer item clicked
     */
    private void selectShortSoundFromDrawer(int position) {
        modelControl.stopAllFromPlaying();
        resetSeekBarToZero();
        mActiveShortSound = sounds.get(position);  // Set the currently active ShortSound.
        // If the selected ShortSound was not the currently active one...
        if (this.position != position) {
            Log.d("SHORT_SOUNDS", "Selected ShortSound ["+mActiveShortSound.getId()+"] from the sidebar.");
            modelControl.setmAudioPlayer(new AudioPlayer(mActiveShortSound));  // Setup the new AudioPlayer for this SS.
            currentView = TRACKS;
            if (position != -1) {
                // Highlight item, update title, close drawer
                mDrawerList.setItemChecked(position, true);
                setTitle(mShortSoundsTitles[position]);
            } else {
                mDrawerList.setItemChecked(this.position, false);
            }
            this.position = position;

                // load the mix into a view and replace it in the animator
            if (mActiveShortSound != null && mActiveShortSound.getTracks().size() >= 0) {
                setPopulatedTrackView();
            } else {
                setEmptyTrackView();
            }

            invalidateOptionsMenu();

            resetSeekBarToZero();

            mGlobalPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
        }
        // selected mix is already loaded so close the drawer
        try {
            mShareActionProvider.setShareIntent(createShareIntent());
        } catch (IOException e) {
            mShareActionProvider.setShareIntent(null);
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void resetSeekBarToZero() {
        mGlobalSeekBar.setProgress(0);
        modelControl.updateCurrentPosition(0);
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

        // This is done globally because BackPressed is global
        // for effect's views
        if (effectController != null) {
            // Resets model to default values
            effectController.resetModel();
            effectController = null;
        }
    }

    private void animateToTrack() {
        Animation in = animator.getInAnimation();
        Animation out = animator.getOutAnimation();
        animator.setInAnimation(inFromLeftAnimation());
        animator.setOutAnimation(outToRightAnimation());
        animator.setDisplayedChild(viewMap.get(TRACKS));
        animator.setInAnimation(in);
        animator.setOutAnimation(out);
        currentView = TRACKS;}


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
        mActiveShortSound = sounds.get( position );  // Set the currently active ShortSound.
        // Highlight item, update title, close drawer
        mDrawerList.setItemChecked(position, true);

        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(mShortSoundsTitles[position]);
        invalidateOptionsMenu();
    }

    /**
     * This method sets up the effect views.  Its main two purposes are currently
     * for the EQ & Reverb effect.  It gets the view, attaches the controller to it,
     * attaches the model to the controller, and then sets the click listeners for cancel
     * and save.
     * @param track
     * @param effect
     */
    private void setupEffect(int track, String effect) {
        // Resets effect controller
        // Set effect view with values pulled from model
        PointF[] values = mActiveShortSound.getTracks().get(track).getEffectVals(effect);
        if (effect.equals(EQ)) {
            // EQ
            Fx_EQCanvas eqCanvas = (Fx_EQCanvas) findViewById(R.id.eq_canvas);
            // Set saved values -- if null it defaults
            eqCanvas.setValues(values);
            // Attach the EQ model to the EQ controller
            EqEffect eqEffect = mActiveShortSound.getTracks().get(track).getmEqEffect();
            EQEffectController eqController = new EQEffectController(eqEffect);
            // Attach the EQ controller to the EQ view
            eqCanvas.setController(eqController);
            eqController.setCancel(values);
            // Set current controller
            effectController = eqController;
            // Set button listeners on save & cancel on EQ
            findViewById(R.id.saveEQButton).setOnClickListener(new SaveButtonListener(effect, track));
            findViewById(R.id.cancelEQButton).setOnClickListener(new CancelButtonListener(effect));
        } else if (effect.equals(REVERB)) {
            //REVERB
            Fx_ReverbCanvas reverbCanvas = (Fx_ReverbCanvas) findViewById(R.id.reverb_canvas);
            // Set saved point value (if null it defaults)
            reverbCanvas.setValue(values);
            // Attach the Reverb model to the Reverb controller
            ReverbEffect reverbEffect = mActiveShortSound.getTracks().get(track).getmReverbEffect();
            ReverbEffectController reverbController = new ReverbEffectController(reverbEffect);
            // Attach the Reverb controller to the Reverb view
            reverbCanvas.setController(reverbController);
            reverbController.setCancel(values);
            // Set current controller
            effectController = reverbController;
            // Set button listeners on save & cancel on Reverb
            findViewById(R.id.saveReverbButton).setOnClickListener(new SaveButtonListener(effect, track));
            findViewById(R.id.cancelReverbButton).setOnClickListener(new CancelButtonListener(effect));
        } else {
            // Set cancel and save for other effects
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
        mTimerIsRunning = true;
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
        mTimerIsRunning = false;
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
        return true;
    }

    /**
     * Specifies the share intent
     * @return the share Intent
     */
    private Intent createShareIntent() throws IOException {
        if (mActiveShortSound == null) {
            return null;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        File absolutePath = mActiveShortSound.generateAudioFile();

        Uri contentURI = FileProvider.getUriForFile(MainActivity.this, "com.sloths.speedy.shortsounds.fileprovider", absolutePath);

        if (contentURI != null) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentURI);
            shareIntent.setType("audio/wav");
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
                renameShortSound();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
 
    }

    /**
     * Deletes the current ShortSound from the library.
     * And returns the user to a blank start recording screen.
     */
    private void deleteShortSound() {
        if (mActiveShortSound != null) {
            this.position = -1;
            Log.d("CHECK", "" + sounds.size());
            sounds.remove(mActiveShortSound);
            Log.d("CHECK", "" + sounds.size());
            mActiveShortSound.removeShortSound();
            mShortSoundsTitles = getShortSoundTitles(ShortSound.getAll());
            createNew();
        }
    }

    public void removeShortSoundTrack(int track) {
        modelControl.removeTrack(track);
        mActiveShortSound.removeTrack(mActiveShortSound.getTracks().get(track));
    }



    /**
     * Creates a new ShortSound and takes it to an empty screen with no
     * current tracks for this ShortSound
     */
    private void createNew() {
        position = -1;
        mActiveShortSound = null;
        modelControl.setmAudioPlayer(null);  // Clear the existing AudioPlayer
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mShortSoundsTitles));
        setEmptyTrackView();
        ActionBar ab = getActionBar();
        if (ab != null)
            ab.setTitle("ShortSounds");
    }

    /**
     * Set the view to the "Record a Sound" view
     */
    private void setEmptyTrackView() {
        setPlayerVisibility(View.INVISIBLE);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.nested_track_view);
        View add = getLayoutInflater().inflate(R.layout.empty_tracks, relativeLayout, false);
        animator.addView(add, viewMap.get(TRACKS) + 1);
        animator.setDisplayedChild(viewMap.get(TRACKS) + 1);
        animator.removeViewAt(viewMap.get(TRACKS));
        invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
    }

    /**
     * Set the view when we have a populated ShortSound
     */
    private void setPopulatedTrackView() {
        setPlayerVisibility(View.VISIBLE);
        TrackView tv = (TrackView) findViewById(R.id.track_list);
        View add = getLayoutInflater().inflate(R.layout.track_list_xml, tv, false);
        animator.addView(add, viewMap.get(TRACKS) + 1);
        animator.setDisplayedChild(viewMap.get(TRACKS) + 1);
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
     * @param inputText
     */
    public void onRenameShortSound(String inputText) {
        if (!inputText.matches("\\s+") && !inputText.equals("")) {
            mActiveShortSound.setTitle(inputText);
            mShortSoundsTitles = getShortSoundTitles(ShortSound.getAll());
            setTitle(inputText);
            mDrawerList.setAdapter(new ArrayAdapter<>(this,
                    R.layout.drawer_list_item, mShortSoundsTitles));
        }
    }

    public void renameTrack(int position) {
        RenameShortSoundTrackDialog dialog = new RenameShortSoundTrackDialog();
        dialog.setTrack(position);
        dialog.show(getFragmentManager(), "Input Dialog");
    }

    /**
     * Handles event okay selected from NoticeDialogFragment
     * @param inputText
     */
    @Override
    public void onRenameShortSoundTrack(String inputText, int track) {
        if (!inputText.matches("\\s+") && !inputText.equals("")) {
            mActiveShortSound.getTracks().get(track).saveTrackName(inputText);
            ((TrackView) findViewById(R.id.track_list)).getAdapter().notifyDataSetChanged();
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
            Log.d("DEBUG", "Ended recording while no ShortSound existed");
            // Update the sidebar with the new ShortSound.
            sounds.add(newShortSound);
            mShortSoundsTitles = getShortSoundTitles(sounds);
            mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                    R.layout.drawer_list_item, mShortSoundsTitles));
            // Select the new ShortSound to be active.
            selectShortSoundFromDrawer(sounds.size() - 1);
        } else {
            Log.d("DEBUG", "Ended recording on existing ShortSound");
            // Update the existing fragment manager to add new track to list
            TrackView tv = ((TrackView) findViewById(R.id.track_list));
            tv.notifyTrackAdded(mActiveShortSound.getTracks().size() - 1);
            try {
                mShareActionProvider.setShareIntent(createShareIntent());
            } catch (IOException e) {
                mShareActionProvider.setShareIntent(null);
            }
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
     * Class used for saving an effect on the UI.
     * Should save the values to the model
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
     *
     * @param effect
     * @param position
     * @return
     */
    public boolean isEffectOn(Effect.Type effect, int position) {
        return mActiveShortSound.isEffectOn(effect, position);
    }
}