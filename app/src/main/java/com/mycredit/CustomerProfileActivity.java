package com.mycredit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.akash.RevealSwitch;
import com.akash.revealswitch.OnToggleListener;
import com.esafirm.imagepicker.features.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import bean.CustomerBean;
import bean.SignupBean;
import bean.TransactionBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import database.SqlLiteDbHelper;
import de.hdodenhof.circleimageview.CircleImageView;
import utils.Constants;
import utils.Permission;
import utils.Utils;

import static utils.Constants.CUSTOMER;
import static utils.Constants.DELETE_CUSTOMER_REQUEST;
import static utils.Constants.GET_BACK_UP_REFERENCE;
import static utils.Constants.GET_REFERENCE;
import static utils.Constants.LANGUAGE_DISABLE;
import static utils.Constants.PROFILE;
import static utils.Constants.STORAGE_REFERENCE;
import static utils.Constants.TIME;
import static utils.Constants.TRANSACTION;
import static utils.Utils.closeProgress;
import static utils.Utils.showProgress;

public class CustomerProfileActivity extends BaseActivity {
    CustomerBean customerBean;
    @BindView(R.id.imgProfile)
    CircleImageView imgProfile;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtNumber)
    TextView txtNumber;
    @BindView(R.id.txtAddress)
    TextView txtAddress;
    @BindView(R.id.txtLanguage)
    TextView txtLanguage;
    @BindView(R.id.switchLanguage)
    RevealSwitch switchLanguage;
    @BindView(R.id.scrollView)
    ScrollView scrollView;

    FirebaseAuth mAuth;
    String isRefresh = "0";
    String last_transaction_time;
    SqlLiteDbHelper sqlLiteDbHelper;
    ArrayList<TransactionBean> transactionBeans;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);
        ButterKnife.bind(this);

        sqlLiteDbHelper = new SqlLiteDbHelper(this);
        customerBean = (CustomerBean) getIntent().getSerializableExtra(CUSTOMER);
        transactionBeans = (ArrayList<TransactionBean>) getIntent().getSerializableExtra(TRANSACTION);
        last_transaction_time = getIntent().getStringExtra(TIME);

        mAuth = FirebaseAuth.getInstance();

        setCustomerData();
    }

    private void setCustomerData() {
        if (customerBean.address.length() > 0)
            txtAddress.setText(customerBean.address);
        txtName.setText(customerBean.name);
        txtNumber.setText(customerBean.number);
        setSelectedLan();
        if (customerBean.profile != null && customerBean.profile.length() > 0) {
            loadImage(customerBean.profile);
        }
        if (customerBean.language != null && customerBean.language.length() > 0 && !customerBean.language.contains(LANGUAGE_DISABLE))
            switchLanguage.setEnable(true);
        else switchLanguage.setEnable(false);
        switchLanguage.setToggleListener(new OnToggleListener() {
            @Override
            public void onToggle(boolean b) {
                if (b)
                    customerBean.language = customerBean.language.replace(LANGUAGE_DISABLE, "");
                else customerBean.language = customerBean.language + LANGUAGE_DISABLE;
                updateCustomerData();
            }
        });
    }

    private void setSelectedLan() {
        if (customerBean.language.equalsIgnoreCase(Constants.HINDI_CODE)) {
            txtLanguage.setText(getResources().getString(R.string.hindi));
        } else if (customerBean.language.equalsIgnoreCase(Constants.HINGLISH_CODE)) {
            txtLanguage.setText(getResources().getString(R.string.hinglish));
        } else if (customerBean.language.equalsIgnoreCase(Constants.ENGLISH_CODE)) {
            txtLanguage.setText(getResources().getString(R.string.english));
        }
    }

    private void loadImage(String profile_image) {
        if (Utils.isValidURL(profile_image))
            Picasso.get().load(profile_image).placeholder(R.drawable.ic_profile).into(imgProfile);
        else {
            if (new File(profile_image).exists() && new File(profile_image).length() > 0) {
                Picasso.get().load(new File(profile_image)).placeholder(R.drawable.ic_profile).into(imgProfile);
                Picasso.get().invalidate(new File(profile_image));
            } else
                getImageFromFirebase();
        }
    }

    private void getImageFromFirebase() {
        DatabaseReference myRef = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(CUSTOMER).child(customerBean.number).child("profile");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String profile_image = dataSnapshot.getValue(String.class);
                Log.d("===== profile_image :- ", profile_image);
                if (profile_image.length() > 0) {
                    if (Utils.isValidURL(profile_image)) {
                        Picasso.get().load(profile_image).placeholder(R.drawable.ic_profile).into(imgProfile);
                        customerBean.profile = profile_image;
                        sqlLiteDbHelper.updateCustomerData(customerBean);
                    } else
                        Picasso.get().load(new File(profile_image)).placeholder(R.drawable.ic_profile).into(imgProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @OnClick({R.id.llLanguage, R.id.imgBack, R.id.imgCamera, R.id.llAddress, R.id.rlDeleteCustomer, R.id.llName})
    void clicks(View view) {
        switch (view.getId()) {
            case R.id.llLanguage:
                showLanguageDialog();
                break;
            case R.id.imgBack:
                onBackPressed();
                break;
            case R.id.rlDeleteCustomer:
                startActivityForResult(new Intent(CustomerProfileActivity.this, DeleteCustomerActivity.class)
                                .putExtra(CUSTOMER, customerBean)
                                .putExtra(TRANSACTION, transactionBeans)
                                .putExtra(TIME, last_transaction_time),
                        DELETE_CUSTOMER_REQUEST);
                break;
            case R.id.imgCamera:
                if (Permission.checkStoragePermission(CustomerProfileActivity.this))
                    selectImage();
                break;
            case R.id.llAddress:
                showAddressDialog(Constants.ADDRESS);
                break;
            case R.id.llName:
                showAddressDialog(Constants.FULL_NAME);
                break;
        }
    }

    private void openDeleteCustomeDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.delete_customer_dialog));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        deleteCustomer();
                    }
                });

        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deleteCustomer() {
        showProgress(CustomerProfileActivity.this);

        DatabaseReference reference = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(CUSTOMER).child(customerBean.number);
        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(TRANSACTION).child(customerBean.number).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                DatabaseReference reference = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber());
                                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        SignupBean signupBean = dataSnapshot.getValue(SignupBean.class);
                                        startActivity(new Intent(CustomerProfileActivity.this, MainActivity.class)
                                                .putExtra("bean", signupBean)
                                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        closeProgress();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                closeProgress();
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                closeProgress();
            }
        });
    }

    public void showLanguageDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.select_language_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView imgEnglish = dialog.findViewById(R.id.imgEnglish);
        ImageView imgHindi = dialog.findViewById(R.id.imgHindi);
        ImageView imgHinglish = dialog.findViewById(R.id.imgHinglish);
        if (customerBean.language.equalsIgnoreCase(Constants.HINDI_CODE)) {
            imgHindi.setVisibility(View.VISIBLE);
        } else if (customerBean.language.equalsIgnoreCase(Constants.HINGLISH_CODE)) {
            imgHinglish.setVisibility(View.VISIBLE);
        } else if (customerBean.language.equalsIgnoreCase(Constants.ENGLISH_CODE)) {
            imgEnglish.setVisibility(View.VISIBLE);
        }

        dialog.findViewById(R.id.rlHinglish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerBean.language = Constants.HINGLISH_CODE;
                updateCustomerData();
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.rlHindi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerBean.language = Constants.HINDI_CODE;
                updateCustomerData();
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.rlEnglish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerBean.language = Constants.ENGLISH_CODE;
                updateCustomerData();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void showAddressDialog(final String type) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_address_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText edtAddress = dialog.findViewById(R.id.edt);
        if (type.equals(Constants.FULL_NAME))
            edtAddress.setText(customerBean.name);
        else
            edtAddress.setText(customerBean.address);

        dialog.findViewById(R.id.txtDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtAddress.getText().toString().length() > 0) {
                    if (type.equals(Constants.FULL_NAME))
                        customerBean.name = edtAddress.getText().toString();
                    else
                        customerBean.address = edtAddress.getText().toString();
                    updateCustomerData();
                    dialog.dismiss();
                } else
                    edtAddress.setError(getResources().getString(R.string.add_address));
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
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.IS_REFRESH, isRefresh);
        setResult(PROFILE, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            List<com.esafirm.imagepicker.model.Image> images = ImagePicker.getImages(data);
            if (images.size() > 0) {
                String path = images.get(0).getPath();
                File file = new File(path);
                if (Utils.isNetworkAvailable(CustomerProfileActivity.this)) {
                    Utils.showProgress(CustomerProfileActivity.this);
                    uploadImage(file);
                }
                copyAndSaveInDataBase(file);
            } else Utils.closeProgress();
        }
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DELETE_CUSTOMER_REQUEST) {
            isRefresh = data.getStringExtra(Constants.IS_REFRESH);
        }
    }

    private void copyAndSaveInDataBase(File file) {
        try {
            File destination = Utils.copyFile(CustomerProfileActivity.this, file, customerBean.number.replace("+", ""));
            customerBean.profile = destination.getAbsolutePath();
            isRefresh = "1";
            sqlLiteDbHelper.updateCustomerData(customerBean);
            setCustomerData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadImage(File file) {
        final StorageReference ref = STORAGE_REFERENCE.child(CUSTOMER
                + "/" + mAuth.getCurrentUser().getPhoneNumber()
                + "/" + customerBean.number + "/" + customerBean.number + ".jpg");
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
                            // getting image uri and converting into string
                            Uri downloadUrl = uri;
                            String fileUrl = downloadUrl.toString();
                            customerBean.profile = fileUrl;
                            updateCustomerData();
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

    private void updateCustomerData() {
        isRefresh = "1";
        sqlLiteDbHelper.updateCustomerData(customerBean);
        customerBean = sqlLiteDbHelper.getCustomer(customerBean.number);
        setCustomerData();

        DatabaseReference myBackUpRef = GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(CUSTOMER).child(customerBean.number);
        myBackUpRef.setValue(customerBean);
        DatabaseReference myRef = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(CUSTOMER).child(customerBean.number);
        myRef.setValue(customerBean);
    }
}
