package com.wajahatkarim3.stacklayoutmanager

import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import com.wajahatkarim3.stacklayoutmanager.StackLayoutManager.SavedState
import java.util.ArrayList

/**
 * Created by wajahat.karim on 02/01/2018.
 */
open class StackLayoutManager : RecyclerView.LayoutManager, RecyclerView.SmoothScroller.ScrollVectorProvider {

    companion object {
        val VERTICAL = 1
        val HORIZONTAL = 0
    }

    private val INVALIDATE_SCROLL_OFFSET = Integer.MAX_VALUE
    private val DEFAULT_CHILD_LAYOUT_OFFSET = 0.2f
    val UNLIMITED = 0
    private var mCheckedChildSize: Boolean = false
    private var mChildSize: IntArray = IntArray(0)
    private var mChildPeekSize: Int = 0
    private var mChildPeekSizeInput: Int = 0
    private var mReverse: Boolean = false
    private var mScrollOffset = INVALIDATE_SCROLL_OFFSET
    private var mItemHeightWidthRatio: Float = 0.toFloat()
    private var mScale: Float = 0.toFloat()
    private var mChildCount: Int = 0
    private var mVanishOffset = 0f
    private var mInterpolator: Interpolator? = null
    private var mOrientation: Int = 0
    private var mDecorateHelper: ChildDecorateHelper? = null
    private var mMaxItemLayoutCount: Int = 0

    constructor(itemHeightWidthRatio: Float) : this(itemHeightWidthRatio, 0f, StackLayoutManager.VERTICAL)

    constructor(itemHeightWidthRatio: Float, scale: Float, orientation: Int)
    {
        this.mItemHeightWidthRatio = itemHeightWidthRatio
        this.mOrientation = orientation
        this.mScale = scale
        this.mChildSize = IntArray(2)
        this.mInterpolator = DecelerateInterpolator() as Interpolator?
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(mChildSize[0], mChildSize[1])
    }

    fun setChildDecorateHelper(layoutHelper: ChildDecorateHelper): StackLayoutManager {
        mDecorateHelper = layoutHelper
        return this
    }

    fun setMaxItemLayoutCount(count: Int) {
        mMaxItemLayoutCount = Math.max(2, count)
        if (childCount > 0) {
            requestLayout()
        }
    }

    fun setVanishOffset(offset: Float) {
        mVanishOffset = offset
        if (childCount > 0) {
            requestLayout()
        }
    }

    fun setChildPeekSize(childPeekSize: Int) {
        mChildPeekSizeInput = childPeekSize
        mCheckedChildSize = false
        if (childCount > 0) {
            requestLayout()
        }
    }

    fun setItemHeightWidthRatio(itemHeightWidthRatio: Float) {
        mItemHeightWidthRatio = itemHeightWidthRatio
        mCheckedChildSize = false
        if (childCount > 0) {
            requestLayout()
        }
    }

    fun setReverse(reverse: Boolean) {
        if (mReverse != reverse) {
            mReverse = reverse
            if (childCount > 0) {
                requestLayout()
            }
        }
    }

    fun isReverse(): Boolean {
        return mReverse
    }

    fun getFixedScrollPosition(direction: Int, fixValue: Float): Int {
        if (mCheckedChildSize) {
            if (mScrollOffset % mChildSize[mOrientation] == 0) {
                return RecyclerView.NO_POSITION
            }
            val position = mScrollOffset * 1.0f / mChildSize[mOrientation]
            return convert2AdapterPosition((if (direction > 0) position + fixValue else position + (1 - fixValue)).toInt() - 1)
        }
        return RecyclerView.NO_POSITION
    }

