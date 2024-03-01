package app.abnabat.virtualpetgame.feature.game

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.lifecycleScope
import app.abnabat.virtualpetgame.R
import app.abnabat.virtualpetgame.databinding.ActivityPetGameBinding
import app.abnabat.virtualpetgame.listener.PetGameMotionLayoutListener
import app.abnabat.virtualpetgame.model.UserPetFoodModel
import app.abnabat.virtualpetgame.model.UserPetModel
import app.abnabat.virtualpetgame.state.PetEyesState
import app.abnabat.virtualpetgame.state.PetSates
import app.abnabat.virtualpetgame.utils.anim.petBoxSerumRotationAnimation
import app.abnabat.virtualpetgame.utils.extentions.invisible
import app.abnabat.virtualpetgame.utils.extentions.visible

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PetGameActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityPetGameBinding

    private var happyProgress = 0
    private var animationIsStarting = false
    private var repeatFoodCounter = 0
    private var maxRepeatFoodCounter = 4
    private var lastFoodId = 0
    private var openLipsCounter = 0
    private var maxOpenLipsForEating = 5
    private var movementEyesState = PetEyesState.MIDDLE
    private lateinit var eyelidsTimer: CountDownTimer
    private lateinit var eyesVibrateAnimation: Animation
    private lateinit var foodAnimation: ValueAnimator
    private var currentFoodIndex = 0
    private var mFullVoices: MediaPlayer? = null
    private var mEatingVoices: MediaPlayer? = null
    private var mEyeLidsVoices: MediaPlayer? = null
    private var mNaVoices: MediaPlayer? = null
    private var mTouchVoices: MediaPlayer? = null
    private var mSadVoices: MediaPlayer? = null
    private var mHappyVoices: MediaPlayer? = null
    private var mOpenLipsVoices: MediaPlayer? = null
    private var mSwitchFoodVoices: MediaPlayer? = null
    private var state = PetSates.FULL
    private var userFandoghPrize = 200
    private val minStatePercentage = 0
    private val maxStatePercentage = 100
    private var mUserPetModel: UserPetModel = petModelMockData()
    private var foodList: ArrayList<UserPetFoodModel> = foodMockData()


    companion object {
        fun start(
            mContext: Context,
        ) {
            Intent(mContext, PetGameActivity::class.java)
                .also {
                    mContext.startActivity(it)
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPetGameBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        init()
    }

    private fun foodMockData(): ArrayList<UserPetFoodModel> {
        return arrayListOf(
            UserPetFoodModel(
                id = 1,
                fandoghPrize = 2,
                percent = 5,
                imageRes = R.drawable.ic_food_1
            ),
            UserPetFoodModel(
                id = 2,
                fandoghPrize = 10,
                percent = 10,
                imageRes = R.drawable.ic_food_2
            ),
            UserPetFoodModel(
                id = 3,
                fandoghPrize = 15,
                percent = 15,
                imageRes = R.drawable.ic_food_3
            )
        )
    }

    private fun petModelMockData() = UserPetModel(hungryPercent = minStatePercentage)

    private fun init() {
        initViews()
        initSerumRotationAnimation()
        initVibrateAnimation()
        initClick()
        initEyelidsMotion()
        initStateMotions()
        initEyesMotions()
        initBundle()
        iniSerumMotion()
        lifecycleScope.launch {
            updatePetStatus()
        }
        initFoods()
        initPetState()
    }

    private fun getPetStatus(onResult: (Boolean) -> Unit) {
        lifecycleScope.launch {
            onResult.invoke(true)
        }
    }

    private fun initViews() {

    }

    private fun iniSerumMotion() {

    }

    private suspend fun updatePetStatus() {

        delay(30 * 1000)
        decreaseFoodProgress(10)
        updateProgress(mUserPetModel.hungryPercent)
        resetState()
        updateCharacterState()
        updatePetStatus()

    }

    private fun updateProgress(happyProgress: Int) {
        this.happyProgress = happyProgress
        mBinding.foodProgress.progress = happyProgress / 100f
    }

    private fun initVoices() {
        mSwitchFoodVoices = MediaPlayer.create(this, R.raw.switch_food)
        mFullVoices = MediaPlayer.create(this, R.raw.full)
        mEyeLidsVoices = MediaPlayer.create(this, R.raw.eyelips)
        mTouchVoices = MediaPlayer.create(this, R.raw.touch)
        mSadVoices = MediaPlayer.create(this, R.raw.sad)
        mHappyVoices = MediaPlayer.create(this, R.raw.happy)
    }

    private fun initBundle() {

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initClick() {

        mBinding.clRoot.setOnTouchListener(touchCallback)

        mBinding.cvToolbarBack.setOnClickListener { onBackPressed() }

        mBinding.ivFandogh.setOnClickListener {

        }

        mBinding.ivHelp.setOnClickListener {
        }

    }


    private fun initEyesMotions() {
        mBinding.eyes.eyesMotionLayout.setTransitionListener(object :
            PetGameMotionLayoutListener() {

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                if (mBinding.eyes.eyesMotionLayout.currentState == mBinding.defaultEyelids.defaultEyelidsMotionLayout.endState) {
                    if (state == PetSates.HUNGRY || state == PetSates.VERY_HUNGRY) {
                        startVibrateEyesAnimation()
                    } else {
                        stopVibrateEyesAnimation()
                    }
                } else {
                    stopVibrateEyesAnimation()
                }
            }

        })
    }

    private fun initStateMotions() {

        mBinding.sadLips.sadMotionLayout.setTransitionListener(object :
            PetGameMotionLayoutListener() {

            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
                animationIsStarting = true
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                animationIsStarting = false
                if (mBinding.sadLips.sadMotionLayout.currentState == mBinding.sadLips.sadMotionLayout.startState) {
                    when {
                        state == PetSates.OPEN_LIPS || state == PetSates.EATING -> {
                            mBinding.sadLips.sadMotionLayout.invisible()
                            mBinding.openLips.openLipsMotionLayout.visible()
                            mBinding.openLips.openLipsMotionLayout.transitionToEnd()
                        }

                        state == PetSates.FULL -> moveToFullMode()
                        state == PetSates.SIK -> {
                            mBinding.sadLips.sadMotionLayout.transitionToEnd()
                            moveToSik()
                        }

                        state == PetSates.VERY_HUNGRY -> {
                            mBinding.sadLips.sadMotionLayout.transitionToEnd()
                            moveToVeryHungry()
                        }

                        else -> mBinding.sadLips.sadMotionLayout.transitionToEnd()

                    }
                }
            }

        })

        mBinding.smileLips.smileMotionLayout.setTransitionListener(object :
            PetGameMotionLayoutListener() {

            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
                animationIsStarting = true
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                animationIsStarting = false
                if (mBinding.smileLips.smileMotionLayout.currentState == mBinding.smileLips.smileMotionLayout.startState) {
                    when {
                        state == PetSates.OPEN_LIPS || state == PetSates.EATING -> {
                            mBinding.sadLips.sadMotionLayout.invisible()
                            mBinding.openLips.openLipsMotionLayout.visible()
                            mBinding.openLips.openLipsMotionLayout.transitionToEnd()
                        }

                        state != PetSates.FULL -> moveToHungryMode()
                        else -> mBinding.smileLips.smileMotionLayout.transitionToEnd()
                    }
                }
            }

        })

        mBinding.openLips.openLipsMotionLayout.setTransitionListener(object :
            PetGameMotionLayoutListener() {

            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
                animationIsStarting = true
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {

                animationIsStarting = false
                if (mBinding.openLips.openLipsMotionLayout.currentState == mBinding.openLips.openLipsMotionLayout.startState) {
                    when {

                        openLipsCounter >= maxOpenLipsForEating || openLipsCounter == 0 -> {
                            resetState()
                            mBinding.openLips.openLipsMotionLayout.invisible()

                            when (state) {
                                PetSates.FULL -> {
                                    mBinding.smileLips.smileMotionLayout.visible()
                                    moveToFullMode()
                                }

                                PetSates.SIK -> {
                                    moveToSik()
                                }

                                PetSates.VERY_HUNGRY -> {
                                    moveToVeryHungry()
                                }

                                else -> {
                                    mBinding.sadLips.sadMotionLayout.visible()
                                    moveToHungryMode()
                                }
                            }
                        }

                        state == PetSates.EATING -> openLipsAnimation()


                    }
                }


                if (mBinding.openLips.openLipsMotionLayout.currentState == mBinding.openLips.openLipsMotionLayout.endState) {
                    when {
                        state == PetSates.EATING -> closeLipsAnimation()
                        state != PetSates.OPEN_LIPS -> closeLipsAnimation()

                    }
                }
            }

        })

        mBinding.leftHandMotionLayout.setTransitionListener(object :
            PetGameMotionLayoutListener() {

            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
                animationIsStarting = true
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                animationIsStarting = false
            }

        })

        mBinding.rightHandMotionLayout.setTransitionListener(object :
            PetGameMotionLayoutListener() {

            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
                animationIsStarting = true
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                animationIsStarting = false
            }

        })

        mBinding.mlCharacter.setTransitionListener(object :
            PetGameMotionLayoutListener() {

            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
                animationIsStarting = true
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                animationIsStarting = false
            }

        })


    }

    private fun initPetState() {

        happyProgress = mUserPetModel.hungryPercent
        resetState()

        when (state) {
            PetSates.FULL -> {
                moveToFullMode()
            }

            PetSates.HUNGRY -> {
                moveToHungryMode()
            }

            PetSates.VERY_HUNGRY -> {
                moveToVeryHungry()
            }

            else -> {
                moveToSik()
            }
        }

        mBinding.foodProgress.progress = happyProgress / 100f
    }

    private fun initFoods() {

        mBinding.ivPet.post {
            mBinding.ivFood.post {
                mBinding.ivFood.startMovement(
                    right = mBinding.vRightCharacter.x,
                    left = mBinding.vLeftCharacter.x,
                    top = mBinding.vTopCharacter.y,
                    bottom = mBinding.vBottomCharacter.y,
                    onResultListener = { isLipsPosition, eat ->

                        if (state == PetSates.EATING)
                            return@startMovement

                        if (eat) {
                            if (foodList[currentFoodIndex].id == lastFoodId) {
                                repeatFoodCounter++
                            } else {
                                lastFoodId = foodList[currentFoodIndex].id
                                repeatFoodCounter = 1
                            }

                            when {
                                foodList[currentFoodIndex].fandoghPrize > userFandoghPrize -> {
                                    resetState()
                                    resetEatingVoices()
                                    playNaVoice()
                                    Toast.makeText(this, "فندق کافی نداری!", Toast.LENGTH_SHORT)
                                        .show()
                                }

                                repeatFoodCounter > maxRepeatFoodCounter -> {
                                    resetEatingVoices()
                                    resetState()
                                    playNaVoice()
                                }


                                happyProgress + foodList[currentFoodIndex].percent > maxStatePercentage -> {
                                    resetState()
                                    resetEatingVoices()
                                    playNaVoice()
                                }

                                else -> {
                                    openLipsCounter++
                                    state = PetSates.EATING
                                    resetEatingVoices()
                                    playEatingVoice()
                                    increaseFoodProgress()
                                }
                            }
                            closeLipsAnimation()
                        } else {
                            if (isLipsPosition) state = PetSates.OPEN_LIPS
                            if (isLipsPosition)
                                openLipsAnimation()
                            else {
                                resetState()
                                closeLipsAnimation()
                            }
                        }

                    },
                    onMoveListener = { x, y ->
                        startAnimate(
                            y = y,
                            x = x,
                            leftXLoc = mBinding.eyes.leftWhitEye.x + mBinding.eyes.leftWhitEye.width / 2 - mBinding.eyes.leftBlackEye.width / 2,
                            leftYLoc = mBinding.eyes.leftWhitEye.y + mBinding.eyes.leftWhitEye.height / 2 - mBinding.eyes.leftBlackEye.height / 2,
                            rightXLoc = mBinding.eyes.rightWhitEye.x + mBinding.eyes.rightWhitEye.width / 2 - mBinding.eyes.rightBlackEye.width / 2,
                            rightYLoc = mBinding.eyes.rightWhitEye.y + mBinding.eyes.rightWhitEye.height / 2 - mBinding.eyes.rightBlackEye.height / 2,
                            playVoice = false
                        )
                    },
                    onActionUpListener = {
                        animateToMiddle(
                            leftXLoc = mBinding.eyes.leftWhitEye.x + mBinding.eyes.leftWhitEye.width / 2 - mBinding.eyes.leftBlackEye.width / 2,
                            leftYLoc = mBinding.eyes.leftWhitEye.y + mBinding.eyes.leftWhitEye.height / 2 - mBinding.eyes.leftBlackEye.height / 2,
                            rightXLoc = mBinding.eyes.rightWhitEye.x + mBinding.eyes.rightWhitEye.width / 2 - mBinding.eyes.rightBlackEye.width / 2,
                            rightYLoc = mBinding.eyes.rightWhitEye.y + mBinding.eyes.rightWhitEye.height / 2 - mBinding.eyes.rightBlackEye.height / 2
                        )
                    }
                )
            }
        }

        //default
        selectFood(false)

        mBinding.mbNext.setOnClickListener {
            currentFoodIndex++
            selectFood(true)
        }

        mBinding.mbPre.setOnClickListener {
            currentFoodIndex--
            selectFood(true)
        }

        mBinding.ivFood.post {
            initFoodAnimation()
        }
    }

    private fun resetState() {
        openLipsCounter = 0
        state = when {
            isFull() -> PetSates.FULL
            isHungry() -> PetSates.HUNGRY
            isVeryHungry() -> PetSates.VERY_HUNGRY
            isSik() -> PetSates.SIK
            else -> PetSates.HUNGRY
        }

    }

    private fun playNaVoice() {
        mNaVoices = MediaPlayer.create(this, R.raw.na)
        mNaVoices?.start()
    }

    private fun playEatingVoice() {
        mEatingVoices = MediaPlayer.create(this, R.raw.eating)
        mEatingVoices?.start()
    }

    private fun resetEatingVoices() {
        mEatingVoices?.release()
        mOpenLipsVoices?.release()
        mNaVoices?.release()
    }

    private fun openLipsAnimation() {

        if (mBinding.openLips.openLipsMotionLayout.currentState == mBinding.openLips.openLipsMotionLayout.endState)
            return


        if (animationIsStarting)
            return


        if (state == PetSates.OPEN_LIPS) {
            resetEatingVoices()
            playOpenLipsVoice()
        }



        when {
            state == PetSates.EATING -> {
                openLipsCounter++
                mBinding.openLips.openLipsMotionLayout.transitionToEnd()
            }

            isFull() -> {
                mBinding.smileLips.smileMotionLayout.transitionToStart()
            }

            else -> {
                mBinding.sadLips.sadMotionLayout.transitionToStart()
            }
        }

    }

    private fun isFull(): Boolean {
        return happyProgress >= 60
    }

    private fun isHungry(): Boolean {
        return happyProgress in (40..60)
    }

    private fun isVeryHungry(): Boolean {
        return happyProgress in (20..40)
    }

    private fun isSik(): Boolean {
        return happyProgress in (0..20)
    }

    private fun playOpenLipsVoice() {
        mOpenLipsVoices = MediaPlayer.create(this, R.raw.open_lips)
        mOpenLipsVoices?.start()
    }

    private fun closeLipsAnimation() {

        if (mBinding.openLips.openLipsMotionLayout.currentState == mBinding.openLips.openLipsMotionLayout.startState)
            return

        if (animationIsStarting)
            return

        mBinding.openLips.openLipsMotionLayout.transitionToStart()
    }

    private fun selectFood(playVoice: Boolean) {

        if (currentFoodIndex >= foodList.size)
            currentFoodIndex = 0

        if (currentFoodIndex < 0)
            currentFoodIndex = foodList.size - 1

        mSwitchFoodVoices?.let {
            if (!it.isPlaying && playVoice)
                it.start()
        }

        mBinding.ivFood.setImageResource(foodList[currentFoodIndex].imageRes)
        mBinding.tvFoodFandoghAmount.text = " * ${foodList[currentFoodIndex].fandoghPrize}"
    }

    private fun initEyelidsMotion() {
        eyelidsTimer = object : CountDownTimer(5000, 5000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                if (state == PetSates.FULL || state == PetSates.HUNGRY) {
                    mBinding.defaultEyelids.defaultEyelidsMotionLayout.transitionToEnd()
                    mEyeLidsVoices?.start()
                }
            }
        }
        mBinding.defaultEyelids.defaultEyelidsMotionLayout.setTransitionListener(object :
            PetGameMotionLayoutListener() {

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                if (mBinding.defaultEyelids.defaultEyelidsMotionLayout.currentState == mBinding.defaultEyelids.defaultEyelidsMotionLayout.endState) {
                    mBinding.defaultEyelids.defaultEyelidsMotionLayout.transitionToStart()
                } else {
                    eyelidsTimer.start()
                }
            }


        })

        eyelidsTimer.start()
    }

    private fun initSerumRotationAnimation() {
        mBinding.vBoxSerum.petBoxSerumRotationAnimation()
    }

    private fun initVibrateAnimation() {
        eyesVibrateAnimation = AnimationUtils.loadAnimation(this, R.anim.vibrate_anim)
        eyesVibrateAnimation.repeatCount = Animation.INFINITE
        eyesVibrateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                Handler().postDelayed({
                    mBinding.eyes.rightBlackEye.startAnimation(animation)
                    mBinding.eyes.leftBlackEye.startAnimation(animation)
                }, 0)
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })
    }

    private fun initFoodAnimation() {
        if (!::foodAnimation.isInitialized) {
            foodAnimation = ValueAnimator.ofFloat(1f, 1.25f)
            foodAnimation.duration = 500
            foodAnimation.addUpdateListener { animation ->
                mBinding.ivFood.scaleX = animation.animatedValue as Float
                mBinding.ivFood.scaleY = animation.animatedValue as Float
            }
            foodAnimation.repeatCount = Animation.INFINITE
            foodAnimation.repeatMode = ValueAnimator.REVERSE
        }
        foodAnimation.start()
    }

    private fun moveToFullMode() {

        eyelidsTimer.cancel()
        eyelidsTimer.start()

        stopVibrateEyesAnimation()

        mBinding.sadLips.sadMotionLayout.invisible()
        mBinding.smileLips.smileMotionLayout.visible().also {
            mBinding.smileLips.smileMotionLayout.transitionToEnd()
            mBinding.eyes.eyesMotionLayout.transitionToStart()
        }

        if (mBinding.veryHungryEyelids.veryHungryEyelidsMotionLayout.currentState == mBinding.veryHungryEyelids.veryHungryEyelidsMotionLayout.endState) {
            mBinding.veryHungryEyelids.veryHungryEyelidsMotionLayout.transitionToStart()
        }

        if (mBinding.sikEyelids.sikEyelidsMotionLayout.currentState == mBinding.sikEyelids.sikEyelidsMotionLayout.endState) {
            mBinding.sikEyelids.sikEyelidsMotionLayout.transitionToStart()
        }

        lifecycleScope.launch {
            if (mBinding.rightHandMotionLayout.currentState == mBinding.rightHandMotionLayout.endState) {
                mBinding.rightHandMotionLayout.transitionToStart()
                delay(200)
            }
            mBinding.rightHandMotionLayout.translationZ = (mBinding.ivPet.translationZ - 1)
        }

        lifecycleScope.launch {
            if (mBinding.leftHandMotionLayout.currentState == mBinding.leftHandMotionLayout.endState) {
                mBinding.leftHandMotionLayout.transitionToStart()
                delay(200)
            }
            mBinding.leftHandMotionLayout.translationZ = (mBinding.ivPet.translationZ - 1)
        }


        if (mBinding.mlCharacter.currentState == mBinding.mlCharacter.endState) {
            mBinding.mlCharacter.transitionToStart()
        }

        hideBandAids()
        hideTearDrop()
    }

    private fun moveToHungryMode() {

        mBinding.eyes.vBigLeftReflect.alpha = 1f
        mBinding.eyes.vSmallLeftReflect.alpha = 1f
        mBinding.eyes.vBigRightReflect.alpha = 1f
        mBinding.eyes.vSmallRightReflect.alpha = 1f

        eyelidsTimer.cancel()
        eyelidsTimer.start()

        mBinding.smileLips.smileMotionLayout.invisible()
        mBinding.sadLips.sadMotionLayout.visible().also {
            mBinding.sadLips.sadMotionLayout.transitionToEnd()
            mBinding.eyes.eyesMotionLayout.transitionToEnd()
        }

        if (mBinding.veryHungryEyelids.veryHungryEyelidsMotionLayout.currentState == mBinding.veryHungryEyelids.veryHungryEyelidsMotionLayout.endState) {
            mBinding.veryHungryEyelids.veryHungryEyelidsMotionLayout.transitionToStart()
        }

        if (mBinding.sikEyelids.sikEyelidsMotionLayout.currentState == mBinding.sikEyelids.sikEyelidsMotionLayout.endState) {
            mBinding.sikEyelids.sikEyelidsMotionLayout.transitionToStart()
        }

        lifecycleScope.launch {
            if (mBinding.rightHandMotionLayout.currentState == mBinding.rightHandMotionLayout.endState) {
                mBinding.rightHandMotionLayout.transitionToStart()
                delay(200)
            }
            mBinding.rightHandMotionLayout.translationZ = (mBinding.ivPet.translationZ - 1)
        }

        lifecycleScope.launch {
            if (mBinding.leftHandMotionLayout.currentState == mBinding.leftHandMotionLayout.endState) {
                mBinding.leftHandMotionLayout.transitionToStart()
                delay(200)
            }
            mBinding.leftHandMotionLayout.translationZ = (mBinding.ivPet.translationZ - 1)
        }

        if (mBinding.mlCharacter.currentState == mBinding.mlCharacter.endState) {
            mBinding.mlCharacter.transitionToStart()
        }

        hideTearDrop()
        hideBandAids()
    }

    private fun moveToVeryHungry() {

        mBinding.rightHandMotionLayout.transitionToEnd()
        mBinding.leftHandMotionLayout.transitionToEnd()


        mBinding.rightHandMotionLayout.translationZ = (mBinding.ivPet.translationZ + 1)
        mBinding.leftHandMotionLayout.translationZ = (mBinding.ivPet.translationZ + 1)


        if (mBinding.veryHungryEyelids.veryHungryEyelidsMotionLayout.currentState == mBinding.veryHungryEyelids.veryHungryEyelidsMotionLayout.startState) {
            lifecycleScope.launch {
                delay(500)
                if (state == PetSates.VERY_HUNGRY)
                    mBinding.veryHungryEyelids.veryHungryEyelidsMotionLayout.transitionToEnd()
            }
        }


        if (mBinding.sikEyelids.sikEyelidsMotionLayout.currentState == mBinding.sikEyelids.sikEyelidsMotionLayout.endState) {
            mBinding.sikEyelids.sikEyelidsMotionLayout.transitionToStart()
        }



        mBinding.smileLips.smileMotionLayout.invisible()
        mBinding.sadLips.sadMotionLayout.visible().also {
            mBinding.sadLips.sadMotionLayout.transitionToEnd()
            mBinding.eyes.eyesMotionLayout.transitionToStart()
        }

        if (mBinding.mlCharacter.currentState == mBinding.mlCharacter.endState) {
            mBinding.mlCharacter.transitionToStart()
        }

        mBinding.eyes.eyesMotionLayout.transitionToEnd()

        hideBandAids()
        hideTearDrop()
    }

    private fun startVibrateEyesAnimation() {
        mBinding.eyes.rightBlackEye.startAnimation(eyesVibrateAnimation)
        mBinding.eyes.leftBlackEye.startAnimation(eyesVibrateAnimation)

        mBinding.eyes.vBigLeftReflect.alpha = 1f
        mBinding.eyes.vSmallLeftReflect.alpha = 1f
        mBinding.eyes.vBigRightReflect.alpha = 1f
        mBinding.eyes.vSmallRightReflect.alpha = 1f
    }

    private fun stopVibrateEyesAnimation() {
        mBinding.eyes.rightBlackEye.animation = null
        mBinding.eyes.leftBlackEye.animation = null

        mBinding.eyes.vBigLeftReflect.alpha = 0f
        mBinding.eyes.vSmallLeftReflect.alpha = 0f
        mBinding.eyes.vBigRightReflect.alpha = 0f
        mBinding.eyes.vSmallRightReflect.alpha = 0f
    }

    private fun moveToSik() {

        mBinding.leftHandMotionLayout.transitionToEnd()
        mBinding.leftHandMotionLayout.translationZ = (mBinding.ivPet.translationZ + 1)

        /**
        if (mBinding.sikEyelids.sikEyelidsMotionLayout.currentState == mBinding.sikEyelids.sikEyelidsMotionLayout.startState) {
        mBinding.sikEyelids.sikEyelidsMotionLayout.transitionToEnd()
        }
         **/

        if (mBinding.veryHungryEyelids.veryHungryEyelidsMotionLayout.currentState == mBinding.veryHungryEyelids.veryHungryEyelidsMotionLayout.startState) {
            mBinding.veryHungryEyelids.veryHungryEyelidsMotionLayout.transitionToEnd()
        }

        lifecycleScope.launch {
            if (mBinding.rightHandMotionLayout.currentState == mBinding.rightHandMotionLayout.endState) {
                mBinding.rightHandMotionLayout.transitionToStart()
                delay(200)
            }
            mBinding.rightHandMotionLayout.translationZ = (mBinding.ivPet.translationZ - 1)
        }


        stopVibrateEyesAnimation()

        mBinding.smileLips.smileMotionLayout.invisible()
        mBinding.sadLips.sadMotionLayout.visible().also {
            mBinding.sadLips.sadMotionLayout.transitionToEnd()
            mBinding.eyes.eyesMotionLayout.transitionToStart()
        }

        if (mBinding.mlCharacter.currentState == mBinding.mlCharacter.startState) {
            mBinding.mlCharacter.transitionToEnd()
        }

        showTearDrop()
        showBandAids()

    }

    private fun showBandAids() {
        if (mBinding.ivBandAids.alpha == 1f)
            return

        mBinding.ivBandAids.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    private fun hideBandAids() {
        if (mBinding.ivBandAids.alpha == 0f)
            return

        mBinding.ivBandAids.animate()
            .alpha(0f)
            .setDuration(500)
            .start()
    }

    private fun showTearDrop() {
        if (mBinding.ivTearDrop.alpha == 1f)
            return

        mBinding.ivTearDrop.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    private fun hideTearDrop() {
        if (mBinding.ivTearDrop.alpha == 0f)
            return

        mBinding.ivTearDrop.animate()
            .alpha(0f)
            .setDuration(500)
            .start()
    }

    private fun decreaseFoodProgress(percentage: Int) {
        mUserPetModel.hungryPercent =
            if (mUserPetModel.hungryPercent <= minStatePercentage) minStatePercentage
            else mUserPetModel.hungryPercent - percentage
        getPetStatus {
            updateProgress(mUserPetModel.hungryPercent)
        }
        resetState()
    }

    private fun increaseFoodProgress() {

        if (happyProgress >= maxStatePercentage) happyProgress = maxStatePercentage

        mBinding.foodProgress.progress = happyProgress / 100f
        userFandoghPrize -= foodList[currentFoodIndex].fandoghPrize
        mUserPetModel.hungryPercent += foodList[currentFoodIndex].percent
        getUserFandoghRank()
        getPetStatus {
            updateProgress(mUserPetModel.hungryPercent)
        }
    }

    private fun updateCharacterState() {

        when (state) {
            PetSates.FULL -> {
                if (mBinding.smileLips.smileMotionLayout.visibility == View.INVISIBLE) {
                    mBinding.sadLips.sadMotionLayout.transitionToStart()
                }
            }

            PetSates.SIK -> {
                if (mBinding.sadLips.sadMotionLayout.visibility == View.INVISIBLE) {
                    mBinding.smileLips.smileMotionLayout.transitionToStart()
                }
                moveToSik()
            }

            PetSates.VERY_HUNGRY -> {
                if (mBinding.sadLips.sadMotionLayout.visibility == View.INVISIBLE) {
                    mBinding.smileLips.smileMotionLayout.transitionToStart()
                }

                moveToVeryHungry()
            }

            else -> {
                if (mBinding.sadLips.sadMotionLayout.visibility == View.INVISIBLE) {
                    mBinding.smileLips.smileMotionLayout.transitionToStart()
                }
            }
        }


    }


    @SuppressLint("ClickableViewAccessibility")
    private val touchCallback = View.OnTouchListener { v, event ->

        if (state == PetSates.SIK || state == PetSates.VERY_HUNGRY) {
            return@OnTouchListener false
        }

        val x = event?.rawX ?: 0f
        val y = event?.rawY ?: 0f

        val leftXLoc =
            mBinding.eyes.leftWhitEye.x + mBinding.eyes.leftWhitEye.width / 2 - mBinding.eyes.leftBlackEye.width / 2
        val leftYLoc =
            mBinding.eyes.leftWhitEye.y + mBinding.eyes.leftWhitEye.height / 2 - mBinding.eyes.leftBlackEye.height / 2
        val rightXLoc =
            mBinding.eyes.rightWhitEye.x + mBinding.eyes.rightWhitEye.width / 2 - mBinding.eyes.rightBlackEye.width / 2
        val rightYLoc =
            mBinding.eyes.rightWhitEye.y + mBinding.eyes.rightWhitEye.height / 2 - mBinding.eyes.rightBlackEye.height / 2



        when (event?.action) {

            MotionEvent.ACTION_UP -> {
                initFoodAnimation()
                animateToMiddle(leftXLoc, leftYLoc, rightXLoc, rightYLoc)
            }

            MotionEvent.ACTION_DOWN -> {
                mBinding.ivFood.clearAnimation()
                mBinding.ivFood.animation = null
                startAnimate(y, x, leftXLoc, leftYLoc, rightXLoc, rightYLoc, true)
            }

            MotionEvent.ACTION_MOVE -> {
                startAnimate(y, x, leftXLoc, leftYLoc, rightXLoc, rightYLoc, false)
            }
        }
        true
    }

    private fun startAnimate(
        y: Float,
        x: Float,
        leftXLoc: Float,
        leftYLoc: Float,
        rightXLoc: Float,
        rightYLoc: Float,
        playVoice: Boolean
    ) {


        when {

            y < mBinding.vBottomCharacter.y && y > mBinding.vTopCharacter.y && x < mBinding.vRightCharacter.x && x > mBinding.vLeftCharacter.x -> {
                // Middle
                animateToMiddle(leftXLoc, leftYLoc, rightXLoc, rightYLoc)
            }

            y > mBinding.vTopCharacter.y && y < mBinding.vBottomCharacter.y && x < mBinding.vLeftCharacter.x -> {
                // left
                animateToLeft(leftYLoc, rightYLoc)
            }

            y > mBinding.vTopCharacter.y && y < mBinding.vBottomCharacter.y && x > mBinding.vRightCharacter.x -> {
                // right
                animateToRight(leftYLoc, rightYLoc)
            }

            x < mBinding.vRightCharacter.x && x > mBinding.vLeftCharacter.x && y < mBinding.vTopCharacter.y -> {
                // Top
                animateToUp(leftXLoc, rightXLoc)
            }

            x < mBinding.vRightCharacter.x && x > mBinding.vLeftCharacter.x && y > mBinding.vBottomCharacter.y -> {
                // Bottom
                animateToDown(leftXLoc, rightXLoc)
            }

            y < mBinding.vTopCharacter.y -> {
                if (x > mBinding.vRightCharacter.x) {
                    // Right - Up
                    animateToRightUp()
                }

                if (x < mBinding.vLeftCharacter.x) {
                    // Left - Up
                    animateToLeftUp()

                }
            }


            else -> {
                if (x > mBinding.vRightCharacter.x) {
                    // Right - Down
                    animateToRightDown()
                }

                if (x < mBinding.vLeftCharacter.x) {
                    // Left - Down
                    animateToLeftDown()

                }
            }


        }

        if (playVoice && movementEyesState != PetEyesState.MIDDLE) {
            mTouchVoices?.start()
        }

    }


    private fun animateToDown(leftXLoc: Float, rightXLoc: Float) {

        if (movementEyesState == PetEyesState.DOWN) {
            return
        }

        movementEyesState = PetEyesState.DOWN

        mBinding.eyes.leftBlackEye.animate()
            ?.x(leftXLoc)
            ?.y(mBinding.eyes.vBottomLeftEye.y - mBinding.eyes.leftBlackEye.width)
            ?.setDuration(50)
            ?.start()

        mBinding.eyes.rightBlackEye.animate()
            ?.x(rightXLoc)
            ?.y(mBinding.eyes.vBottomRightEye.y - mBinding.eyes.rightBlackEye.width)
            ?.setDuration(50)
            ?.start()
    }

    private fun animateToLeftDown() {

        if (movementEyesState == PetEyesState.DOWN_LEFT) {
            return
        }

        movementEyesState = PetEyesState.DOWN_LEFT

        mBinding.eyes.leftBlackEye.animate()
            ?.x(mBinding.eyes.vLeftLeftEye.x)
            ?.y(mBinding.eyes.vBottomLeftEye.y - mBinding.eyes.leftBlackEye.width)
            ?.setDuration(50)
            ?.start()

        mBinding.eyes.rightBlackEye.animate()
            ?.x(mBinding.eyes.vLeftRightEye.x)
            ?.y(mBinding.eyes.vBottomRightEye.y - mBinding.eyes.rightBlackEye.width)
            ?.setDuration(50)
            ?.start()

    }

    private fun animateToRightDown() {

        if (movementEyesState == PetEyesState.DOWN_RIGHT) {
            return
        }

        movementEyesState = PetEyesState.DOWN_RIGHT

        mBinding.eyes.leftBlackEye.animate()
            ?.x(mBinding.eyes.vRightLeftEye.x - mBinding.eyes.leftBlackEye.width)
            ?.y(mBinding.eyes.vBottomLeftEye.y - mBinding.eyes.leftBlackEye.width)
            ?.setDuration(50)
            ?.start()

        mBinding.eyes.rightBlackEye.animate()
            ?.x(mBinding.eyes.vRightRightEye.x - mBinding.eyes.rightBlackEye.width)
            ?.y(mBinding.eyes.vBottomRightEye.y - mBinding.eyes.rightBlackEye.width)
            ?.setDuration(50)
            ?.start()

    }

    private fun animateToLeftUp() {

        if (movementEyesState == PetEyesState.UP_LEFT) {
            return
        }

        movementEyesState = PetEyesState.UP_LEFT

        mBinding.eyes.leftBlackEye.animate()
            ?.x(mBinding.eyes.vLeftLeftEye.x)
            ?.y(mBinding.eyes.vTopLeftEye.y)
            ?.setDuration(50)
            ?.start()

        mBinding.eyes.rightBlackEye.animate()
            ?.x(mBinding.eyes.vLeftRightEye.x)
            ?.y(mBinding.eyes.vTopRightEye.y)
            ?.setDuration(50)
            ?.start()

    }

    private fun animateToRightUp() {

        if (movementEyesState == PetEyesState.UP_RIGHT) {
            return
        }

        movementEyesState = PetEyesState.UP_RIGHT

        mBinding.eyes.leftBlackEye.animate()
            ?.x(mBinding.eyes.vRightLeftEye.x - mBinding.eyes.leftBlackEye.width)
            ?.y(mBinding.eyes.vTopLeftEye.y)
            ?.setDuration(50)
            ?.start()

        mBinding.eyes.rightBlackEye.animate()
            ?.x(mBinding.eyes.vRightRightEye.x - mBinding.eyes.rightBlackEye.width)
            ?.y(mBinding.eyes.vTopRightEye.y)
            ?.setDuration(50)
            ?.start()

    }

    private fun animateToUp(leftXLoc: Float, rightXLoc: Float) {

        if (movementEyesState == PetEyesState.UP) {
            return
        }

        movementEyesState = PetEyesState.UP

        mBinding.eyes.leftBlackEye.animate()
            ?.x(leftXLoc)
            ?.y(mBinding.eyes.vTopLeftEye.y)
            ?.setDuration(50)
            ?.start()

        mBinding.eyes.rightBlackEye.animate()
            ?.x(rightXLoc)
            ?.y(mBinding.eyes.vTopRightEye.y)
            ?.setDuration(50)
            ?.start()
    }

    private fun animateToRight(leftYLoc: Float, rightYLoc: Float) {

        if (movementEyesState == PetEyesState.RIGHT) {
            return
        }

        movementEyesState = PetEyesState.RIGHT

        mBinding.eyes.leftBlackEye.animate()
            ?.x(mBinding.eyes.vRightLeftEye.x - mBinding.eyes.leftBlackEye.width)
            ?.y(leftYLoc)
            ?.setDuration(50)
            ?.start()

        mBinding.eyes.rightBlackEye.animate()
            ?.x(mBinding.eyes.vRightRightEye.x - mBinding.eyes.rightBlackEye.width)
            ?.y(rightYLoc)
            ?.setDuration(50)
            ?.start()
    }

    private fun animateToLeft(leftYLoc: Float, rightYLoc: Float) {

        if (movementEyesState == PetEyesState.LEFT) {
            return
        }

        movementEyesState = PetEyesState.LEFT

        mBinding.eyes.leftBlackEye.animate()
            ?.x(mBinding.eyes.vLeftLeftEye.x)
            ?.y(leftYLoc)
            ?.setDuration(50)
            ?.start()

        mBinding.eyes.rightBlackEye.animate()
            ?.x(mBinding.eyes.vLeftRightEye.x)
            ?.y(rightYLoc)
            ?.setDuration(50)
            ?.start()

    }

    private fun animateToMiddle(
        leftXLoc: Float,
        leftYLoc: Float,
        rightXLoc: Float,
        rightYLoc: Float
    ) {

        if (movementEyesState == PetEyesState.MIDDLE) {
            return
        }

        movementEyesState = PetEyesState.MIDDLE

        mBinding.eyes.leftBlackEye
            .animate()
            ?.x(leftXLoc)
            ?.y(leftYLoc)
            ?.setDuration(100)
            ?.start()

        mBinding.eyes.rightBlackEye
            .animate()
            ?.x(rightXLoc)
            ?.y(rightYLoc)
            ?.setDuration(100)
            ?.start()
    }

    override fun onResume() {
        super.onResume()

        val leftXLoc =
            mBinding.eyes.leftWhitEye.x + mBinding.eyes.leftWhitEye.width / 2 - mBinding.eyes.leftBlackEye.width / 2
        val leftYLoc =
            mBinding.eyes.leftWhitEye.y + mBinding.eyes.leftWhitEye.height / 2 - mBinding.eyes.leftBlackEye.height / 2
        val rightXLoc =
            mBinding.eyes.rightWhitEye.x + mBinding.eyes.rightWhitEye.width / 2 - mBinding.eyes.rightBlackEye.width / 2
        val rightYLoc =
            mBinding.eyes.rightWhitEye.y + mBinding.eyes.rightWhitEye.height / 2 - mBinding.eyes.rightBlackEye.height / 2

        getUserFandoghRank()

        animateToMiddle(leftXLoc, leftYLoc, rightXLoc, rightYLoc)

        resetState()

        updateCharacterState()

        initVoices()

        eyelidsTimer.start()

    }

    override fun onStop() {
        super.onStop()
        resetEatingVoices()
        mHappyVoices?.release()
        mSadVoices?.release()
        mFullVoices?.release()
        mEyeLidsVoices?.release()
        mTouchVoices?.release()
        mSwitchFoodVoices?.release()
        eyelidsTimer.cancel()
    }

    private fun getUserFandoghRank() {
        mBinding.tvUserFandoghCount.text = userFandoghPrize.toString()
    }


}