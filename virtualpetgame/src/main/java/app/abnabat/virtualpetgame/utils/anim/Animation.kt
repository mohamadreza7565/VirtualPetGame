package app.abnabat.virtualpetgame.utils.anim

import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation

fun View.petBoxSerumRotationAnimation() {


    var toLeftAnim: Animation
    toLeftAnim = RotateAnimation(2f, -2f)

    toLeftAnim.setDuration(1000)
    toLeftAnim.setFillAfter(true)
    toLeftAnim.setFillEnabled(true)


    var toRightAnim: Animation
    toRightAnim = RotateAnimation(-2f, 2f)

    toRightAnim.setDuration(1000)
    toRightAnim.setFillAfter(true)
    toRightAnim.setFillEnabled(true)



    toLeftAnim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(p0: Animation?) {

        }

        override fun onAnimationEnd(p0: Animation?) {

            this@petBoxSerumRotationAnimation.startAnimation(toRightAnim)

        }

        override fun onAnimationRepeat(p0: Animation?) {

        }

    })



    toRightAnim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(p0: Animation?) {

        }

        override fun onAnimationEnd(p0: Animation?) {

            this@petBoxSerumRotationAnimation.startAnimation(toLeftAnim)

        }

        override fun onAnimationRepeat(p0: Animation?) {

        }

    })



    this.startAnimation(toLeftAnim)


}
