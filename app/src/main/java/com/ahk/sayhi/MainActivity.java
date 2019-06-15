package com.ahk.sayhi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.stephentuso.welcome.WelcomeHelper;

import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {
    WelcomeHelper welcomeScreen;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private Toolbar mToolBar;
    private Bundle b;
    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;
    private Query mQuery;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b = savedInstanceState;


        mToolBar = (Toolbar) findViewById(R.id.mainPageToolBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        mQuery=FirebaseDatabase.getInstance().getReference().child("Users").limitToLast(50);

        mViewPager = (ViewPager) findViewById(R.id.mainTabPager);
        mSectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.mainTabs);
        mTabLayout.setupWithViewPager(mViewPager);
        startService(new Intent(getApplicationContext(), firebaseMessagingService.class));

    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToStart();
        } else {

            mUserDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(mAuth.getCurrentUser().getUid());
            mUserDatabase.child("online").setValue("true");
            mUserDatabase.keepSynced(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

    private void goToStart() {
        welcomeScreen = new WelcomeHelper(this, welcomeActivity.class);
        welcomeScreen.show(b);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAuth.getCurrentUser() == null)
            welcomeScreen.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        ComponentName componentName=new ComponentName(MainActivity.this,usersActivity.class);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(componentName));

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
//                firebaseUserSearch();

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });


        return true;
    }

    private void firebaseUserSearch() {
        FirebaseRecyclerOptions<users> options = new FirebaseRecyclerOptions.Builder<users>()
                .setQuery(mQuery, users.class)
                .build();
        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<users, usersActivity.UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull usersActivity.UsersViewHolder holder, int position, @NonNull users model) {

                holder.setName(model.getName());
                holder.setProfileImage(model.getThumbImage());
                final String userId=getRef(position).getKey();
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profileActivityIntent=new Intent(MainActivity.this,profileActivity.class);
                        profileActivityIntent.putExtra("userId",userId);
                        startActivity(profileActivityIntent);
                    }
                });
            }

            @NonNull
            @Override
            public usersActivity.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.single_user_layout, viewGroup, false);

                return new usersActivity.UsersViewHolder(view);
            }
        };
//        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.buSignOut) {
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
            Toasty.success(MainActivity.this, "Salam", Toast.LENGTH_SHORT, false).show();
            FirebaseAuth.getInstance().signOut();
            goToStart();
        }
        if (item.getItemId() == R.id.buAccountSetting) {
            mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userName=dataSnapshot.child("name").getValue(String.class);
                    Log.i("userName",userName);
                    startActivity(new Intent(getApplicationContext(), settingActivity.class).putExtra("userName",userName));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
//        if (item.getItemId() == R.id.buAllUsers) {
//            startActivity(new Intent(MainActivity.this, usersActivity.class));
//        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(){
            @Override
            public void run() {
                super.run();
                startService(new Intent(getApplicationContext(), firebaseMessagingService.class));
            }
        }.run();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                startService(new Intent(getApplicationContext(), firebaseMessagingService.class));

            }
        }, 5000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startService(new Intent(getApplicationContext(), firebaseMessagingService.class));
            }
        }, 6000);

        new Thread() {
            @Override
            public void run() {
                super.run();
                AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(getApplicationContext(), firebaseMessagingService.class);
                PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000), pi);
            }
        }.run();
    }
}