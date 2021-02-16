package com.example.managementappproject.dialogs

import android.app.Dialog
import android.content.Context
import android.media.MediaMetadata
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.managementappproject.R
import com.example.managementappproject.adapters.MemberListItemsAdapter
import com.example.managementappproject.models.User
import kotlinx.android.synthetic.main.dialog_list.view.*

abstract class MemberListDialog (
        context: Context,
        private var list: ArrayList<User>,
        private val title: String = ""
): Dialog(context) {

    private var adapter: MemberListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        view.tvTitle.text = title
        // if there are entries to the list
        if (list.size > 0) {
            view.rvList.layoutManager = LinearLayoutManager(context)
            adapter = MemberListItemsAdapter(context, list)
            view.rvList.adapter = adapter

            adapter!!.setOnClickListener( object :
            MemberListItemsAdapter.OnClickListener {
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user, action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: User, action: String)
}