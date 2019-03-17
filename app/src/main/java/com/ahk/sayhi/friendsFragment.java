package com.ahk.sayhi;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class friendsFragment extends Fragment {

    //Android layout
    private ShimmerRecyclerView mFriendList;


    //Firebase
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private Query mQuery;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    //Other
    private String mCurrentUserId;
    private View mMainView;

    public friendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mMainView=inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendList=(ShimmerRecyclerView) mMainView.findViewById(R.id.friendsRecycleView);
        mFriendList.showShimmerAdapter();
        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId=mAuth.getCurrentUser().getUid();
        mFriendsDatabase= FirebaseDatabase.getInstance().getReference().child("friends").child(mCurrentUserId);

        mQuery=mFriendsDatabase.limitToLast(50);
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        mFriendsDatabase.keepSynced(true);
        return mMainView;
    }

    @Override
    public void onResume() {
        super.onResume();

        FirebaseRecyclerOptions<friends> options = new FirebaseRecyclerOptions.Builder<friends>()
                .setQuery(mQuery, friends.class)
                .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<friends, friendsFragment.FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final friendsFragment.FriendsViewHolder holder, int position, @NonNull friends model) {

//                holder.setName(model.getName());
//                holder.setProfileImage(model.getThumbImage());

//                holder.mView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//
//                    }
//                });
                final String userId=getRef(position).getKey();
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
            public friendsFragment.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.single_user_layout, viewGroup, false);

                return new friendsFragment.FriendsViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();

                mFriendList.hideShimmerAdapter();
                mFriendList.setAdapter(firebaseRecyclerAdapter);
            }
        };

        firebaseRecyclerAdapter.startListening();



    }



    @Override
    public void onStop() {
        super.onStop();
        mFriendList.showShimmerAdapter();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
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



