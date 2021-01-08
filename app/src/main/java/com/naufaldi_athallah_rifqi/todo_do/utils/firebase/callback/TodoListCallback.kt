package com.naufaldi_athallah_rifqi.todo_do.utils.firebase.callback

import com.naufaldi_athallah_rifqi.todo_do.data.models.Todo

interface TodoListCallback {
    fun onResponse(todoList: ArrayList<Todo>?, error: String?)
}