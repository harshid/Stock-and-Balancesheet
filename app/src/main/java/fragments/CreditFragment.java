package fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mycredit.PaymentActivity;
import com.mycredit.R;
import com.mycredit.SelectContactActivity;
import com.mycredit.TransactionActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import adapter.CustomerListAdapter;
import bean.ContactBean;
import bean.CustomerBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import database.SqlLiteDbHelper;
import utils.Constants;
import utils.Permission;
import utils.Utils;

import static android.app.Activity.RESULT_CANCELED;
import static utils.Constants.CONTACT;
import static utils.Constants.GET_BACK_UP_REFERENCE;
import static utils.Constants.GET_PHONE_NUMBER;
import static utils.Constants.GET_REFERENCE;
import static utils.Constants.IS_REFRESH;

public class CreditFragment extends Fragment {
    @BindView(R.id.list)
    ListView list;
    @BindView(R.id.pullToRefresh)
    SwipeRefreshLayout pullToRefresh;
    @BindView(R.id.edtSearch)
    EditText edtSearch;

    Unbinder unbinder;

    FirebaseAuth mAuth;
    CustomerListAdapter adapter;
    ArrayList<CustomerBean> customerBeans, filteredCustomerBeans;
    SqlLiteDbHelper sqlLiteDbHelper;

    public CreditFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static CreditFragment newInstance(String param1, String param2) {
        CreditFragment fragment = new CreditFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_credit, container, false);
        unbinder = ButterKnife.bind(this, view);

        customerBeans = new ArrayList<>();
        filteredCustomerBeans = new ArrayList<>();
        adapter = new CustomerListAdapter(getActivity(), filteredCustomerBeans);
        list.setAdapter(adapter);

        sqlLiteDbHelper = new SqlLiteDbHelper(getActivity());
        mAuth = FirebaseAuth.getInstance();
        /* if (mAuth.getCurrentUser() != null && Utils.isNetworkAvailable(getActivity()))
            getCreditData();
        else */
        getOfflineData();

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                edtSearch.setText("");
                pullToRefresh.setRefreshing(true);
                /* if(mAuth.getCurrentUser() != null && Utils.isNetworkAvailable(getActivity()))
                    getCreditData();
                else */
                getOfflineData();
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filteredCustomerBeans.clear();
                for (int i = 0; i < customerBeans.size(); i++) {
                    if (customerBeans.get(i).name.toUpperCase().contains(s.toString().toUpperCase())) {
                        filteredCustomerBeans.add(customerBeans.get(i));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return view;
    }

    private void getOfflineData() {
        filteredCustomerBeans.clear();
        customerBeans.clear();
        ArrayList<CustomerBean> allCustomers = sqlLiteDbHelper.getCustomerData();

        for (int i = 0; i < allCustomers.size(); i++) {
            if (allCustomers.get(i).is_deleted.equals("0")) {
                customerBeans.add(allCustomers.get(i));
                filteredCustomerBeans.add(allCustomers.get(i));
            } else {
                if (Utils.isNetworkAvailable(getActivity())) {
                    sqlLiteDbHelper.deleteCustomerAndTransaction(allCustomers.get(i));
                }
            }
        }
        notifyData();
    }

   /* private void getCreditData() {
        Utils.showProgress(getActivity());
        myRef = database.getReference().child(mAuth.getCurrentUser().getPhoneNumber()).child(Constants.CUSTOMER);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                customerBeans.clear();
                filteredCustomerBeans.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    CustomerBean customerBean = data.getValue(CustomerBean.class);
                    customerBeans.add(customerBean);
                    filteredCustomerBeans.add(customerBean);
                }
                notifyData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                pullToRefresh.setRefreshing(false);
                Utils.closeProgress();
            }
        });
    } */

    private void notifyData() {
        adapter = new CustomerListAdapter(getActivity(), filteredCustomerBeans);
        list.setAdapter(adapter);
        if (pullToRefresh != null)
            pullToRefresh.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == GET_PHONE_NUMBER) {
            if (data != null) {
                ContactBean contactBean = (ContactBean) data.getSerializableExtra(CONTACT);
                if (contactBean != null && contactBean.number != null && contactBean.name != null) {
                    Utils.setEngilsh(getActivity());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                    String strDate = sdf.format(Calendar.getInstance().getTime());
                    Utils.setSavedLanguage(getActivity());

                    CustomerBean newCustomer = null;
                    boolean isAlreadyAdded = false;
                    for (int i = 0; i < customerBeans.size(); i++) {
                        if (customerBeans.get(i).number.equalsIgnoreCase(contactBean.number.replace(" ", ""))) {
                            newCustomer = customerBeans.get(i);
                            isAlreadyAdded = true;
                            break;
                        }
                    }
                    if (!isAlreadyAdded) {
                        newCustomer = new CustomerBean();
                        newCustomer.name = contactBean.name;
                        newCustomer.number = contactBean.number
                                .replace(" ", "").replace("-", "");
                        newCustomer.amount = "0";
                        newCustomer.profile = "";
                        newCustomer.type = "";
                        newCustomer.is_deleted = "";
                        newCustomer.language = "en";
                        newCustomer.address = "";
                        newCustomer.add_time = strDate;

                        sqlLiteDbHelper.insertCustomerData(newCustomer);
                        /* if (Utils.isNetworkAvailable(getActivity()))
                          getCreditData();
                        else */
                        getOfflineData();

                        DatabaseReference myRef = GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                                .child(Constants.CUSTOMER).child(newCustomer.number);
                        myRef.setValue(newCustomer);

                        DatabaseReference backUpMyRef = GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                                .child(Constants.CUSTOMER).child(newCustomer.number);
                        backUpMyRef.setValue(newCustomer);

                    }
                    startActivityForResult(new Intent(getActivity(), TransactionActivity.class).putExtra(Constants.CUSTOMER, newCustomer), Constants.TRANSACTION_REQUEST);
                }
            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("User closed the picker without selecting items.");
            }
        } else if (requestCode == Constants.TRANSACTION_REQUEST) {
            if (data != null && data.getStringExtra(IS_REFRESH).equals("1")) {
                getOfflineData();
            }
        }
    }

    @OnClick({R.id.imgFilter, R.id.imgRefresh, R.id.cardAddCustomer})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgFilter:
                break;
            case R.id.imgRefresh:
                /* if(mAuth.getCurrentUser() != null && Utils.isNetworkAvailable(getActivity()))
                    getCreditData();
                else */
                getOfflineData();
                break;
            case R.id.cardAddCustomer:
                if (Permission.checkContactPermission(getActivity())) {
                    startActivityForResult(new Intent(getActivity(), SelectContactActivity.class), GET_PHONE_NUMBER);
                }
                break;
        }
    }
}
