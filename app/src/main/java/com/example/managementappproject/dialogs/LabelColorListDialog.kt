package com.example.managementappproject.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.managementappproject.R
import com.example.managementappproject.adapters.LabelColorListItemsAdapter
import kotlinx.android.synthetic.main.dialog_list.view.*

abstract class LabelColorListDialog(
        context: Context,
        private var list: ArrayList<String>,
        private val title: String = "",
        private var mSelectedColor: String = ""
): Dialog(context) { // inherits from Dialog class (pass context to it)

    private var adapter: LabelColorListItemsAdapter? = null // empty adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // new view. allow us to inflate our own layout that we created
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true) // if the user touch outside of the dialog it will be close
        setCancelable(true)
        setUpRecyclerView(view) // we call the method and pass the view we prepared before

    }

    // method to take care of the recyclerView
    private fun setUpRecyclerView(view: View){
        // design the view
        view.tvTitle.text = title // title that is passed to us ( set as param of the class)
        view.rvList.layoutManager = LinearLayoutManager(context)
        // set adapter
        adapter = LabelColorListItemsAdapter(context, list, mSelectedColor)
        // set adapter to rvList
        view.rvList.adapter = adapter
        // each adapter need to have an onClickListener
        adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener{
                    // once we click the item we need to select it
                    override fun onClick(position: Int, color: String) {
                        dismiss() // dismiss this dialog
                        onItemSelected(color) // when user click on a color we pass the item selected with the color as string
                    }
                }
    }
    /* declared as abstract(it is possible because the class itself is abstract because later we can implement it implement
       what should happen when user click an Item (we need to pass the color of the item we selected). protected it's becuase
       we can only use it within the project */
    protected abstract fun onItemSelected(color: String)
}