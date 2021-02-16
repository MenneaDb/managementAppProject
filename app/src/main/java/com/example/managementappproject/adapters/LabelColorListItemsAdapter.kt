package com.example.managementappproject.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.managementappproject.R
import kotlinx.android.synthetic.main.item_label_color.view.*

class LabelColorListItemsAdapter(private val context: Context,
                                 private var list: ArrayList<String>,
                                 private var mSelectedColor: String)
                                 : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_label_color, parent, false)) // we inflate this layout to every element of the list
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // get individual item we are looking at( item at the position of the list we get passed)
        val item = list[position]
        if (holder is MyViewHolder){
            // we set the background color based on the parseColor(item-> individual item of our list and we parse it into a color) ex #FFFFFF passed as String and it's parse to WHITE
            holder.itemView.view_main.setBackgroundColor(Color.parseColor(item))
            // set visibility of iv_selected_color depending on the selected color
            if (item == mSelectedColor){
                holder.itemView.iv_selected_color.visibility = View.VISIBLE
            } else {
                holder.itemView.iv_selected_color.visibility = View.GONE
            }

            // make clickable each color choice in order to set one of them for the selected card
            holder.itemView.setOnClickListener {
                if (onItemClickListener != null){
                    onItemClickListener!!.onClick(position, item)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

    // to be able to click on the elements(we need to know the position and which color the user press on)
    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }
}