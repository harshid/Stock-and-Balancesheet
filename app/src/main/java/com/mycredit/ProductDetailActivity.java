package com.mycredit;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import adapter.StockListAdapter;
import bean.ProductBean;
import bean.SignupBean;
import bean.StockBean;
import bean.TransactionBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utils.Constants;
import utils.Utils;

import static utils.Constants.GET_BACK_UP_REFERENCE_STOCK;
import static utils.Constants.GET_REFERENCE_STOCK;
import static utils.Constants.IS_REFRESH;
import static utils.Constants.PRODUCT;
import static utils.Constants.PRODUCT_DETAIL_REQUEST;
import static utils.Constants.STOCK;

public class ProductDetailActivity extends AppCompatActivity {
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.list)
    ListView list;
    @BindView(R.id.txtTotalStock)
    TextView txtTotalStock;
    @BindView(R.id.txtTotalProfit)
    TextView txtTotalProfit;
    @BindView(R.id.rlMain)
    RelativeLayout rlMain;
    @BindView(R.id.txtTotalStockValue)
    TextView txtTotalStockValue;
    @BindView(R.id.txtTotalPrice)
    TextView txtTotalPrice;
    @BindView(R.id.llSend)
    LinearLayout llSend;

    ArrayList<StockBean> stockBeans;
    FirebaseAuth mAuth;
    Integer totalProfit = 0, totalPrice = 0;
    ProductBean productBean;

    //-------  Add Sell Stock -------
    // Add Stock
    @BindView(R.id.edtStockPrice)
    EditText edtStockPrice;
    @BindView(R.id.edtStockQuantity)
    EditText edtStockQuantity;
    @BindView(R.id.rlAddStock)
    RelativeLayout rlAddStock;
    @BindView(R.id.txtSelectProduct)
    TextView txtSelectProduct;

    @BindView(R.id.txtAddSellStockFirebase)
    TextView txtAddSellStockFirebase;
    Boolean isSell, isRefresh = false;
    String productPrice, quantity, product;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        productBean = (ProductBean) getIntent().getSerializableExtra(PRODUCT);

        llSend.setVisibility(View.VISIBLE);

        txtTitle.setText(productBean.name);
        txtTotalStock.setText(productBean.stock);
        txtTotalStockValue.setText((Integer.parseInt(productBean.stock) * Integer.parseInt(productBean.price)) + "");
        getStockList();
    }

    @OnClick({R.id.llSend, R.id.sellStockRelative, R.id.addStockRelative, R.id.imgBack, R.id.imgCloseAddStock, R.id.txtAddSellStockFirebase})
    void Clcik(View view) {
        switch (view.getId()) {
            case R.id.llSend:
                Utils.showProgress(ProductDetailActivity.this);
                generatePdf();
                break;
            case R.id.imgBack:
                onBackPressed();
                break;
            case R.id.sellStockRelative:
                txtAddSellStockFirebase.setText(getResources().getString(R.string.sell_stock));
                isSell = true;
                edtStockPrice.setVisibility(View.VISIBLE);
                txtSelectProduct.setText(productBean.name);
                slideToTop(rlAddStock);
                break;
            case R.id.addStockRelative:
                txtAddSellStockFirebase.setText(getResources().getString(R.string.add_stock));
                isSell = false;
                edtStockPrice.setVisibility(View.GONE);
                txtSelectProduct.setText(productBean.name);
                slideToTop(rlAddStock);
                break;
            case R.id.imgCloseAddStock:
                slideToBottom(rlAddStock);
                break;
            case R.id.txtAddSellStockFirebase:
                if (stockValidate()) {
                    DatabaseReference myRef = GET_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                            .child(STOCK).child(productBean.id);
                    DatabaseReference myRefBackup = GET_BACK_UP_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                            .child(STOCK).child(productBean.id);

                    String key = myRef.push().getKey();
                    StockBean stockBean = new StockBean();
                    stockBean.id = key;
                    stockBean.productName = productBean.name;
                    stockBean.productId = productBean.id;
                    stockBean.price = productPrice;
                    stockBean.quantity = isSell ? "-" + quantity : quantity;
                    stockBean.total = (Integer.parseInt(quantity) * Integer.parseInt(productPrice)) + "";

                    myRef.child(key).setValue(stockBean);
                    myRefBackup.child(key).setValue(stockBean);

                    DatabaseReference ref = GET_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                            .child(Constants.PRODUCT).child(stockBean.productId);
                    DatabaseReference refBackup = GET_BACK_UP_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                            .child(Constants.PRODUCT).child(stockBean.productId);

                    productBean.stock = (Integer.parseInt(productBean.stock) + Integer.parseInt(stockBean.quantity)) + "";

                    ref.setValue(productBean);
                    refBackup.setValue(productBean);

                    txtTotalStock.setText(productBean.stock);
                    txtTotalStockValue.setText((Integer.parseInt(productBean.stock) * Integer.parseInt(productBean.price)) + "");
                    txtSelectProduct.setText("");
                    edtStockPrice.setText("");
                    edtStockQuantity.setText("");
                    rlAddStock.setVisibility(View.GONE);
                    isRefresh = true;
                    getStockList();
                }
                break;
        }
    }

    private void addSpaceInTopView(PdfPTable pdfPTable) {
        pdfPTable.addCell(new Paragraph("\n"));
        pdfPTable.addCell(new Paragraph("\n"));
        pdfPTable.addCell(new Paragraph("\n"));
        pdfPTable.addCell(new Paragraph("\n"));
    }

    private PdfPTable addPriceView() {
        PdfPTable pdfPTable = new PdfPTable(4);
        try {
            float[] colWidths = {25, 25, 25, 25};
            pdfPTable.setWidths(colWidths);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
            pdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            Paragraph ph = new Paragraph();
            ph.add("\n");
            ph.add(new Phrase(new Chunk(getResources().getString(R.string.stock), Utils.fontNormal15)));
            ph.add("\n");
            ph.add("\n");
            ph.add(new Phrase(new Chunk(txtTotalStock.getText().toString(), Utils.fontBold20)));
            pdfPTable.addCell(ph);

            ph = new Paragraph();
            ph.add("\n");
            ph.add(new Phrase(new Chunk(getResources().getString(R.string.stock_price), Utils.fontNormal15)));
            ph.add("\n");
            ph.add("\n");
            ph.add(new Phrase(new Chunk(txtTotalStockValue.getText().toString(), Utils.fontBold20)));
            pdfPTable.addCell(ph);

            ph = new Paragraph();
            ph.add("\n");
            ph.add(new Phrase(new Chunk(getResources().getString(R.string.profit), Utils.fontNormal15)));
            ph.add("\n");
            ph.add("\n");
            ph.add(new Phrase(new Chunk(txtTotalProfit.getText().toString(), Utils.fontBold20)));
            pdfPTable.addCell(ph);

            ph = new Paragraph();
            ph.add("\n");
            ph.add(new Phrase(new Chunk(getResources().getString(R.string.total), Utils.fontNormal15)));
            ph.add("\n");
            ph.add("\n");
            ph.add(new Phrase(new Chunk(txtTotalPrice.getText().toString(), Utils.fontBold20)));
            pdfPTable.addCell(ph);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return pdfPTable;
    }

    private PdfPTable addTopView() {
        PdfPTable pdfPTable = new PdfPTable(1);
        try {
            float[] colWidths = {100};
            pdfPTable.setWidths(colWidths);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
            pdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            pdfPTable.addCell(new Phrase(new Chunk(productBean.name.toUpperCase(), Utils.fontBold25)));
            pdfPTable.addCell(new Paragraph("\n\n"));

        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return pdfPTable;
    }

    private PdfPCell addCell(String s, Font boldF) {
        Paragraph title1 = new Paragraph(s, boldF);
        title1.setSpacingBefore(10);
        title1.setSpacingAfter(10);
        title1.setAlignment(Element.ALIGN_CENTER);
        PdfPCell pdfWordCell = new PdfPCell();
        pdfWordCell.addElement(title1);
        return pdfWordCell;
    }

    private Element addStockDataInTable() {
        PdfPTable table = new PdfPTable(new float[]{1f, 1f, 1f, 1f});
        table.setWidthPercentage(100);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);

        table.addCell(addCell("PRICE", Utils.fontBold15));
        table.addCell(addCell("ADD STOCK", Utils.fontBold15));
        table.addCell(addCell("SELL STOCK", Utils.fontBold15));
        table.addCell(addCell("TOTAL PRICE", Utils.fontBold15));
        table.setHeaderRows(1);

        PdfPCell[] cells = table.getRow(0).getCells();
        for (int j = 0; j < cells.length; j++) {
            cells[j].setBackgroundColor(new BaseColor(154, 226, 211));
        }

        for (int i = 0; i < stockBeans.size(); i++) {
            StockBean stockBean = stockBeans.get(i);
            int quantity = Integer.parseInt(stockBean.quantity);
            table.addCell(addCell(stockBean.price, Utils.fontNormal15));
            table.addCell(addCell(quantity > 0 ? stockBean.quantity : "-", Utils.fontNormal15));
            table.addCell(addCell(quantity < 0 ? stockBean.quantity.replace("-", "") : "-", Utils.fontNormal15));
            table.addCell(addCell(stockBean.total, Utils.fontNormal15));
        }
        return table;
    }

    private PdfPTable addBottomView() {
        PdfPTable pdfPTable = new PdfPTable(4);
        try {
            float[] colWidths = {5, 50, 5, 40};
            pdfPTable.setWidths(colWidths);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            pdfPTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            pdfPTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawColor(getResources().getColor(R.color.pdf_top));
            canvas.drawBitmap(bitmap, 0, 0, null);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            Image myImg = null;
            try {
                myImg = Image.getInstance(stream.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            myImg.setWidthPercentage(30);
            addSpaceInTopView(pdfPTable);

            Paragraph ph = new Paragraph();
            ph.setAlignment(Element.ALIGN_CENTER);
            Chunk c = new Chunk(myImg, 0, -24);
            ph.add(c);
            ph.add("\n");
            ph.add("\n");
            ph.add("\n");
            ph.add("\n");
            ph.add(new Phrase(new Chunk(getResources().getString(R.string.app_name), Utils.fontBold20)));
            ph.add("\n");

            PdfPCell cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorderColor(new BaseColor(255, 255, 255));
            cell.addElement(ph);

            pdfPTable.addCell(new Paragraph(""));
            pdfPTable.addCell(new Paragraph(""));
            pdfPTable.addCell(new Paragraph(""));
            pdfPTable.addCell(cell);

            addSpaceInTopView(pdfPTable);

        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return pdfPTable;
    }

    private void generatePdf() {
        Utils.setInitPdfFonts();
        String directory_path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "." + getResources().getString(R.string.app_name).replace(" ", "") + File.separator;
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String targetPdf = directory_path + productBean.id + ".pdf";
        File pdfFile = new File(targetPdf);

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            document.add(addTopView());
            document.add(addPriceView());
            document.add(new Paragraph("\n\n"));
            document.add(addStockDataInTable());

            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("\n\n"));
            document.add(addBottomView());

            document.close();

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            Uri path = Uri.fromFile(pdfFile);
            Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
            pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfOpenintent.setDataAndType(path, "application/pdf");
            try {
                startActivity(pdfOpenintent);
            } catch (ActivityNotFoundException e) {
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Utils.closeProgress();
    }

    private boolean stockValidate() {
        if (isSell)
            productPrice = edtStockPrice.getText().toString();
        else
            productPrice = productBean.price;
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
            if (!(Integer.parseInt(productBean.stock) >= Integer.parseInt(quantity))) {
                edtStockQuantity.setError(getResources().getString(R.string.available_stock) + productBean.stock);
                return false;
            } else if (!(Integer.parseInt(productBean.price) <= Integer.parseInt(productPrice))) {
                edtStockPrice.setError(getResources().getString(R.string.purchase_price) + productBean.price);
                return false;
            }
        }
        return true;
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

    private void getStockList() {
        Utils.showProgress(this);
        DatabaseReference myRef = GET_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                .child(Constants.STOCK).child(productBean.id);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stockBeans = new ArrayList<>();
                totalProfit = 0;
                totalPrice = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    StockBean stockBean = data.getValue(StockBean.class);
                    stockBeans.add(stockBean);
                    if (Integer.parseInt(stockBean.quantity) < 0) {
                        totalProfit = totalProfit + (Integer.parseInt(stockBean.total) -
                                (Integer.parseInt(stockBean.quantity.replace("-", ""))) * Integer.parseInt(productBean.price));
                        totalPrice = totalPrice + Integer.parseInt(stockBean.total);
                    }
                }
                txtTotalProfit.setText(totalProfit + "");
                txtTotalPrice.setText((totalPrice + (Integer.parseInt(productBean.stock) * Integer.parseInt(productBean.price))) + "");

                final StockListAdapter stockListAdapter = new StockListAdapter(ProductDetailActivity.this, stockBeans);
                list.setAdapter(stockListAdapter);
                list.post(new Runnable() {
                    @Override
                    public void run() {
                        list.setSelection(stockListAdapter.getCount() - 1);
                    }
                });
                Utils.closeProgress();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utils.closeProgress();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(IS_REFRESH, isRefresh);
        setResult(PRODUCT_DETAIL_REQUEST, intent);
        finish();
    }
}
