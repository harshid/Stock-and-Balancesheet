package fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mycredit.ProductListActivity;
import com.mycredit.R;

import java.util.ArrayList;

import adapter.ProductListDialogAdapter;
import bean.ProductBean;
import bean.StockBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import utils.Constants;
import utils.Utils;

import static utils.Constants.GET_BACK_UP_REFERENCE_STOCK;
import static utils.Constants.GET_REFERENCE_STOCK;
import static utils.Constants.STOCK;

public class StockFragment extends Fragment {
    @BindView(R.id.txtAddStock)
    TextView txtAddStock;
    @BindView(R.id.txtSellProduct)
    TextView txtSellProduct;
    @BindView(R.id.rlMain)
    RelativeLayout rlMain;

    @BindView(R.id.txtAddSellStockFirebase)
    TextView txtAddSellStockFirebase;
    // Add Product
    @BindView(R.id.edtProductName)
    EditText edtProductName;
    @BindView(R.id.rlAddProduct)
    RelativeLayout rlAddProduct;
    @BindView(R.id.edtProductPrice)
    TextView edtProductPrice;
    // Add Stock
    @BindView(R.id.edtStockPrice)
    EditText edtStockPrice;
    @BindView(R.id.edtStockQuantity)
    EditText edtStockQuantity;
    @BindView(R.id.rlAddStock)
    RelativeLayout rlAddStock;
    @BindView(R.id.txtSelectProduct)
    TextView txtSelectProduct;

    Unbinder unbinder;
    FirebaseAuth mAuth;
    String quantity, product;
    ProductBean selectedProductBean;
    ArrayList<ProductBean> productBeans = new ArrayList<>();
    Boolean isSell;
    String productName, productPrice;

    public StockFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static StockFragment newInstance(String param1, String param2) {
        StockFragment fragment = new StockFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stock, container, false);
        unbinder = ButterKnife.bind(this, view);

        mAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.txtProductList, R.id.rlAddProduct, R.id.rlAddStock, R.id.txtSellProduct, R.id.txtSelectProduct, R.id.imgCloseAddStock, R.id.txtAddStock, R.id.imgCloseAddProduct, R.id.txtAddProduct, R.id.txtAddProductFirebase, R.id.txtAddSellStockFirebase})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txtProductList:
                startActivity(new Intent(getActivity(), ProductListActivity.class));
                break;
            case R.id.rlAddProduct:
                break;
            case R.id.rlAddStock:
                break;
            case R.id.txtAddProduct:
                slideToTop(rlAddProduct);
                break;
            case R.id.imgCloseAddProduct:
                slideToBottom(rlAddProduct);
                break;
            case R.id.txtSellProduct:
                txtAddSellStockFirebase.setText(getResources().getString(R.string.sell_stock));
                isSell = true;
                edtStockPrice.setVisibility(View.VISIBLE);
                slideToTop(rlAddStock);
                break;
            case R.id.txtAddStock:
                txtAddSellStockFirebase.setText(getResources().getString(R.string.add_stock));
                isSell = false;
                edtStockPrice.setVisibility(View.GONE);
                slideToTop(rlAddStock);
                break;
            case R.id.imgCloseAddStock:
                slideToBottom(rlAddStock);
                break;
            case R.id.txtAddProductFirebase:
                if (productValidate()) {
                    DatabaseReference myRef = GET_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                            .child(Constants.PRODUCT);
                    DatabaseReference myRefBackup = GET_BACK_UP_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                            .child(Constants.PRODUCT);

                    String key = myRef.push().getKey();
                    ProductBean productBean = new ProductBean();
                    productBean.id = key;
                    productBean.name = productName;
                    productBean.stock = 0 + "";
                    productBean.price = productPrice;

                    myRef.child(key).setValue(productBean);
                    myRefBackup.child(key).setValue(productBean);

                    rlAddProduct.setVisibility(View.GONE);
                } else
                    edtProductName.setError(getResources().getString(R.string.add_product));
                break;
            case R.id.txtAddSellStockFirebase:
                if (stockValidate()) {
                    // productName = edtProductName.getText().toString();
                    // if (productName != null && productName.length() > 0) {
                    DatabaseReference myRef = GET_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                            .child(STOCK).child(selectedProductBean.id);
                    DatabaseReference myRefBackup = GET_BACK_UP_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                            .child(STOCK).child(selectedProductBean.id);

                    String key = myRef.push().getKey();
                    StockBean stockBean = new StockBean();
                    stockBean.id = key;
                    stockBean.productName = selectedProductBean.name;
                    stockBean.productId = selectedProductBean.id;
                    stockBean.price = productPrice;
                    stockBean.quantity = isSell ? "-" + quantity : quantity;
                    stockBean.total = (Integer.parseInt(quantity) * Integer.parseInt(productPrice)) + "";

                    myRef.child(key).setValue(stockBean);
                    myRefBackup.child(key).setValue(stockBean);

                    DatabaseReference ref = GET_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                            .child(Constants.PRODUCT).child(stockBean.productId);
                    DatabaseReference refBackup = GET_BACK_UP_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                            .child(Constants.PRODUCT).child(stockBean.productId);

                    selectedProductBean.stock = (Integer.parseInt(selectedProductBean.stock) + Integer.parseInt(stockBean.quantity)) + "";

                    ref.setValue(selectedProductBean);
                    refBackup.setValue(selectedProductBean);

                    selectedProductBean = null;
                    txtSelectProduct.setText("");
                    edtStockPrice.setText("");
                    edtStockQuantity.setText("");
                    rlAddStock.setVisibility(View.GONE);
                } else
                    edtProductName.setError(getResources().getString(R.string.add_product));
                break;
            case R.id.txtSelectProduct:
                getProductList();
                break;
        }
    }

    public void slideToBottom(final View view) {
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(400);
        transition.addTarget(view);
        TransitionManager.beginDelayedTransition(rlMain, transition);
        view.setVisibility(View.GONE);
    }

    public void slideToTop(final View view) {
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(400);
        transition.addTarget(view);
        TransitionManager.beginDelayedTransition(rlMain, transition);
        view.setVisibility(View.VISIBLE);
    }

    private void getProductList() {
        Utils.showProgress(getActivity());
        DatabaseReference myRef = GET_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                .child(Constants.PRODUCT);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productBeans.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ProductBean productBean = data.getValue(ProductBean.class);
                    productBeans.add(productBean);
                }
                Utils.closeProgress();
                selectProcudtDialog(productBeans);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utils.closeProgress();
            }
        });
    }

    public void selectProcudtDialog(final ArrayList<ProductBean> categoryBeans) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_product);

        ListView list = dialog.findViewById(R.id.list);
        TextView txtCancel = dialog.findViewById(R.id.txtCancel);
        TextView txtTitle = dialog.findViewById(R.id.txtTitle);

        txtTitle.setText(getResources().getString(R.string.select_product));
        list.setAdapter(new ProductListDialogAdapter(getActivity(), categoryBeans));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedProductBean = productBeans.get(position);
                txtSelectProduct.setText(productBeans.get(position).name);
                dialog.dismiss();
            }
        });
        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private boolean productValidate() {
        productName = edtProductName.getText().toString();
        productPrice = edtProductPrice.getText().toString();
        if (productName == null || !(productName.length() > 0)) {
            edtProductName.setError(getResources().getString(R.string.enter_product));
            return false;
        } else if (productPrice == null || !(productPrice.length() > 0)) {
            edtProductPrice.setError(getResources().getString(R.string.enter_price));
            return false;
        } else if (!(Integer.parseInt(productPrice) > 0)) {
            edtProductPrice.setError(getResources().getString(R.string.enter_valid_price));
            return false;
        }
        return true;
    }

    private boolean stockValidate() {
        if (isSell)
            productPrice = edtStockPrice.getText().toString();
        else
            productPrice = selectedProductBean.price;
        quantity = edtStockQuantity.getText().toString();
        product = txtSelectProduct.getText().toString();
        if (product == null || !(product.length() > 0)) {
            txtSelectProduct.setError(getResources().getString(R.string.select_product));
            return false;
        } else if (quantity == null || !(quantity.length() > 0)) {
            edtStockQuantity.setError(getResources().getString(R.string.enter_quantity));
            return false;
        } else if (!(Integer.parseInt(quantity) > 0)) {
            edtStockQuantity.setError(getResources().getString(R.string.enter_valid_quantity));
            return false;
        } else if (productPrice == null || !(productPrice.length() > 0)) {
            edtStockPrice.setError(getResources().getString(R.string.enter_price));
            return false;
        } else if (!(Integer.parseInt(productPrice) > 0)) {
            edtStockPrice.setError(getResources().getString(R.string.enter_valid_price));
            return false;
        } else if (isSell) {
            if (!(Integer.parseInt(selectedProductBean.stock) >= Integer.parseInt(quantity))) {
                edtStockQuantity.setError(getResources().getString(R.string.available_stock) + selectedProductBean.stock);
                return false;
            } else if (!(Integer.parseInt(selectedProductBean.price) <= Integer.parseInt(productPrice))) {
                edtStockPrice.setError(getResources().getString(R.string.purchase_price) + selectedProductBean.price);
                return false;
            }
        }
        return true;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
