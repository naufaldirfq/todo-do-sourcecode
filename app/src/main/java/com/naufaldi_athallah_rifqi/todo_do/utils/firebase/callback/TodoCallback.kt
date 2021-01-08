package com.naufaldi_athallah_rifqi.todo_do.utils.firebase.callback

import com.naufaldi_athallah_rifqi.todo_do.data.models.Todo

interface TodoCallback {
    fun onResponse(todo: Todo?, error: String?)
}