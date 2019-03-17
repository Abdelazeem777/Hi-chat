package com.ahk.sayhi;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class profileActivity extends AppCompatActivity {

    //Android Layout
    private ImageView mProfileImage;
    private TextView mDisplayName;
    private Button buSendRequest, buDecline;

    //Firebase
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mfriendDatabase;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mUserDatabaseForOnlineFeature;

    //Other
    private String mCurrentState;
    private String userId;
    private String displayName;
    private int notificationId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.i("notificationId2",String.valueOf(getIntent().getExtras().getInt("notificationId")));
        if(String.valueOf(getIntent().getExtras().getInt("notificationId"))!=null) {
            notificationId = getIntent().getExtras().getInt("notificationId");
            Log.i("notificationId3",String.valueOf(notificationId));
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
        userId = getIntent().getExtras().getString("userId");
        displayName=getIntent().getExtras().getString("userName");
        mDisplayName = (TextView) findViewById(R.id.displayName);
        mDisplayName.setText(displayName);
        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        buSendRequest = (Button) findViewById(R.id.buSendRequest);
        buDecline = (Button) findViewById(R.id.buDecline);

        mCurrentState = "notFriends";

        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("friendReq");
        mfriendDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final String imageUrl = dataSnapshot.child("image").getValue().toString();


                //Picasso.get().load(imageUrl).placeholder(R.drawable.default_avatar).into(mProfileImage);
                Picasso.get().load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(imageUrl).placeholder(R.drawable.default_avatar).into(mProfileImage);
                    }
                });
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(userId)) {
                            String requestType = dataSnapshot.child(userId).child("requestType").getValue().toString();
                            if (requestType.equals("received")) {
                                mCurrentState = "requestReceived";
                                buSendRequest.setText("accept friend request");
                                buSendRequest.setBackgroundColor(Color.rgb(50, 205, 50));
                                buSendRequest.setTextColor(Color.parseColor("white"));

                                buDecline.setVisibility(View.VISIBLE);

                            } else if (requestType.equals("sent")) {
                                buSendRequest.setText("cancel friend request");
                                buSendRequest.setBackgroundColor(Color.parseColor("red"));
                                buSendRequest.setTextColor(Color.parseColor("white"));
                                mCurrentState = "requestSent";
                            }

                        } else {
                            mfriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)) {
                                        buSendRequest.setText("Unfriend");
                                        buSendRequest.setBackgroundColor(Color.parseColor("red"));
                                        buSendRequest.setTextColor(Color.parseColor("white"));
                                        mCurrentState = "friends";

                                        buDecline.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        buSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buSendRequest.setEnabled(false);
                buSendRequest.setBackgroundColor(Color.parseColor("gray"));

                //Send Request
                if (mCurrentState.equals("notFriends")) {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userId).child("requestType")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                mFriendRequestDatabase.child(userId).child(mCurrentUser.getUid()).child("requestType")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", mCurrentUser.getUid());
                                        notificationData.put("type", "friendRequest");
                                        mNotificationDatabase.child(userId).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                buSendRequest.setText("cancel friend request");
                                                buSendRequest.setBackgroundColor(Color.parseColor("red"));
                                                buSendRequest.setTextColor(Color.parseColor("white"));
                                                mCurrentState = "requestSent";
                                            }
                                        });


                                        // Toasty.success(profileActivity.this,"Request sent",Toast.LENGTH_SHORT,false).show();
                                    }
                                });
                            } else {
                                Toasty.error(
                                        profileActivity.this,
                                        "Failed sending request, please check internet connection",
                                        Toast.LENGTH_SHORT,
                                        false)
                                        .show();
                            }
                            buSendRequest.setEnabled(true);
                        }
                    });

                }

                //Cancel Request
                else if (mCurrentState.equals("requestSent")) {

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mFriendRequestDatabase.child(userId).child(mCurrentUser.getUid())
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        buSendRequest.setText("send friend request");
                                                        buSendRequest.setBackgroundColor(Color.parseColor("white"));
                                                        buSendRequest.setTextColor(getResources().getColor(R.color.colorPrimary));
                                                        mCurrentState = "notFriends";
                                                    }
                                                });
                                    } else {
                                        Toasty.error(
                                                profileActivity.this,
                                                "Failed to cancel, please check internet connection",
                                                Toast.LENGTH_SHORT,
                                                false)
                                                .show();
                                    }
                                    buSendRequest.setEnabled(true);
                                }
                            });

                }

                //Accept Request
                else if (mCurrentState.equals("requestReceived")) {

                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    mfriendDatabase.child(mCurrentUser.getUid()).child(userId).child("Date").setValue(currentDate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mfriendDatabase.child(userId).child(mCurrentUser.getUid()).child("Date").setValue(currentDate)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userId)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            mFriendRequestDatabase.child(userId).child(mCurrentUser.getUid())
                                                                                    .removeValue()
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {

                                                                                            buSendRequest.setText("Unfriend");
                                                                                            buSendRequest.setBackgroundColor(Color.parseColor("red"));
                                                                                            buSendRequest.setTextColor(Color.parseColor("white"));
                                                                                            mCurrentState = "friends";

                                                                                            buDecline.setVisibility(View.GONE);

                                                                                            Toasty.success(profileActivity.this, "You are now friends", Toast.LENGTH_SHORT, false).show();
                                                                                        }
                                                                                    });
                                                                        } else {
                                                                            Toasty.error(
                                                                                    profileActivity.this,
                                                                                    "Failed to cancel, please check internet connection",
                                                                                    Toast.LENGTH_SHORT,
                                                                                    false)
                                                                                    .show();
                                                                        }
                                                                        buSendRequest.setEnabled(true);
                                                                    }
                                                                });
                                                    }
                                                });
                                    } else {
                                        Toasty.error(
                                                profileActivity.this,
                                                "Failed to accept, please check internet connection",
                                                Toast.LENGTH_SHORT,
                                                false)
                                                .show();
                                    }
                                    buSendRequest.setEnabled(true);
                                }
                            });

                }

                //Unfriend request
                else if (mCurrentState.equals("friends")) {
                    mfriendDatabase.child(mCurrentUser.getUid()).child(userId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mfriendDatabase.child(userId).child(mCurrentUser.getUid())
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        buSendRequest.setText("send friend request");
                                                        buSendRequest.setBackgroundColor(Color.parseColor("white"));
                                                        buSendRequest.setTextColor(getResources().getColor(R.color.colorPrimary));
                                                        mCurrentState = "notFriends";
                                                    }
                                                });
                                    } else {
                                        Toasty.error(
                                                profileActivity.this,
                                                "Failed, please check internet connection",
                                                Toast.LENGTH_SHORT, false)
                                                .show();
                                    }
                                    buSendRequest.setEnabled(true);
                                }
                            });
                }
            }
        });


        buDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buDecline.setEnabled(false);
                buSendRequest.setEnabled(false);
                buSendRequest.setBackgroundColor(Color.parseColor("gray"));
                buDecline.setBackgroundColor(Color.parseColor("gray"));


                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userId)
                        .removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mFriendRequestDatabase.child(userId).child(mCurrentUser.getUid())
                                            .removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    buSendRequest.setText("send friend request");
                                                    buSendRequest.setBackgroundColor(Color.parseColor("white"));
                                                    buSendRequest.setTextColor(getResources().getColor(R.color.colorPrimary));
                                                    mCurrentState = "notFriends";

                                                    buDecline.setVisibility(View.GONE);
                                                    buDecline.setBackgroundColor(Color.parseColor("red"));


                                                }
                                            });
                                } else {
                                    Toasty.error(
                                            profileActivity.this,
                                            "Failed, please check internet connection",
                                            Toast.LENGTH_SHORT,
                                            false)
                                            .show();
                                }
                                buSendRequest.setEnabled(true);
                                buDecline.setEnabled(true);
                            }
                        });
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserDatabaseForOnlineFeature = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
            mUserDatabaseForOnlineFeature.child("online").setValue("true");
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserDatabaseForOnlineFeature = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
            mUserDatabaseForOnlineFeature.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

}
