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
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import adapter.TransactionListAdapter;
import bean.CustomerBean;
import bean.SignupBean;
import bean.TransactionBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import database.SqlLiteDbHelper;
import utils.Constants;
import utils.Permission;
import utils.Utils;

import static utils.Constants.GET_BACK_UP_REFERENCE;
import static utils.Constants.GET_REFERENCE;
import static utils.Constants.IMAGE_FILE;
import static utils.Constants.IS_REFRESH;
import static utils.Constants.KEY;
import static utils.Constants.MAKE_PAYMENT;
import static utils.Constants.STORAGE_REFERENCE;
import static utils.Constants.TIME;
import static utils.Constants.TRANSACTION;
import static utils.Constants.TRANSACTION_REQUEST;

public class TransactionActivity extends BaseActivity {
    @BindView(R.id.list)
    ListView list;
    @BindView(R.id.txtBalance)
    TextView txtBalance;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtType)
    TextView txtType;
    @BindView(R.id.llCall)
    LinearLayout llCall;
    @BindView(R.id.llSend)
    LinearLayout llSend;

    ArrayList<TransactionBean> transactionBeans;
    CustomerBean customerBean;
    TransactionListAdapter listAdapter;

    FirebaseAuth mAuth;
    String isRefresh = "0";
    SqlLiteDbHelper sqlLiteDbHelper;
    File pdfFile;

    Document document;
    int paymentCount = 0, paymentAmount = 0, creditAmount = 0, creditCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        sqlLiteDbHelper = new SqlLiteDbHelper(this);
        transactionBeans = new ArrayList<>();

        llSend.setVisibility(View.VISIBLE);
        llCall.setVisibility(View.VISIBLE);

        customerBean = (CustomerBean) getIntent().getSerializableExtra(Constants.CUSTOMER);
        /*if (mAuth.getCurrentUser() != null && customerBean.number != null)
            getTransactionData();*/
        if (customerBean.number != null) {
            getTransactionDataFromDataBase();
        }

        setAmountData();
    }


    private void getTransactionDataFromDataBase() {
        customerBean = sqlLiteDbHelper.getCustomer(customerBean.number);
        transactionBeans = sqlLiteDbHelper.getTransactionData(customerBean.number);
        setAmountData();
        listAdapter = new TransactionListAdapter(TransactionActivity.this, transactionBeans, customerBean);
        list.setAdapter(listAdapter);
        list.post(new Runnable() {
            @Override
            public void run() {
                list.setSelection(listAdapter.getCount() - 1);
            }
        });
    }

    private void setAmountData() {
        txtBalance.setText(customerBean.amount);
        txtTitle.setText(customerBean.name);
        txtType.setText(customerBean.type.equalsIgnoreCase(Constants.DUE)
                ? getResources().getString(R.string.due) : getResources().getString(R.string.advance));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.IS_REFRESH, isRefresh);
        setResult(TRANSACTION_REQUEST, intent);
        finish();
    }

    /* private void getTransactionData() {
         myRef = database.getReference().child(mAuth.getCurrentUser().getPhoneNumber()).child(Constants.TRANSACTION).child(customerBean.number);
         myRef.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 transactionBeans.clear();
                 for (DataSnapshot data : dataSnapshot.getChildren()) {
                     TransactionBean transactionBean = data.getValue(TransactionBean.class);
                     transactionBeans.add(transactionBean);
                 }
                 getCustomerDetail();
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {
             }
         });
     } */

    @OnClick({R.id.txtTitle, R.id.imgBack, R.id.rlAccept, R.id.rlGive, R.id.action_bar, R.id.llSend, R.id.llCall})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txtTitle:
                openCustomerProfileActivity();
                break;
            case R.id.llSend:
                generatePdf();
                break;
            case R.id.llCall:
                if (Permission.checkCallPermission(TransactionActivity.this)) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + customerBean.number));
                    startActivity(intent);
                }
                break;
            case R.id.action_bar:
                openCustomerProfileActivity();
                break;
            case R.id.imgBack:
                onBackPressed();
                break;
            case R.id.rlAccept:
                startActivityForResult(new Intent(TransactionActivity.this, PaymentActivity.class)
                        .putExtra(Constants.CUSTOMER, customerBean)
                        .putExtra(TIME, transactionBeans.size() > 0 ? transactionBeans.get(transactionBeans.size() - 1).trans_date : "")
                        .putExtra(Constants.ACCEPT, "1"), MAKE_PAYMENT);
                break;
            case R.id.rlGive:
                startActivityForResult(new Intent(TransactionActivity.this, PaymentActivity.class)
                        .putExtra(Constants.CUSTOMER, customerBean)
                        .putExtra(TIME, transactionBeans.size() > 0 ? transactionBeans.get(transactionBeans.size() - 1).trans_date : "")
                        .putExtra(Constants.ACCEPT, "0"), MAKE_PAYMENT);
                break;
        }
    }

    private void generatePdf() {
        Utils.showProgress(TransactionActivity.this, getResources().getString(R.string.generating_pdf));
        Utils.setInitPdfFonts();
        String directory_path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "." + getResources().getString(R.string.app_name).replace(" ", "") + File.separator;
        File file = new File(directory_path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String targetPdf = directory_path + "MyPdf.pdf";
        pdfFile = new File(targetPdf);

        try {
            document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            document.add(addTopView());

            document.add(new Paragraph("\n"));
            Paragraph paragraph3 = new Paragraph((customerBean.name.length() > 0 ?
                    customerBean.name + (customerBean.number.length() > 0 ?
                            "\n" + "(" + customerBean.number + ")" : "") : customerBean.number), Utils.fontBold25);
            paragraph3.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph3);
            document.add(new Paragraph("\n\n"));

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_wallet);
            Paragraph ph = new Paragraph();
            ph.add("\n");
            ph.add(new Phrase(new Chunk(getResources().getString(R.string.net_balance), Utils.fontNormal15)));
            ph.add("\n");
            ph.add(new Phrase(customerBean.amount, customerBean.type.equalsIgnoreCase(Constants.ADVANCE) ? Utils.greenFont35 : Utils.redFont35));
            ph.add(" ");
            ph.add(new Phrase(customerBean.type, Utils.fontNormal15));
            ph.add("\n\n");
            document.add(addNetBalanceView(25, ph, bitmap));

            document.add(new Paragraph("\n\n"));

            document.add(addPaymentCreditView());

            document.add(new Paragraph("\n\n"));

            Paragraph paragraph6 = new Paragraph(getResources().getString(R.string.account_statement).toUpperCase(), Utils.boldUnderList);
            paragraph6.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph6);
            document.add(new Paragraph("\n\n"));

            document.add(addStatementTable());

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

    private Element addPaymentCreditView() {
        PdfPTable pdfPTable = new PdfPTable(2);
        try {
            float[] colWidths = {50, 50};
            pdfPTable.setWidths(colWidths);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            pdfPTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);

            calculatePaymentData();

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_down_green);
            Paragraph ph = new Paragraph();
            ph.add("\n");
            ph.add(new Phrase(new Chunk(" " + paymentCount + " " + getResources().getString(R.string.payments).toUpperCase(), Utils.fontNormal15)));
            ph.add("\n");
            ph.add(new Phrase(paymentAmount + "", Utils.greenFont25));
            ph.add("\n\n");
            // pdfPTable.addCell(new Paragraph(""));
            pdfPTable.addCell(addNetBalanceView(40, ph, bitmap));

            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_up_red);
            ph = new Paragraph();
            ph.add("\n");
            ph.add(new Phrase(new Chunk(" " + creditCount + " " + getResources().getString(R.string.credits).toUpperCase(), Utils.fontNormal15)));
            ph.add("\n");
            ph.add(new Phrase(creditAmount + "", Utils.redFont25));
            ph.add("\n\n");
            pdfPTable.addCell(addNetBalanceView(40, ph, bitmap));
            // pdfPTable.addCell(new Paragraph(""));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return pdfPTable;
    }

    private void calculatePaymentData() {
        for (int i = 0; i < transactionBeans.size(); i++) {
            TransactionBean transactionBean = transactionBeans.get(i);
            if (transactionBean.accept.equals("1")) {
                paymentCount++;
                paymentAmount = paymentAmount + (int) Float.parseFloat(transactionBean.Amount);
            } else {
                creditCount++;
                creditAmount = creditAmount + (int) Float.parseFloat(transactionBean.Amount);
            }
        }
    }

    private Element addStatementTable() {
        PdfPTable table = new PdfPTable(new float[]{1f, 2f, 1.5f, 1.5f});
        table.setWidthPercentage(100);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);

        table.addCell(addCell("DATE", Utils.fontBold15));
        table.addCell(addCell("PARTICULARS", Utils.fontBold15));
        table.addCell(addCell("PAYMENT", Utils.fontBold15));
        table.addCell(addCell("CREDIT", Utils.fontBold15));
        table.setHeaderRows(1);

        PdfPCell[] cells = table.getRow(0).getCells();
        for (int j = 0; j < cells.length; j++) {
            cells[j].setBackgroundColor(new BaseColor(154, 226, 211));
        }

        for (int i = 0; i < transactionBeans.size(); i++) {
            TransactionBean transactionBean = transactionBeans.get(i);
            String current_date = "";
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh:mm aa");
                Date current = (Date) sdf.parse(transactionBean.time);
                SimpleDateFormat newFormat = new SimpleDateFormat("dd MMM yy");
                current_date = newFormat.format(current);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            table.addCell(addCell(current_date, Utils.fontNormal15));
            table.addCell(addCell(transactionBean.note.length() > 0 ?
                    transactionBean.note : getResources().getString(R.string.no_notes), transactionBean.note.length() > 0 ? Utils.fontNormal15 : Utils.fontNormalGray15));
            String Amount = "₹ " + transactionBean.Amount;
            table.addCell(addCell((transactionBean.accept.equals("1") ? Amount : "-"), Utils.fontNormal15));
            table.addCell(addCell((transactionBean.accept.equals("0") ? Amount : "-"), Utils.fontNormal15));
        }
        return table;
    }

    private PdfPTable addNetBalanceView(int imagePerc, Paragraph ph, Bitmap bitmap) {
        PdfPTable pdfPTable = new PdfPTable(2);
        try {
            float[] colWidths = {45, 55};
            pdfPTable.setWidths(colWidths);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            pdfPTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawColor(getResources().getColor(R.color.white));
            canvas.drawBitmap(bitmap, 0, 0, null);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            Image myImg = null;
            try {
                myImg = Image.getInstance(stream.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            myImg.setWidthPercentage(imagePerc);
            myImg.setAlignment(Image.RIGHT | Image.BOTTOM);

            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPaddingTop(10);
            cell.addElement(myImg);

            pdfPTable.addCell(cell);

            PdfPCell pdfPCell = new PdfPCell(ph);
            pdfPCell.setBorder(Rectangle.NO_BORDER);
            pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
            pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            pdfPTable.addCell(pdfPCell);

        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return pdfPTable;
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

    private PdfPTable addTopView() {
        PdfPTable pdfPTable = new PdfPTable(4);
        try {
            float[] colWidths = {5, 40, 50, 5};
            pdfPTable.setWidths(colWidths);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            pdfPTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
            pdfPTable.getDefaultCell().setBorderColor(new BaseColor(32, 160, 94));

            SignupBean signupBean = sqlLiteDbHelper.getDataBean();

            Bitmap bitmap = null;
            if (signupBean.profile_image != null && signupBean.profile_image.length() > 0) {
                if (Utils.isValidURL(signupBean.profile_image))
                    bitmap = getBitmapFromUrl(signupBean.profile_image);
                else if (new File(signupBean.profile_image).length() > 0)
                    bitmap = BitmapFactory.decodeFile(new File(signupBean.profile_image).getAbsolutePath());
            }
            if (bitmap == null)
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile);

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

            PdfPCell cell = new PdfPCell();
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingTop(10);
            cell.addElement(myImg);
            cell.setBorderColor(new BaseColor(32, 160, 94));

            pdfPTable.addCell(new Paragraph(""));
            pdfPTable.addCell(cell);

            Paragraph ph = new Paragraph();
            ph.add("\n");
            ph.add(new Phrase(new Chunk((signupBean.fullname.length() > 0 ? signupBean.fullname : signupBean.phoneNo), Utils.bfBoldWhite)));
            ph.add("\n\n");
            ph.add(new Phrase(new Chunk(signupBean.phoneNo, Utils.bfWhite)));
            if (signupBean.business.length() > 0) {
                ph.add("\n\n");
                ph.add(new Phrase(new Chunk(signupBean.business, Utils.bfWhite)));
            }
            if (signupBean.email.length() > 0) {
                ph.add("\n\n");
                ph.add(new Phrase(new Chunk(signupBean.email, Utils.bfWhite)));
            }
            ph.add("\n\n");

            pdfPTable.addCell(ph);
            pdfPTable.addCell(new Paragraph(""));

            addSpaceInTopView(pdfPTable);

            for (int i = 0; i < pdfPTable.getRows().size(); i++) {
                PdfPCell[] cells = pdfPTable.getRow(i).getCells();
                for (int j = 0; j < cells.length; j++) {
                    cells[j].setBackgroundColor(new BaseColor(32, 160, 94));
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return pdfPTable;
    }

    private Bitmap getBitmapFromUrl(String profile_image) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = new URL(profile_image);
            return BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            System.out.println(e);
        }
        return null;
    }

    private void addSpaceInTopView(PdfPTable pdfPTable) {
        pdfPTable.addCell(new Paragraph("\n"));
        pdfPTable.addCell(new Paragraph("\n"));
        pdfPTable.addCell(new Paragraph("\n"));
        pdfPTable.addCell(new Paragraph("\n"));
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

    private void openCustomerProfileActivity() {
        startActivityForResult(new Intent(TransactionActivity.this, CustomerProfileActivity.class)
                .putExtra(Constants.CUSTOMER, customerBean)
                .putExtra(Constants.TRANSACTION, transactionBeans)
                .putExtra(TIME, transactionBeans.size() > 0 ? transactionBeans.get(transactionBeans.size() - 1).trans_date : ""), MAKE_PAYMENT);
    }

    /* private void getCustomerDetail() {
        DatabaseReference myRef = database.getReference().child(mAuth.getCurrentUser()
                .getPhoneNumber()).child(Constants.CUSTOMER).child(customerBean.number);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                customerBean = dataSnapshot.getValue(CustomerBean.class);
                setAmountData();
                listAdapter = new TransactionListAdapter(TransactionActivity.this, transactionBeans, customerBean);
                list.setAdapter(listAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    } */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAKE_PAYMENT) {
            if (data != null) {
                if (data.getStringExtra(IS_REFRESH).equals("1")) {
                    isRefresh = "1";
                    if ((File) data.getSerializableExtra(IMAGE_FILE) != null && ((File) data.getSerializableExtra(IMAGE_FILE)).length() > 0) {
                        uploadImage((File) data.getSerializableExtra(IMAGE_FILE),
                                (TransactionBean) data.getSerializableExtra(TRANSACTION), data.getStringExtra(KEY));
                    }
                     /* if (Utils.isNetworkAvailable(TransactionActivity.this))
                        getTransactionData(); */
                    getTransactionDataFromDataBase();

                    /* TransactionBean transactionBean = (TransactionBean) data.getSerializableExtra(Constants.TRANSACTION);
                    if (transactionBean != null) {
                        SignupBean signupBean = sqlLiteDbHelper.getDataBean();
                        String name = signupBean.fullname.length() > 0 ? signupBean.fullname : signupBean.phoneNo;
                        if (customerBean.language != null && customerBean.language.length() > 0 && !customerBean.language.contains(LANGUAGE_DISABLE)) {
                            Utils.setLanguage(TransactionActivity.this, customerBean.language);
                            if (transactionBean.type.equals(Constants.ADVANCE)) {
                                Utils.sendMessage("+918780735806", getResources().getString(R.string.delete_advance_msg)
                                        .replace("AMOUNT", transactionBean.Amount)
                                        .replace("NAME", name)
                                        .replace("TOTAL", customerBean.amount)
                                        .replace("TYPE", transactionBean.type.equalsIgnoreCase(Constants.ADVANCE)
                                                ? getResources().getString(R.string.advance) : getResources().getString(R.string.due)));
                            } else {
                                Utils.sendMessage("+918780735806", getResources().getString(R.string.delete_give_msg)
                                        .replace("AMOUNT", transactionBean.Amount)
                                        .replace("NAME", name)
                                        .replace("TOTAL", customerBean.amount)
                                        .replace("TYPE", transactionBean.type.equalsIgnoreCase(Constants.ADVANCE)
                                                ? getResources().getString(R.string.advance) : getResources().getString(R.string.due)));
                            }
                            Utils.setSavedLanguage(TransactionActivity.this);
                        }
                    } */
                }
            }
        } else if (requestCode == TRANSACTION_REQUEST) {
            if (data != null) {
                if (data.getStringExtra(IS_REFRESH).equals("1")) {
                    // getTransactionData();
                    isRefresh = "1";
                    getTransactionDataFromDataBase();
                }
            }
        }
    }

    private void uploadImage(File file, final TransactionBean transactionBean, final String key) {
        final StorageReference ref = STORAGE_REFERENCE.child(Constants.TRANSACTION + "/" + mAuth.getCurrentUser().getPhoneNumber()
                + "/" + customerBean.number + "/" + key + ".jpg");
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            UploadTask uploadTask = ref.putStream(stream);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception exception) {
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // getting image uri and converting into string
                            String fileUrl = uri.toString();
                            transactionBean.image = fileUrl;
                            GET_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                                    .child(Constants.TRANSACTION).child(customerBean.number).child(key).setValue(transactionBean);
                            GET_BACK_UP_REFERENCE.child(mAuth.getCurrentUser().getPhoneNumber())
                                    .child(Constants.TRANSACTION).child(customerBean.number).child(key).setValue(transactionBean);
                            // getTransactionData();
                        }
                    });
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
