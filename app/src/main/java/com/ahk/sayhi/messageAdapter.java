package com.ahk.sayhi;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class messageAdapter extends RecyclerView.Adapter<messageAdapter.MessageViewHolder> {

    private List<messages> mMessageList;
    private FirebaseAuth mAuth;
    private String imageUrl;
    public messageAdapter(List<messages> mMessageList) {
        this.mMessageList = mMessageList;
        mAuth=FirebaseAuth.getInstance();

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view= LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message_single_layout,viewGroup,false);

        return new MessageViewHolder(view);
    }

    public void setImageUrl(String thumbImageUrl) {
        this.imageUrl=thumbImageUrl;



    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public RelativeLayout relativeLayout;
        public TextView messageText;
        public CircleImageView profileImage;
        public ImageView imageAsMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText=(TextView)itemView.findViewById(R.id.messageTextLayout);
            profileImage=(CircleImageView)itemView.findViewById(R.id.profileImageInMessageLayout);
            relativeLayout=(RelativeLayout)itemView.findViewById(R.id.relativeLayout);
            imageAsMessage=(ImageView)itemView.findViewById(R.id.imageAsMessage);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageAdapter, int i) {
        String currentUserId=mAuth.getCurrentUser().getUid();
        messages m=mMessageList.get(i);
        String formUserId=m.getFrom();
        String messageType=m.getType();

        Picasso.get().load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.default_avatar).into(messageAdapter.profileImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(imageUrl).placeholder(R.drawable.default_avatar).into(messageAdapter.profileImage);
            }

        });

        if(formUserId.equals(currentUserId)){
            if(messageType.equals("text")) {
                messageAdapter.messageText.setBackgroundResource(R.drawable.message_sent_shape);
                messageAdapter.messageText.setTextColor(Color.parseColor("white"));
                messageAdapter.messageText.setText(m.getMessage());

                messageAdapter.relativeLayout.setGravity(Gravity.RIGHT);
                messageAdapter.profileImage.setVisibility(View.GONE);

                messageAdapter.imageAsMessage.setVisibility(View.GONE);
                messageAdapter.imageAsMessage.setBackgroundResource(R.drawable.message_sent_shape);

            }
            else{
                messageAdapter.messageText.setBackgroundResource(R.drawable.message_sent_shape);
                messageAdapter.messageText.setTextColor(Color.parseColor("white"));
                messageAdapter.messageText.setVisibility(View.GONE);

                messageAdapter.relativeLayout.setGravity(Gravity.RIGHT);
                messageAdapter.profileImage.setVisibility(View.GONE);

                messageAdapter.imageAsMessage.setVisibility(View.VISIBLE);
                messageAdapter.imageAsMessage.setBackgroundResource(R.drawable.message_sent_shape);
                Picasso.get().load(m.getMessage()).placeholder(R.drawable.wait_icon).into(messageAdapter.imageAsMessage);
            }
        }
        else{
            if(messageType.equals("text")) {
                messageAdapter.messageText.setBackgroundResource(R.drawable.message_received_shape);
                messageAdapter.messageText.setTextColor(Color.parseColor("black"));
                messageAdapter.messageText.setText(m.getMessage());

                messageAdapter.profileImage.setVisibility(View.VISIBLE);
                messageAdapter.relativeLayout.setGravity(Gravity.LEFT);

                messageAdapter.imageAsMessage.setVisibility(View.GONE);
                messageAdapter.imageAsMessage.setBackgroundResource(R.drawable.message_received_shape);
            }
            else{
                messageAdapter.messageText.setBackgroundResource(R.drawable.message_received_shape);
                messageAdapter.messageText.setTextColor(Color.parseColor("black"));
                messageAdapter.messageText.setVisibility(View.GONE);

                messageAdapter.profileImage.setVisibility(View.VISIBLE);
                messageAdapter.relativeLayout.setGravity(Gravity.LEFT);

                messageAdapter.imageAsMessage.setVisibility(View.VISIBLE);
                messageAdapter.imageAsMessage.setBackgroundResource(R.drawable.message_received_shape);
                Picasso.get().load(m.getMessage()).placeholder(R.drawable.wait_icon).into(messageAdapter.imageAsMessage);
            }
        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}
