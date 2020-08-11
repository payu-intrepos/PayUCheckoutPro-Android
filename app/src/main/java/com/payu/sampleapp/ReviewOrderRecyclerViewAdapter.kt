package com.payu.sampleapp

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.payu.base.models.OrderDetails
import java.util.*

class ReviewOrderRecyclerViewAdapter :
    RecyclerView.Adapter<ReviewOrderRecyclerViewAdapter.ViewHolder>() {
    private val orderDetailsList: ArrayList<OrderDetails>?

    init {
        orderDetailsList = ArrayList<OrderDetails>()
        orderDetailsList.add(OrderDetails("Milk", "1"))
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val row: View =
            layoutInflater.inflate(R.layout.review_order_row_layout, null)
        return ViewHolder(
            row,
            MyCustomKeyEditTextListener(),
            MyCustomValueEditTextListener()
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        // update MyCustomEditTextListener every time we bind a new item
        // so that it knows what item in mDataset to update
        holder.myCustomKeyTextListener.updatePosition(holder.adapterPosition)
        holder.mEditTextKey.setText(orderDetailsList!![holder.adapterPosition].key)
        holder.myCustomValueTextListener.updatePosition(holder.adapterPosition)
        holder.mEditTextValue.setText(
            orderDetailsList[holder.adapterPosition].value
        )

        if (orderDetailsList.size > 1) holder.ivDeleteOrderItem.visibility = View.VISIBLE else holder.ivDeleteOrderItem.visibility = View.GONE
    }

    fun addRow() {
        orderDetailsList!!.add(OrderDetails("", ""))
        notifyItemInserted(orderDetailsList.size - 1)
    }

    fun getOrderDetailsList() = orderDetailsList

    override fun getItemCount(): Int {
        return if (orderDetailsList == null || orderDetailsList.isEmpty()) 1 else orderDetailsList.size
    }

    inner class ViewHolder(
        v: View,
        myCustomKeyTextListener: MyCustomKeyEditTextListener,
        myCustomValueTextListener: MyCustomValueEditTextListener
    ) : RecyclerView.ViewHolder(v) {
        var mEditTextKey: EditText
        var mEditTextValue: EditText
        val ivDeleteOrderItem: ImageView
        var myCustomKeyTextListener: MyCustomKeyEditTextListener
        var myCustomValueTextListener: MyCustomValueEditTextListener

        init {
            mEditTextKey =
                v.findViewById<View>(R.id.etReviewOrderKey) as EditText
            mEditTextValue =
                v.findViewById<View>(R.id.etReviewOrderValue) as EditText
            ivDeleteOrderItem = v.findViewById(R.id.ivDeleteOrderItem)
            ivDeleteOrderItem.setOnClickListener {
                orderDetailsList?.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
            }
            this.myCustomKeyTextListener = myCustomKeyTextListener
            this.myCustomValueTextListener = myCustomValueTextListener
            mEditTextKey.addTextChangedListener(myCustomKeyTextListener)
            mEditTextValue.addTextChangedListener(myCustomValueTextListener)


        }
    }

    // we make TextWatcher to be aware of the position it currently works with
    // this way, once a new item is attached in onBindViewHolder, it will
    // update current position MyCustomEditTextListener, reference to which is kept by ViewHolder
    inner class MyCustomKeyEditTextListener : TextWatcher {
        private var position = 0
        fun updatePosition(position: Int) {
            this.position = position
        }

        override fun beforeTextChanged(
            charSequence: CharSequence,
            i: Int,
            i2: Int,
            i3: Int
        ) {
            // no op
        }

        override fun onTextChanged(
            charSequence: CharSequence,
            i: Int,
            i2: Int,
            i3: Int
        ) {
            val data: OrderDetails = orderDetailsList!![position]
            val newData = OrderDetails(charSequence.toString(), data.value!!)
            orderDetailsList[position] = newData
        }

        override fun afterTextChanged(editable: Editable) {
            // no op
        }
    }

    inner class MyCustomValueEditTextListener : TextWatcher {
        private var position = 0
        fun updatePosition(position: Int) {
            this.position = position
        }

        override fun beforeTextChanged(
            charSequence: CharSequence,
            i: Int,
            i2: Int,
            i3: Int
        ) {
            // no op
        }

        override fun onTextChanged(
            charSequence: CharSequence,
            i: Int,
            i2: Int,
            i3: Int
        ) {
            val data: OrderDetails = orderDetailsList!![position]
            val newData = OrderDetails(data.key!!, charSequence.toString())
            orderDetailsList[position] = newData
        }

        override fun afterTextChanged(editable: Editable) {
            // no op
        }
    }
}