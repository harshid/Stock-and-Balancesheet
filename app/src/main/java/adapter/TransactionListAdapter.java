package adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mycredit.PaymentActivity;
import com.mycredit.R;
import com.mycredit.TransactionActivity;
import com.mycredit.TransactionDetailActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import bean.CustomerBean;
import bean.TransactionBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import utils.Constants;
import utils.Utils;

import static utils.Constants.TRANSACTION_REQUEST;

public class TransactionListAdapter extends BaseAdapter {
    Activity activity;
    ArrayList<TransactionBean> transactionBeans;
    CustomerBean customerBean;

    public TransactionListAdapter(Activity activity, ArrayList<TransactionBean> transactionBeans, CustomerBean customerBean) {
        this.activity = activity;
        this.transactionBeans = transactionBeans;
        this.customerBean = customerBean;
    }

    @Override
    public int getCount() {
        return transactionBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        View rootView = view;
        ViewHolder holder;
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (rootView == null) {
            rootView = layoutInflater.inflate(R.layout.adapter_payment_list_item, null);
            holder = new ViewHolder(rootView);
            rootView.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final TransactionBean bean = transactionBeans.get(position);
        String note = bean.note;
        String amount = activity.getResources().getString(R.string.rupees_symbol) + bean.Amount;
        String total = activity.getResources().getString(R.string.rupees_symbol) + bean.total;
        String time = bean.time;
        String date = bean.date;
        String type = bean.type.equalsIgnoreCase(Constants.ADVANCE) ? activity.getResources().getString(R.string.advance) : activity.getResources().getString(R.string.due);

        String image = bean.image;
        String deleted_on = bean.deleted_on;
        try {
            Utils.setEngilsh(activity);
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh:mm aa");
            Date tran_date = (Date) sdf.parse(bean.time);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
            String current_date = dateFormat.format(c.getTime());
            if (date.equals(current_date)) {
                date = activity.getResources().getString(R.string.today);
            }
            SimpleDateFormat newFormat = new SimpleDateFormat("hh:mm aa");
            String tran_time = newFormat.format(tran_date);
            time = tran_time;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Utils.setSavedLanguage(activity);

        final boolean accept = bean.accept.equals("1");
        holder.llAccept.setVisibility(deleted_on.length() > 0 ? View.GONE : accept ? View.VISIBLE : View.GONE);
        holder.llGive.setVisibility(deleted_on.length() > 0 ? View.GONE : accept ? View.GONE : View.VISIBLE);
        holder.llDate.setVisibility(date.length() > 0 ? View.VISIBLE : View.GONE);

        holder.txtDate.setText(date);
        if (accept) {
            holder.txtAcceptAmount.setText(amount);
            holder.txtAcceptTime.setText(time);
            holder.txtAcceptTotal.setText(total + " " + type);
            holder.txtAcceptNote.setText(note);
            holder.txtAcceptNote.setVisibility(note.length() > 0 ? View.VISIBLE : View.GONE);
            holder.imgAcceptAvailable.setVisibility(image.length() > 0 ? View.VISIBLE : View.GONE);
            holder.ll_deleted_accept.setVisibility(deleted_on.length() > 0 ? View.VISIBLE : View.GONE);
            holder.ll_deleted_give.setVisibility(View.GONE);
        } else {
            holder.txtGiveAmount.setText(amount);
            holder.txtGiveTime.setText(time);
            holder.txtGiveTotal.setText(total + " " + type);
            holder.txtGiveNote.setText(note);
            holder.txtGiveNote.setVisibility(note.length() > 0 ? View.VISIBLE : View.GONE);
            holder.imgGiveAvailable.setVisibility(image.length() > 0 ? View.VISIBLE : View.GONE);
            holder.ll_deleted_give.setVisibility(deleted_on.length() > 0 ? View.VISIBLE : View.GONE);
            holder.ll_deleted_accept.setVisibility(View.GONE);
        }
        holder.relMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivityForResult(new Intent(activity, TransactionDetailActivity.class)
                        .putExtra(Constants.CUSTOMER, customerBean)
                        .putExtra(Constants.TRANSACTION, bean), TRANSACTION_REQUEST);
            }
        });
        return rootView;
    }

    static class ViewHolder {
        @BindView(R.id.txtAcceptAmount)
        TextView txtAcceptAmount;
        @BindView(R.id.txtAcceptTime)
        TextView txtAcceptTime;
        @BindView(R.id.txtAcceptTotal)
        TextView txtAcceptTotal;
        @BindView(R.id.ll_accept)
        LinearLayout llAccept;
        @BindView(R.id.txtGiveAmount)
        TextView txtGiveAmount;
        @BindView(R.id.txtGiveTime)
        TextView txtGiveTime;
        @BindView(R.id.txtGiveTotal)
        TextView txtGiveTotal;
        @BindView(R.id.ll_give)
        LinearLayout llGive;
        @BindView(R.id.relMain)
        RelativeLayout relMain;
        @BindView(R.id.txtAcceptNote)
        TextView txtAcceptNote;
        @BindView(R.id.txtGiveNote)
        TextView txtGiveNote;
        @BindView(R.id.llDate)
        LinearLayout llDate;
        @BindView(R.id.txtDate)
        TextView txtDate;
        @BindView(R.id.imgAcceptAvailable)
        ImageView imgAcceptAvailable;
        @BindView(R.id.imgGiveAvailable)
        ImageView imgGiveAvailable;
        @BindView(R.id.ll_deleted_accept)
        LinearLayout ll_deleted_accept;
        @BindView(R.id.ll_deleted_give)
        LinearLayout ll_deleted_give;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
