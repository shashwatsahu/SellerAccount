package com.example.hp.selleraccount;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.PACKAGE_USAGE_STATS;
import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static String TAG = "LoginActivity";

    private static final int REQUEST_READ_CONTACTS = 0;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private static final String ADDRESS_REQUEST_KEY = "address-request-pending";
    private static final String LOCATION_ADDRESS_KEY = "location-address";


    //Firebase Login references...

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    public FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    public FirebaseUser firebaseUser;
    private List<AuthUI.IdpConfig> providers;

    private String[] strings;

    //fetch information...
    private Location location;
    private boolean mAddressRequested;

    private String mAddressOutput;
    private AddressResultReceiver mResultReceiver;
    private TextView mLocationAddressTextView;
    private FusedLocationProviderClient mFusedLocationClient;

    private static final int RC_SIGN_IN = 123;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private AutoCompleteTextView mEmailView, city, state, address, country;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private EditText nameOfPerson, nameOfShop, mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        providers = Arrays.asList(new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        firebaseUser = mAuth.getCurrentUser();

        nameOfPerson = findViewById(R.id.name);
        nameOfShop = findViewById(R.id.name_of_shop);
        mobile = findViewById(R.id.mobile);
        city = findViewById(R.id.city);
        state = findViewById(R.id.state);
        address = findViewById(R.id.address);
        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);

        if(FirebaseAuth.getInstance().getUid() == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);

        }
        else {
            checkRegistration();
        }

        setContentView(R.layout.activity_login);

        nameOfPerson = findViewById(R.id.name);
        nameOfShop = findViewById(R.id.name_of_shop);
        mobile = findViewById(R.id.mobile);
        city = findViewById(R.id.city);
        state = findViewById(R.id.state);
        address = findViewById(R.id.address);
        country = findViewById(R.id.country);
        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        if(FirebaseAuth.getInstance().getUid() != null) {
            nameOfPerson.setText(firebaseUser.getDisplayName());
            mEmailView.setText(firebaseUser.getEmail());
            mobile.setText(firebaseUser.getPhoneNumber());

            Log.i(TAG, "Name of person:" + firebaseUser.getDisplayName() + " PhoneNo. " + firebaseUser.getPhoneNumber());
        }
        else
            Log.i(TAG, "Value is null");

        updateValuesFromBundle(savedInstanceState);

        mResultReceiver = new AddressResultReceiver(new Handler());

        mAddressRequested = false;
        mAddressOutput = "";

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //firebase login initializing...

        Log.i(TAG, "Auth:" + mAuth.getUid());

        // Create and launch sign-in intent

        // Set up the login form.


        if (location != null) {
            startIntentService();
            Log.i(TAG, "location not null");
            return;
        }
        else
            Log.i(TAG, "Location is null");

        mAddressRequested = true;

        Button mEmailSignInButton =  findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

       }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "updateValues");
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(ADDRESS_REQUEST_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUEST_KEY);
            }
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                displayAddressOutput();
            }
        }
    }

    private void displayAddressOutput(){

        //address.setText(mAddressOutput);
        Log.i(TAG, "1String:" + mAddressOutput);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
               // updateUI(firebaseUser);
                //  Log.i(TAG, "OnCreateResult:" + firebaseUser.getUid());
                // ...
            } else {

                Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
              //  setAdapter();
                // Sign in failed, check response for error code
                // ...
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else
            getAddress();

        //Check if user is signed in and update UI accordingly.
        firebaseUser = mAuth.getCurrentUser();
        if(mAuth.getUid() != null)
            Log.w(TAG, "oncreate4:" + firebaseUser.getUid());

    }

    private boolean checkPermissions(){
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions(){
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(shouldProvideRationale){
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
                }
            });
        } else{
            Log.i(TAG, "Requesting permission");
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void startIntentService() {
        Log.i(TAG, "startIntentService");
        Intent intent = new Intent(LoginActivity.this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    private void getAddress() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(LoginActivity.this, "not granted",Toast.LENGTH_SHORT).show();
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location mlocation) {
                if (mlocation == null) {
                    Log.i(TAG, "onSuccess:null");
                    return;
                }

                location = mlocation;

                if(!Geocoder.isPresent()){
                    showSnackbar(getString(R.string.no_geocoder_available));
                    return;
                }

                    startIntentService();

            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "getLastLocation:onFailure",e);
            }
        });
    }

    private void showSnackbar(final String text){
        View container = findViewById(android.R.id.content);
        if(container != null){
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener){
        Snackbar.make(findViewById(android.R.id.content), getString(mainTextStringId),Snackbar.LENGTH_INDEFINITE).setAction(getString(actionStringId), listener).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        Log.i(TAG,"onRequestPermissionResult");
        if(requestCode == REQUEST_PERMISSIONS_REQUEST_CODE){
            if(grantResults.length <= 0){
                Log.i(TAG,"User interaction was cancelled.");
            }
            else if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getAddress();
            }
            else{
                showSnackbar(R.string.permission_denied_explanation, R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }
        }
    }


    private void checkRegistration() {

    }

    /**
     * Callback received when a permissions request has been completed.
     */


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

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

            if((mEmailView.length()>0) && (nameOfPerson.length() > 0)&& (nameOfShop.length() > 0)&& (mobile.length() > 0)
                    && (mPasswordView.length() > 0)&& (city.length() > 0)&& (state.length() > 0)
                    && (address.length() > 0)){
                String nameOfShop = nameOfPerson.getText().toString().trim();
                String merchant = nameOfPerson.getText().toString().trim();
                String mobileNo = mobile.getText().toString().trim();
                String cityName = city.getText().toString().trim();
                String stateName = state.getText().toString().trim();
                String latitude = strings[2];
                String longitude = strings[1];

              Log.i(TAG, "name" + merchant + " shopName: " + nameOfShop + " mobile:" + mobileNo + " city:" + cityName
               + " stateName: " + stateName);

                // FirebaseDatabase.getInstance().getReference().child("Shops_database").child("Shops_info").child(firebaseUser.getUid()).child("info").updateChildren();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
            else {
                Toast.makeText(this, "Complete All Details", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class AddressResultReceiver extends android.os.ResultReceiver {

        AddressResultReceiver(Handler handler){
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            displayAddressOutput();

            if(resultCode == Constants.SUCCESS_RESULT){
                showLog(getString(R.string.address_found));

                 strings = resultData.getStringArray(Constants.LIST_OF_DATA);
                if(strings != null) {
                    Log.i(TAG, "Lat" + strings[1] + " Lon: " + strings[2]);
                    address.setText(strings[0]);
                    state.setText(strings[6]);
                    country.setText(strings[7]);
                    city.setText(strings[8]);
                }
            }

            mAddressRequested = false;
            //updateUIWidgets();

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean(ADDRESS_REQUEST_KEY, mAddressRequested);

        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);
        super.onSaveInstanceState(savedInstanceState);
    }
    //Extending ResultReceiver class and implementing onReceiverResult()

    private void showLog(String text){
        Log.i(TAG, text);
    }

}