    override fun onMeasure(recycler: RecyclerView.Recycler?, state: RecyclerView.State?, widthSpec: Int, heightSpec: Int) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)
        mCheckedChildSize = false
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        if (state!!.itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }
        if (!mCheckedChildSize) {
            if (mOrientation == VERTICAL) {
                mChildSize[0] = getHorizontalSpace()
                mChildSize[1] = (mItemHeightWidthRatio * mChildSize[0]).toInt()
            } else {
                mChildSize[1] = getVerticalSpace()
                mChildSize[0] = (mChildSize[1] / mItemHeightWidthRatio).toInt()
            }
            mChildPeekSize = if (mChildPeekSizeInput == 0)
                (mChildSize[mOrientation] * DEFAULT_CHILD_LAYOUT_OFFSET).toInt()
            else
                mChildPeekSizeInput
            mCheckedChildSize = true
        }
        val itemCount = itemCount
        if (mReverse) {
            mScrollOffset += (itemCount - mChildCount) * mChildSize[mOrientation]
        }
        mChildCount = itemCount
        mScrollOffset = makeScrollOffsetWithinRange(mScrollOffset)
        fill(recycler)
    }

    fun fill(recycler: RecyclerView.Recycler) {
        if (mChildSize[mOrientation] == 0) return
        var bottomItemPosition = Math.floor((mScrollOffset / mChildSize[mOrientation]).toDouble()).toInt()//>=1
        val bottomItemVisibleSize = mScrollOffset % mChildSize[mOrientation]
        val offsetPercent:Float = mInterpolator?.getInterpolation(
                bottomItemVisibleSize * 1.0f / mChildSize[mOrientation]) ?: 0f//[0,1)
        val space = if (mOrientation == VERTICAL) getVerticalSpace() else getHorizontalSpace()

        val layoutInfos = ArrayList<ItemLayoutInfo>()
        run {
            var i = bottomItemPosition - 1
            var j = 1
            var remainSpace = space - mChildSize[mOrientation]
            while (i >= 0) {
                val maxOffset = mChildPeekSize * Math.pow(mScale.toDouble(), j.toDouble())
                val start = (remainSpace - offsetPercent * maxOffset).toInt()
                val info = ItemLayoutInfo(start,
                        (Math.pow(mScale.toDouble(), (j - 1).toDouble()) * (1 - offsetPercent * (1 - mScale))).toFloat(),
                        offsetPercent,
                        start * 1.0f / space
                )
                layoutInfos.add(0, info)

                if (mMaxItemLayoutCount != UNLIMITED && j == mMaxItemLayoutCount - 1) {
                    if (offsetPercent != 0f) {
                        info.start = remainSpace
                        info.positionOffsetPercent = 0f
                        info.layoutPercent = (remainSpace / space).toFloat()
                        info.scaleXY = Math.pow(mScale.toDouble(), (j - 1).toDouble()).toFloat()
                    }
                    break
                }
                remainSpace = remainSpace - maxOffset.toInt()
                if (remainSpace <= 0) {
                    info.start = (remainSpace + maxOffset).toInt()
                    info.positionOffsetPercent = 0f
                    info.layoutPercent = (info.start / space).toFloat()
                    info.scaleXY = Math.pow(mScale.toDouble(), (j - 1).toDouble()).toFloat()
                    break
                }
                i--
                j++
            }
        }

        if (bottomItemPosition < mChildCount) {
            val start = space - bottomItemVisibleSize
            layoutInfos.add(ItemLayoutInfo(start, 1.0f,
                    bottomItemVisibleSize * 1.0f / mChildSize[mOrientation], start * 1.0f / space).setIsBottom())
        } else {
            bottomItemPosition -= 1
        }

        val layoutCount = layoutInfos.size

        val startPos = bottomItemPosition - (layoutCount - 1)
        val endPos = bottomItemPosition
        val childCount = childCount
        for (i in childCount - 1 downTo 0) {
            val childView = getChildAt(i)
            val pos = convert2LayoutPosition(getPosition(childView))
            if (pos > endPos || pos < startPos) {
                removeAndRecycleView(childView, recycler)
            }
        }
        detachAndScrapAttachedViews(recycler)

        for (i in 0 until layoutCount) {
            fillChild(recycler.getViewForPosition(convert2AdapterPosition(startPos + i)), layoutInfos[i])
        }
    }

    private fun fillChild(view: View, layoutInfo: ItemLayoutInfo) {
        addView(view)
        measureChildWithExactlySize(view)
        val scaleFix = (mChildSize[mOrientation] * (1 - layoutInfo.scaleXY) / 2).toInt()
        val gap = (if (mOrientation == VERTICAL) getHorizontalSpace() else getVerticalSpace()) - mChildSize[(mOrientation + 1) % 2] * layoutInfo.scaleXY

        if (mOrientation == VERTICAL) {
            val left = (paddingLeft + gap.toDouble() * 0.5 * mVanishOffset.toDouble()).toInt()
            layoutDecoratedWithMargins(view, left, layoutInfo.start - scaleFix, left + mChildSize[0], layoutInfo.start + mChildSize[1] - scaleFix)
        } else {
            val top = (paddingTop + gap.toDouble() * 0.5 * mVanishOffset.toDouble()).toInt()
            layoutDecoratedWithMargins(view, layoutInfo.start - scaleFix, top, layoutInfo.start + mChildSize[0] - scaleFix, top + mChildSize[1])
        }
        ViewCompat.setScaleX(view, layoutInfo.scaleXY)
        ViewCompat.setScaleY(view, layoutInfo.scaleXY)
        if (mDecorateHelper != null) {
            mDecorateHelper?.decorateChild(view, layoutInfo.positionOffsetPercent, layoutInfo.layoutPercent, layoutInfo.isBottom)
        }
    }

    private fun measureChildWithExactlySize(child: View) {
        val lp = child.layoutParams as RecyclerView.LayoutParams
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
                mChildSize[0] - lp.leftMargin - lp.rightMargin, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(
                mChildSize[1] - lp.topMargin - lp.bottomMargin, View.MeasureSpec.EXACTLY)
        child.measure(widthSpec, heightSpec)
    }

    private fun makeScrollOffsetWithinRange(scrollOffset: Int): Int {
        return Math.min(Math.max(mChildSize[mOrientation], scrollOffset), mChildCount * mChildSize[mOrientation])
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State?): Int {
        val pendingScrollOffset = mScrollOffset + dy
        mScrollOffset = makeScrollOffsetWithinRange(pendingScrollOffset)
        fill(recycler)
        return mScrollOffset - pendingScrollOffset + dy
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State?): Int {
        val pendingScrollOffset = mScrollOffset + dx
        mScrollOffset = makeScrollOffsetWithinRange(pendingScrollOffset)
        fill(recycler)
        return mScrollOffset - pendingScrollOffset + dx
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
        val linearSmoothScroller = object : LinearSmoothScroller(recyclerView!!.context) {
            override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
                return if (mOrientation == VERTICAL) {
                    -calculateDistanceToPosition(getPosition(view))
                } else 0
            }

            override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
                return if (mOrientation == HORIZONTAL) {
                    -calculateDistanceToPosition(getPosition(view))
                } else 0
            }
        }
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    fun calculateDistanceToPosition(targetPos: Int): Int {
        val pendingScrollOffset = mChildSize[mOrientation] * (convert2LayoutPosition(targetPos) + 1)
        return pendingScrollOffset - mScrollOffset
    }

    override fun scrollToPosition(position: Int) {
        if (position > 0 && position < mChildCount) {
            mScrollOffset = mChildSize[mOrientation] * (convert2LayoutPosition(position) + 1)
            requestLayout()
        }
    }

    override fun canScrollVertically(): Boolean {
        return mOrientation == VERTICAL
    }

    override fun canScrollHorizontally(): Boolean {
        return mOrientation == HORIZONTAL
    }

    fun convert2AdapterPosition(layoutPosition: Int): Int {
        return if (mReverse) mChildCount - 1 - layoutPosition else layoutPosition
    }

    fun convert2LayoutPosition(adapterPostion: Int): Int {
        return if (mReverse) mChildCount - 1 - adapterPostion else adapterPostion
    }

    fun getVerticalSpace(): Int {
        return height - paddingTop - paddingBottom
    }

    fun getHorizontalSpace(): Int {
        return width - paddingLeft - paddingRight
    }


    override fun computeScrollVectorForPosition(targetPosition: Int): PointF {
        val pos = convert2LayoutPosition(targetPosition)
        val scrollOffset = (pos + 1) * mChildSize[mOrientation]
        return if (mOrientation == VERTICAL)
            PointF(0f, Math.signum((scrollOffset - mScrollOffset).toFloat()))
        else
            PointF(Math.signum((scrollOffset - mScrollOffset).toFloat()), 0f)
    }

    inner class ItemLayoutInfo
    {
        var scaleXY: Float = 0.toFloat()
        var layoutPercent: Float = 0.toFloat()
        var positionOffsetPercent: Float = 0.toFloat()
        var start: Int = 0
        var isBottom: Boolean = false

        constructor(top: Int, scale: Float, positonOffset: Float, percent: Float)
        {
            this.start = top
            this.scaleXY = scale
            this.positionOffsetPercent = positonOffset
            this.layoutPercent = percent
        }

        fun setIsBottom(): ItemLayoutInfo {
            isBottom = true
            return this
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState()
        savedState.scrollOffset = mScrollOffset
        savedState.reverse = mReverse
        savedState.vanishOffset = mVanishOffset
        savedState.scale = mScale
        savedState.childLayoutOffsetInput = mChildPeekSizeInput
        savedState.itemHeightWidthRatio = mItemHeightWidthRatio
        savedState.orientation = mOrientation
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            val s = state as SavedState?
            mScrollOffset = s!!.scrollOffset
            mReverse = s!!.reverse
            mVanishOffset = s!!.vanishOffset
            mScale = s!!.scale
            mChildPeekSizeInput = s!!.childLayoutOffsetInput
            mItemHeightWidthRatio = s!!.itemHeightWidthRatio
            mOrientation = s!!.orientation
            requestLayout()
        }
    }

    inner class SavedState : Parcelable
    {
        var scrollOffset: Int = 0
        var childLayoutOffsetInput:Int = 0
        var orientation:Int = 0
        var itemHeightWidthRatio: Float = 0.toFloat()
        var scale:Float = 0.toFloat()
        var elevation:Float = 0.toFloat()
        var vanishOffset:Float = 0.toFloat()
        var reverse: Boolean = false

        constructor() {}

        constructor(other: SavedState)
        {
            scrollOffset = other.scrollOffset
            childLayoutOffsetInput = other.childLayoutOffsetInput
            orientation = other.orientation
            itemHeightWidthRatio = other.itemHeightWidthRatio
            scale = other.scale
            elevation = other.elevation
            vanishOffset = other.vanishOffset
            reverse = other.reverse
        }

        constructor(parcel: Parcel)
        {
            scrollOffset = parcel.readInt()
            childLayoutOffsetInput = parcel.readInt()
            orientation = parcel.readInt()
            itemHeightWidthRatio = parcel.readFloat()
            scale = parcel.readFloat()
            elevation = parcel.readFloat()
            vanishOffset = parcel.readFloat()
            reverse = parcel.readInt() == 1
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(scrollOffset)
            dest.writeInt(childLayoutOffsetInput)
            dest.writeInt(orientation)
            dest.writeFloat(itemHeightWidthRatio)
            dest.writeFloat(scale)
            dest.writeFloat(elevation)
            dest.writeFloat(vanishOffset)
            dest.writeInt(if (reverse) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls<SavedState?>(size)
            }
        }
    }

    interface ChildDecorateHelper
    {
        fun decorateChild(child: View, posOffsetPercent: Float, layoutPercent: Float, isBottom: Boolean)
    }


    class DefaultChildDecorateHelper : ChildDecorateHelper
    {
        private var mElevation: Float = 0f

        constructor(maxElevation: Float) {
            mElevation = maxElevation
        }

        override fun decorateChild(child: View, posOffsetPercent: Float, layoutPercent: Float, isBottom: Boolean) {
            ViewCompat.setElevation(child, (layoutPercent.toDouble() * mElevation.toDouble() * 0.7 + mElevation * 0.3).toFloat())
        }
    }


}