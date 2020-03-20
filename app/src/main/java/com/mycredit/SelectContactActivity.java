package com.mycredit;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import adapter.ContactListAdapter;
import bean.ContactBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static utils.Constants.CONTACT;
import static utils.Constants.GET_PHONE_NUMBER;

/**
 * Created by mac on 02/07/19.
 */

public class SelectContactActivity extends BaseActivity {
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.list)
    ListView list;
    @BindView(R.id.edtSearch)
    EditText edtSearch;

    ArrayList<ContactBean> contactBeans, filterContactBeans;
    ContactListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);
        ButterKnife.bind(this);

        txtTitle.setText(getResources().getString(R.string.select_contact));
        txtTitle.setGravity(Gravity.CENTER);

        new getAllContactDataFromContactList().execute();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContactBeans.clear();
                for (int i = 0; i < contactBeans.size(); i++) {
                    if (contactBeans.get(i).name.toUpperCase().contains(s.toString().toUpperCase())) {
                        filterContactBeans.add(contactBeans.get(i));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra(CONTACT, filterContactBeans.get(position));
                setResult(GET_PHONE_NUMBER, intent);
                finish();
            }
        });
    }

    @OnClick(R.id.imgBack)
    public void onViewClicked() {
        finish();
    }

    private class getAllContactDataFromContactList extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;

        @Override
        protected Void doInBackground(Void... voids) {
            contactBeans = new ArrayList<>();
            filterContactBeans = new ArrayList<>();
            ContentResolver cr = getContentResolver(); // Activity/Application
            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String name = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            ContactBean contactBean = new ContactBean();
                            contactBean.name = name;
                            contactBean.number = contactNumber;
                            contactBeans.add(contactBean);
                            filterContactBeans.add(contactBean);
                            break;
                        }
                        pCur.close();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SelectContactActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
            dialog.setMessage(getResources().getString(R.string.loading));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            adapter = new ContactListAdapter(SelectContactActivity.this, filterContactBeans);
            list.setAdapter(adapter);
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}
