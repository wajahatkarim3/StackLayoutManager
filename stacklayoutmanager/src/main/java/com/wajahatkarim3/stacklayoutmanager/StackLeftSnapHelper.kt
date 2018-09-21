package com.wajahatkarim3.stacklayoutmanager

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SnapHelper
import android.view.View

/**
 * @author Wajahat Karim
 */
class StackLeftSnapHelper : SnapHelper() {

    private var direction: Int = 0

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray?
    {
        if (layoutManager is StackLeftLayoutManager)
        {
            var out = intArrayOf(2)
            if (layoutManager.canScrollHorizontally())
            {
                out[0] = layoutManager.calculateDistanceToPosition(layoutManager.getPosition(targetView))
                out[1] = 0
            }
            else
            {
                out[0] = 0
                out[1] = layoutManager.calculateDistanceToPosition(layoutManager.getPosition(targetView))
            }
            return out
        }
        return null
    }

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager?, velocityX: Int, velocityY: Int): Int
    {
        if (layoutManager?.canScrollHorizontally() == true)
            direction = velocityX
        else
            direction = velocityY
        return RecyclerView.NO_POSITION
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View?
    {
        if (layoutManager is StackLeftLayoutManager)
        {
            var pos = layoutManager.getFixedScrollPosition(direction, if (direction != 0) 0.8f else 0.5f )
            direction = 0
            if (pos != RecyclerView.NO_POSITION)
                return layoutManager.findViewByPosition(pos)
        }
        return null
    }
}