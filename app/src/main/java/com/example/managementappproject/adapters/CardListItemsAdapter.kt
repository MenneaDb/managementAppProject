package com.example.managementappproject.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.managementappproject.R
import com.example.managementappproject.models.Card
import kotlinx.android.synthetic.main.item_card.view.*

open class CardListItemsAdapter (
        private val context: Context,
        private var list: ArrayList<Card>
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        )

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            // set the tick to the selected color
                if (model.labelColor.isNotEmpty()){
                    holder.itemView.view_label_color.visibility = View.VISIBLE
                    holder.itemView.view_label_color.setBackgroundColor(Color.parseColor(model.labelColor)) // set as background color the labelColor
                } else {
                    // in case labelColor is empty
                    holder.itemView.view_label_color.visibility = View.GONE
                }
            // we set the name of the card depending of which the user will set for it
            holder.itemView.tv_card_name.text = model.name
            // give to every single card an onClick event
            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    onClickListener!!.onClick(position) // the position came from the onBindViewHolder method itself
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(cardPosition: Int)
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}