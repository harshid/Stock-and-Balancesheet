package com.mycredit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appizona.yehiahd.fastsave.FastSave;

import bean.SignupBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utils.Constants;

import static utils.Constants.ENGLISH_CODE;
import static utils.Constants.HINDI_CODE;
import static utils.Constants.HINGLISH_CODE;
import static utils.Constants.LANGUAGE;
import static utils.Constants.USER;

public class LanguageActivity extends BaseActivity {
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.imgEnglish)
    ImageView imgEnglish;
    @BindView(R.id.imgHindi)
    ImageView imgHindi;
    @BindView(R.id.imgHinglish)
    ImageView imgHinglish;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        ButterKnife.bind(this);

        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setText(getResources().getString(R.string.language));
        txtTitle.setTextColor(getResources().getColor(R.color.green));

        setSelectedLan();
    }

    private void setSelectedLan() {
        String lan = FastSave.getInstance().getString(LANGUAGE, "en");
        if (lan.equalsIgnoreCase(Constants.HINDI_CODE)) {
            imgHindi.setVisibility(View.VISIBLE);
        } else if (lan.equalsIgnoreCase(Constants.HINGLISH_CODE)) {
            imgHinglish.setVisibility(View.VISIBLE);
        } else if (lan.equalsIgnoreCase(Constants.ENGLISH_CODE)) {
            imgEnglish.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.imgBack, R.id.rlEnglish, R.id.rlHindi, R.id.rlHinglish})
    public void onViewClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("bean", FastSave.getInstance().getObject(USER, SignupBean.class));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        switch (view.getId()) {
            case R.id.imgBack:
                finish();
                break;
            case R.id.rlEnglish:
                FastSave.getInstance().saveString(LANGUAGE, ENGLISH_CODE);
                startActivity(intent);
                finish();
                break;
            case R.id.rlHindi:
                FastSave.getInstance().saveString(LANGUAGE, HINDI_CODE);
                startActivity(intent);
                finish();
                break;
            case R.id.rlHinglish:
                FastSave.getInstance().saveString(LANGUAGE, HINGLISH_CODE);
                startActivity(intent);
                finish();
                break;
        }
    }
}
