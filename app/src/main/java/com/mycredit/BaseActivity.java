package com.mycredit;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.appizona.yehiahd.fastsave.FastSave;
import java.util.Locale;
import utils.ContextWrapper;
import static utils.Constants.LANGUAGE;

class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        String lan = FastSave.getInstance().getString(LANGUAGE, "en");
        Locale locale = new Locale(lan);
        Context context = ContextWrapper.wrap(newBase, locale);
        super.attachBaseContext(context);
    }
}
