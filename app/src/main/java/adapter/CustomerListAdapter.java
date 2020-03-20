package adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mycredit.R;
import com.mycredit.TransactionActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import bean.CustomerBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.internal.Util;
import utils.Constants;
import utils.Utils;

public class CustomerListAdapter extends BaseAdapter {
    Activity activity;
    ArrayList<CustomerBean> customerBeans;

    public CustomerListAdapter(Activity activity, ArrayList<CustomerBean> customerBeans) {
        this.activity = activity;
        this.customerBeans = customerBeans;
    }

    @Override
    public int getCount() {
        return customerBeans.size();
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
        Holder holder;
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (rootView == null) {
            rootView = layoutInflater.inflate(R.layout.adapter_customer_list_item, null);
            holder = new Holder(rootView);
            rootView.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        holder.txtName.setText(customerBeans.get(position).name);
        holder.txtAddTime.setText(customerBeans.get(position).add_time);
        holder.txtAmount.setText(activity.getResources().getString(R.string.rupees_symbol) + customerBeans.get(position).amount);
        holder.txtType.setText(customerBeans.get(position).type.length() > 0 ? customerBeans.get(position).type.equalsIgnoreCase(Constants.DUE)
                ? activity.getResources().getString(R.string.due) : activity.getResources().getString(R.string.advance) : activity.getResources().getString(R.string.due));
        holder.viewBelow.setBackgroundColor(customerBeans.get(position).type.equalsIgnoreCase(Constants.DUE)
                ? activity.getResources().getColor(R.color.red) : activity.getResources().getColor(R.color.green));
        holder.txtAmount.setTextColor(customerBeans.get(position).type.equalsIgnoreCase(Constants.DUE)
                ? activity.getResources().getColor(R.color.dark_red) : activity.getResources().getColor(R.color.dark_green));

        if (customerBeans.get(position).profile != null && customerBeans.get(position).profile.length() > 0) {
            if (Utils.isValidURL(customerBeans.get(position).profile))
                Picasso.get().load(customerBeans.get(position).profile).placeholder(R.drawable.ic_profile).into(holder.imgProfile);
            else
                Picasso.get().load(new File(customerBeans.get(position).profile)).placeholder(R.drawable.ic_profile).into(holder.imgProfile);
            holder.txtSortName.setVisibility(View.GONE);
            holder.imgProfile.setVisibility(View.VISIBLE);
        } else {
            Drawable unwrappedDrawable = AppCompatResources.getDrawable(activity, R.drawable.rounded_green);
            Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, Utils.generateRandomColor());
            holder.txtSortName.setBackground(unwrappedDrawable);
            holder.txtSortName.setText(customerBeans.get(position).name.length() > 0 ? customerBeans.get(position).name.substring(0, 1) : "");
            holder.txtSortName.setVisibility(customerBeans.get(position).name.length() > 0 ? View.VISIBLE : View.GONE);
            holder.imgProfile.setVisibility(View.GONE);
        }

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivityForResult(new Intent(activity, TransactionActivity.class).putExtra(Constants.CUSTOMER, customerBeans.get(position)), Constants.TRANSACTION_REQUEST);
            }
        });
        return rootView;
    }

    public class Holder {
        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtAddTime)
        TextView txtAddTime;
        @BindView(R.id.txtAmount)
        TextView txtAmount;
        @BindView(R.id.imgProfile)
        CircleImageView imgProfile;
        @BindView(R.id.txtType)
        TextView txtType;
        @BindView(R.id.txtSortName)
        TextView txtSortName;
        @BindView(R.id.viewBelow)
        View viewBelow;

        Holder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
