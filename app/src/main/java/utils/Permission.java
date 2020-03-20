package utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permission {
    public static boolean checkCallPermission(final Activity act) {
        int result;
        boolean isGranted = false;
        for (String p : new String[]{Manifest.permission.CALL_PHONE}) {
            result = ContextCompat.checkSelfPermission(act, p);
            if (result == PackageManager.PERMISSION_GRANTED) {
                isGranted = true;
            } else {
                isGranted = false;
                requestCallPermission(act);
                break;
            }
        }
        return isGranted;
    }

    public static boolean checkContactPermission(final Activity act) {
        int result;
        boolean isGranted = false;
        for (String p : new String[]{Manifest.permission.READ_CONTACTS}) {
            result = ContextCompat.checkSelfPermission(act, p);
            if (result == PackageManager.PERMISSION_GRANTED) {
                isGranted = true;
            } else {
                isGranted = false;
                requestContactPermission(act);
                break;
            }
        }
        return isGranted;
    }

    public static boolean checkStoragePermission(final Activity act) {
        int result;
        boolean isGranted = false;
        for (String p : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}) {
            result = ContextCompat.checkSelfPermission(act, p);
            if (result == PackageManager.PERMISSION_GRANTED) {
                isGranted = true;
            } else {
                isGranted = false;
                requestStoragePermission(act);
                break;
            }
        }
        return isGranted;
    }

    public static void requestCallPermission(Activity act) {
        List<String> listPermissionsNeeded;
        int result;
        listPermissionsNeeded = new ArrayList<>();
        for (String p : new String[]{Manifest.permission.CALL_PHONE}) {
            result = ContextCompat.checkSelfPermission(act, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(act, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 144);
        }
    }

    public static void requestStoragePermission(Activity act) {
        List<String> listPermissionsNeeded;
        int result;
        listPermissionsNeeded = new ArrayList<>();
        for (String p : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}) {
            result = ContextCompat.checkSelfPermission(act, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(act, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 144);
        }
    }

    public static void requestContactPermission(Activity act) {
        List<String> listPermissionsNeeded;
        int result;
        listPermissionsNeeded = new ArrayList<>();
        for (String p : new String[]{Manifest.permission.READ_CONTACTS}) {
            result = ContextCompat.checkSelfPermission(act, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(act, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 144);
        }
    }

    public static boolean checkInitialPermission(final Activity act) {
        int result;
        boolean isGranted = false;
        for (String p : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE}) {
            result = ContextCompat.checkSelfPermission(act, p);
            if (result == PackageManager.PERMISSION_GRANTED) {
                isGranted = true;
            } else {
                isGranted = false;
                requestInitialPermission(act);
                break;
            }
        }
        return isGranted;
    }

    public static void requestInitialPermission(Activity act) {
        List<String> listPermissionsNeeded;
        int result;
        listPermissionsNeeded = new ArrayList<>();
        for (String p : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE}) {
            result = ContextCompat.checkSelfPermission(act, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(act, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 144);
        }
    }
    /*public static void ShowPermissionDialog(final Activity act) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act)
                .setMessage(act.getString(R.string.app_name) + act.getString(R.string.permission_message))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", act.getPackageName(), null);
                        intent.setData(uri);
                        act.startActivity(intent);
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static boolean isPermission(Activity act) {
        List<String> listPermissionsNeeded;
        int result;
        listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(act, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }*/
}
