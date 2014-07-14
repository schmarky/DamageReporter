package de.schmarky.damagereporter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * @author schmarky schmarky@gmail.com
 * @version 1.0
 * @since 2014-06-10
 */
public class Settings extends Activity implements OnClickListener,
    OnEditorActionListener {

  private static final String LOG_TAG = "Damage_Reporter";

  private Intent thisIntent;
  EditText etGameLength;
  EditText etReportLength;
  CheckBox cbVibrate;

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);
    Log.e(LOG_TAG, "Settings2");

    // init
    thisIntent = getIntent();
    etGameLength = (EditText) findViewById(R.id.etGameLength);
    etReportLength = (EditText) findViewById(R.id.etReportLength);
    cbVibrate = (CheckBox) findViewById(R.id.cbVibrate);

    // get passed settings for this intent
    Bundle b = thisIntent.getExtras();
    boolean hasVibrator = b.getBoolean("hasVibrator");
    int gameLength = b.getInt("gameLength");
    int reportLength = b.getInt("reportLength");

    // logging
    // Log.e(LOG_TAG, ""+gameLength);
    // Log.e(LOG_TAG, ""+reportLength);

    // set the values to the view
    etGameLength.setText("" + gameLength);
    etReportLength.setText("" + reportLength);
    if (hasVibrator) {
      cbVibrate.isEnabled();
    }

    // set listeners
    etGameLength.setOnClickListener(this);
    etReportLength.setOnClickListener(this);
    cbVibrate.setOnClickListener(this);
    //
    etGameLength.setOnEditorActionListener(this);
    etReportLength.setOnEditorActionListener(this);

  }

  /*
   * (non-Javadoc)
   * 
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   */
  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.etGameLength:
      Log.e(LOG_TAG, "etGameLength");
      thisIntent.putExtra("gameLength", etGameLength.getText());
      break;
    case R.id.etReportLength:
      Log.e(LOG_TAG, "etReportLength");
      thisIntent.putExtra("reportLength", etReportLength.getText());
      break;
    case R.id.cbVibrate:
      Log.e(LOG_TAG, "cbVibrate");
      break;
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * android.widget.TextView.OnEditorActionListener#onEditorAction(android.widget
   * .TextView, int, android.view.KeyEvent)
   */
  @Override
  public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    boolean handeled;
    Log.e(LOG_TAG, "onEditorAction");
    Log.e(LOG_TAG, "onEditorAction" + v.getId());
    Log.e(LOG_TAG, "onEditorAction" + event);

    Log.e(LOG_TAG, "GetId: "+v.getId());
    Log.e(LOG_TAG, "R.id.etGameLength: "+R.id.etGameLength);
    Log.e(LOG_TAG, "R.id.etReportLength: "+R.id.etReportLength);
    
    // put new time to intent  
    switch (v.getId()) {
    case R.id.etGameLength:
      thisIntent.putExtra("gameLength", v.getText().toString());
      Log.e(LOG_TAG, "onEditorAction - gameLength: " + v.getText().toString());
      handeled = true;
      break;
    case R.id.etReportLength:
      thisIntent.putExtra("reportLength", v.getText().toString());
      Log.e(LOG_TAG, "onEditorAction - reportLength: " + v.getText().toString());
      handeled = true;
      break;
    default:
      handeled = true;
      break;
    }
    
    // click done on softkeyboard
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      // set the result of this intent to OK
      // since we have changes something
      setResult(RESULT_OK, thisIntent);
      hide_keyboard(this);
    }
    return handeled;
  }

  /* (non-Javadoc)
   * @see android.app.Activity#onDestroy()
   */
  @Override
  protected void onDestroy() {
    Log.e(LOG_TAG,"onDestroy");
    super.onDestroy();
  }

  /* (non-Javadoc)
   * @see android.app.Activity#onPause()
   */
  @Override
  protected void onPause() {
    Log.e(LOG_TAG,"onPause");
    super.onPause();
  }

  /* (non-Javadoc)
   * @see android.app.Activity#onStop()
   */
  @Override
  protected void onStop() {
    Log.e(LOG_TAG,"onStop");
    super.onStop();
  }

  // thanks to rmirabelle (http://stackoverflow.com/users/680583/rmirabelle)
  /**
   * @param activity
   */
  public static void hide_keyboard(Activity activity) {
    InputMethodManager inputMethodManager = (InputMethodManager) activity
        .getSystemService(Activity.INPUT_METHOD_SERVICE);
    // Find the currently focused view, so we can grab the correct window token
    // from it.
    View view = activity.getCurrentFocus();
    // If no view currently has focus, create a new one, just so we can grab a
    // window token from it
    if (view == null) {
      view = new View(activity);
    }
    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

}
