package com.naufaldi_athallah_rifqi.todo_do.view.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.naufaldi_athallah_rifqi.todo_do.R

class IntroSecondFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intro_second, container, false)
    }


}