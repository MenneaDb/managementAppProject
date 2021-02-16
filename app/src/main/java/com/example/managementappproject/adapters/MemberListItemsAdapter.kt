package com.example.managementappproject.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.managementappproject.R
import com.example.managementappproject.models.User
import com.example.managementappproject.utils.Constants
import kotlinx.android.synthetic.main.item_member.view.*


open class MemberListItemsAdapter(private val context: Context, private var list: ArrayList<User>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // default LayoutInflater where we inflate our item_member.xml
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_member, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // we create a model out of the list[position](list that is given to the adapter from the attributes)
        val model = list[position]
        // if the holder is the one we declare inside this class we use Glide to display the right image of the user
        if (holder is MyViewHolder){

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.itemView.iv_member_image)

            // we assign the name and the email from the member
            holder.itemView.tv_member_name.text = model.name
            holder.itemView.tv_member_email.text = model.email

            // model = user
            if (model.selected){
                holder.itemView.iv_selected_member.visibility = View.VISIBLE
            } else {
                holder.itemView.iv_selected_member.visibility = View.GONE
            }
            holder.itemView.setOnClickListener{
                if (onClickListener != null) {
                    if (model.selected) {
                        onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                    } else {
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
       return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener // we use the onClickListener we created here  to use the onClick method

    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

    interface OnClickListener {
        fun onClick(position: Int, user: User, action: String)
    }


}