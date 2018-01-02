package com.wajahatkarim3.stacklayoutmanager.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import com.wajahatkarim3.stacklayoutmanager.StackLayoutManager

class MainActivity : AppCompatActivity() {

    lateinit var recyclerRecommended : RecyclerView
    var recommededAdapter : BookStackAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setDummyData()
    }

    fun initViews()
    {
        // Recommended Books RecyclerView
        recyclerRecommended = findViewById(R.id.recyclerRecommended)
        recommededAdapter = BookStackAdapter()

        var layoutManager = StackLayoutManager(1f, 1.85f, StackLayoutManager.HORIZONTAL)
        layoutManager.setChildDecorateHelper(StackLayoutManager.DefaultChildDecorateHelper(resources.getDimension(R.dimen.item_max_elevation)))
        layoutManager.setChildPeekSize( (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                30f, getResources().getDisplayMetrics())).toInt())
        layoutManager.setMaxItemLayoutCount(5)
        recyclerRecommended.layoutManager = layoutManager

        recyclerRecommended.adapter = recommededAdapter
        recommededAdapter?.notifyDataSetChanged()
    }

    fun setDummyData()
    {
        // Recommended Books
        var recList = ArrayList<BookModel>()
        recList.add(BookModel("Still Here", "Lara Vapnyar", "https://www.publishersweekly.com/images/cached/ARTICLE_PHOTO/photo/000/000/041/41490-v1-600x.JPG"))
        recList.add(BookModel("Still Here", "Lara Vapnyar", "https://www.publishersweekly.com/images/cached/ARTICLE_PHOTO/photo/000/000/041/41490-v1-600x.JPG"))
        recList.add(BookModel("Still Here", "Lara Vapnyar", "https://www.publishersweekly.com/images/cached/ARTICLE_PHOTO/photo/000/000/041/41490-v1-600x.JPG"))
        recList.add(BookModel("Still Here", "Lara Vapnyar", "https://www.publishersweekly.com/images/cached/ARTICLE_PHOTO/photo/000/000/041/41490-v1-600x.JPG"))
        recList.add(BookModel("Still Here", "Lara Vapnyar", "https://www.publishersweekly.com/images/cached/ARTICLE_PHOTO/photo/000/000/041/41490-v1-600x.JPG"))
        recList.add(BookModel("Still Here", "Lara Vapnyar", "https://www.publishersweekly.com/images/cached/ARTICLE_PHOTO/photo/000/000/041/41490-v1-600x.JPG"))
        recList.add(BookModel("Still Here", "Lara Vapnyar", "https://www.publishersweekly.com/images/cached/ARTICLE_PHOTO/photo/000/000/041/41490-v1-600x.JPG"))
        recList.add(BookModel("Still Here", "Lara Vapnyar", "https://www.publishersweekly.com/images/cached/ARTICLE_PHOTO/photo/000/000/041/41490-v1-600x.JPG"))
        recommededAdapter?.setItems(recList)

    }
}
