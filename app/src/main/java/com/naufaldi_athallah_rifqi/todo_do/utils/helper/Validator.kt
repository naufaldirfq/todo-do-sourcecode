package com.naufaldi_athallah_rifqi.todo_do.utils.helper

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

object Validator {

    // this function receives TextInputEditText list as input and perform validation.
    // here, validation is simple: the input is empty or not.
    // if all input fields are okay it enables the submit button of the dialog
    fun forceValidation(editTextArray: Array<EditText>, dialog: AlertDialog) {
        editTextArray.forEach {editText ->
            editText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    var shouldEnable = !TextUtils.isEmpty(s)
                    if(editTextArray.size > 1) {
                        editTextArray.forEach {
                            if(editText != it)
                                shouldEnable = shouldEnable && !TextUtils.isEmpty(it.text)
                        }
                    }
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = shouldEnable
                }
            })
        }
    }
}