package com.mycredit;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.jar.Attributes;

import bean.CustomerBean;
import bean.SignupBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import database.SqlLiteDbHelper;
import de.hdodenhof.circleimageview.CircleImageView;
import utils.Constants;
import utils.Permission;
import utils.Utils;

import static utils.Constants.ADDRESS;
import static utils.Constants.BUSINESS;
import static utils.Constants.EMAIL;
import static utils.Constants.FIREBASE_STORAGE_BASEURL;
import static utils.Constants.FULL_NAME;
import static utils.Constants.GET_BACK_UP_REFERENCE;
import static utils.Constants.GET_REFERENCE;
import static utils.Constants.PROFILE;
import static utils.Constants.PROFILE_IMAGE;

public class ProfileActivity extends BaseActivity {
    @BindView(R.id.imgProfile)
    CircleImageView imgProfile;
    @BindView(R.id.imgCamera)
    ImageView imgCamera;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtNumber)
    TextView txtNumber;
    @BindView(R.id.txtEmail)
    TextView txtEmail;
    @BindView(R.id.txtAddress)
    TextView txtAddress;
    @BindView(R.id.txtBusiness)
    TextView txtBusiness;
    @BindView(R.id.llShareBusinessCard)
    LinearLayout llShareBusinessCard;

    SignupBean signupBean;
    SqlLiteDbHelper sqlLiteDbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);


        sqlLiteDbHelper = new SqlLiteDbHelper(ProfileActivity.this);

        signupBean = (SignupBean) getIntent().getSerializableExtra("bean");


        setData();
    }

    private void setData() {
        if (signupBean.fullname.length() > 0)
            txtName.setText(signupBean.fullname);
        if (signupBean.email.length() > 0)
            txtEmail.setText(signupBean.email);
        if (signupBean.business.length() > 0)
            txtBusiness.setText(signupBean.business);
        if (signupBean.address.length() > 0)
            txtAddress.setText(signupBean.address);
        txtNumber.setText(signupBean.phoneNo);

        if (signupBean.profile_image != null && signupBean.profile_image.length() > 0) {
            loadImage(signupBean.profile_image);
        } else {
            SignupBean beanDatabase = sqlLiteDbHelper.getDataBean();
            if (beanDatabase.profile_image != null && beanDatabase.profile_image.length() > 0) {
                loadImage(beanDatabase.profile_image);
            }
        }
    }

    private void loadImage(String profile_image) {
        if (Utils.isValidURL(profile_image))
            Picasso.get().load(profile_image).placeholder(R.drawable.ic_profile).into(imgProfile);
        else {
            if (new File(profile_image).exists() && new File(profile_image).length() > 0)
                Picasso.get().load(new File(profile_image)).placeholder(R.drawable.ic_profile).into(imgProfile);
            else
                getImageFromFirebase();
        }
    }

    private void getImageFromFirebase() {
        DatabaseReference myRef = GET_REFERENCE.child(signupBean.phoneNo).child(PROFILE_IMAGE);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profile_image = dataSnapshot.getValue(String.class);
                Log.d("===== profile_image :- ", profile_image);
                if (profile_image.length() > 0) {
                    if (Utils.isValidURL(profile_image)) {
                        Picasso.get().load(profile_image).placeholder(R.drawable.ic_profile).into(imgProfile);
                        signupBean.profile_image = profile_image;
                        sqlLiteDbHelper.updateInfoData(signupBean);
                    } else
                        Picasso.get().load(new File(profile_image)).placeholder(R.drawable.ic_profile).into(imgProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void showEditDialog(final String type) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_address_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText edt = dialog.findViewById(R.id.edt);
        edt.setHint(type.toUpperCase());
        if (type.equals(FULL_NAME))
            edt.setText(signupBean.fullname.length() > 0 ? signupBean.fullname : "");
        else if (type.equals(BUSINESS))
            edt.setText(signupBean.business.length() > 0 ? signupBean.business : "");
        else if (type.equals(ADDRESS))
            edt.setText(signupBean.address.length() > 0 ? signupBean.address : "");
        else if (type.equals(EMAIL)) {
            edt.setText(signupBean.email.length() > 0 ? signupBean.email : "");
            edt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }

        dialog.findViewById(R.id.txtDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt.getText().toString().length() > 0) {
                    if (type.equals(FULL_NAME))
                        signupBean.fullname = edt.getText().toString();
                    else if (type.equals(BUSINESS))
                        signupBean.business = edt.getText().toString();
                    else if (type.equals(ADDRESS))
                        signupBean.address = edt.getText().toString();
                    else if (type.equals(EMAIL)) {
                        if (Utils.isValidEmailId(edt.getText().toString()))
                            signupBean.email = edt.getText().toString();
                        else {
                            edt.setError(getResources().getString(R.string.enter_valid_email));
                            return;
                        }
                    }
                    updateData(type);
                    dialog.dismiss();
                } else edt.setError(type);
            }
        });

        dialog.findViewById(R.id.imgClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.txtCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void updateData(String child) {
        DatabaseReference myRef = GET_REFERENCE.child(signupBean.phoneNo).child(child);
        DatabaseReference backupRmyRef = GET_BACK_UP_REFERENCE.child(signupBean.phoneNo).child(child);
        if (child.equalsIgnoreCase(FULL_NAME)) {
            myRef.setValue(signupBean.fullname);
            backupRmyRef.setValue(signupBean.fullname);
        } else if (child.equalsIgnoreCase(BUSINESS)) {
            myRef.setValue(signupBean.business);
            backupRmyRef.setValue(signupBean.business);
        } else if (child.equalsIgnoreCase(ADDRESS)) {
            myRef.setValue(signupBean.address);
            backupRmyRef.setValue(signupBean.address);
        } else if (child.equalsIgnoreCase(EMAIL)) {
            myRef.setValue(signupBean.email);
            backupRmyRef.setValue(signupBean.email);
        } else if (child.equalsIgnoreCase(PROFILE_IMAGE)) {
            myRef.setValue(signupBean.profile_image);
            backupRmyRef.setValue(signupBean.profile_image);
        }

        sqlLiteDbHelper.updateInfoData(signupBean);
        setData();

        if (Utils.isNetworkAvailable(ProfileActivity.this))
            GET_REFERENCE.child(signupBean.phoneNo).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    signupBean = dataSnapshot.getValue(SignupBean.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
    }

    @Override
    public void onBackPressed() {
        sendData();
    }

    private void sendData() {
        Intent intent = new Intent();
        intent.putExtra("signupBean", signupBean);
        setResult(PROFILE, intent);
        finish();
    }

    @OnClick({R.id.imgBack, R.id.imgCamera, R.id.llName, R.id.llAddress, R.id.llEmail, R.id.llBusiness})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgBack:
                onBackPressed();
                break;
            case R.id.imgCamera:
                if (Permission.checkStoragePermission(ProfileActivity.this))
                    selectImage();
                break;
            case R.id.llName:
                showEditDialog(FULL_NAME);
                break;
            case R.id.llAddress:
                showEditDialog(ADDRESS);
                break;
            case R.id.llEmail:
                showEditDialog(EMAIL);
                break;
            case R.id.llBusiness:
                showEditDialog(BUSINESS);
                break;
        }
    }

    private void selectImage() {
        ImagePicker.create(this)
                .folderMode(true)
                .toolbarFolderTitle("Folder")
                .toolbarImageTitle("Tap to select")
                .toolbarArrowColor(Color.BLACK)
                .includeVideo(false)
                .single()
                .limit(1)
                .showCamera(true)
                .imageDirectory("Camera")
                .enableLog(false)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            List<Image> images = ImagePicker.getImages(data);
            if (images.size() > 0) {
                String path = images.get(0).getPath();
                File file = new File(path);
                if (file.length() > 0) {
                    if (Utils.isNetworkAvailable(ProfileActivity.this)) {
                        Utils.showProgress(ProfileActivity.this);
                        uploadImage(file);
                    }
                    copyAndSaveInDataBase(file);
                }
            } else Utils.closeProgress();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void copyAndSaveInDataBase(File file) {
        try {
            File destination = Utils.copyFile(ProfileActivity.this, file, signupBean.phoneNo.replace("+", ""));
            signupBean.profile_image = destination.getAbsolutePath();
            sqlLiteDbHelper.updateInfoData(signupBean);
            setData();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("===== exception :- ", " profile " + e.getMessage());
        }
    }

    private void uploadImage(File file) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(FIREBASE_STORAGE_BASEURL);
        final StorageReference ref = storageRef.child(Constants.USER + "/" + signupBean.phoneNo + ".jpg");
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            UploadTask uploadTask = ref.putStream(stream);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception exception) {
                    Utils.closeProgress();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            String fileUrl = downloadUrl.toString();
                            signupBean.profile_image = fileUrl;
                            updateData(PROFILE_IMAGE);
                            Utils.closeProgress();
                        }
                    });
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Utils.closeProgress();
        }
    }
}