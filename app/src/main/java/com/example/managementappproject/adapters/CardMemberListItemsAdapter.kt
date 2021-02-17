package com.example.managementappproject.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.managementappproject.R
import com.example.managementappproject.models.SelectedMembers
import kotlinx.android.synthetic.main.item_card_selected_member.view.*

// open class that requires context and list
open class CardMemberListItemsAdapter
    (private val context: Context,
     private val list:ArrayList<SelectedMembers>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // how it's going to look like by inflating the layout we want. (parent = ViewGroup)
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_card_selected_member, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]  // model = user

        if (holder is MyViewHolder) {
            // if we don't have people on the list we only want the + button to be visible in the RV
            if (position == list.size - 1){
                holder.itemView.iv_add_member.visibility = View.VISIBLE
                holder.itemView.iv_selected_member_image.visibility = View.GONE
            } else {
                // if we do have people on the list, we want the other way around
                holder.itemView.iv_add_member.visibility = View.GONE
                holder.itemView.iv_selected_member_image.visibility = View.VISIBLE

                // we want to use the specific image of the user that we have added to the card
                Glide
                        .with(context)
                        .load(model.image)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(holder.itemView.iv_selected_member_image)
            }

            // we also want to give them an onClickListener
            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick() // if it's not null, trigger the onClick() method
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

    // we want to be able to click on elements
   fun setOnClickListener(onClickListener: OnClickListener) {
       this.onClickListener = onClickListener
   }

    interface OnClickListener {
        fun onClick()
    }
}