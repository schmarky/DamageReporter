package de.schmarky.damagereporter;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.NumberPicker;

public class Settings extends Activity implements  NumberPicker.OnValueChangeListener {
  
  final String LOG_TAG = "Damage_Reporter";
  
  NumberPicker npGameTenInd;
  NumberPicker npGameMinInd;
  NumberPicker npRepMinutes;
  
  /* (non-Javadoc)
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.settings);
    
    Log.e("LOG_TAG", "onCreate()");
    
    npGameMinInd = (NumberPicker) findViewById(R.id.npGameMinInd);
    npGameTenInd = (NumberPicker) findViewById(R.id.npGameTenInd);
    //npRepMinutes = (NumberPicker) findViewById(R.id.npRepMinutes);
    
    npGameTenInd.setMinValue(0);
    npGameTenInd.setMaxValue(4);  
    npGameTenInd.setValue(4);
    
    npGameMinInd.setMinValue(0);
    npGameMinInd.setMaxValue(9);  
    npGameMinInd.setValue(5);
    //npRepMinutes.setValue(3);
    
    npGameTenInd.setOnValueChangedListener(this);
    npGameMinInd.setOnValueChangedListener(this);
    //npRepMinutes.setOnValueChangedListener(this);
  }

  @Override
  public void onValueChange(NumberPicker obj, int oldVal, int newVal) {
    
    Log.e(LOG_TAG, obj.toString());
    Log.e(LOG_TAG, ""+oldVal);
    Log.e(LOG_TAG, ""+newVal);
    
    switch (obj.getId()) {
    case R.id.npGameMinInd:
      Log.e("LOG_TAG", "npGameMinInd");
      break;
    case R.id.npGameTenInd:
      Log.e("LOG_TAG", "npGameTenInd");
      break;
    case R.id.npRepMinutes:
      Log.e("LOG_TAG", "npRepMinutes");
      break;

    default:
      break;
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
    // TODO Auto-generated method stub
    super.onConfigurationChanged(newConfig);
    Log.e(LOG_TAG, "onConfigurationChanged()");
  }
}
