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
    private var snapHelper = StackLeftSnapHelper()

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
            RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?)
    {
        if (state?.itemCount == 0 || state?.isPreLayout == true) return

        removeAndRecycleAllViews(recycler)

        if (!containsChild)
        {
            itemHeight = height - paddingTop - paddingBottom
            itemWidth = (itemHeight / ratio).toInt()
            containsChild = true
        }

        scrollOffset = calculateScrollOffset(scrollOffset)
        fillAllItems(recycler)
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

    protected fun fillAllItems(recycler: RecyclerView.Recycler?)
    {
        var bottomItemPos = Math.floor((scrollOffset / itemWidth).toDouble()).toInt()
        var itemVisibleSize = scrollOffset % itemWidth
        val offsetPercent = itemVisibleSize / itemWidth
        val exactWidth = width - paddingLeft - paddingRight


    }

    protected fun calculateScrollOffset(offset: Int) : Int =
            Math.min(Math.max(itemWidth, offset), itemCount * itemWidth)

    private fun getAdapterPosition(position: Int): Int {
        return 0
    }

    private data class ItemModel (
            var scale: Float = 0f,
            var percent: Float = 0f,
            var offset: Float = 0f,
            var top: Int = 0,
            var isBottom: Boolean = false )
    {
        fun setBottom() : ItemModel {
            isBottom = true
            return this
        }
    }

}