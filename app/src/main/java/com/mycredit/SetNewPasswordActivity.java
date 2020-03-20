package com.mycredit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utils.Utils;

import static utils.Constants.GET_BACK_UP_REFERENCE;
import static utils.Constants.GET_REFERENCE;
import static utils.Constants.PASSWORD;

public class SetNewPasswordActivity extends BaseActivity {
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.edtPassword)
    EditText edtPassword;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_password);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setText(getResources().getString(R.string.set_new_password));
    }

    @OnClick({R.id.imgBack, R.id.txtSetNewPassword})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgBack:
                finish();
                break;
            case R.id.txtSetNewPassword:
                if (Utils.isNetworkAvailable(SetNewPasswordActivity.this)) {
                    String setPassword = edtPassword.getText().toString();
                    if (setPassword.length() > 5)
                        setPassword(setPassword);
                    else
                        edtPassword.setError(getResources().getString(R.string.enter_min_six_digit));
                }
                break;
        }
    }

    private void setPassword(String pass) {
        DatabaseReference backUpReference = GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(PASSWORD);
        backUpReference.setValue(pass);

        DatabaseReference reference = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber()).child(PASSWORD);
        reference.setValue(pass);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String password = dataSnapshot.getValue(String.class);
                Utils.showToast(getApplicationContext(), getResources().getString(R.string.password_change_succes));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
