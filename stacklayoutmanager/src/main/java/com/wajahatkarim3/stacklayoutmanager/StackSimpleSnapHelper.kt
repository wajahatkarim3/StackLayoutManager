package com.wajahatkarim3.stacklayoutmanager

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SnapHelper
import android.view.View

/**
 * Created by wajahat.karim on 02/01/2018.
 */
class StackSimpleSnapHelper : SnapHelper() {

    private var mDirection: Int = 0

    override fun calculateDistanceToFinalSnap(
            layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {

        if (layoutManager is StackLayoutManager) {
            val out = IntArray(2)
            if (layoutManager.canScrollHorizontally()) {
                out[0] = (layoutManager as StackLayoutManager).calculateDistanceToPosition(
                        layoutManager.getPosition(targetView))
                out[1] = 0
            } else {
                out[0] = 0
                out[1] = (layoutManager as StackLayoutManager).calculateDistanceToPosition(
                        layoutManager.getPosition(targetView))
            }
            return out
        }
        return null
    }

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int,
                                        velocityY: Int): Int {
        if (layoutManager.canScrollHorizontally()) {
            mDirection = velocityX
        } else {
            mDirection = velocityY
        }
        return RecyclerView.NO_POSITION
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager is StackLayoutManager) {
            val pos = (layoutManager as StackLayoutManager).getFixedScrollPosition(
                    mDirection, if (mDirection != 0) 0.8f else 0.5f)
            mDirection = 0
            if (pos != RecyclerView.NO_POSITION) {
                return layoutManager.findViewByPosition(pos)
            }
        }
        return null
    }
}