package com.wajahatkarim3.stacklayoutmanager

import android.support.v7.widget.RecyclerView

/**
 * @author Wajahat Karim
 */
class StackLeftLayoutManager(private var ratio: Float, private var scale: Float) : RecyclerView.LayoutManager() {

    private var containsChild = false
    private var itemWidth = 0
    private var itemHeight = 0
    private var scrollOffset = Integer.MAX_VALUE
    private var itemsNum = 0
    private var snapHelper = StackLeftSnapHelper()

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
            RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {

    }

    protected fun getFixedScrollPosition(direction: Int, value: Float) : Int
    {
        if (containsChild)
        {
            if (scrollOffset % itemWidth == 0)
                return RecyclerView.NO_POSITION
            var pos = scrollOffset / itemWidth
            var layoutPos: Int = if (direction > 0) (pos + value).toInt() else (pos + (1 - value)).toInt()
            return getAdapterPosition(layoutPos-1)
        }
        return RecyclerView.NO_POSITION
    }

    private fun getAdapterPosition(position: Int): Int {
        return 0
    }

}