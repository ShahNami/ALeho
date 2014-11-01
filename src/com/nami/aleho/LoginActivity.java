package com.nami.aleho;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A login screen that offers login via email/password.

 */
public class LoginActivity extends Activity implements OnClickListener{


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String username, password;
    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;
    private Button ok;
    private Map<String, String> cookies;
    private ProgressDialog mProgressDialog;
    private double newVersion = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        cookies = new HashMap<String, String>();
        setTitle("ALeho");

        ok = (Button)findViewById(R.id.email_sign_in_button);
        ok.setOnClickListener(this);
        mEmailView = (EditText)findViewById(R.id.email);
        mPasswordView = (EditText)findViewById(R.id.password);
        saveLoginCheckBox = (CheckBox)findViewById(R.id.cbxRemember);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            mEmailView.setText(loginPreferences.getString("username", ""));
            try {
                mPasswordView.setText(decrypt(loginPreferences.getString("password", "")));
            } catch (Exception e) {
                //e.printStackTrace();
            }
            saveLoginCheckBox.setChecked(true);
        }
        mLoginFormView = findViewById(R.id.email_login_form);
        mProgressView = findViewById(R.id.login_progress);
        
        new DeleteTask().execute("");
		Toast.makeText(LoginActivity.this, "Checking for updates...",Toast.LENGTH_LONG).show();
		new UpdateTask().execute("");
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 2;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private boolean login(String username, String password) throws InterruptedException
    {
        try
        {
            Connection.Response res = Jsoup.connect("https://leho.howest.be/secure/index.php").data(new String[] { "login", username, "password", password, "submitAuth", "OK", "_qf__formLogin", "" }).method(Connection.Method.POST).execute();
            if (res.parse().select("#login_fail").size() > 0) {
            	return false;
            } else {
                cookies = res.cookies();
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onClick(View view) {
        if (view == ok) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEmailView.getWindowToken(), 0);

            username = mEmailView.getText().toString();
            password = mPasswordView.getText().toString();

            if (saveLoginCheckBox.isChecked()) {
                loginPrefsEditor.putBoolean("saveLogin", true);
                loginPrefsEditor.putString("username", username);
                try {
                   loginPrefsEditor.putString("password", encrypt(password));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                loginPrefsEditor.commit();
            } else {
                loginPrefsEditor.clear();
                loginPrefsEditor.commit();
            }
            attemptLogin();
        }
    }

    private String encrypt(String password) {
        byte[] bytesEncoded = Base64.encodeBase64(password.getBytes());
        return new String(bytesEncoded);
    }

    private String decrypt(String password) throws UnsupportedEncodingException {
        // Decrypt data on other side, by processing encoded data
        byte[] valueDecoded= Base64.decodeBase64(password.getBytes());
        return new String(valueDecoded, "UTF-8");
    }
    
    
    private class DeleteTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			boolean deleted;
			int k = Integer.parseInt(String.valueOf(Constants.VERSION)
					.replace(".", ""));
			for (int i = 0; i < k; i++) {
				int major = 0;
				int minor = 0;
				int l = String.valueOf(i).length();
				if (l == 1) {
					major = 0;
					minor = i;
				} else {
					major = Integer.parseInt(String.valueOf(i).substring(0,
							l - 1));
					minor = Integer.parseInt(String.valueOf(i).substring(l - 1,
							l));
				}
				File file = new File(Constants.DOWNLOAD_PATH
						+ Constants.SAVE_AS + major + "." + minor + ".apk");
				if (file.exists()) {
					deleted = file.delete();
				}
			}
			return null;
		}
	}
    
	private boolean checkForUpdate()
			throws PackageManager.NameNotFoundException, IOException {
		//Get current Version Number
		Document doc = Jsoup.connect(Constants.VERSION_URL)
				.ignoreContentType(true).timeout(10 * 1000).get();
		Elements tagList = doc.select(".tag-name");
		if (Constants.VERSION < Double.valueOf(tagList.first().html())) {
			// Download new version
			newVersion = Double.valueOf(tagList.first().html());
			return true;
		}
		return false;
	}

	private class UpdateTask extends AsyncTask<String, Void, String> {
		Boolean needsUpdate = false;

		@Override
		protected String doInBackground(String... params) {
			try {
				needsUpdate = checkForUpdate();
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (needsUpdate) {
				// instantiate it within the onCreate method
				mProgressDialog = new ProgressDialog(LoginActivity.this);
				mProgressDialog.setMessage("Downloading Newer Version");
				mProgressDialog.setIndeterminate(true);
				mProgressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.setCancelable(false);
				final DownloadTask downloadTask = new DownloadTask(
						LoginActivity.this);
				// execute this when the downloader must be fired
				downloadTask
						.execute("https://github.com/ShahNami/ALeho/releases/download/" +
								+ newVersion
								+ "/ALeho"
								+ newVersion
								+ ".apk");
				mProgressDialog
						.setOnCancelListener(new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								downloadTask.cancel(false);
							}
						});
			}
		}
	}

	private class DownloadTask extends AsyncTask<String, Integer, String> {

		private Context context;
		private PowerManager.WakeLock mWakeLock;

		public DownloadTask(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(String... sUrl) {
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try {
				URL url = new URL(sUrl[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();

				// expect HTTP 200 OK, so we don't mistakenly save error report
				// instead of the file
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					return "Server returned HTTP "
							+ connection.getResponseCode() + " "
							+ connection.getResponseMessage();
				}

				// this will be useful to display download percentage
				// might be -1: server did not report the length
				int fileLength = connection.getContentLength();

				// download the file
				input = connection.getInputStream();
				output = new FileOutputStream(Constants.DOWNLOAD_PATH
						+ Constants.SAVE_AS + newVersion + ".apk");
				if (fileLength > 0) {
					byte data[] = new byte[fileLength];
					long total = 0;
					int count;
					while ((count = input.read(data)) != -1) {
						// allow canceling with back button
						if (isCancelled()) {
							input.close();
							return null;
						}
						total += count;
						// publishing the progress....
						if (fileLength > 0) // only if total length is known
							publishProgress((int) (total * 100 / fileLength));
						output.write(data, 0, count);
					}
				} else {
					return "Unable to download new version";
				}
			} catch (Exception e) {
				return e.toString();
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				} catch (IOException ignored) {
				}

				if (connection != null)
					connection.disconnect();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// take CPU lock to prevent CPU from going off if the user
			// presses the power button during download
			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					getClass().getName());
			mWakeLock.acquire();
			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			// if we get here, length is known, now set indeterminate to false
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			mWakeLock.release();
			mProgressDialog.dismiss();
			if (result != null) {
				Toast.makeText(context, "Download error: " + result,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT)
						.show();
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(
						Uri.fromFile(new File(Constants.DOWNLOAD_PATH
								+ "ALeho" + newVersion + ".apk")),
						"application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);

			}
		}
	}

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return login(mEmail, mPassword);
            } catch (InterruptedException e) {
                //e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            if (success) {
                finish();
                Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                    myIntent.putExtra("cookie", new String[]{cookie.getKey(), cookie.getValue()});
                }
                LoginActivity.this.startActivity(myIntent);
                //overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
            } else {
                if(!isNetworkAvailable()){
                    Toast.makeText(LoginActivity.this, "Check your internet connection.", Toast.LENGTH_SHORT).show();
                    mPasswordView.requestFocus();
                } else {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
            }
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}