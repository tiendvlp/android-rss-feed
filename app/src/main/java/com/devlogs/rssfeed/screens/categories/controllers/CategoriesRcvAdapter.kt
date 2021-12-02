package com.devlogs.rssfeed.screens.categories.controllers

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.devlogs.rssfeed.R
import com.devlogs.rssfeed.screens.categories.presentable_model.CategoryPresentableModel
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter
import org.sufficientlysecure.htmltextview.HtmlTextView

class CategoriesRcvAdapter : RecyclerView.Adapter<CategoriesRcvAdapter.ViewHolder> {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val txtTitle: TextView = view.findViewById(R.id.txtTitle)
        private val img1 : ImageView = view.findViewById(R.id.img1)
        private val img2 : ImageView = view.findViewById(R.id.img2)
        private val img3 : ImageView = view.findViewById(R.id.img3)

        private val imgHtml1 : WebView = view.findViewById(R.id.imgHtml1)
        private val imgHtml2 : WebView = view.findViewById(R.id.imgHtml2)
        private val imgHtml3 : WebView = view.findViewById(R.id.imgHtml3)
        internal fun bind (category: CategoryPresentableModel) {
            txtTitle.text = category.title
            img1.clipToOutline = true
            img2.clipToOutline = true
            img3.clipToOutline = true
//            imgHtml1.clipToOutline = true
            imgHtml2.clipToOutline = true
            imgHtml3.clipToOutline = true
            if (category.feedImgs.size >= 3 ) {
                Log.d("CategoriesViewHolder", "Binding: ${category.title}")
            }

            if (category.feedImgs.size >= 1 && !category.feedImgs[0].isNullOrBlank()) {
               loadUrlInToImg(category.feedImgs[0]!!, img1, imgHtml1)
            }
            else {
                imgHtml1.visibility = View.GONE
                img1.visibility = View.VISIBLE
                img1.setImageResource(R.drawable.ic_off)
            }
            if (category.feedImgs.size >= 2 && !category.feedImgs[1].isNullOrBlank()) {
                loadUrlInToImg(category.feedImgs[1]!!, img2, imgHtml2)

            } else {
                imgHtml2.visibility = View.GONE
                img2.visibility = View.VISIBLE
                img2.setImageResource(R.drawable.ic_off)
            }
            if (category.feedImgs.size >= 3 && !category.feedImgs[2].isNullOrBlank()) {
                Log.d("ViewImg", "Img 3 load: ${category.feedImgs[2]}")
                loadUrlInToImg(category.feedImgs[2]!!, img3, imgHtml3)
            } else {
                Log.d("ViewImg", "Img 3 load: null")
                imgHtml3.visibility = View.GONE
                img3.visibility = View.VISIBLE
                img3.setImageResource(R.drawable.ic_off)
            }

            view.setOnClickListener {
                onItemClicked?.invoke(category)
            }

        }

        private fun loadUrlInToImg (url: String, img: ImageView, imgHtml: WebView) {
            img.visibility = View.VISIBLE
            imgHtml.visibility = View.GONE
            Log.d("LoadUrlIntoImg", "Started + $url")
            Glide
                .with(itemView.context)
                .load(url)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("LoadUrlIntoImg", "Failed: + $url")
                        img.visibility = View.GONE
                        imgHtml.visibility = View.VISIBLE
                        imgHtml.setBackgroundColor(Color.TRANSPARENT)
                        imgHtml.loadData( """<body style="margin: 0px; padding: 0px; width:100%; height: 100%;">
                            |                   <img style="width: 100%;height: 100%; margin: 0px; padding: 0px" src="${url}"/>
                            |                </body>
                            |             """.trimMargin(), "text/html; charset=utf-8", "UTF-8");
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                })
                .into(img)
        }

    }

    var onItemClicked : ((CategoryPresentableModel) -> Unit)? = null
    private val categories : HashSet<CategoryPresentableModel>

    constructor(categories: HashSet<CategoryPresentableModel>) {
        this.categories = categories
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_categories_full, parent, false)
        return ViewHolder (itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories.elementAt(position))
    }

    override fun getItemCount(): Int {
        return categories.size
    }

}