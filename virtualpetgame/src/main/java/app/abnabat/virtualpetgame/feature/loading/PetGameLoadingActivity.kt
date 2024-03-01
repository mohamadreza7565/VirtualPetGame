package app.abnabat.virtualpetgame.feature.loading

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import app.abnabat.virtualpetgame.databinding.ActivityPetGameLoadingBinding
import app.abnabat.virtualpetgame.feature.game.PetGameActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PetGameLoadingActivity : AppCompatActivity() {

    //binding
    private lateinit var mBinding: ActivityPetGameLoadingBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPetGameLoadingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        init()
    }

    private fun init() {
        initViews()
        changeActivity()
    }

    private fun changeActivity() {
        lifecycleScope.launch {
            delay(3000)
            PetGameActivity.start(this@PetGameLoadingActivity)
            finish()
        }
    }

    private fun initViews() {
        mBinding.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(100, true)
            }
        }
    }

}