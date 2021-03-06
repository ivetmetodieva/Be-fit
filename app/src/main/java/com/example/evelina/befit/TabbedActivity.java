package com.example.evelina.befit;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.evelina.befit.adapters.TrainingRecyclerAdapter;
import com.example.evelina.befit.model.DbManager;
import com.example.evelina.befit.model.TrainingManager;
import com.example.evelina.befit.model.User;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class TabbedActivity extends AppCompatActivity {


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private NetworkStateChangedReceiver receiver;
    static String username;
    private static String name;
    boolean isBasic;
    private static String loginState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_tabbed);
        // DbManager.getInstance(TabbedActivity.this).loadUsers();
        receiver = new NetworkStateChangedReceiver();
        registerReceiver(receiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Be Fit");
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(0);  // c
        if (getIntent().getStringExtra("username") != null) {
            Log.e("username", getIntent().getStringExtra("username") + " in tabbed activity ");
            username = getIntent().getStringExtra("username");
            DbManager.getInstance(TabbedActivity.this).loadNotifications(username, TabbedActivity.this);
        }

        String state = getSharedPreferences("Login", Context.MODE_PRIVATE).getString("loggedWith", "nothing");
        loginState = state;

        if (getIntent().getStringExtra("name") != null) {
            name = getIntent().getStringExtra("name");
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DbManager.getInstance(this).loadUsers();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(TabbedActivity.this, SettingsActivity.class);
            intent.putExtra("loggedUser", username);
            startActivity(intent);
            finish();
            return true;
        }
        if (id == R.id.action_logout) {
            //here logout
            SharedPreferences sharedPreferences = TabbedActivity.this.getSharedPreferences("Login", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("logged_in", false);
            editor.putString("currentUser", null);
            editor.commit();
            FacebookSdk.sdkInitialize(getApplicationContext());
            if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(TabbedActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            Intent intent = new Intent(TabbedActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        CircleImageView profilePicture;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                Log.e("USER in Tabbed 1 ", username);
                // Inflate the layout for this fragment
                View root = inflater.inflate(R.layout.fragment_basic_training, container, false);
                RecyclerView categories = (RecyclerView) root.findViewById(R.id.basicTraining_view);
                categories.setAdapter(new TrainingRecyclerAdapter((TabbedActivity) getActivity(), TrainingManager.getInstance().getBasicChallengesName(), true));
                categories.setLayoutManager(new LinearLayoutManager(getActivity()));
                return root;
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                User user = DbManager.getInstance(getActivity()).getUser(username);
                Log.e("USER in Tabbed 2 ", username + " and has  ===== " + user.getCustomChallengesName().size() + " custom trainings");
                View root = inflater.inflate(R.layout.fragment_custom_training, container, false);
                RecyclerView custom = (RecyclerView) root.findViewById(R.id.customTraining_view);
                FloatingActionButton add = (FloatingActionButton) root.findViewById(R.id.fabTraining);
                user.cleanEmptyChallenge(getActivity());
                if (!user.hasCustomChallenges()) {
                    List<String> noNames = Arrays.asList("Please add your custom Training");
                    custom.setAdapter(new TrainingRecyclerAdapter((TabbedActivity) getActivity(), noNames, false));
                } else {
                    custom.setAdapter(new TrainingRecyclerAdapter((TabbedActivity) getActivity(), DbManager.getInstance(getActivity()).getUser(username).getCustomChallengesName(), false));
                }
                custom.setLayoutManager(new LinearLayoutManager(getActivity()));
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle b = new Bundle();
                        b.putString("username", username);
                        ((TabbedActivity) getActivity()).showFragment(b);
                    }
                });
                return root;

            } else {
                View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
                profilePicture = (CircleImageView) rootView.findViewById(R.id.picture_profile);
                TextView numberPointsTV = (TextView) rootView.findViewById(R.id.number_points_profile_TV);
                TextView numberTrainingsTV = (TextView) rootView.findViewById(R.id.number_trainings_profile_TV);
                Button viewTrainingsButton = (Button) rootView.findViewById(R.id.view_trainings_profile_Button);
                TextView kilogramsTV = (TextView) rootView.findViewById(R.id.kg_profile_TV);
                TextView metersTV = (TextView) rootView.findViewById(R.id.meters_profile_TV);
                final FloatingActionMenu menu = (FloatingActionMenu) rootView.findViewById(R.id.menu);
                com.github.clans.fab.FloatingActionButton fab = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.show_chart);
                com.github.clans.fab.FloatingActionButton fabPie = (com.github.clans.fab.FloatingActionButton) rootView.findViewById(R.id.show_Piechart);
                TextView usernameF = (TextView) rootView.findViewById(R.id.username_profile_TV);
                LinearLayout layout1 = (LinearLayout) rootView.findViewById(R.id.container1);
                LinearLayout layout2 = (LinearLayout) rootView.findViewById(R.id.container2);
                LinearLayout layout3 = (LinearLayout) rootView.findViewById(R.id.container3);
                LinearLayout layout4 = (LinearLayout) rootView.findViewById(R.id.container4);
                layout1.setBackgroundColor(getActivity().getResources().getColor(R.color.lightgreen));
                layout2.setBackgroundColor(getActivity().getResources().getColor(R.color.lightgreen));
                layout3.setBackgroundColor(getActivity().getResources().getColor(R.color.lightgreen));
                layout4.setBackgroundColor(getActivity().getResources().getColor(R.color.lightgreen));
                Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "RockoUltraFLF.ttf");
                TextView pointsTV = (TextView) rootView.findViewById(R.id.points_profile_TV);
                TextView trainingsTV = (TextView) rootView.findViewById(R.id.trainings_profile_TV);
                TextView weightTV = (TextView) rootView.findViewById(R.id.weight_profile_TV);
                TextView heightTV = (TextView) rootView.findViewById(R.id.height_profile_TV);
                pointsTV.setTypeface(typeface);
                weightTV.setTypeface(typeface);
                trainingsTV.setTypeface(typeface);
                heightTV.setTypeface(typeface);
                numberPointsTV.setTypeface(typeface);
                viewTrainingsButton.setTypeface(typeface);
                kilogramsTV.setTypeface(typeface);
                metersTV.setTypeface(typeface);
                numberTrainingsTV.setTypeface(typeface);
                usernameF.setText(username);
                final User user = DbManager.getInstance(getActivity()).getUser(username);
                numberTrainingsTV.setText(user.getCompletedTrainingsNum() + "");
                numberPointsTV.setText(user.getPoints() + "");
                kilogramsTV.setText(user.getWeight() + "");
                metersTV.setText(user.getHeight() + "");
                usernameF.setTypeface(typeface);
                if (isNetworkAvailable(getActivity())) {
                    if (loginState.equals("facebook") && user.getProfilePic() == null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < username.length(); i++) {
                            if (!(username.charAt(i) >= 48 && username.charAt(i) <= 57)) {
                                sb.append(username.charAt(i));
                            }
                        }
                        usernameF.setText(sb.toString());
                        Bundle params = new Bundle();
                        params.putBoolean("redirect", false);
                        new GraphRequest(AccessToken.getCurrentAccessToken(), "me/picture?width=1000&height=1000", params, HttpMethod.GET, new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                if (response != null) {
                                    String picUrlString = null;
                                    try {
                                        picUrlString = (String) response.getJSONObject().getJSONObject("data").get("url");
                                        MyAsyncTask showPic = new MyAsyncTask();
                                        showPic.execute(picUrlString);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).executeAsync();
                    } else {
                        profilePicture.setImageURI(user.getProfilePic());
                        usernameF.setText(username);
                    }
                }
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!user.getCompletedTrainings().isEmpty()) {
                            Intent intent = new Intent(getActivity(), AnalysisExercises.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), "You haven`t completed any Trainings yet!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                fabPie.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (user.getWeight() != 0 || user.getHeight() != 0) {
                            Intent intent = new Intent(getActivity(), ChartsActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                            //TODO
                            getActivity().finish();
                        } else {
                            Toast.makeText(getActivity(), "Please enter weight and height!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                viewTrainingsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (user.getCompletedTrainingsNum() != 0) {
                            Intent intent = new Intent(getActivity(), CompletedTrainings.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                            //TODO
                            getActivity().finish();
                        } else {
                            Toast.makeText(getActivity(), "You haven`t completed any Trainings yet!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return rootView;
            }
        }

        public static boolean isNetworkAvailable(Context context) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (conMan.getActiveNetworkInfo() != null && conMan.getActiveNetworkInfo().isConnected())
                return true;
            else
                return false;
        }

        class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {

            @Override
            protected Bitmap doInBackground(String... strings) {
                InputStream is = null;
                Bitmap img = null;
                URL url = null;
                try {
                    url = new URL(strings[0]);
                    HttpURLConnection connection = null;
                    connection = (HttpURLConnection) url.openConnection();
                    is = connection.getInputStream();
                    img = BitmapFactory.decodeStream(is);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return img;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                profilePicture.setImageBitmap(bitmap);
            }
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Training";
                case 1:
                    return "Custom training";
                case 2:
                    return "Profile";
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void ChallengeSelected(String challenge, boolean isBasic) {
        Intent intent = new Intent(this, StartTrainingActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("challenge", challenge);
        intent.putExtra("isBasic", isBasic);
        startActivity(intent);
        //TODO
        finish();
    }


    public void showFragment(Bundle bundle) {
        if (bundle != null) {
            CustomChallengeNameDialogFragment fragment = new CustomChallengeNameDialogFragment();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), "ChallengeName");
        }
    }
}
