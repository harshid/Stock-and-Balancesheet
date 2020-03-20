package adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mycredit.R;

import java.util.ArrayList;

import bean.StockBean;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mac on 17/07/19.
 */

public class StockListAdapter extends BaseAdapter {
    Activity activity;
    ArrayList<StockBean> stockBeans;

    public StockListAdapter(Activity activity, ArrayList<StockBean> frameBeans) {
        this.activity = activity;
        this.stockBeans = frameBeans;
    }

    @Override
    public int getCount() {
        return stockBeans.size();
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
            view = layoutInflater.inflate(R.layout.adapter_stock_list_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (Integer.parseInt(stockBeans.get(position).quantity) > 0) {
            viewHolder.llAdd.setVisibility(View.VISIBLE);
            viewHolder.llSell.setVisibility(View.GONE);
            viewHolder.txtAddStock.setText(stockBeans.get(position).quantity.replace("-", ""));
            viewHolder.txtAddPrice.setText(stockBeans.get(position).price);
            viewHolder.txtAddTotalPrice.setText(stockBeans.get(position).total);
        } else {
            viewHolder.llAdd.setVisibility(View.GONE);
            viewHolder.llSell.setVisibility(View.VISIBLE);
            viewHolder.txtSellStock.setText(stockBeans.get(position).quantity.replace("-", ""));
            viewHolder.txtSellPrice.setText(stockBeans.get(position).price);
            viewHolder.txtSellTotalPrice.setText(stockBeans.get(position).total);
        }
        return view;
    }

    static class ViewHolder {
        @BindView(R.id.txtAddStock)
        TextView txtAddStock;
        @BindView(R.id.txtAddPrice)
        TextView txtAddPrice;
        @BindView(R.id.ll_add)
        LinearLayout llAdd;
        @BindView(R.id.txtSellStock)
        TextView txtSellStock;
        @BindView(R.id.txtSellPrice)
        TextView txtSellPrice;
        @BindView(R.id.ll_sell)
        LinearLayout llSell;
        @BindView(R.id.txtAddTotalPrice)
        TextView txtAddTotalPrice;
        @BindView(R.id.txtSellTotalPrice)
        TextView txtSellTotalPrice;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
