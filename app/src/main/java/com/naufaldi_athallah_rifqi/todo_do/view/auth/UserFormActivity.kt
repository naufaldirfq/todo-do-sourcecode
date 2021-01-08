package com.naufaldi_athallah_rifqi.todo_do.view.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.naufaldi_athallah_rifqi.todo_do.R
import com.naufaldi_athallah_rifqi.todo_do.utils.helper.AppPreferences
import com.naufaldi_athallah_rifqi.todo_do.utils.helper.Toaster
import com.naufaldi_athallah_rifqi.todo_do.view.todo.TodoLocalActivity
import kotlinx.android.synthetic.main.activity_user_form.*

class UserFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_form)
        Log.d("USER FORM", "MUNCUL")
        initSharedPreference()
        initView()
    }

    private fun initSharedPreference() {
        AppPreferences.init(this)
    }

    private fun initView() {
        save_name.setOnClickListener {
            val username = et_name.text.toString()
            AppPreferences.username = username
            AppPreferences.isLogin = true
            AppPreferences.image = ""
            Toaster(this).showToast("Hello $username")
            intent = Intent(this, TodoLocalActivity::class.java)
            startActivity(intent)
        }
    }
}