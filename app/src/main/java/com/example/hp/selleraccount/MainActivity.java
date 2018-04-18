package com.example.hp.selleraccount;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    Button b1;

    private static final String TAG = "MAINACTIVITY";

    private static final int RC_SIGN_IN = 123;

    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int LIST_PENDING_ID = 100;
    private static final String LIST_NOTIFICATION_ID = "reminder_notification_channel";
    private static final int LIST_NOTIFICATION_CHANNEL_ID = 101;
    private static final int LIST_REMINDER_NOTIFICATION_ID = 102;

    private String[] mListTitles;
    ArrayList<DrawerList> arrayList;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private Toolbar toolbar;

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    public FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private List<AuthUI.IdpConfig> providers;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    public FirebaseUser firebaseUser;

    private TextView userName, emailID;
    private ImageView userProfile;

    private SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName = findViewById(R.id.name_id);
        emailID =  findViewById(R.id.email_id);
        userProfile = findViewById(R.id.image_id);
        //Shimmer effect...

       /* ShimmerFrameLayout container =
                (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container);
        container.startShimmerAnimation();*/

        providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(), new AuthUI.IdpConfig.FacebookBuilder().build(), new AuthUI.IdpConfig.GoogleBuilder().build());


        //Making database object...

            if(isNetworkConnected()) {
                Toast.makeText(this, "Welcomes U!", Toast.LENGTH_SHORT).show();

                    //Firebase OAuth starts here...
                    mAuth = FirebaseAuth.getInstance();
                Log.i(TAG, "hello Im working!");

                if (mAuth.getCurrentUser() != null) {
                    Bitmap bitmap;
                    InputStream is = null;
                    BufferedInputStream bis = null;

                    firebaseUser = mAuth.getCurrentUser();
                    try {

                        userName.setText(firebaseUser.getDisplayName());
                        emailID.setText(firebaseUser.getEmail());
                        Uri personPhoto = firebaseUser.getPhotoUrl();

                        URLConnection conn = new URL(personPhoto.toString()).openConnection();
                        conn.connect();
                        is = conn.getInputStream();
                        bis = new BufferedInputStream(is, 8192);
                        bitmap = BitmapFactory.decodeStream(bis);
                        userProfile.setImageBitmap(bitmap);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (bis != null) {
                            try {
                                bis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }

                //Firstly Iam creating an Instance of database this is the User's Id.

                toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

                //SearchView coding...
                ImageButton searchView = toolbar.findViewById(R.id.search_button);
                searchView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                        startActivity(intent);
                    }
                });

                //mListTitles = getResources().getStringArray(R.array.navigation_list);
                arrayList = getArrayList();

                mTitle = mDrawerTitle = getTitle();
                mDrawerLayout = findViewById(R.id.drawer_layout);
                mDrawerList = findViewById(R.id.list_view);

                mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

                mDrawerList.setAdapter(new DrawerAdapter(this, arrayList));

                //Action Bar Drawer Toggle

                mDrawerToggle = getActionBarDrawerToggle();

                mDrawerLayout.addDrawerListener(mDrawerToggle);
                try {
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setHomeButtonEnabled(true);
                } catch (Exception e) {
                    Log.i(TAG, "3Exception:" + e);
                }
                mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                        switch (position) {
                            case 0:
                                addNotification();
                                break;
                            case 1:

                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            case 4:
                                break;
                            case 5:
                                startActivity(new Intent(MainActivity.this, SellerAccount.class));
                                break;
                            case 6:
                                break;
                            case 7:
                                break;
                            case 8:
                                break;
                            case 9:
                                Toast.makeText(MainActivity.this, "Log Out Successfully!", Toast.LENGTH_SHORT).show();

                                AuthUI.getInstance(FirebaseApp.getInstance())
                                        .signOut(MainActivity.this)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            public void onComplete(@NonNull Task<Void> task) {
                                                // ...
                                                Log.i(TAG, "Log out:" + position);
                                                startActivity(
                                                        AuthUI.getInstance()
                                                                .createSignInIntentBuilder()
                                                                .setAvailableProviders(providers)
                                                                .build());
                                                Log.i(TAG, "Logout");
                                            }
                                        });
                                break;
                        }
                    }
                });


                //Navigation Drawer Button
                ImageButton navButton = toolbar.findViewById(R.id.nav_button);
                navButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDrawerLayout.openDrawer(Gravity.START);
                    }
                });

                if (mAuth.getCurrentUser() != null) {
                    updateUI(mAuth.getCurrentUser());
                }

                //Query data from database which is readable...
            }
            else{
                Toast.makeText(MainActivity.this, "Failed to connect!", Toast.LENGTH_SHORT).show();
            }
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
               // firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
               // Log.i(TAG, "OnCreateResult:" + firebaseUser.getUid());
                // ...
            } else {

                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                // Sign in failed, check response for error code
                // ...

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Check if user is signed in and update UI accordingly.
        if(mAuth.getCurrentUser() != null) {
            firebaseUser = mAuth.getCurrentUser();

            mFirebaseDatabase = FirebaseDatabase.getInstance();
            if (firebaseUser != null) {
                Log.i(TAG, "oncreate4:" + firebaseUser.getUid());
            }
            updateUI(firebaseUser);
        }
    }

    //Below method updates UI by the valid user login
    public void updateUI(FirebaseUser currentUser){

        if(currentUser != null) {
            userName = findViewById(R.id.name_id);
            emailID = findViewById(R.id.email_id);
            userProfile = findViewById(R.id.user_profile);

            userName.setText(currentUser.getDisplayName());
            emailID.setText(currentUser.getEmail());

            if(currentUser.getPhotoUrl() != null)
                Glide.with(this).load(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "")
                        .into(userProfile);

            Log.w(TAG, "UpdateUI:" + currentUser.getUid()+ " " + currentUser.getEmail());
            //userProfile.setImageDrawable(Drawable.createFromPath(currentUser.getPhotoUrl().toString()));
        }
        else {
            Log.w(TAG, "Cannot update UI");
            userName.setText(R.string.guest_user);
            emailID.setText("");
        }

           Log.i(TAG, "UpdateUI:" + currentUser.getUid());
            //userProfile.setImageDrawable(Drawable.createFromPath(currentUser.getPhotoUrl().toString()));
        }

    public void createAccount(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    //Sign in successful, update UI with the signed-in user's info
                    Log.i(TAG, "createUserWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }

    public void signIn (String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                }
                else {
                    Log.w (TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }

    public void getCurrentUser() {

        if(mAuth.getCurrentUser() != null) {
        FirebaseUser user = mAuth.getCurrentUser();

            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUri = user.getPhotoUrl();

            boolean emailVerified = user.isEmailVerified();
            String uid = user.getUid();
        }
    }

    public void getInputList(View view) {
       /* Intent intent = new Intent(MainActivity.this, EnterList.class);
        intent.putExtra(UID, firebaseUser.getUid());
        startActivity(intent); */
    }

    private ActionBarDrawerToggle getActionBarDrawerToggle(){
        return new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                try {
                    getActionBar().setTitle(mTitle);
                } catch (NullPointerException e) {
                    Log.i (TAG, " 1Exception :" + e);
                }
                invalidateOptionsMenu();
            }

            public void onDrawerOpen(View view) {
                super.onDrawerClosed(view);
                try{
                    getActionBar().setTitle(mDrawerTitle);
                } catch (Exception e) {
                    Log.i( TAG, "2Exception:" + e);
                }
                invalidateOptionsMenu();
            }
        };
    }

    private ArrayList<DrawerList> getArrayList(){

        ArrayList arrayList = new ArrayList<DrawerList>();
        arrayList.add(new DrawerList("Home",getDrawable(R.drawable.ic_home)));
        arrayList.add(new DrawerList("Notifications",getDrawable(R.drawable.ic_bell)));
        arrayList.add(new DrawerList("Add Offers",null));
        arrayList.add(new DrawerList("My Rewards",getDrawable(R.drawable.ic_trophy)));
        arrayList.add(new DrawerList("Pending Orders", null));
        arrayList.add(new DrawerList("My Account", getDrawable(R.drawable.ic_account_circle_grey_36dp)));
        arrayList.add(new DrawerList("Send Feedback",getDrawable(R.drawable.ic_comment)));
        arrayList.add(new DrawerList("Help Centre", getDrawable(R.drawable.ic_help)));
        arrayList.add(new DrawerList("Legal", null));
        arrayList.add(new DrawerList("Log Out",null));

        return arrayList;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

   /* private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.i(TAG, "Click Listener :" + position);
            selectItem(position);

            }

        }


    private void selectItem(int position) {
        Fragment fragment = new ShopFragment();
        Bundle args = new Bundle();
        args.putInt(KEY, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

       // mDrawerList.setItemChecked(position, true);
        // TODO: 11-02-2018 you have to do coding for navigation drawer lists as u have to actually set click listener and see output
        Toast.makeText(MainActivity.this, "Position:"+ position, Toast.LENGTH_SHORT).show();

        mDrawerLayout.closeDrawer(mDrawerList);
    }*/

   /* @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        try{
            getActionBar().setTitle(mTitle);
        } catch (Exception e) {
            Log.i( TAG, "4Exception:" + e);
        }
    }

    public static class ShopFragment extends Fragment {
        public static final String ARG_NAVIGATION_NUMBER = "planet_number";

        public ShopFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.drawer_list_item, container, false);
            int i = getArguments().getInt(ARG_NAVIGATION_NUMBER);
            String planet = getResources().getStringArray(R.array.navigation_list)[i];

            getActivity().setTitle(planet);
            return rootView;
        }
    }*/


    private void addNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Context context = MainActivity.this;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, LIST_NOTIFICATION_ID)
                        .setColor(ContextCompat.getColor(context, R .color.colorPrimary))
                        .setSmallIcon(R.drawable.ic_bell)
                        .setLargeIcon(largeIcon(context))
                        .setContentTitle(context.getString(R.string.list_notification))
                        .setContentText(context.getString(R.string.please_check_list))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.waiting_for_your_response)))
                        .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);

        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
       /* manager.notify(0, builder.build());
       */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        }

        manager.notify(LIST_REMINDER_NOTIFICATION_ID, builder.build());

    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_bag);
        return largeIcon;
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
           /* Snackbar.make(, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });*/
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }
}
