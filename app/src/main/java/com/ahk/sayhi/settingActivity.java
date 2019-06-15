package com.ahk.sayhi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class settingActivity extends AppCompatActivity {
    //Firebase
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageRef;
    private StorageReference filePath;
    private StorageReference thumbFilePathRef;

    //Android layout
    private CircleImageView mDisplayImage;
    private TextView mName;
    private Button buChangeImage;


    //Other
    Uri destinationUri;
    Uri resultUri;
    Uri sourceUri;
    byte[] thumbBytes;
    private String mDisplayName;


    private static final int GALLERY_PICK=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mDisplayName=getIntent().getStringExtra("userName");
        mDisplayImage=(CircleImageView)findViewById(R.id.circleImageView);
        mName=(TextView)findViewById(R.id.displayName);
        mName.setText(mDisplayName);
        buChangeImage =(Button)findViewById(R.id.buChangeImage);

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String currentUid=mCurrentUser.getUid();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        mUserDatabase.keepSynced(true);
        mStorageRef= FirebaseStorage.getInstance().getReference();
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final String image=dataSnapshot.child("image").getValue().toString();


                if(!image.equals("default"))
//                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(mDisplayImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(settingActivity.this, databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        });


        buChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);




            }
        });

    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                Uri imageUri = result.getUri();
//                // start cropping activity for pre-acquired image saved on the device
//                CropImage.activity(imageUri)
//                        .start(this);
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Exception error = result.getError();
//            }
//        }finiss
//    }



@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(data==null) {
    return;
    }
    else {
        UCrop.of(data.getData(), Uri.fromFile(new File(getCacheDir(), "destinationImage.jpg")))
                .withAspectRatio(1, 1)
                .withMaxResultSize(2000, 2000)
                .start(settingActivity.this);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {

            Toast.makeText(this,"Uploading...\nPlease wait.",Toast.LENGTH_LONG).show();
            final Uri resultUri = UCrop.getOutput(data);

            final File thumbFilePath = new File(resultUri.getPath());
            try {
                Bitmap thumbBitMap = new Compressor(this)
                        .setMaxHeight(150)
                        .setMaxWidth(150)
                        .setQuality(10)
                        .compressToBitmap(thumbFilePath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                thumbBytes = baos.toByteArray();
            } catch (IOException e) {
                Toast.makeText(this, e.getStackTrace().toString(), Toast.LENGTH_SHORT).show();
            }


            filePath = mStorageRef.child("profileImages").child(mCurrentUser.getUid() + ".jpg");

            thumbFilePathRef = mStorageRef.child("profileImages").child("thumbs").child(mCurrentUser.getUid() + ".jpg");
            filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();

                                final UploadTask uploadTask = thumbFilePathRef.putBytes(thumbBytes);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {

                                        if (thumbTask.isSuccessful()) {
                                            thumbFilePathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    final String thumbDownloadUrl = uri.toString();

                                                    Map<String, Object> updateHashMap = new HashMap<>();
                                                    updateHashMap.put("image", downloadUrl);
                                                    updateHashMap.put("thumbImage", thumbDownloadUrl);

                                                    mUserDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {

                                                                Toasty.success(settingActivity.this, "Picture changed successfully", Toast.LENGTH_SHORT, false).show();
                                                                finish();
                                                                startActivity(new Intent(getApplicationContext(),settingActivity.class).putExtra("userName",mDisplayName));
                                                            }
                                                        }
                                                    });
                                                }
                                            });


                                        }
                                    }
                                });


                            }
                        });


                    } else {
                        Toast.makeText(settingActivity.this, "Error", Toast.LENGTH_SHORT).show();

                    }
                }
            });

        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }
}

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            mUserDatabase.child("online").setValue("true");
        }



    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

}
