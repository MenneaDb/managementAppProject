package com.example.managementappproject.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.managementappproject.R
import com.example.managementappproject.activities.TaskListActivity
import com.example.managementappproject.models.Card
import com.example.managementappproject.models.SelectedMembers
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

            // we use the global var from TaskListActivity
            if ((context as TaskListActivity).mAssignedMemberDetailList.size > 0){
                // instance of selectedMember
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

                // we want to add to the selectedMembersList all the cases that match with these conditions
                for (i in context.mAssignedMemberDetailList.indices){
                    for (j in model.assignedTo){
                        if (context.mAssignedMemberDetailList[i].id==j){
                            val selectedMembers = SelectedMembers(
                                    //pass the objects that the selectedMembers need
                                    context.mAssignedMemberDetailList[i].id,
                                    context.mAssignedMemberDetailList[i].image
                            )
                            // add all of it in selectedMembersList
                            selectedMembersList.add(selectedMembers)
                        }
                    }
                }
                // check if the list is not empty
                if (selectedMembersList.size > 0){
                    // we want to hide the recyclerView of the membersList displayed on the Board if the creator is the only member
                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                        holder.itemView.rv_card_selected_members_list.visibility = View.GONE
                    } else {
                        // otherwise we show the view with the members assignedTo the task
                        holder.itemView.rv_card_selected_members_list.visibility = View.VISIBLE

                        // we also want to set max 4 members displayed given the size of the Card
                        holder.itemView.rv_card_selected_members_list.layoutManager = GridLayoutManager(context, 4)
                        // we need an adapter for the RV
                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                        // we can now add the adapter to the RV
                        holder.itemView.rv_card_selected_members_list.adapter = adapter
                        // we set an onClickListener for the object of the RV in case the list.size is > 0
                        adapter.setOnClickListener (
                            object : CardMemberListItemsAdapter.OnClickListener{
                                override fun onClick() {
                                    if (onClickListener != null){
                                        onClickListener!!.onClick(position)
                                    }
                                }
                        })
                    }
                } else {
                    // if the list.size is not > 0
                    holder.itemView.rv_card_selected_members_list.visibility = View.GONE
                }
            }

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