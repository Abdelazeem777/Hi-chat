package com.ahk.sayhi;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class chatsFragment extends Fragment {

    //Android layout
    private ShimmerRecyclerView mChatList;

    //Firebase
    private DatabaseReference mChatsDatabase;
    private DatabaseReference mMessagesDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private Query mQuery;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    //Other
    private String mCurrentUserId;
    private View mMainView;
    private SharedPreferences mPref;

    public chatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView=inflater.inflate(R.layout.fragment_chats, container, false);

        mChatList=mMainView.findViewById(R.id.chatRecyclerView);
        mChatList.showShimmerAdapter();

        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId=mAuth.getCurrentUser().getUid();
        mChatsDatabase= FirebaseDatabase.getInstance().getReference().child("chat").child(mCurrentUserId);
        mChatsDatabase.keepSynced(true);
        mQuery=mChatsDatabase.orderByChild("timeStamp");
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);
        mMessagesDatabase=FirebaseDatabase.getInstance().getReference().child("message").child(mCurrentUserId);
        mMessagesDatabase.keepSynced(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);


        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(layoutManager);


        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<chats> options = new FirebaseRecyclerOptions.Builder<chats>()
                .setQuery(mQuery, chats.class)
                .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<chats, chatsFragment.chatsViewHolder>(options) {


            @Override
            protected void onBindViewHolder(@NonNull final chatsFragment.chatsViewHolder holder, int position, @NonNull final chats model) {
                final String userId=getRef(position).getKey();



                Query lastMessageQuery = mMessagesDatabase.child(userId);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.child("message").getValue()!=null) {
                            String data = dataSnapshot.child("message").getValue().toString();
                            boolean isSeen=dataSnapshot.child("seen").getValue(boolean.class);

                            holder.setMessage(data, isSeen);
                        }
                        else{
                            return;
                        }



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

                final String[] userName = new String[1];
                mUserDatabase.child(userId).addValueEventListener(new ValueEventListener() {


                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userName[0] =dataSnapshot.child("name").getValue(String.class);
                        String thumbImageUrl=dataSnapshot.child("thumbImage").getValue(String.class);
                        if (dataSnapshot.hasChild("online")){
                            String onlineStatus=dataSnapshot.child("online").getValue().toString();
                            holder.setUseronlineStatus(onlineStatus);
                        }
                        holder.setName(userName[0]);
                        holder.setProfileImage(thumbImageUrl);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                holder.mView.findViewById(R.id.singleUserImage).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Bundle bundle=new Bundle();
                        bundle.putString("userId", userId);
                        bundle.putString("userName",userName[0]);
                        Intent profileActivityIntent=new Intent(getContext(),profileActivity.class);
                        profileActivityIntent.putExtras(bundle);
                        startActivity(profileActivityIntent);
                    }
                });

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent chatActivityIntent=new Intent(getContext(),chatActivity.class);
                        chatActivityIntent.putExtra("userId",userId);
                        chatActivityIntent.putExtra("userName", userName[0]);
                        startActivity(chatActivityIntent);
                    }
                });


            }
            @NonNull
            @Override
            public chatsFragment.chatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.single_user_for_chat_fragment, viewGroup, false);

                return new chatsFragment.chatsViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                hideShimmerAndSetAdapter();
                }
        };


        firebaseRecyclerAdapter.startListening();

    }

    private void hideShimmerAndSetAdapter() {

        new Thread() {
            public void run() {
                
                    try {
                        Thread.sleep(1000);
                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mChatList.setAdapter(firebaseRecyclerAdapter);
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            
        }.start();

    }




    public static class chatsViewHolder extends RecyclerView.ViewHolder
    {View mView;

        public chatsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setMessage(String message, boolean isSeen){

            TextView userStatusView = (TextView) mView.findViewById(R.id.lastReceivedMessage);
            Log.i("isSeenValue", String.valueOf(isSeen));
            if (!isSeen) {
                userStatusView.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                userStatusView.setTypeface(Typeface.DEFAULT);

            }

            if(message.contains("firebasestorage")){

                userStatusView.setText("New image");
            }
            else {

                userStatusView.setText(message);
            }
        }

        public void setName(String name) {
            TextView mUserName=(TextView) mView.findViewById(R.id.singleUserName);
            mUserName.setText(name);
        }

        public void setProfileImage(final String image) {
            final CircleImageView mProfileImage=(CircleImageView)mView.findViewById(R.id.singleUserImage);
            //Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);
            Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.default_avatar).into(mProfileImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);
                }
            });
        }


        public void setUseronlineStatus(String onlineStatus) {
            CircleImageView mOnlineIcon=mView.findViewById(R.id.onlineIcon);
            if(onlineStatus.equals("true")){

                mOnlineIcon.setVisibility(View.VISIBLE);
            }
            else{
                mOnlineIcon.setVisibility(View.INVISIBLE);
            }
        }
    }


}
