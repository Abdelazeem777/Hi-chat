package com.ahk.sayhi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class viewImageActivity extends AppCompatActivity {

    //Android Layout
    private ImageView mCurrentImage;
    private ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        String imageUrl =getIntent().getStringExtra("imageUrl");
        mCurrentImage=findViewById(R.id.imageViewed);
        mProgressBar=findViewById(R.id.progressBarForImage);

        Picasso.get().load(imageUrl).into(mCurrentImage, new Callback() {
            @Override
            public void onSuccess() {
                mProgressBar.setVisibility(View.GONE);
                mCurrentImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {

            }
        });







    }
}
