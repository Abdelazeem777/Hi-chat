package com.ahk.sayhi;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;


/**
 * A simple {@link Fragment} subclass.
 */
public class requestsFragment extends Fragment {

    //Android layout
    private RecyclerView mRequestList;

    //Firebase
    private DatabaseReference mRequestsDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private Query mQuery;

    //Other
    private String mCurrentUserId;
    private View mMainView;

    public requestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestList = (RecyclerView) mMainView.findViewById(R.id.requestsRecycleView);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        mRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("friendReq");
        mQuery = FirebaseDatabase.getInstance().getReference()
                .child("friendReq")
                .child(mCurrentUserId)
                .orderByChild("requestType")
                .equalTo("received")
                .limitToLast(50);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mFriendsDatabase.keepSynced(true);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<friendReq> options = new FirebaseRecyclerOptions.Builder<friendReq>()
                .setQuery(mQuery, friendReq.class)
                .build();
        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<friendReq, requestsFragment.RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final requestsFragment.RequestsViewHolder holder, int position, @NonNull friendReq model) {

//                holder.setName(model.getName());
//                holder.setProfileImage(model.getThumbImage());

//                holder.mView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//
//                    }
//                });
                final String userId = getRef(position).getKey();
                final String[] userName = new String[1];
                mUserDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userName[0] = dataSnapshot.child("name").getValue(String.class);
                        String thumbImageUrl = dataSnapshot.child("thumbImage").getValue(String.class);

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
                        Bundle bundle = new Bundle();
                        bundle.putString("userId", userId);
                        bundle.putString("userName", userName[0]);
                        Intent profileActivityIntent = new Intent(getContext(), profileActivity.class);
                        profileActivityIntent.putExtras(bundle);
                        startActivity(profileActivityIntent);
                    }
                });

                final Button buDecline = (Button) holder.mView.findViewById(R.id.buDeclineRequestFragment);
                final Button buAccept = (Button) holder.mView.findViewById(R.id.buAcceptRequestFragment);
                buAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buAccept.setEnabled(false);
                        buDecline.setEnabled(false);
                        acceptRequest(userId);
                        buAccept.setEnabled(true);
                        buDecline.setEnabled(true);

                    }
                });


                buDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buAccept.setEnabled(false);
                        buDecline.setEnabled(false);
                        declineRequest(userId);
                        buAccept.setEnabled(true);
                        buDecline.setEnabled(true);

                    }
                });
            }

            @NonNull
            @Override
            public requestsFragment.RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.single_user_for_requests_fragment, viewGroup, false);

                return new requestsFragment.RequestsViewHolder(view);
            }
        };
        mRequestList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }

    private void acceptRequest(final String userId) {

        final String currentDate = DateFormat.getDateInstance().format(new Date());

        mFriendsDatabase.child(mCurrentUserId).child(userId).child("Date").setValue(currentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFriendsDatabase.child(userId).child(mCurrentUserId).child("Date").setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mRequestsDatabase.child(mCurrentUserId).child(userId)
                                                    .removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                mRequestsDatabase.child(userId).child(mCurrentUserId)
                                                                        .removeValue()
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {


                                                                                Toasty.success(getContext(), "You are now friends", Toast.LENGTH_SHORT, false).show();
                                                                            }
                                                                        });
                                                            } else {
                                                                Toasty.error(
                                                                        getContext(),
                                                                        "Failed to cancel, please check internet connection",
                                                                        Toast.LENGTH_SHORT,
                                                                        false)
                                                                        .show();
                                                            }

                                                        }
                                                    });
                                        }
                                    });
                        } else {
                            Toasty.error(
                                    getContext(),
                                    "Failed to accept, please check internet connection",
                                    Toast.LENGTH_SHORT,
                                    false)
                                    .show();
                        }

                    }
                });


    }

    private void declineRequest(final String userId) {
        mRequestsDatabase.child(mCurrentUserId).child(userId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mRequestsDatabase.child(userId).child(mCurrentUserId)
                                    .removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toasty.success(getContext(), "Friend request declined", Toast.LENGTH_SHORT, false).show();

                                        }
                                    });
                        } else {
                            Toasty.error(
                                    getContext(),
                                    "Failed, please check internet connection",
                                    Toast.LENGTH_SHORT,
                                    false)
                                    .show();
                        }

                    }
                });
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView mUserName = (TextView) mView.findViewById(R.id.singleUserName);
            mUserName.setText(name);
        }

        public void setProfileImage(final String image) {
            final CircleImageView mProfileImage = (CircleImageView) mView.findViewById(R.id.singleUserImage);
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


    }


}
