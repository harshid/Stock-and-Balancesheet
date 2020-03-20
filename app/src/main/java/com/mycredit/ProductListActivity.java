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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import adapter.ProductListAdapter;
import adapter.StockListAdapter;
import bean.ProductBean;
import bean.StockBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utils.Constants;
import utils.Utils;

import static utils.Constants.GET_REFERENCE_STOCK;
import static utils.Constants.IS_REFRESH;
import static utils.Constants.PRODUCT;
import static utils.Constants.PRODUCT_DETAIL_REQUEST;

public class ProductListActivity extends AppCompatActivity {
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.list)
    ListView list;
    @BindView(R.id.txtTotalStock)
    TextView txtTotalStock;
    @BindView(R.id.llSend)
    LinearLayout llSend;

    ArrayList<ProductBean> productBeans;
    FirebaseAuth mAuth;
    int totalStock = 0, productCount = 0;
    Document document;
    ArrayList<ArrayList<StockBean>> stockBeansLists;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        ButterKnife.bind(this);

        llSend.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();
        txtTitle.setText(getResources().getString(R.string.product_list));
        getProductList();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivityForResult(new Intent(ProductListActivity.this, ProductDetailActivity.class).putExtra(PRODUCT, productBeans.get(position)), PRODUCT_DETAIL_REQUEST);
            }
        });
    }

    @OnClick({R.id.imgBack, R.id.llSend})
    void Clcik(View view) {
        switch (view.getId()) {
            case R.id.imgBack:
                finish();
                break;
            case R.id.llSend:
                stockBeansLists = new ArrayList<>();
                productCount = 0;
                if (productBeans.size() > 0) {
                    Utils.showProgress(ProductListActivity.this);
                    getStockData();
                }
                break;
        }
    }

    private void getStockData() {
        DatabaseReference myRef = GET_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                .child(Constants.STOCK).child(productBeans.get(productCount).id);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<StockBean> stockBeans = new ArrayList<>();
                // totalProfit = 0;
                // totalPrice = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    StockBean stockBean = data.getValue(StockBean.class);
                    stockBeans.add(stockBean);
                    /* if (Integer.parseInt(stockBean.quantity) < 0) {
                        totalProfit = totalProfit + (Integer.parseInt(stockBean.total) -
                                (Integer.parseInt(stockBean.quantity.replace("-", ""))) * Integer.parseInt(productBean.price));
                        totalPrice = totalPrice + Integer.parseInt(stockBean.total);
                    } */
                }
                stockBeansLists.add(stockBeans);
                // txtTotalProfit.setText(totalProfit + "");
                // txtTotalPrice.setText((totalPrice + (Integer.parseInt(productBean.stock) * Integer.parseInt(productBean.price))) + "");
                productCount++;
                if (productBeans.size() > productCount) {
                    getStockData();
                } else {
                    generatePdf();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utils.closeProgress();
            }
        });

    }

    private void getProductList() {
        Utils.showProgress(ProductListActivity.this);
        DatabaseReference myRef = GET_REFERENCE_STOCK.child(mAuth.getCurrentUser().getPhoneNumber())
                .child(Constants.PRODUCT);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productBeans = new ArrayList<>();
                totalStock = 0;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ProductBean productBean = data.getValue(ProductBean.class);
                    productBeans.add(productBean);
                    totalStock = totalStock + Integer.parseInt(productBean.stock);
                }
                list.setAdapter(new ProductListAdapter(ProductListActivity.this, productBeans));
                Utils.closeProgress();
                txtTotalStock.setText(totalStock + "");
                Log.d("----- ttal Stok :- ", totalStock + "");
                Log.d("----- from text :- ", txtTotalStock.getText().toString() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utils.closeProgress();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.PRODUCT_DETAIL_REQUEST) {
            if (data != null && data.getBooleanExtra(IS_REFRESH, false)) {
                getProductList();
            }
        }
    }

    private void addProductData() {
        PdfPTable pdfPTable;
        try {
            document.add(new Paragraph("\n\n"));
            pdfPTable = new PdfPTable(1);
            float[] colWidth = {100};
            pdfPTable.setWidths(colWidth);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
            pdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            pdfPTable.addCell(new Phrase(new Chunk(productBeans.get(productCount).name.toUpperCase(), Utils.boldUnderList)));
            document.add(pdfPTable);
            document.add(new Paragraph("\n"));

            pdfPTable = new PdfPTable(2);
            float[] colWidths = {50, 50};
            pdfPTable.setWidths(colWidths);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
            pdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell pdfPCell = new PdfPCell();
            pdfPCell.setBorder(Rectangle.NO_BORDER);

            Paragraph ph = new Paragraph();
            ph.add(new Phrase(new Chunk("Stock", Utils.fontNormal15)));
            ph.add(new Phrase("\n"));
            ph.add(new Phrase(new Chunk(productBeans.get(productCount).stock.toUpperCase(), Utils.fontBold20)));
            pdfPTable.addCell(ph);

            ph = new Paragraph();
            ph.add(new Phrase(new Chunk("Price", Utils.fontNormal15)));
            ph.add(new Phrase("\n"));
            ph.add(new Phrase(new Chunk((Integer.parseInt(productBeans.get(productCount).stock)
                    * Integer.parseInt(productBeans.get(productCount).price)) + "", Utils.fontBold20)));
            pdfPTable.addCell(ph);

            document.add(pdfPTable);
            document.add(new Paragraph("\n"));
            addStockDataInTable(stockBeansLists.get(productCount));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
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

            pdfPTable.addCell(new Phrase(new Chunk(getResources().getString(R.string.product_list).toUpperCase(), Utils.fontBold25)));
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

    private void addStockDataInTable(ArrayList<StockBean> stockBeans) {
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
            table.addCell(addCell(quantity > 0 ? stockBean.quantity : "", Utils.fontNormal15));
            table.addCell(addCell(quantity < 0 ? stockBean.quantity.replace("-", "") : "", Utils.fontNormal15));
            table.addCell(addCell(stockBean.total, Utils.fontNormal15));
        }
        try {
            document.add(table);
            document.add(new Paragraph("\n\n"));
            document.add(new LineSeparator());

            productCount++;
            if (productBeans.size() > productCount)
                addProductData();
            else {
                document.add(new Paragraph("\n\n"));
                document.add(new Paragraph("\n\n"));
                document.add(addBottomView());
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
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

    private void addSpaceInTopView(PdfPTable pdfPTable) {
        pdfPTable.addCell(new Paragraph("\n"));
        pdfPTable.addCell(new Paragraph("\n"));
        pdfPTable.addCell(new Paragraph("\n"));
        pdfPTable.addCell(new Paragraph("\n"));
    }

    private void generatePdf() {
        productCount = 0;
        Utils.setInitPdfFonts();
        String directory_path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "." + getResources().getString(R.string.app_name).replace(" ", "") + File.separator;
        File file = new File(directory_path);
        if (!file.exists())
            file.mkdirs();

        String targetPdf = directory_path + getResources().getString(R.string.product_list) + ".pdf";
        File pdfFile = new File(targetPdf);

        try {
            document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            document.add(addTopView());
            addProductData();
            document.add(new Paragraph("\n\n"));

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
        productCount = 0;
        Utils.closeProgress();
    }
}
