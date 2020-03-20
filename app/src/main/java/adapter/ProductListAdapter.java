package adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mycredit.R;

import java.util.ArrayList;

import bean.ProductBean;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mac on 17/07/19.
 */

public class ProductListAdapter extends BaseAdapter {
    Activity activity;
    ArrayList<ProductBean> productBeans;

    public ProductListAdapter(Activity activity, ArrayList<ProductBean> frameBeans) {
        this.activity = activity;
        this.productBeans = frameBeans;
    }

    @Override
    public int getCount() {
        return productBeans.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = layoutInflater.inflate(R.layout.adapter_product_list_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.txtProductName.setText(productBeans.get(position).name);
        viewHolder.txtStock.setText(productBeans.get(position).stock);
        return view;
    }

    static class ViewHolder {
        @BindView(R.id.txtProductName)
        TextView txtProductName;
        @BindView(R.id.txtStock)
        TextView txtStock;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
