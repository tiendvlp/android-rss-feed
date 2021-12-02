package com.devlogs.rssfeed.screens.bottomsheet_categories.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.bottomsheet_categories.presentable_model.CategoryPresentableModel

class CategoriesRcvAdapter : RecyclerView.Adapter<CategoriesRcvAdapter.ViewHolder> {

    inner class ViewHolder (private val view : View): RecyclerView.ViewHolder (view) {

        private val checkBox : CheckBox = view.findViewById(R.id.chk)
        private val txtTitle : TextView = view.findViewById(R.id.txtTitle)


        internal fun bind (category: CategoryPresentableModel) {
            txtTitle.text = category.title
            if (category.added) {
                checkedCategories.add(category)
                checkBox.isEnabled = true
                checkBox.isChecked = true
                checkBox.isEnabled = false
            } else {
                checkedCategories.remove(category)
                checkBox.isEnabled = true
                checkBox.isChecked = false
            }
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked && buttonView.isEnabled) {
                    checkedCategories.add(category)
                } else {
                    checkedCategories.remove(category)
                }
            }
        }
    }

    val checkedCategories = HashSet<CategoryPresentableModel> ()
    val categories : HashSet<CategoryPresentableModel>

    constructor(categories: HashSet<CategoryPresentableModel>) {
        this.categories = categories
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_categories_add, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories.elementAt(position))
    }

    override fun getItemCount(): Int {
        return categories.size
    }

}