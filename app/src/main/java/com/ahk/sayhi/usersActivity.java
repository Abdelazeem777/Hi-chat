package com.ahk.sayhi;

import android.app.SearchManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class usersActivity extends AppCompatActivity {

    //Android Layout
    private RecyclerView mUsersList;

    //Firebase
    private DatabaseReference mUsersDatabase;
    private Query mQuery;
    private FirebaseAuth mAuth;

    //Other
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        handleIntent(getIntent());

        mAuth=FirebaseAuth.getInstance();

        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mQuery=FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("mainName")
                .startAt(query.toLowerCase())
                .endAt(query.toLowerCase()+"\uf8ff")
                .limitToLast(50);

        mUsersList=(RecyclerView)findViewById(R.id.usersRecycleView);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(usersActivity.this));

    }
    @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);


        }
    }



    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<users> options = new FirebaseRecyclerOptions.Builder<users>()
                .setQuery(mQuery, users.class)
                .build();
        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<users, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull final users model) {

                    holder.setName(model.getName());
                    holder.setProfileImage(model.getThumbImage());
                    final String userId = getRef(position).getKey();
                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(!userId.equals(mAuth.getCurrentUser().getUid())) {
                                Bundle bundle=new Bundle();
                                bundle.putString("userId", userId);
                                bundle.putString("userName",model.getName());
                                Intent profileActivityIntent = new Intent(usersActivity.this, profileActivity.class);
                                profileActivityIntent.putExtras(bundle);
                                startActivity(profileActivityIntent);
                            }
                            else{
                                startActivity(new Intent(getApplicationContext(), settingActivity.class).putExtra("userName",model.getName()));
                            }
                        }
                    });



            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.single_user_layout, viewGroup, false);

                return new UsersViewHolder(view);
            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            mUsersDatabase.child(currentUser.getUid()).child("online").setValue("true");
        }


    }



    @Override
    protected void onPause() {
        super.onPause();

        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            mUsersDatabase.child(currentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);

        }
    }
    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {View mView;

        public UsersViewHolder(@NonNull View itemView) {
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
    }
}
