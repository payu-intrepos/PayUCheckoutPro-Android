package com.payu.sampleapp;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.payu.base.models.OrderDetails;

import java.util.ArrayList;

public class ReviewOrderRecyclerViewAdapter extends RecyclerView.Adapter<ReviewOrderRecyclerViewAdapter.ViewHolder> {
    private final ArrayList<OrderDetails> orderDetailsList = new ArrayList();

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View row = layoutInflater.inflate(R.layout.review_order_row_layout, null);
        return new ViewHolder(row, new MyCustomKeyEditTextListener(), new MyCustomValueEditTextListener());
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        // update MyCustomEditTextListener every time we bind a new item
        // so that it knows what item in mDataset to update
        holder.myCustomKeyTextListener.updatePosition(holder.getAdapterPosition());
        holder.mEditTextKey.setText(orderDetailsList.get(position).getKey());
        holder.myCustomValueTextListener.updatePosition(holder.getAdapterPosition());
        holder.mEditTextValue.setText(orderDetailsList.get(position).getValue());
        if (this.orderDetailsList.size() > 1)
            holder.ivDeleteOrderItem.setVisibility(View.VISIBLE);
        else
            holder.ivDeleteOrderItem.setVisibility(View.GONE);
    }

    public final void addRow() {
        orderDetailsList.add(new OrderDetails("", ""));
        this.notifyItemInserted(this.orderDetailsList.size() - 1);
    }

    public ArrayList getOrderDetailsList() {
        return this.orderDetailsList;
    }

    public int getItemCount() {
        return this.orderDetailsList != null && !this.orderDetailsList.isEmpty() ? this.orderDetailsList.size() : 1;
    }

    public ReviewOrderRecyclerViewAdapter() {
        this.orderDetailsList.add(new OrderDetails("Milk", "1"));
    }

    public class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        EditText mEditTextKey;
        EditText mEditTextValue;
        ImageView ivDeleteOrderItem;
        MyCustomKeyEditTextListener myCustomKeyTextListener;
        MyCustomValueEditTextListener myCustomValueTextListener;

        public ViewHolder(View v, MyCustomKeyEditTextListener myCustomKeyTextListener, MyCustomValueEditTextListener myCustomValueTextListener) {
            super(v);
            mEditTextKey = v.findViewById(R.id.etReviewOrderKey);
            mEditTextValue = v.findViewById(R.id.etReviewOrderValue);
            ivDeleteOrderItem = v.findViewById(R.id.ivDeleteOrderItem);
            this.myCustomKeyTextListener = myCustomKeyTextListener;
            this.myCustomValueTextListener = myCustomValueTextListener;
            this.mEditTextKey.addTextChangedListener((TextWatcher) myCustomKeyTextListener);
            this.mEditTextValue.addTextChangedListener((TextWatcher) myCustomValueTextListener);
            this.ivDeleteOrderItem.setOnClickListener((OnClickListener) (it -> {
                orderDetailsList.remove(getAdapterPosition());
                ReviewOrderRecyclerViewAdapter.this.notifyItemRemoved(getAdapterPosition());
            }));
        }
    }

    // we make TextWatcher to be aware of the position it currently works with
    // this way, once a new item is attached in onBindViewHolder, it will
    // update current position MyCustomEditTextListener, reference to which is kept by ViewHolder
    public final class MyCustomKeyEditTextListener implements TextWatcher {
        private int position;

        public final void updatePosition(int position) {
            this.position = position;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            OrderDetails data = orderDetailsList.get(position);
            OrderDetails newData = new OrderDetails(charSequence.toString(), data.getValue());
            orderDetailsList.set(position, newData);
        }

        public void afterTextChanged(Editable editable) {
        }
    }

    public final class MyCustomValueEditTextListener implements TextWatcher {
        private int position;

        public final void updatePosition(int position) {
            this.position = position;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            OrderDetails data = orderDetailsList.get(position);
            OrderDetails newData = new OrderDetails(data.getKey(), charSequence.toString());
            orderDetailsList.set(position, newData);
        }

        public void afterTextChanged(Editable editable) {
        }
    }
}
