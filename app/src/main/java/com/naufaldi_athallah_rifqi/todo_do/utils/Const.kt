package com.naufaldi_athallah_rifqi.todo_do.utils

object Const {
    //Request codes in one place
    interface RequestCode {
        companion object {
            const val AUTH = 11
            const val RC_SIGN_IN = 123
        }
    }

    //Collections used in firestore
    interface Collection {
        companion object {
            const val USER = "user"
            const val TODO = "todo"
            const val TAG = "FirebaseAuthAppTag"
            const val USERS = "users"
        }
    }

    //each collection's fields
    interface Key {
        interface User {
            companion object {
                const val ID = "id"
                const val NAME = "name"
                const val EMAIL = "email"
                const val IMAGE = "image"
            }
        }

        interface Todo {
            companion object {
                const val ID = "id"
                const val TODO = "todo"
                const val COMPLETED = "completed"
                const val DATE = "date"
                const val USER = "user"
                const val CREATED_AT = "created_at"
            }
        }
    }
}