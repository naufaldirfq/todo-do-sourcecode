package com.naufaldi_athallah_rifqi.todo_do.view.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.naufaldi_athallah_rifqi.todo_do.data.models.local.UserLocal
import com.naufaldi_athallah_rifqi.todo_do.data.repositories.UserRepository

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository = UserRepository(application)
    private val allUserLocalList: LiveData<List<UserLocal>> = repository.getAllUserList()

    fun addUser(userLocal: UserLocal) {
        repository.addUser(userLocal)
    }

    fun updateUser(userLocal: UserLocal){
        repository.updateUser(userLocal)
    }

    fun deleteUser(userLocal: UserLocal) {
        repository.deleteUser(userLocal)
    }

    fun getAllUserList(): LiveData<List<UserLocal>> {
        return allUserLocalList
    }

    fun getUserWithId(id: Int){
        repository.getUserWithId(id)
    }

}