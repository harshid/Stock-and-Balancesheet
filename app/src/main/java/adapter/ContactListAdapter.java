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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mycredit.R;
import com.mycredit.TransactionActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import bean.ContactBean;
import bean.CustomerBean;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import utils.Constants;
import utils.Utils;

public class ContactListAdapter extends BaseAdapter {
    Activity activity;
    ArrayList<ContactBean> contactBeans;

    public ContactListAdapter(Activity activity, ArrayList<ContactBean> contactBeans) {
        this.activity = activity;
        this.contactBeans = contactBeans;
    }

    @Override
    public int getCount() {
        return contactBeans.size();
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
            rootView = layoutInflater.inflate(R.layout.adapter_contact_list_item, null);
            holder = new Holder(rootView);
            rootView.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        holder.txtName.setText(contactBeans.get(position).name);
        holder.txtNumber.setText(contactBeans.get(position).number);
        holder.txtSortName.setText(contactBeans.get(position).name.length() > 0 ? contactBeans.get(position).name.substring(0, 1) : "");
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(activity, R.drawable.rounded_green);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Utils.generateRandomColor());
        holder.txtSortName.setBackground(unwrappedDrawable);
        return rootView;
    }

    public class Holder {
        @BindView(R.id.txtSortName)
        TextView txtSortName;
        @BindView(R.id.txtName)
        TextView txtName;
        @BindView(R.id.txtNumber)
        TextView txtNumber;

        Holder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
