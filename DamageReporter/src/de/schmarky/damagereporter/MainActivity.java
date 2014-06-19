package de.schmarky.damagereporter;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * @author schmarky schmarky@gmail.com
 * @version 1.0
 * @since 2014-06-10
 */
public class MainActivity extends Activity implements View.OnClickListener,
    Chronometer.OnChronometerTickListener, DialogInterface.OnClickListener,
    NumberPicker.OnScrollListener {

  // constants
  private static final String TIMER_PAUSED_TIME = "PausedTime";
  private static final String TIME_DISP_STR = "TimeDispStr";
  private static final String GAME_LENGTH_TIME = "GameLengthTime";
  private static final String VIBRATOR_STATE = "VibratorState";
  private static final String TIMER_BASE_TIME = "base";
  private static final String LOG_TAG = "Damage_Reporter";
  private static final String FILE_NAME = "MySharedPrefs";
  private static final int DAMAGE_REPORT_DEFAULT_LENGTH = 3;
  private static final int GAME_DEFAULT_LENGTH = 45;
  private static final long VIBRATE_DURATION = 1000;

  // globals
  boolean isTimerReset = true;
  boolean isTimerRunning = false;
  boolean isVibratorOn = true;

  int dmgReportLength = DAMAGE_REPORT_DEFAULT_LENGTH;
  int gameLength = GAME_DEFAULT_LENGTH;
  int sndDmgReport = 0;
  int sndGameOver = 0;
  long pausedTime = 0L;
  long baseTime = 0L;

  // global objects
  String chronoText;
  SharedPreferences someData;
  Button bStart, bReset;
  Chronometer cmTimer;
  SoundPool spSounds;
  TextView tvGameLength;
  Vibrator vib;
  AlertDialog alert;
  CheckBox cbVibrate;

  // Settings Objects
  NumberPicker npGameTenInd;
  NumberPicker npGameMinInd;

  // NumberPicker npRepMinutes;

  /**
   * Initialize the Activity
   * <p>
   * Find all components, load sounds and set listeners
   */
  private void init() {
    setContentView(R.layout.activity_main);

    cmTimer = (Chronometer) findViewById(R.id.cmTimer);
    bStart = (Button) findViewById(R.id.bStart);
    bReset = (Button) findViewById(R.id.bReset);
    tvGameLength = (TextView) findViewById(R.id.tvGameLength);
    spSounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    sndDmgReport = spSounds.load(this, R.raw.damagereport, 1);
    sndGameOver = spSounds.load(this, R.raw.gameover, 1);
    someData = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
    vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

    // start button
    bStart.setOnClickListener(this);
    // reset button
    bReset.setOnClickListener(this);
    // timer display
    cmTimer.setOnChronometerTickListener(this);
    // Game Length TextView
    tvGameLength.setOnClickListener(this);
    // make volume buttons work
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
  }

  public void resetTimer() {
    cmTimer.stop();
    isTimerRunning = false;
    isTimerReset = true;
    cmTimer.setText("00:00");
    pausedTime = 0L;
    baseTime = 0L;
    // clean up shared preferences
    SharedPreferences.Editor editor = someData.edit();
    editor.clear();
    editor.commit();
  }

  public void toggleTimer() {
    long base;
    // make sure the first click starts at 00:00
    if (isTimerReset) {
      cmTimer.setBase(SystemClock.elapsedRealtime());
      cmTimer.start();
      isTimerReset = false;
      isTimerRunning = true;
      // return;
    }
    // pause click
    else if (isTimerRunning) {
      pauseTimer();
    }
    // start timer after a resume
    else if (pausedTime != 0L && !isTimerRunning) {
      // get the base time
      if (baseTime != 0L) {
        base = baseTime;
      } else {
        base = cmTimer.getBase();
      }
      cmTimer.setBase((SystemClock.elapsedRealtime() - (pausedTime - base)));
      cmTimer.start();
      isTimerRunning = true;
      pausedTime = 0L;
      baseTime = 0L;
      // gameOver = GAME_LENGTH;
      // return;
    }
  }

  void pauseTimer() {
    if (isTimerRunning) {
      isTimerRunning = false;
      cmTimer.stop();
      pausedTime = SystemClock.elapsedRealtime();
    }
  }

  private void setTimerFromSharedPrefs() {
    String Time;
    // get shared prefs
    // someData = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
    // get the saved time string

    // game length
    if (someData.contains(GAME_LENGTH_TIME)) {
      setGameLength(someData.getInt(GAME_LENGTH_TIME, GAME_DEFAULT_LENGTH));
    }
    
    // vibrator setting
    if (someData.contains(VIBRATOR_STATE)) {
      isVibratorOn = someData.getBoolean(VIBRATOR_STATE, true);
    }
    
    // current time
    if (someData.contains(TIME_DISP_STR)) {
      Time = someData.getString(TIME_DISP_STR, "00:00");
      cmTimer.setText(Time);
    }
    // fill globals from shared prefs
    if (someData.contains(TIMER_PAUSED_TIME)) {
      pausedTime = someData.getLong(TIMER_PAUSED_TIME, 0);
    }

    if (someData.contains(TIMER_BASE_TIME)) {
      baseTime = someData.getLong(TIMER_BASE_TIME, 0);
    }
    // clear after data has been retrieved
    clearSharedPrefs();
  }

  private void setTimerToSharedPrefs() {
    Long pTime;
    Long bTime;

    // get shared prefs
    SharedPreferences.Editor editor = someData.edit();

    // game length
    if (!someData.contains(GAME_LENGTH_TIME)) {
      editor.putInt(GAME_LENGTH_TIME, getGameLength());
    }
    
    // vibrator setting
    if (!someData.contains(VIBRATOR_STATE)) {
      editor.putBoolean(VIBRATOR_STATE, isVibratorOn);
    }
    
    
    // only save when there is something to save - timer was not reset
    if (!isTimerReset) {

      // stop running timer
      if (isTimerRunning) {
        isTimerRunning = false;
        cmTimer.stop();
      }

      // get the saved time string
      if (!someData.contains(TIME_DISP_STR)) {
        editor.putString(TIME_DISP_STR, cmTimer.getText().toString());
      }

      // if base was already set save this one
      if (baseTime != 0L) {
        bTime = baseTime;
      } else {
        bTime = cmTimer.getBase();
      }

      // save globals to shared prefs
      if (!someData.contains(TIMER_BASE_TIME)) {
        editor.putLong(TIMER_BASE_TIME, bTime);
      }

      // is the timer currently paused
      if (pausedTime != 0L) {
        pTime = pausedTime;
      } else { // or not
        pTime = SystemClock.elapsedRealtime();
      }

      // fill globals from shared prefs
      if (!someData.contains(TIMER_PAUSED_TIME)) {
        editor.putLong(TIMER_PAUSED_TIME, pTime);
      }

    }
    editor.commit();
  }

  private void clearSharedPrefs() {
    // clean up shared preferences
    SharedPreferences.Editor editor = someData.edit();
    editor.clear();
    editor.commit();
  }

  protected void initNumberPicker(View promptsView) {
    String gL;
    int digit;

    npGameTenInd = (NumberPicker) promptsView.findViewById(R.id.npGameTenInd);
    npGameMinInd = (NumberPicker) promptsView.findViewById(R.id.npGameMinInd);

    npGameTenInd.setOnScrollListener(this);
    npGameTenInd.setMaxValue(4);
    npGameTenInd.setMinValue(0);
    npGameTenInd.setWrapSelectorWheel(true);
    npGameTenInd.setValue(getGameLength() / 10);

    npGameMinInd.setOnScrollListener(this);
    npGameMinInd.setMaxValue(9);
    npGameMinInd.setMinValue(0);
    npGameMinInd.setWrapSelectorWheel(true);

    // get the minutes
    gL = Integer.toString(getGameLength());
    if (gL.length() > 1) {
      digit = Integer.parseInt(gL.substring(1));
    } else {
      digit = Integer.parseInt(gL);
    }
    npGameMinInd.setValue(digit);
  }

  private void initSettingsDialog() {
    LayoutInflater inflater = (LayoutInflater) this
        .getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);

    View promptsView = inflater.inflate(R.layout.settings, null);

    alert = new AlertDialog.Builder(MainActivity.this).create();

    initNumberPicker(promptsView);

    cbVibrate = (CheckBox) promptsView.findViewById(R.id.cbVibrate);
    if(vib.hasVibrator()){
      cbVibrate.setChecked(isVibratorOn);  
    } else {
      isVibratorOn = false;
      cbVibrate.setChecked(false);
      cbVibrate.setEnabled(false);
    }
    

    // set prompts.xml to alertdialog builder
    alert.setView(promptsView);
    alert.setTitle("Settings");
    alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", this);
    alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", this);
    alert.setButton(AlertDialog.BUTTON_NEUTRAL, "Defaults", this);
    alert.show();
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
    case R.id.bStart:
      toggleTimer();
      break;
    case R.id.bReset:
      resetTimer();
      break;
    case R.id.tvGameLength:
      pauseTimer();
      initSettingsDialog();
      break;
    }
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    switch (which) {
    case AlertDialog.BUTTON_NEUTRAL:
      // set game length to default(45min) and close the settings dialog
      setGameLength(GAME_DEFAULT_LENGTH);
      if(!vib.hasVibrator()){
        isVibratorOn = false;  
      }else{
        isVibratorOn = true;  
      } 
      break;
    case AlertDialog.BUTTON_NEGATIVE:
      // do nothing and close the settings dialog
      break;
    case AlertDialog.BUTTON_POSITIVE:
      int newVal;
      // get the values from the number picker
      newVal = (npGameTenInd.getValue() * 10) + npGameMinInd.getValue();
      // set the game length and close the settings dialog
      setGameLength(newVal);
      if (cbVibrate.isChecked()) {
        isVibratorOn = true;
      } else {
        isVibratorOn = false;
      }

      break;
    }
  }

  @Override
  public void onChronometerTick(Chronometer chronometer) {
    int minutes, seconds;
    String array[];

    chronoText = chronometer.getText().toString();
    array = chronoText.split(":");
    minutes = Integer.parseInt(array[0]);
    seconds = Integer.parseInt(array[1]);

    if ((minutes % dmgReportLength) == 0 & (seconds == 0) & minutes != 0) {
      // play damage report sound + maybe screen flash
      if (sndDmgReport != 0) {
        spSounds.play(sndDmgReport, 1, 1, 0, 0, 1);
      }

      if (vib != null && isVibratorOn) {
        vib.vibrate(VIBRATE_DURATION);
      }
      // Toast.makeText(getApplicationContext(),
      // "3 Minute timer",Toast.LENGTH_SHORT).show();

    } else if (minutes == getGameLength()) {
      // play time is up sound + game over graphic
      if (sndGameOver != 0) {
        spSounds.play(sndGameOver, 1, 1, 0, 0, 1);
      }

      if (vib != null && isVibratorOn) {
        vib.vibrate(VIBRATE_DURATION * 2);
      }
      isTimerRunning = false;
      isTimerReset = true;
      cmTimer.stop();
      cmTimer.setText("00:00");
    }
  }

  @Override
  public void onScrollStateChange(NumberPicker view, int scrollState) {
    // disallow a game length of 0
    if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
      if (npGameTenInd.getValue() == 0 && npGameMinInd.getValue() == 0) {
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
      } else if (!alert.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled()) {
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
      }
    }
  }

  // options menu
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
    case R.id.action_about:
      Intent i = new Intent("de.schmarky.damagereporter.ABOUT");
      startActivity(i);
      return true;
    case R.id.action_settings:
      pauseTimer();
      initSettingsDialog();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * android.app.Activity#onConfigurationChanged(android.content.res.Configuration
   * )
   */
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    Boolean isRunning = isTimerRunning;

    // save data if timer is running
    // if (!isTimerReset) {
    setTimerToSharedPrefs();
    // }
    // set new layout and init vars
    setContentView(R.layout.activity_main);
    init();

    // load data if timer was running
    // if (!isTimerReset) {
    setTimerFromSharedPrefs();
    // }

    // start timer if it was running
    if (isRunning) {
      toggleTimer();
    }

    super.onConfigurationChanged(newConfig);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onCreate()
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    init();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onResume()
   */
  @Override
  protected void onResume() {
    super.onResume();
    setTimerFromSharedPrefs();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onPause()
   */
  @Override
  protected void onPause() {
    super.onPause();
    setTimerToSharedPrefs();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onDestroy()
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    clearSharedPrefs();
    setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onRestart()
   */
  @Override
  protected void onRestart() {
    // TODO Auto-generated method stub
    super.onRestart();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onStart()
   */
  @Override
  protected void onStart() {
    // TODO Auto-generated method stub
    super.onStart();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onStop()
   */
  @Override
  protected void onStop() {
    // TODO Auto-generated method stub
    super.onStop();
  }

  /*
   * Getter Setter Region
   */

  /**
   * @return the gameLength
   */
  public int getGameLength() {
    return gameLength;
  }

  /**
   * @param gameLength
   *          the gameLength to set
   */
  protected void setGameLength(int gameLength) {
    this.gameLength = gameLength;
    this.tvGameLength.setText(gameLength + ":00");
  }

}
