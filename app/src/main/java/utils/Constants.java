package utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by mac on 17/06/19.
 */

public class Constants {
    public static String LANGUAGE = "language";
    public static String LANGUAGE_DISABLE = "_disable";
    public static String ENGLISH_CODE = "en";
    public static String HINDI_CODE = "hi";
    public static String HINGLISH_CODE = "fr";

    public static String BACK_UP = "back_up";
    public static String BALANCE_SHEET = "Balance_Sheet";
    public static String STOCK_MANAGEMENT = "Stock_Management";
    public static String REGULAR = "Regular";
    public static String PRODUCT = "Product";
    public static String STOCK = "Stock";

    public static String ADDRESS = "address";
    public static String FULL_NAME = "fullname";
    public static String USER = "user";
    public static String BUSINESS = "business";
    public static String EMAIL = "email";
    public static String PHONE_NUMBER = "phoneNo";
    public static String NUMBER = "number";
    public static String DEVICE_IMEI = "deviceIMEI";
    public static String PASSWORD = "password";
    public static String PROFILE_IMAGE = "profile_image";

    public static String IMAGE = "image";
    public static String CONTACT = "contact";
    public static String TRANSACTION = "Transaction";
    public static String CREDIT = "Credit";
    public static String CUSTOMER = "Customer";
    public static String AMOUNT = "Amount";
    public static String ACCEPT = "accept";
    public static String NOTE = "note";
    public static String TIME = "time";
    public static String DETAIL = "detail";
    public static String IMAGE_FILE = "imageFile";
    public static String KEY = "key";
    public static String IS_REFRESH = "isRefresh";
    public static String ADVANCE = "Advance";
    public static String DUE = "Due";

    public static int MAKE_PAYMENT = 467;
    public static int PROFILE = 124;
    public static int DELETE_TRANSACTION_REQUEST = 852;
    public static int DELETE_CUSTOMER_REQUEST = 369;
    public static int TRANSACTION_REQUEST = 741;
    public static int PRODUCT_DETAIL_REQUEST = 235;
    public static int GET_PHONE_NUMBER = 175;

    public static String FIREBASE_STORAGE_BASEURL = "gs://test-galaxy-browser.appspot.com/";
    public static DatabaseReference GET_REFERENCE = FirebaseDatabase.getInstance().getReference().child(Constants.REGULAR).child(Constants.BALANCE_SHEET);
    public static DatabaseReference GET_BACK_UP_REFERENCE = FirebaseDatabase.getInstance().getReference().child(Constants.BACK_UP).child(Constants.BALANCE_SHEET);

    public static DatabaseReference GET_REFERENCE_STOCK = FirebaseDatabase.getInstance().getReference().child(Constants.REGULAR).child(Constants.STOCK_MANAGEMENT);
    public static DatabaseReference GET_BACK_UP_REFERENCE_STOCK = FirebaseDatabase.getInstance().getReference().child(Constants.BACK_UP).child(Constants.STOCK_MANAGEMENT);

    public static FirebaseStorage storage = FirebaseStorage.getInstance();
    public static StorageReference STORAGE_REFERENCE = storage.getReferenceFromUrl(FIREBASE_STORAGE_BASEURL);

    // Twilio
    public static String TWILIO_NUMBER = "+13343104023";
    public static String TWILIO_ACCOUNT_SID = "ACb261dd361a6d6ad7bf0d82bf19ce3582";
    public static String TWILIO_AUTH_TOKEN = "d098875fc9f0a3742c2620796a8f808d";
    public static String TWILIO_URL = "https://api.twilio.com/2010-04-01/Accounts/" + TWILIO_ACCOUNT_SID + "/SMS/Messages";
}
