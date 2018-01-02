package com.wajahatkarim3.stacklayoutmanager.demo

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView


/**
 * Created by wajahat.karim on 02/01/2018.
 */
class BookStackAdapter : RecyclerView.Adapter<BookStackAdapter.BookViewHolder>()
{
    var itemsList = ArrayList<BookModel>()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent?.getContext()).inflate(R.layout.book_item_layout, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder?, position: Int) {
        holder?.render(itemsList.get(position));
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    fun setItems(list: ArrayList<BookModel>)
    {
        itemsList.clear()
        itemsList.addAll(list)
        notifyDataSetChanged()
    }

    inner class BookViewHolder : RecyclerView.ViewHolder
    {
        lateinit var imgCover : SimpleDraweeView
        lateinit var txtName : TextView
        lateinit var txtAuthor : TextView

        constructor(itemView: View) : super(itemView)
        {
            imgCover = itemView.findViewById(R.id.imgCover)
            txtName = itemView.findViewById(R.id.txtName)
            txtAuthor = itemView.findViewById(R.id.txtAuthor)
        }

        fun render(bookModel: BookModel)
        {
            imgCover.setImageURI(bookModel.coverUrl)
            txtName.setText(bookModel.bookName)
            txtAuthor.setText("by " + bookModel.authorName)
        }

    }
}