package app.abnabat.virtualpetgame.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup.MarginLayoutParams


class MovablePetFoodImageView : androidx.appcompat.widget.AppCompatImageView,
    OnTouchListener {

    private val CLICK_DRAG_TOLERANCE: Float = 10f
    private var right: Float = 0f
    private var left: Float = 0f
    private var top: Float = 0f
    private var bottom: Float = 0f
    private var downRawX = 0f
    private var downRawY: Float = 0f
    private var defaultXLoc: Float = 0f
    private var defaultYLoc: Float = 0f
    private var dX = 0f
    private var dY: Float = 0f
    private var onResultListener: ((onLipsPosition: Boolean, eat: Boolean) -> Unit)? = null
    private var onMoveListener: ((Float, Float) -> Unit)? = null
    private var onActionUpListener: (() -> Unit)? = null

    constructor(context: Context?) : super(context!!) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
    }

    fun startMovement(
        right: Float,
        left: Float,
        top: Float,
        bottom: Float,
        onResultListener: (onLipsPosition: Boolean, eat: Boolean) -> Unit,
        onMoveListener: (Float, Float) -> Unit,
        onActionUpListener: () -> Unit
    ) {
        this.bottom = bottom
        this.left = left
        this.right = right
        this.top = top
        this.onResultListener = onResultListener
        this.onMoveListener = onMoveListener
        this.onActionUpListener = onActionUpListener
        setOnTouchListener(this)

        defaultXLoc =
            x + width / 2 - width / 2

        defaultYLoc =
            y + height / 2 - height / 2


        Log.e("TAG", "$right - $left - $top - $bottom -$defaultXLoc - $defaultYLoc")
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val layoutParams = view.layoutParams as MarginLayoutParams
        val x = motionEvent.rawX
        val y = motionEvent.rawY
        val action = motionEvent.action
        return if (action == MotionEvent.ACTION_DOWN) {
            downRawX = motionEvent.rawX
            downRawY = motionEvent.rawY
            dX = view.x - downRawX
            dY = view.y - downRawY
            true // Consumed
        } else if (action == MotionEvent.ACTION_MOVE) {
            val viewWidth = view.width
            val viewHeight = view.height
            val viewParent = view.parent as View
            val parentWidth = viewParent.width
            val parentHeight = viewParent.height
            var newX = motionEvent.rawX + dX
            newX = Math.max(
                layoutParams.leftMargin.toFloat(), newX
            ) // Don't allow the FAB past the left hand side of the parent
            newX = Math.min(
                (parentWidth - viewWidth - layoutParams.rightMargin).toFloat(), newX
            ) // Don't allow the FAB past the right hand side of the parent
            var newY: Float = motionEvent.rawY + dY
            newY = Math.max(
                layoutParams.topMargin.toFloat(), newY
            ) // Don't allow the FAB past the top of the parent
            newY = Math.min(
                (parentHeight - viewHeight - layoutParams.bottomMargin).toFloat(), newY
            ) // Don't allow the FAB past the bottom of the parent
            view.animate().x(newX).y(newY).setDuration(0).start()
            onMoveListener?.invoke(newX, newY)


            var currentX = motionEvent.rawX
            var currentY = motionEvent.rawY
            if (currentX < right && currentX > left && currentY > top && currentY < bottom) {
                onResultListener?.invoke(true,false)
            }else{
                onResultListener?.invoke(false,false)
            }


            true // Consumed
        } else if (action == MotionEvent.ACTION_UP) {

            var newX = motionEvent.rawX
            var newY = motionEvent.rawY

            if (newX < right && newX > left && newY > top && newY < bottom) {
                onResultListener?.invoke(true,true)
            }else{
                onResultListener?.invoke(false,false)
            }

            view.animate().x(defaultXLoc).y(defaultYLoc).setDuration(0).start()
            onActionUpListener?.invoke()

            true // Consumed
        } else {
            super.onTouchEvent(motionEvent)
        }
    }
}