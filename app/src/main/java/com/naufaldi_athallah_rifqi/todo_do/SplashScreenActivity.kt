package com.naufaldi_athallah_rifqi.todo_do

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.naufaldi_athallah_rifqi.todo_do.data.models.User
import com.naufaldi_athallah_rifqi.todo_do.utils.Const
import com.naufaldi_athallah_rifqi.todo_do.utils.helper.AppPreferences
import com.naufaldi_athallah_rifqi.todo_do.view.auth.IntroSliderActivity
import com.naufaldi_athallah_rifqi.todo_do.view.todo.TodoLocalActivity

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var splashScreenViewModel: SplashScreenViewModel
    // Splash screen timer
    private val SPLASH_TIME_OUT = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContentView(R.layout.splash_screen)
        initSplashScreenViewModel()
        initSharedPreferences()
        Handler().postDelayed(
            {
//                intent = Intent(this, IntroSliderActivity::class.java)
//                startActivity(intent)
                checkIfUserIsAuthenticated()
            }, SPLASH_TIME_OUT)
    }

    private fun initSplashScreenViewModel() {
        Log.d("INIT", "INIT VIEW MODEL")
        splashScreenViewModel = ViewModelProvider(this).get(SplashScreenViewModel::class.java)
    }

    private fun initSharedPreferences() {
        AppPreferences.init(this)
    }

    private fun checkIfUserIsAuthenticated() {
        Log.d("INIT", "CHECK USER")
        splashScreenViewModel.checkIfUserIsAuthenticated()
        splashScreenViewModel.isUserAuthenticatedLiveData.observe(this, Observer {
            Log.d("OBSERVE", "OBSERVE IS AUTHENTICATED OR NOT")
            if (!it?.isAuthenticated!!) {
                if (AppPreferences.isLogin) {
                    goToTodoLocalActivity()
                } else {
                    goToIntroActivity()
                }
                finish()
            } else {
                Log.d("OBSERVE", "OBSERVE IS AUTHENTICATED")
                getUserFromDatabase(it.uid)
            }
        })
    }

    private fun goToIntroActivity() {
        Log.d("INTENT", "MASUK INTRO")
        intent = Intent(this, IntroSliderActivity::class.java)
        startActivity(intent)
    }

    private fun getUserFromDatabase(uid: String) {
        Log.d("GET", "GET USER FROM DATABASE")
        Log.d("UID", uid)
        splashScreenViewModel.setUid(uid)
        splashScreenViewModel.userLiveData.observe(this, Observer {
            Log.d("GO", "GO TO MAIN AFTER GET USER")
            goToMainActivity(it)
            finish()
        })
    }

    private fun goToTodoLocalActivity() {
        intent = Intent(this, TodoLocalActivity::class.java)
        startActivity(intent)
    }

    private fun goToMainActivity(user : User) {
        Log.d("INTENT", "MASUK MAIN ACTIVITY")
        intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Const.Collection.USER, user)
        startActivity(intent)
    }
}