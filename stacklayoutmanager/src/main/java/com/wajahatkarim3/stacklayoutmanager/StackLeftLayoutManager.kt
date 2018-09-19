package com.wajahatkarim3.stacklayoutmanager

import android.support.v7.widget.RecyclerView
import android.view.View

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

        var itemModelsList = arrayListOf<ItemModel>()
        var j = 1
        var remainingWidth = exactWidth - itemWidth

        for (i in bottomItemPos-1 downTo 0 )
        {
            var maxOffset = (width - paddingLeft - paddingRight - itemWidth) / 2 * Math.pow(scale.toDouble(), j.toDouble())
            var start = (remainingWidth - offsetPercent * maxOffset).toInt()
            var model = ItemModel(
                    top = start,
                    scale = (Math.pow(scale.toDouble(), j-1.toDouble()) * (1 - offsetPercent * (1 - scale))).toFloat(),
                    offset = offsetPercent.toFloat(),
                    percent = start / exactWidth.toFloat() )
            itemModelsList.add(0, model)

            remainingWidth -= maxOffset.toInt()
            if (remainingWidth <= 0)
            {
                model.top = (remainingWidth + maxOffset).toInt()
                model.offset = 0f
                model.percent = (model.top / exactWidth).toFloat()
                model.scale = Math.pow(scale.toDouble(), (j-1).toDouble()).toFloat()
                break
            }

            j++
        }

        if (bottomItemPos < itemCount)
        {
            val start = exactWidth - itemVisibleSize
            itemModelsList.add(ItemModel(
                    top = start,
                    scale = 1f,
                    offset = (itemVisibleSize / itemWidth).toFloat(),
                    percent = (start / exactWidth).toFloat()
            ).setBottom())
        }
        else
        {
            bottomItemPos -= 1
        }

        var modelsCount = itemModelsList.size

        val startPos = bottomItemPos - (modelsCount - 1)
        val endPos = bottomItemPos
        for (i in childCount-1 downTo 0)
        {
            var childView = getChildAt(i)
            var pos = itemCount - 1 - getPosition(childView)
            if (pos > endPos || pos < startPos)
                removeAndRecycleView(childView, recycler)
        }
        detachAndScrapAttachedViews(recycler)

        for (i in 0 until modelsCount)
            fillChildItem(recycler?.getViewForPosition(getAdapterPosition(startPos+i)), itemModelsList[i])
    }

    private fun fillChildItem(viewForPosition: View?, itemModel: ItemModel) {

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