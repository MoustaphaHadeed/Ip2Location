package com.mustaphahadid.ip2location;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import static android.text.TextUtils.isEmpty;

public class MainActivity extends AppCompatActivity {

  public static final String URL_API = "http://ip-api.com/json/%s";
  public static final String STATUS_FAILED = "fail";
  private EditText mIpEditText;
  private OkHttpClient mClient;
  private TextView mCityTextView;
  private TextView mCountryTextView;
  private TextView mIspTextView;
  private ProgressBar mSearchProgressBar;
  private Button mSearchButton;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mIpEditText = ((EditText) findViewById(R.id.ip_address));
    mCityTextView = ((TextView) findViewById(R.id.text_city));
    mCountryTextView = (TextView) findViewById(R.id.text_country);
    mIspTextView = (TextView) findViewById(R.id.text_isp);
    mSearchProgressBar = (ProgressBar) findViewById(R.id.progress_bar_search);
    mSearchButton = (Button) findViewById(R.id.button_search);

    mClient = new OkHttpClient();
  }

  public void onSearchBtnClicked(View view) {
    // Clear fields
    mCountryTextView.setText(null);
    mCityTextView.setText(null);
    mIspTextView.setText(null);

    // Check for validity
    String ipAddress = mIpEditText.toString();
    if (isEmpty(ipAddress)) {
      mIpEditText.setError(getString(R.string.msg_empty_field));
      return;
    }
    //else if (!Patterns.IP_ADDRESS.matcher(ipAddress).matches()) {
    //  // Not a valid ip address
    //  mIpEditText.setError(getString(R.string.msg_not_a_valid_ip_address));
    //  return;
    //}

    // Show Progress bar
    mSearchProgressBar.setVisibility(View.VISIBLE);
    mSearchButton.setVisibility(View.GONE);

    // Make http request
    String url = String.format(URL_API, mIpEditText.getText());
    Request request = new Request.Builder().url(url).build();
    mClient.newCall(request).enqueue(new Callback() {

      private Context mContext = MainActivity.this;
      private Runnable mRunnableHideProgressBar = new Runnable() {
        @Override public void run() {
          mSearchProgressBar.setVisibility(View.GONE);
          mSearchButton.setVisibility(View.VISIBLE);
        }
      };

      @Override public void onFailure(Call call, IOException e) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(R.string.title_http_request_failed)
            .setMessage(R.string.msg_http_request_failed)
            .setPositiveButton(R.string.action_ok, null)
            .show();
      }

      @Override public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful()) {
          AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
          alertDialogBuilder.setTitle(R.string.title_http_error_response)
              .setMessage(R.string.msg_http_error_response)
              .setPositiveButton(R.string.action_ok, null)
              .show();

          // Toggle progress bar to search button
          runOnUiThread(mRunnableHideProgressBar);

          return;
        }

        // Check IP validity
        String responseString = response.body().string();
        try {
          String status = new JSONObject(responseString).getString("status");
          if (status.equals(STATUS_FAILED)) {
            runOnUiThread(new Runnable() {
              @Override public void run() {
                mIpEditText.setError(getString(R.string.msg_not_a_valid_ip_address));

                // Toggle progress bar to search button
                mSearchProgressBar.setVisibility(View.GONE);
                mSearchButton.setVisibility(View.VISIBLE);
              }
            });
          }
        } catch (JSONException e) {
          runOnUiThread(mRunnableHideProgressBar);
          e.printStackTrace();
          return;
        }

        // Convert JSON to java object
        Gson gson = new GsonBuilder().setLenient().create();
        final Location location = gson.fromJson(responseString, Location.class);

        // Update UI
        runOnUiThread(new Runnable() {
          @Override public void run() {
            mCityTextView.setText(location.getCity());
            mCountryTextView.setText(location.getCountry());
            mIspTextView.setText(location.getIsp());

            // Toggle progress bar to search button
            mSearchProgressBar.setVisibility(View.GONE);
            mSearchButton.setVisibility(View.VISIBLE);
          }
        });
      }
    });
  }
}
