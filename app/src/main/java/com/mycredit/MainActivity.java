package com.mycredit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appizona.yehiahd.fastsave.FastSave;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import bean.CustomerBean;
import bean.DownloadImageBean;
import bean.SignupBean;
import bean.TransactionBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import database.SqlLiteDbHelper;
import de.hdodenhof.circleimageview.CircleImageView;
import fragments.CreditFragment;
import fragments.StockFragment;
import utils.Constants;
import utils.Utils;

import static utils.Constants.FIREBASE_STORAGE_BASEURL;
import static utils.Constants.GET_BACK_UP_REFERENCE;
import static utils.Constants.GET_REFERENCE;
import static utils.Constants.PROFILE;
import static utils.Constants.PROFILE_IMAGE;
import static utils.Constants.USER;

public class MainActivity extends BaseActivity {
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txtNameTitle)
    TextView txtNameTitle;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.profile_image)
    CircleImageView profileImage;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    View nav_header;
    @BindView(R.id.imageProfile)
    CircleImageView imageProfile;
    @BindView(R.id.txtName)
    TextView txtName;
    @BindView(R.id.txtNumber)
    TextView txtNumber;

    FirebaseAuth mAuth;

    Fragment fragment;
    SignupBean signupBean;
    SqlLiteDbHelper sqlLiteDbHelper;
    ArrayList<CustomerBean> UploadCustomersImage, allCustomers;
    ArrayList<TransactionBean> UploadTransactionImage;
    ArrayList<String> UploadTransactionCustomerNumber;
    ArrayList<DownloadImageBean> downloadImageBeans = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        sqlLiteDbHelper = new SqlLiteDbHelper(this);
        signupBean = (SignupBean) getIntent().getSerializableExtra("bean");
        FastSave.getInstance().saveObject(USER, signupBean);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        nav_header = navView.findViewById(R.id.nav_header);

        fragment = new CreditFragment();
        setFragment();
        setHeaderData();
        setTitleData();

        nav_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileActivity();
            }
        });

        if (Utils.isNetworkAvailable(MainActivity.this)) {
            progressBar.setVisibility(View.VISIBLE);
            getUserLoginData();
            UploadContactImage();
            downloadImagesFromServer();
            progressBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void downloadImagesFromServer() {
        SignupBean signupBean = sqlLiteDbHelper.getDataBean();
        if (Utils.isValidURL(signupBean.profile_image)) {
            DownloadImageBean bean = new DownloadImageBean();
            bean.imageUrl = signupBean.profile_image;
            bean.signupBean = signupBean;
            bean.filePath = Utils.getDownloadPath(MainActivity.this, signupBean.phoneNo.replace(" ", "").replace("+", ""));
            downloadImageBeans.add(bean);
        }
        getCustomerDataForDownloadImages();
    }

    private void getCustomerDataForDownloadImages() {
        DownloadImageBean bean;
        ArrayList<CustomerBean> customerBeans = sqlLiteDbHelper.getCustomerData();
        for (int i = 0; i < customerBeans.size(); i++) {
            if (Utils.isValidURL(customerBeans.get(i).profile)) {
                bean = new DownloadImageBean();
                bean.imageUrl = customerBeans.get(i).profile;
                bean.customerBean = customerBeans.get(i);
                bean.filePath = Utils.getDownloadPath(MainActivity.this, customerBeans.get(i).number.replace(" ", "").replace("+", ""));
                downloadImageBeans.add(bean);
            }
            ArrayList<TransactionBean> transactionBeans = sqlLiteDbHelper.getTransactionData(customerBeans.get(i).number);
            for (int j = 0; j < transactionBeans.size(); j++) {
                if (Utils.isValidURL(transactionBeans.get(j).image)) {
                    bean = new DownloadImageBean();
                    bean.imageUrl = transactionBeans.get(j).image;
                    bean.customerBean = customerBeans.get(i);
                    bean.transactionBean = transactionBeans.get(j);
                    bean.filePath = Utils.getTransactionDownloadPath(MainActivity.this, transactionBeans.get(j).key.replace("-", ""));
                    downloadImageBeans.add(bean);
                }
            }
        }
        Log.i("------", " -> downlaod size = " + downloadImageBeans.size());
        // startImageDownload(0);
        if (downloadImageBeans.size() > 0)
            new DownloadsImages().execute(0);
    }

    private void imageDownload(Integer pos) {
        Log.i("------", " -> imageDownload = " + pos);
        DownloadImageBean bean = downloadImageBeans.get(pos);
        URL url = null;

        File path = new File(bean.filePath.substring(0, bean.filePath.lastIndexOf("/")));
        Log.d("----- folder :- ", path.getAbsolutePath());
        if (!path.exists()) {
            path.mkdirs();
        }

        File imageFile = new File(bean.filePath);
        if (imageFile.exists() && imageFile.length() > 0) {
            Log.i("------", " -> alredy exist = " + imageFile.getAbsolutePath());
            storeInDatabaseAndStartNewDownlaod(bean, pos);
            return;
        }

        try {
            url = new URL(bean.imageUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Bitmap bm = null;
        try {
            bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Create Path to save Image
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
        }
        storeInDatabaseAndStartNewDownlaod(bean, pos);
    }

    private void storeInDatabaseAndStartNewDownlaod(DownloadImageBean bean, Integer pos) {
        if (bean.signupBean != null) {
            bean.signupBean.profile_image = bean.filePath;
            sqlLiteDbHelper.updateInfoData(bean.signupBean);
            Log.i("------", " -> signup complete = " + bean.signupBean.fullname);
        } else if (bean.customerBean != null && bean.transactionBean != null) {
            bean.transactionBean.image = bean.filePath;
            sqlLiteDbHelper.updateTransactionData(bean.customerBean.number, bean.transactionBean);
            Log.i("------", " -> transaction complete = " + bean.customerBean.number + " - " + bean.transactionBean.key);
        } else if (bean.customerBean != null) {
            bean.customerBean.profile = bean.filePath;
            sqlLiteDbHelper.updateCustomerData(bean.customerBean);
            Log.i("------", " -> customer complete = " + bean.customerBean.name);
        }
        if (downloadImageBeans.size() > pos + 1)
            imageDownload(pos + 1);
    }

    class DownloadsImages extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            imageDownload(integers[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void type) {
            super.onPostExecute(type);
        }
    }

    private void getUserLoginData() {
        DatabaseReference myRef = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("====== get user :- ", dataSnapshot.toString());
                SignupBean signupBean = dataSnapshot.getValue(SignupBean.class);
                if (!Utils.isValidURL(signupBean.profile_image)) {
                    Log.d("====== profil_image :- ", signupBean.profile_image);
                    File file = new File(signupBean.profile_image);
                    if (file != null && file.exists() && file.length() > 0) {
                        uploadUserImage(file);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void uploadUserImage(File file) {
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
                            DatabaseReference myRef = GET_REFERENCE.child(signupBean.phoneNo);
                            myRef.child(PROFILE_IMAGE).setValue(signupBean.profile_image);

                            DatabaseReference myBackupRef = GET_BACK_UP_REFERENCE.child(signupBean.phoneNo);
                            myBackupRef.child(PROFILE_IMAGE).setValue(signupBean.profile_image);
                        }
                    });
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Utils.closeProgress();
        }
    }

    private void UploadContactImage() {
        DatabaseReference myRef = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(Constants.CUSTOMER);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UploadCustomersImage = new ArrayList<>();
                allCustomers = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    CustomerBean customerBean = data.getValue(CustomerBean.class);
                    Log.d("===== profile :- ", customerBean.profile);
                    if (!Utils.isValidURL(customerBean.profile))
                        UploadCustomersImage.add(customerBean);
                    allCustomers.add(customerBean);
                }
                if (UploadCustomersImage.size() > 0)
                    startUploadContactImage(0);
                if (allCustomers.size() > 0) {
                    UploadTransactionImage = new ArrayList<>();
                    UploadTransactionCustomerNumber = new ArrayList<>();
                    getRemainUplaodTransaction(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getRemainUplaodTransaction(final int i) {
        Log.d("=====  :- ", " getRemainUplaodTransaction " + i);
        DatabaseReference myRef = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(Constants.TRANSACTION).child(allCustomers.get(i).number);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    TransactionBean transactionBean = data.getValue(TransactionBean.class);
                    if (transactionBean.image != null && transactionBean.image.length() > 0) {
                        if (!Utils.isValidURL(transactionBean.image)) {
                            UploadTransactionImage.add(transactionBean);
                            UploadTransactionCustomerNumber.add(allCustomers.get(i).number);
                        }
                    }
                }
                Log.d("===== allCustomers :- ", allCustomers.size() + "");
                if (allCustomers.size() > i + 1) {
                    getRemainUplaodTransaction(i + 1);
                } else {
                    if (UploadTransactionImage.size() > 0)
                        startUploadTransactionImage(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void startUploadContactImage(final int i) {
        Log.d("=====  :- ", " startUploadContactImage " + i);
        File file = new File(UploadCustomersImage.get(i).profile);
        if (file != null && file.exists() && file.length() > 0) {
            Log.d("===== start customr :- ", UploadCustomersImage.get(i).name + " - " + UploadCustomersImage.get(i).profile);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(FIREBASE_STORAGE_BASEURL);
            final StorageReference ref = storageRef.child(Constants.CUSTOMER + "/" + mAuth.getCurrentUser().getPhoneNumber()
                    + "/" + UploadCustomersImage.get(i).number + "/" + UploadCustomersImage.get(i).number + ".jpg");
            InputStream stream = null;
            try {
                stream = new FileInputStream(file);
                UploadTask uploadTask = ref.putStream(stream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception exception) {
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.d("===== suces custmer :- ", UploadCustomersImage.get(i).name + " - " + UploadCustomersImage.get(i).number);
                                Uri downloadUrl = uri;
                                String fileUrl = downloadUrl.toString();
                                UploadCustomersImage.get(i).profile = fileUrl;

                                DatabaseReference myRef = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                                        .child(Constants.CUSTOMER).child(UploadCustomersImage.get(i).number);
                                myRef.setValue(UploadCustomersImage.get(i));

                                DatabaseReference myBackupRef = GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                                        .child(Constants.CUSTOMER).child(UploadCustomersImage.get(i).number);
                                myBackupRef.setValue(UploadCustomersImage.get(i));

                                if (UploadCustomersImage.size() > i + 1)
                                    startUploadContactImage(i + 1);
                                else
                                    UploadCustomersImage.clear();
                            }
                        });
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            if (UploadCustomersImage.size() > i + 1)
                startUploadContactImage(i + 1);
            else UploadCustomersImage.clear();
        }
    }

    private void startUploadTransactionImage(final int i) {
        Log.d("=====  :- ", " startUploadTransactionImage " + i);
        File file = new File(UploadTransactionImage.get(i).image);
        if (file != null && file.exists() && file.length() > 0) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(FIREBASE_STORAGE_BASEURL);
            final StorageReference ref = storageRef.child(Constants.TRANSACTION + "/" + mAuth.getCurrentUser().getPhoneNumber()
                    + "/" + UploadTransactionCustomerNumber.get(i) + "/" + UploadTransactionImage.get(i).key + ".jpg");
            InputStream stream = null;
            try {
                stream = new FileInputStream(file);
                UploadTask uploadTask = ref.putStream(stream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception exception) {
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("===== success trans :- ", UploadTransactionCustomerNumber.get(i) + " - " + UploadTransactionImage.get(i).key);
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String fileUrl = uri.toString();
                                UploadTransactionImage.get(i).image = fileUrl;

                                GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                                        .child(Constants.TRANSACTION).child(UploadTransactionCustomerNumber.get(i))
                                        .child(UploadTransactionImage.get(i).key).setValue(UploadTransactionImage.get(i));

                                GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                                        .child(Constants.TRANSACTION).child(UploadTransactionCustomerNumber.get(i))
                                        .child(UploadTransactionImage.get(i).key).setValue(UploadTransactionImage.get(i));

                                if (UploadTransactionImage.size() > i + 1)
                                    startUploadTransactionImage(i + 1);
                                else UploadTransactionImage.clear();
                            }
                        });
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            if (UploadTransactionImage.size() > i + 1)
                startUploadTransactionImage(i + 1);
            else UploadTransactionImage.clear();
        }
    }

    private void openProfileActivity() {
        startActivityForResult(new Intent(MainActivity.this, ProfileActivity.class)
                .putExtra("bean", signupBean), PROFILE);
    }

    private void setTitleData() {
        txtNameTitle.setText(signupBean.fullname.length() > 0 ? signupBean.fullname : signupBean.phoneNo);
        if (signupBean.profile_image != null && signupBean.profile_image.length() > 0) {
            loadImage(signupBean.profile_image, profileImage);
        } else {
            SignupBean beanDatabase = sqlLiteDbHelper.getDataBean();
            if (beanDatabase.profile_image != null && beanDatabase.profile_image.length() > 0) {
                loadImage(beanDatabase.profile_image, profileImage);
            }
        }
    }

    private void loadImage(String profile_image, CircleImageView imageProfile) {
        if (Utils.isValidURL(profile_image))
            Picasso.get().load(profile_image).placeholder(R.drawable.ic_profile).into(imageProfile);
        else {
            if (new File(profile_image).length() > 0)
                Picasso.get().load(new File(profile_image)).placeholder(R.drawable.ic_profile).into(imageProfile);
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
                if (profile_image.length() > 0) {
                    if (Utils.isValidURL(profile_image)) {
                        Picasso.get().load(profile_image).placeholder(R.drawable.ic_profile).into(imageProfile);
                        signupBean.profile_image = profile_image;
                        sqlLiteDbHelper.updateInfoData(signupBean);
                    } else
                        Picasso.get().load(new File(profile_image)).placeholder(R.drawable.ic_profile).into(imageProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setHeaderData() {
        txtName.setText(signupBean.fullname);
        txtNumber.setText(signupBean.phoneNo);
        if (signupBean.profile_image != null && signupBean.profile_image.length() > 0) {
            loadImage(signupBean.profile_image, imageProfile);
        } else {
            SignupBean beanDatabase = sqlLiteDbHelper.getDataBean();
            if (beanDatabase.profile_image != null && beanDatabase.profile_image.length() > 0) {
                loadImage(beanDatabase.profile_image, imageProfile);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, AccountActivity.class).putExtra("bean", signupBean));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fragment.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.getSerializableExtra("signupBean") != null) {
                signupBean = (SignupBean) data.getSerializableExtra("signupBean");
                setHeaderData();
                setTitleData();
            }
        }
    }

    @OnClick(R.id.toolbar)
    public void onViewClicked() {
        openProfileActivity();
    }

    @OnClick({R.id.llBalanceSheet, R.id.llStockManagement, R.id.llHelp, R.id.llShare, R.id.llAbout, R.id.llPrivacyPolicy, R.id.llLogout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.llBalanceSheet:
                fragment = new CreditFragment();
                setFragment();
                break;
            case R.id.llStockManagement:
                fragment = new StockFragment();
                setFragment();
                break;
            case R.id.llHelp:
                break;
            case R.id.llShare:
                break;
            case R.id.llAbout:
                break;
            case R.id.llPrivacyPolicy:
                break;
            case R.id.llLogout:
                FirebaseAuth.getInstance().signOut();
                sqlLiteDbHelper.clearTable();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                break;
        }
    }

    private void setFragment() {
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}
