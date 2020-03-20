package database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import bean.CustomerBean;
import bean.SignupBean;
import bean.TransactionBean;
import utils.Constants;
import utils.Utils;

import static utils.Constants.GET_BACK_UP_REFERENCE;
import static utils.Constants.GET_REFERENCE;
import static utils.Constants.STORAGE_REFERENCE;

public class SqlLiteDbHelper {
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "ShockAndBalanceSheet.db";
    private static final String USER_INFO = "UserInfo";
    private static final String CUSTOMER = "Customer";
    private static final String TRANSACTION = "PaymentTransaction";

    DataBaseHelper mDbHelper;
    Activity mContex;
    SQLiteDatabase sqLiteDatabase;

    private static final String ADDRESS = "address";
    private static final String BUSINESS = "business";
    private static final String IMEI = "deviceIMEI";
    private static final String EMAIL = "email";
    private static final String FULL_NAME = "fullname";
    private static final String PASSWORD = "password";
    private static final String PHONE_NO = "phoneNo";
    private static final String PROFILE_IMAGE = "profile_image";
    private static final String NUMBER = "number";
    private static final String NAME = "name";
    private static final String ADD_TIME = "add_time";
    private static final String PROFILE = "profile";
    private static final String TYPE = "type";
    private static final String IS_SYNC = "is_sync";
    private static final String LANGUAGE = "language";
    private static final String IS_DELETED = "is_deleted";

    private static final String AMOUNT = "Amount";
    private static final String TIME = "time";
    private static final String NOTE = "note";
    private static final String ACCEPT = "accept";
    private static final String TOTAL = "total";
    private static final String DATE = "date";
    private static final String TRANS_DATE = "trans_date";
    private static final String IMAGE = "image";
    private static final String DELETD_ON = "deleted_on";
    private static final String KEY = "transaction_key";

    private static final String CREATE_INFO_TABLE = "create table if not exists " + USER_INFO + "(" + "id INTEGER primary key autoincrement," + ADDRESS + " text,"
            + BUSINESS + " text," + IMEI + " text," + EMAIL + " text," + FULL_NAME + " text," + PASSWORD + " text,"
            + PHONE_NO + " text," + PROFILE_IMAGE + " text," + IS_SYNC + " text" + ")";

    private static final String CREATE_CUSTOMER_TABLE = "create table if not exists " + CUSTOMER + "("
            + "id INTEGER primary key autoincrement," + NUMBER + " text," + NAME + " text," + AMOUNT + " text,"
            + ADD_TIME + " text," + ADDRESS + " text," + PROFILE + " text," + TYPE + " text," + IS_DELETED + "" +
            " text," + IS_SYNC + " text," + LANGUAGE + " text" + ")";

    private static final String CREATE_TRANSACTION_TABLE = "create table if not exists " + TRANSACTION
            + "(" + "id INTEGER primary key autoincrement, " + NUMBER + " text," + AMOUNT + " text,"
            + TIME + " text," + NOTE + " text," + ACCEPT + " text," + TOTAL + " text," + DATE + " text," + TRANS_DATE + " text,"
            + IMAGE + " text," + TYPE + " text," + DELETD_ON + " text," + KEY + " text," + IS_SYNC + " text" + ")";

    private static class DataBaseHelper extends SQLiteOpenHelper {
        Context context;

