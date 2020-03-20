package utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.appizona.yehiahd.fastsave.FastSave;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.mycredit.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import static utils.Constants.LANGUAGE;
import static utils.Constants.TWILIO_ACCOUNT_SID;
import static utils.Constants.TWILIO_AUTH_TOKEN;

public class Utils {
    public static String Language = "en";

    public static void showToast(Context mContext, String strMessage) {
        Toast.makeText(mContext, strMessage, Toast.LENGTH_LONG).show();
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
            return true;
        else {
            // showToast(activity, activity.getString(R.string.check_network));
            return false;
        }
    }

    public static File copyTransactionFile(Activity activity, File sourceFile, String name) throws IOException {
        //  String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "." + activity.getResources().getString(R.string.app_name).replace(" ", "")
                + File.separator + Constants.TRANSACTION
                + File.separator + name + ".png";
        File destFile = new File(destinationPath);

        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        } else {
            destFile.delete();
        }
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
        return destFile;
    }

    public static File copyFile(Activity activity, File sourceFile, String name) throws IOException {
        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "." + activity.getResources().getString(R.string.app_name).replace(" ", "")
                + File.separator + name + ".png";
        File destFile = new File(destinationPath);

        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        } else {
            destFile.delete();
        }
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
        return destFile;
    }

    public static void showErrorDialog(Context mContext, String strMessage) {
        Toast.makeText(mContext, strMessage, Toast.LENGTH_LONG).show();

        new MaterialDialog.Builder(mContext).
                theme(Theme.LIGHT).
                title(mContext.getResources().getString(R.string.app_name)).
                content(strMessage).
                positiveText("OK").
                onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();

    }

    public static boolean isValidEmailId(String email) {
        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    public static boolean isValidMobile(String phone) {
        boolean check = false;
        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            if (phone.length() < 6 || phone.length() > 13) {
                // if(phone.length() != 10) {
                check = false;
            } else {
                check = true;
            }
        } else {
            check = false;
        }
        return check;
    }

    public static ProgressDialog progress;

    public static void showProgress(Context mContext) {
        if (progress != null)
            progress = null;
        progress = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
        progress.setMessage(mContext.getResources().getString(R.string.loading));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
    }

    public static void showProgress(Context mContext, String message) {
        if (progress != null)
            progress = null;
        progress = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
        progress.setMessage(message);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
    }

    public static void closeProgress() {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
            progress = null;
        }
    }

    public static int getDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;
        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;
        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;
        long elapsedSeconds = different / secondsInMilli;

        return (int) elapsedDays;
    }

    public static void hideKeyBoard(View view, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    static public boolean isValidURL(String urlStr) {
        try {
            URI uri = new URI(urlStr);
            return uri.getScheme().equals("http") || uri.getScheme().equals("https");
        } catch (Exception e) {
            return false;
        }
    }

    public static int generateRandomColor() {
        Random mRandom = new Random(System.currentTimeMillis());
        // This is the base color which will be mixed with the generated one
        final int baseColor = Color.WHITE;

        final int baseRed = Color.red(baseColor);
        final int baseGreen = Color.green(baseColor);
        final int baseBlue = Color.blue(baseColor);

        final int red = (baseRed + mRandom.nextInt(256)) / 2;
        final int green = (baseGreen + mRandom.nextInt(256)) / 2;
        final int blue = (baseBlue + mRandom.nextInt(256)) / 2;

        return Color.rgb(red, green, blue);
    }

    public static String getDownloadPath(Activity activity, String name) {
        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "." + activity.getResources().getString(R.string.app_name).replace(" ", "")
                + File.separator + name + ".png";
        return destinationPath;
    }

    public static String getTransactionDownloadPath(Activity activity, String name) {
        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "." + activity.getResources().getString(R.string.app_name).replace(" ", "")
                + File.separator + Constants.TRANSACTION
                + File.separator + name + ".png";
        return destinationPath;
    }

    public static void setEngilsh(Context context) {
        Locale locale = new Locale("en");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, null);
    }

    public static void setLanguage(Context context, String code) {
        Locale locale = new Locale(code);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, null);
    }

    public static void setSavedLanguage(Context context) {
        String lan = FastSave.getInstance().getString(LANGUAGE, "en");
        Locale locale = new Locale(lan);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, null);
    }

    public static void sendMessage(String number, String msg) {
        /*if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        }
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(Constants.TWILIO_URL);
        String base64EncodedCredentials = "Basic " + Base64.encodeToString((TWILIO_ACCOUNT_SID + ":" + TWILIO_AUTH_TOKEN).getBytes(), Base64.NO_WRAP);
        httppost.setHeader("Authorization", base64EncodedCredentials);
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("From", Constants.TWILIO_NUMBER));
            nameValuePairs.add(new BasicNameValuePair("To", number));
            nameValuePairs.add(new BasicNameValuePair("Body", msg));

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }*/
    }

    // Fonts for pdf generate
    public static Font boldUnderList, fontNormal18, fontNormal15, fontBold15, fontBold20, fontBold25, bfBoldWhite, bfWhite, redFont35, redFont25, fontNormalGray15, greenFont35, greenFont25;
    public static Font.FontFamily pdfFont = Font.FontFamily.TIMES_ROMAN;

    public static void setInitPdfFonts() {
        boldUnderList = new Font(pdfFont, 16, Font.BOLD | Font.UNDERLINE);
        fontNormal18 = new Font(pdfFont, 18, Font.NORMAL);
        fontNormal15 = new Font(pdfFont, 15, Font.NORMAL);
        fontNormalGray15 = new Font(pdfFont, 15, Font.NORMAL, new BaseColor(172, 172, 172));
        fontBold15 = new Font(pdfFont, 15, Font.BOLD);
        fontBold20 = new Font(pdfFont, 20, Font.BOLD, new BaseColor(0, 0, 0));
        fontBold25 = new Font(pdfFont, 25, Font.BOLD, new BaseColor(0, 0, 0));
        bfBoldWhite = new Font(pdfFont, 21, Font.BOLD, new BaseColor(255, 255, 255));
        bfWhite = new Font(pdfFont, 18, Font.NORMAL, new BaseColor(255, 255, 255));
        redFont35 = new Font(pdfFont, 35, Font.BOLD, new BaseColor(139, 39, 27));
        greenFont35 = new Font(pdfFont, 35, Font.BOLD, new BaseColor(52, 131, 8));
        redFont25 = new Font(pdfFont, 25, Font.BOLD, new BaseColor(139, 39, 27));
        greenFont25 = new Font(pdfFont, 25, Font.BOLD, new BaseColor(52, 131, 8));
    }
}
