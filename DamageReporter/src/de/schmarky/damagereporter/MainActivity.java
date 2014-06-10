package de.schmarky.damagereporter;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.support.v4.app.DialogFragment;

public class MainActivity extends Activity implements View.OnClickListener,
    Chronometer.OnChronometerTickListener {

  private static final String TIMER_PAUSED_TIME = "PausedTime";
  // constants
  private static final String TIME_DISP_STR = "TimeDispStr";
  private static final String TIMER_BASE_TIME = "base";
  private static final String LOG_TAG = "Damage_Reporter";
  private static final String FILE_NAME = "MySharedPrefs";

  // globals
  boolean isTimerReset = true;
  boolean isTimerRunning = false;

  int dmgReport = 0;
  long pausedTime = 0L;
  long baseTime = 0L;
  String chronoText;

  SharedPreferences someData;
  Button bStart, bReset;
  Chronometer cmTimer;
  SoundPool spSounds;

  private void init() {
    setContentView(R.layout.activity_main);

    cmTimer = (Chronometer) findViewById(R.id.cmTimer);
    bStart = (Button) findViewById(R.id.bStart);
    bReset = (Button) findViewById(R.id.bReset);
    spSounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    dmgReport = spSounds.load(this, R.raw.damagereport, 1);
    someData = getSharedPreferences(FILE_NAME, MODE_PRIVATE);

    // start button
    bStart.setOnClickListener(this);
    // reset button
    bReset.setOnClickListener(this);
    // timer display
    cmTimer.setOnChronometerTickListener(this);
  }

  @Override
  public void onChronometerTick(Chronometer chronometer) {
    Integer minutes, seconds;
    String array[];

    chronoText = cmTimer.getText().toString();
    array = chronoText.split(":");
    // Log.e(LOG_TAG, "arra0: "+array[0]);
    // Log.e(LOG_TAG, "arra1: "+array[1]);
    minutes = Integer.parseInt(array[0]);
    seconds = Integer.parseInt(array[1]);

    if ((minutes % 3) == 0 & (seconds == 0) & minutes != 0) {
      // play damage report sound + maybe screen flash
      if (dmgReport != 0) {
        spSounds.play(dmgReport, 1, 1, 0, 0, 1);
      }
      // Toast.makeText(getApplicationContext(),
      // "3 Minute timer",Toast.LENGTH_SHORT).show();
      Log.e(LOG_TAG, "3 minute timer");

    } else if (minutes == 45) {
      // play time is up sound + game over graphic
      isTimerRunning = false;
      isTimerReset = true;
      cmTimer.stop();
      cmTimer.setText("00:00");
      Log.e(LOG_TAG, "45 minute timer");
    }
  }

  @Override
  public void onClick(View view) {
    // TODO Auto-generated method stub
    switch (view.getId()) {
    case R.id.bStart:
      toggleTimer();
      break;
    case R.id.bReset:
      resetTimer();
      break;
    }
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
      Log.e(LOG_TAG, "start after reset");
      cmTimer.setBase(SystemClock.elapsedRealtime());
      cmTimer.start();
      isTimerReset = false;
      isTimerRunning = true;
      // return;
    }
    // pause click
    else if (isTimerRunning) {
      isTimerRunning = false;
      cmTimer.stop();
      pausedTime = SystemClock.elapsedRealtime();
    }
    // start timer after a resume
    else if (pausedTime != 0L && !isTimerRunning) {
      // get the base time
      if (baseTime != 0L) {
        base = baseTime;
        Log.e(LOG_TAG, "start after resume");
        Log.e(LOG_TAG, "resume Diff: " + (pausedTime - baseTime));
      } else {
        base = cmTimer.getBase();
        Log.e(LOG_TAG, "start after pause");
      }
      cmTimer.setBase((SystemClock.elapsedRealtime() - (pausedTime - base)));
      cmTimer.start();
      isTimerRunning = true;
      pausedTime = 0L;
      baseTime = 0L;
      // return;
    }
  }

  private void setTimerFromSharedPrefs() {
    String Time;
    // get shared prefs
    someData = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
    // get the saved time string
    Time = someData.getString(TIME_DISP_STR, "#na");
    Log.e(LOG_TAG, "TimeStr: " + Time);

    // only load when there is something to load
    if (Time != "#na") {
      // fill globals from shared prefs
      pausedTime = someData.getLong(TIMER_PAUSED_TIME, 0);
      baseTime = someData.getLong(TIMER_BASE_TIME, 0);
      cmTimer.setText(Time);
      Log.e(LOG_TAG, "base: " + baseTime);
      Log.e(LOG_TAG, "pause: " + pausedTime);
      Log.e(LOG_TAG, "TimeStr: " + Time);
    }
  }

  private void setTimerToSharedPrefs() {
    // logging
    Log.e(LOG_TAG, "base Time: " + cmTimer.getBase());
    Log.e(LOG_TAG, "Paused Time: " + pausedTime);
    Log.e(LOG_TAG, "TimeString: " + cmTimer.getText().toString());

    // stop running timer
    if (isTimerRunning) {
      isTimerRunning = false;
      cmTimer.stop();
    }

    // only save when there is something to save - timer was not reset
    if (!isTimerReset) {
      // save globals to shared prefs
      SharedPreferences.Editor editor = someData.edit();
      editor.putString(TIME_DISP_STR, cmTimer.getText().toString());
      editor.putLong(TIMER_BASE_TIME, cmTimer.getBase());

      // is the timer currently paused
      if (pausedTime != 0L) {
        Log.e(LOG_TAG, "put pause time");
        editor.putLong(TIMER_PAUSED_TIME, pausedTime);
      } else { // or not
        Log.e(LOG_TAG, "put elapsedRealtime");
        editor.putLong(TIMER_PAUSED_TIME, SystemClock.elapsedRealtime());
      }
      editor.commit();
    }
  }

  private void clearSharedPrefs() {
    // clean up shared preferences
    SharedPreferences.Editor editor = someData.edit();
    editor.clear();
    editor.commit();
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
    setTimerToSharedPrefs();
    setContentView(R.layout.activity_main);
    init();
    setTimerFromSharedPrefs();
    toggleTimer();
    super.onConfigurationChanged(newConfig);
    Log.e(LOG_TAG, "onConfigurationChanged()");
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

      LayoutInflater inflater = (LayoutInflater) this
          .getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);

      View promptsView = inflater.inflate(R.layout.settings, null);

      AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();

      // set prompts.xml to alertdialog builder
      alert.setView(promptsView);

      alert.setTitle("Set Game length");
      // alert.setMessage("Minutes:");
      alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
          new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
              // TODO Auto-generated method stub

            }
          });

      alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
          new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
              // TODO Auto-generated method stub

            }
          });
      alert.show();
      /*
       * // get prompts.xml view LayoutInflater li = LayoutInflater.from(this);
       * View promptsView = li.inflate(R.layout.settings, null);
       * 
       * AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( new
       * ContextThemeWrapper(this, R.style.AppBaseTheme)); //AlertDialog.Builder
       * alertDialogBuilder = new AlertDialog.Builder(this);
       * 
       * 
       * // set prompts.xml to alertdialog builder
       * alertDialogBuilder.setView(promptsView);
       * 
       * 
       * // set dialog message
       * alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new
       * DialogInterface.OnClickListener() { public void onClick(DialogInterface
       * dialog,int id) { // get user input and set it to result } })
       * .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
       * public void onClick(DialogInterface dialog,int id) { dialog.cancel(); }
       * });
       * 
       * // create alert dialog AlertDialog alertDialog =
       * alertDialogBuilder.create();
       * 
       * // show it alertDialog.show();
       */

      /*
       * Dialog dlgSettings = new Dialog(this);
       * dlgSettings.setContentView(R.layout.settings);
       * dlgSettings.setTitle("Settings"); dlgSettings.show();
       */

      /*
       * Intent settings = new Intent("de.schmarky.damagereporter.SETTINGS");
       * startActivity(settings);
       */
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onCreate()
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.e(LOG_TAG, "onCreate");
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
    Log.e(LOG_TAG, "OnResume()");
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
    Log.e(LOG_TAG, "OnPause()");
    setTimerToSharedPrefs();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onDestroy()
   */
  @Override
  protected void onDestroy() {
    // TODO Auto-generated method stub
    super.onDestroy();
    Log.e(LOG_TAG, "onDestroy");
    clearSharedPrefs();
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
    Log.e(LOG_TAG, "onRestart");
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
    Log.e(LOG_TAG, "onStart");
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
    Log.e(LOG_TAG, "onStop");
  }

}