        public DataBaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL(CREATE_INFO_TABLE);
            db.execSQL(CREATE_CUSTOMER_TABLE);
            db.execSQL(CREATE_TRANSACTION_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + USER_INFO);
            db.execSQL("DROP TABLE IF EXISTS " + CUSTOMER);
            db.execSQL("DROP TABLE IF EXISTS " + TRANSACTION);
            onCreate(db);
        }
    }

    public SqlLiteDbHelper(Activity context) {
        this.mContex = context;
        mDbHelper = new DataBaseHelper(mContex);
    }

    public boolean isCustomerAvailable(CustomerBean customerBean) {
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        Cursor cursor = null;
        String query = "SELECT number FROM " + CUSTOMER + " WHERE number =?";
        cursor = sqLiteDatabase.rawQuery(query, new String[]{customerBean.number});
        Log.d("Cursor Count : ", cursor.getCount() + "");
        cursor.close();
        sqLiteDatabase.close();
        return cursor.getCount() > 0 ? true : false;
    }

    public void insertCustomerData(CustomerBean bean) {
        if (!isCustomerAvailable(bean)) {
            sqLiteDatabase = mDbHelper.getWritableDatabase();
            ContentValues row = new ContentValues();
            row.put(NUMBER, bean.number);
            Log.d("==== insert :- ", bean.name);
            row.put(NAME, bean.name);
            row.put(AMOUNT, bean.amount);
            row.put(ADD_TIME, bean.add_time);
            row.put(ADDRESS, bean.address);
            row.put(PROFILE, bean.profile);
            row.put(TYPE, bean.type);
            row.put(IS_DELETED, "0");
            row.put(IS_SYNC, Utils.isNetworkAvailable(mContex) ? "1" : "0");
            row.put(LANGUAGE, bean.language);
            sqLiteDatabase.insert(CUSTOMER, null, row);
            sqLiteDatabase.close();
        } else Log.d("==== already avail :- ", bean.name);
    }

    public void updateCustomerData(CustomerBean bean) {
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put(NUMBER, bean.number);
        row.put(NAME, bean.name);
        row.put(AMOUNT, bean.amount);
        row.put(ADD_TIME, bean.add_time);
        row.put(ADDRESS, bean.address);
        row.put(PROFILE, bean.profile);
        row.put(TYPE, bean.type);
        row.put(IS_DELETED, bean.is_deleted);
        row.put(IS_SYNC, Utils.isNetworkAvailable(mContex) ? "1" : "0");
        row.put(LANGUAGE, bean.language);
        sqLiteDatabase.update(CUSTOMER, row, NUMBER + " = '" + bean.number + "'", null);
        sqLiteDatabase.close();
    }

    public ArrayList<CustomerBean> getCustomerData() {
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        String selectQuery = "select * from " + CUSTOMER;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        ArrayList<CustomerBean> customerBeans = new ArrayList<>();
        for (int k = 0; k < cursor.getCount(); k++) {
            CustomerBean bean = new CustomerBean();
            bean.number = cursor.getString(1);
            bean.name = cursor.getString(2);
            bean.amount = cursor.getString(3);
            bean.add_time = cursor.getString(4);
            bean.address = cursor.getString(5);
            bean.profile = cursor.getString(6);
            bean.type = cursor.getString(7);
            bean.is_deleted = cursor.getString(8);
            bean.language = cursor.getString(10);
            customerBeans.add(bean);
            if (Utils.isNetworkAvailable(mContex) && FirebaseAuth.getInstance().getCurrentUser() != null) {
                if (cursor.getString(9).equals("0")) {
                    DatabaseReference myRef = GET_REFERENCE
                            .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                            .child(Constants.CUSTOMER).child(bean.number);
                    myRef.setValue(bean);

                    DatabaseReference myBackUpRef = Constants.GET_BACK_UP_REFERENCE
                            .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                            .child(Constants.CUSTOMER).child(bean.number);
                    myBackUpRef.setValue(bean);

                    updateCustomerData(bean);
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return customerBeans;
    }

    public CustomerBean getCustomer(String number) {
        CustomerBean bean = null;
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        String selectQuery = "select * from " + CUSTOMER + " Where " + NUMBER + " = '" + number + "'";
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int k = 0; k < cursor.getCount(); k++) {
            bean = new CustomerBean();
            bean.number = cursor.getString(1);
            bean.name = cursor.getString(2);
            bean.amount = cursor.getString(3);
            bean.add_time = cursor.getString(4);
            bean.address = cursor.getString(5);
            bean.profile = cursor.getString(6);
            bean.type = cursor.getString(7);
            bean.is_deleted = cursor.getString(8);
            bean.language = cursor.getString(10);
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return bean;
    }

    public void deleteCustomerAndTransaction(final CustomerBean customerBean) {
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        if (Utils.isNetworkAvailable(mContex)) {
            sqLiteDatabase.delete(CUSTOMER, NUMBER + " = '" + customerBean.number + "'", null);
            sqLiteDatabase.delete(TRANSACTION, NUMBER + " = '" + customerBean.number + "'", null);
            sqLiteDatabase.close();

            DatabaseReference backUpReference = GET_BACK_UP_REFERENCE.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child(Constants.CUSTOMER).child(customerBean.number);
            backUpReference.removeValue();

            DatabaseReference reference = GET_REFERENCE.child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child(Constants.CUSTOMER).child(customerBean.number);
            reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).child(Constants.TRANSACTION).child(customerBean.number).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        } else {
            sqLiteDatabase.close();
            customerBean.is_deleted = "1";
            updateCustomerData(customerBean);
        }
    }

    public void insertInfoData(SignupBean bean) {
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put(ADDRESS, bean.address);
        row.put(BUSINESS, bean.business);
        row.put(EMAIL, bean.email);
        row.put(IMEI, bean.deviceIMEI);
        row.put(FULL_NAME, bean.fullname);
        row.put(PASSWORD, bean.password);
        row.put(PHONE_NO, bean.phoneNo);
        row.put(PROFILE_IMAGE, bean.profile_image);
        row.put(IS_SYNC, Utils.isNetworkAvailable(mContex) ? "1" : "0");
        sqLiteDatabase.insert(USER_INFO, null, row);
        sqLiteDatabase.close();
    }

    public void updateInfoData(SignupBean bean) {
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put(ADDRESS, bean.address);
        row.put(BUSINESS, bean.business);
        row.put(EMAIL, bean.email);
        row.put(IMEI, bean.deviceIMEI);
        row.put(FULL_NAME, bean.fullname);
        row.put(PASSWORD, bean.password);
        row.put(PHONE_NO, bean.phoneNo);
        Log.d("-=-=-= :- ", " updateInfoData " + bean.profile_image);
        row.put(PROFILE_IMAGE, bean.profile_image);
        row.put(IS_SYNC, Utils.isNetworkAvailable(mContex) ? "1" : "0");
        sqLiteDatabase.update(USER_INFO, row, PHONE_NO + " = '" + bean.phoneNo + "'", null);
        sqLiteDatabase.close();
    }

    public SignupBean getDataBean() {
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        String selectQuery = "select * from " + USER_INFO;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        SignupBean signupBean = null;
        for (int k = 0; k < cursor.getCount(); k++) {
            signupBean = new SignupBean();
            signupBean.address = cursor.getString(1);
            signupBean.business = cursor.getString(2);
            signupBean.deviceIMEI = cursor.getString(3);
            signupBean.email = cursor.getString(4);
            signupBean.fullname = cursor.getString(5);
            signupBean.password = cursor.getString(6);
            signupBean.phoneNo = cursor.getString(7);
            signupBean.profile_image = cursor.getString(8);
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return signupBean;
    }

    public boolean isTransactionAvailable(CustomerBean customerBean, TransactionBean bean) {
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        Cursor cursor = null;
        String query = "SELECT number FROM " + TRANSACTION + " WHERE " + NUMBER + " = ? AND " + KEY + " = ?";
        cursor = sqLiteDatabase.rawQuery(query, new String[]{customerBean.number, bean.key});
        Log.d("Cursor Count : ", cursor.getCount() + "");
        cursor.close();
        sqLiteDatabase.close();
        Log.d("----- is transction :- ", (cursor.getCount() > 0) + " " + bean.key);
        return cursor.getCount() > 0 ? true : false;
    }

    public void insertTransactionData(CustomerBean customerBean, TransactionBean bean) {
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put(NUMBER, customerBean.number);
        row.put(AMOUNT, bean.Amount);
        row.put(TIME, bean.time);
        row.put(NOTE, bean.note);
        row.put(ACCEPT, bean.accept);
        row.put(TOTAL, bean.total);
        row.put(DATE, bean.date);
        row.put(TRANS_DATE, bean.trans_date);
        row.put(IMAGE, bean.image);
        row.put(TYPE, bean.type);
        row.put(DELETD_ON, bean.deleted_on);
        row.put(KEY, bean.key);
        row.put(IS_SYNC, Utils.isNetworkAvailable(mContex) ? "1" : "0");
        sqLiteDatabase.insert(TRANSACTION, null, row);
        sqLiteDatabase.close();
    }

    public void updateTransactionData(String number, TransactionBean bean) {
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        ContentValues row = new ContentValues();
        row.put(NUMBER, number);
        row.put(AMOUNT, bean.Amount);
        row.put(TIME, bean.time);
        row.put(NOTE, bean.note);
        row.put(ACCEPT, bean.accept);
        row.put(TOTAL, bean.total);
        row.put(DATE, bean.date);
        row.put(TRANS_DATE, bean.trans_date);
        row.put(IMAGE, bean.image);
        row.put(TYPE, bean.type);
        row.put(DELETD_ON, bean.deleted_on);
        row.put(KEY, bean.key);
        row.put(IS_SYNC, Utils.isNetworkAvailable(mContex) ? "1" : "0");
        sqLiteDatabase.update(TRANSACTION, row, KEY + " = ? AND " + NUMBER + " = ?", new String[]{bean.key, number});
        sqLiteDatabase.close();
    }

    public ArrayList<TransactionBean> getTransactionData(String number) {
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        String selectQuery = "select * from " + TRANSACTION + " Where " + NUMBER + " = '" + number + "'";
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        ArrayList<TransactionBean> customerBeans = new ArrayList<>();
        for (int k = 0; k < cursor.getCount(); k++) {
            TransactionBean bean = new TransactionBean();
            bean.Amount = cursor.getString(2);
            bean.time = cursor.getString(3);
            bean.note = cursor.getString(4);
            bean.accept = cursor.getString(5);
            bean.total = cursor.getString(6);
            bean.date = cursor.getString(7);
            bean.trans_date = cursor.getString(8);
            bean.image = cursor.getString(9);
            bean.type = cursor.getString(10);
            bean.deleted_on = cursor.getString(11);
            bean.key = cursor.getString(12);
            bean.is_sync = cursor.getString(13);
            customerBeans.add(bean);
            if (Utils.isNetworkAvailable(mContex) && FirebaseAuth.getInstance().getCurrentUser() != null) {
                if (cursor.getString(13).equals("0")) {
                    DatabaseReference BackUpReference = GET_BACK_UP_REFERENCE
                            .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                            .child(Constants.TRANSACTION).child(number);
                    BackUpReference.child(cursor.getString(12)).setValue(bean);

                    DatabaseReference reference = GET_REFERENCE
                            .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                            .child(Constants.TRANSACTION).child(number);
                    reference.child(cursor.getString(12)).setValue(bean);
                    updateTransactionData(number, bean);
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return customerBeans;
    }

    public void clearTable() {
        sqLiteDatabase = mDbHelper.getWritableDatabase();
        sqLiteDatabase.execSQL("delete from " + USER_INFO);
        sqLiteDatabase.execSQL("delete from " + CUSTOMER);
        sqLiteDatabase.execSQL("delete from " + TRANSACTION);
        sqLiteDatabase.close();
    }
}
