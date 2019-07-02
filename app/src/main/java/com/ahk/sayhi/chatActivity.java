package com.ahk.sayhi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.ahk.sayhi.GetTimeAgo.getTimeAgo;

public class chatActivity extends AppCompatActivity {

    //Other
    private String mChatUserId;
    private String mUserName;
    private String mCurrentUserId;
    private List<messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private messageAdapter mAdapter;
    private static final int TOTAL_ITEM_TO_ADD = 10;
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStorage;
    private String thumbImageUrl;
    private SharedPreferences mPrefs;



    //Android layout
    private Toolbar mToolBar;
    private ImageButton buAdd, buSend;
    private EditText mMessageEdtTxt;
    private RecyclerView mMessageList;
    private SwipeRefreshLayout mRefreshLayout;

    //Firebase
    private DatabaseReference mChatDatabase;
    private DatabaseReference mRootRef;
    private DatabaseReference mMessagesDatabase;
    private DatabaseReference mMessageNotification;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mUserName = getIntent().getStringExtra("userName");
        mChatUserId = getIntent().getStringExtra("userId");
        //Android Layout Init
        buAdd = (ImageButton) findViewById(R.id.buAdd);
        buSend = (ImageButton) findViewById(R.id.buSend);
        mMessageEdtTxt = (EditText) findViewById(R.id.messageEdtTxt);
        mMessageList = (RecyclerView) findViewById(R.id.messageList);
        mToolBar = (Toolbar) findViewById(R.id.chatPageToolBar);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        //Other
        mLinearLayout = new LinearLayoutManager(this);
        mAdapter = new messageAdapter(messagesList);
        mPrefs = getPreferences(MODE_PRIVATE);

        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setHasFixedSize(true);
        mMessageList.setAdapter(mAdapter);

        setSupportActionBar(mToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        mMessageNotification=FirebaseDatabase.getInstance().getReference().child("messageNotification");
        mChatDatabase = FirebaseDatabase.getInstance().getReference();
        mChatDatabase.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();



        //For send images
        mImageStorage = FirebaseStorage.getInstance().getReference();



//        mChatDatabase.child("message").child(mCurrentUserId).child(mChatUserId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                    ds.child("seen").getRef().setValue(true);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


        loadMeassages();

        //Custom Action bar
        TextView mDisplayName = (TextView) findViewById(R.id.displayNameInChatBar);
        final TextView mLastSeen = (TextView) findViewById(R.id.lastSeen);
        final CircleImageView userImageView = (CircleImageView) findViewById(R.id.circleImageViewInChatBar);
        final CircleImageView onlineIcon = (CircleImageView) findViewById(R.id.onlineIconInChatBar);
        mDisplayName.setText(mUserName);
        mRootRef.child("Users").child(mChatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lastSeen = dataSnapshot.child("online").getValue().toString();
                if (lastSeen.equals("true")) {
                    mLastSeen.setText("Online");
                    onlineIcon.setVisibility(View.VISIBLE);
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    mLastSeen.setText(getTimeAgo(Long.parseLong(lastSeen)));
                    onlineIcon.setVisibility(View.INVISIBLE);
                }
                thumbImageUrl = dataSnapshot.child("thumbImage").getValue(String.class);
                Picasso.get().load(thumbImageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(userImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        mAdapter.setImageUrl(thumbImageUrl);
                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(thumbImageUrl).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(userImageView);
                    }
                });
                View view = LayoutInflater.from(chatActivity.this)
                        .inflate(R.layout.message_single_layout, null);

                Picasso.get().load(thumbImageUrl).placeholder(R.drawable.default_avatar).into((ImageView) view.findViewById(R.id.profileImageInMessageLayout));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mAdapter.setImageUrl(thumbImageUrl);



        buSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();

            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;

                itemPos = 0;

                loadMoreMessages();
            }
        });

        buAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            final String current_user_ref = "message/" + mCurrentUserId + "/" + mChatUserId;
            final String chat_user_ref = "message/" + mChatUserId + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mChatDatabase.child("message")
                    .child(mCurrentUserId).child(mChatUserId).push();

            final String push_id = user_message_push.getKey();


            final StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String downloadUrl = uri.toString();

                            Map messageMap = new HashMap();
                            messageMap.put("message", downloadUrl);
                            messageMap.put("seen", false);
                            messageMap.put("type", "image");
                            messageMap.put("time", ServerValue.TIMESTAMP);
                            messageMap.put("from", mCurrentUserId);

                            Map messageUserMap = new HashMap();
                            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);


                            mChatDatabase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if (databaseError != null) {

                                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                    }

                                }
                            });


                        }
                    });

                }
            });

        }

    }


    private void loadMoreMessages() {


        mMessagesDatabase = FirebaseDatabase.getInstance().getReference().child("message").child(mCurrentUserId).child(mChatUserId);
        Query mQuery = mMessagesDatabase.orderByKey().endAt(mLastKey).limitToLast(10);

        mQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                messages message = dataSnapshot.getValue(messages.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {

                    messagesList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if (itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadMeassages() {

        mMessagesDatabase = FirebaseDatabase.getInstance().getReference().child("message").child(mCurrentUserId).child(mChatUserId);
        mMessagesDatabase.keepSynced(true);
        Query mQuery = mMessagesDatabase.limitToLast(mCurrentPage * TOTAL_ITEM_TO_ADD);
        mQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                messages messages = dataSnapshot.getValue(messages.class);
                itemPos++;

                if (itemPos == 1) {

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(messages);
                mAdapter.notifyDataSetChanged();

                mLinearLayout.scrollToPosition(messagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);
                mChatDatabase.child("message").child(mCurrentUserId).child(mChatUserId).child(dataSnapshot.getKey()).child("seen").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("seen","seen changed to true");
                    }
                });
//                mChatDatabase.child("message").child(mCurrentUserId).child(mChatUserId).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                            ds.child("seen").getRef().setValue(true);
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

    }

    private void sendMessage() {
        String message = mMessageEdtTxt.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            String current_user_ref = "message/" + mCurrentUserId + "/" + mChatUserId;
            String chat_user_ref = "message/" + mChatUserId + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mChatDatabase.child("message")
                    .child(mCurrentUserId).child(mChatUserId).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mMessageEdtTxt.setText("");
            mChatDatabase.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("messageSentError", databaseError.getMessage());
                    }

                }
            });

            mChatDatabase.child("message").child(mCurrentUserId).child(mChatUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(mChatUserId)) {
                        Map chatAddMap = new HashMap();
                        chatAddMap.put("seen", false);
                        chatAddMap.put("timeStamp", ServerValue.TIMESTAMP);

                        Map chatUserMap = new HashMap();
                        chatUserMap.put("chat/" + mCurrentUserId + "/" + mChatUserId, chatAddMap);
                        chatUserMap.put("chat/" +  mChatUserId+ "/" +mCurrentUserId , chatAddMap);
                        chatAddMap.clear();
                        chatAddMap.put("seen", true);
                        chatAddMap.put("timeStamp", ServerValue.TIMESTAMP);


                        chatUserMap.put("chat/" + mChatUserId + "/" + mCurrentUserId, chatAddMap);


                        mChatDatabase.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.i("chatError", databaseError.getMessage());
                                }
                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            HashMap<String,String> notificationMap=new HashMap<>();
            notificationMap.put("message",message);


            mMessageNotification.child(mChatUserId).child(mCurrentUserId).push().setValue(notificationMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("notify","notification sent successfully");
                }
            });


            mChatDatabase.child("chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    sendMessage();
                }
            });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mRootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
            mRootRef.child("online").setValue("true");
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mRootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
            mRootRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }


}
