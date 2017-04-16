package com.mustaphahadid.ip2location;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import static android.text.TextUtils.isEmpty;

public class MainActivity extends AppCompatActivity {

  public static final String URL_API = "http://ip-api.com/json/%s";
  private EditText mIpEditText;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mIpEditText = ((EditText) findViewById(R.id.ip_address));
  }

  public void onSearchBtnClicked(View view) {
    String ipAddress = mIpEditText.toString();

    if (isEmpty(ipAddress)) {
      mIpEditText.setError(getString(R.string.msg_empty_field));
      return;
    }

    // Make a request to http://ip-api.com/json/{ipAddress}
  }
}
