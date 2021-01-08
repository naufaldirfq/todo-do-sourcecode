package com.naufaldi_athallah_rifqi.todo_do

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.naufaldi_athallah_rifqi.todo_do.data.models.Todo
import com.naufaldi_athallah_rifqi.todo_do.data.models.User
import com.naufaldi_athallah_rifqi.todo_do.databinding.PromptTodoBinding
import com.naufaldi_athallah_rifqi.todo_do.utils.Const
import com.naufaldi_athallah_rifqi.todo_do.utils.firebase.FirestoreService
import com.naufaldi_athallah_rifqi.todo_do.utils.firebase.callback.TodoCallback
import com.naufaldi_athallah_rifqi.todo_do.utils.firebase.callback.TodoListCallback
import com.naufaldi_athallah_rifqi.todo_do.utils.helper.*
import com.naufaldi_athallah_rifqi.todo_do.view.auth.IntroSliderActivity
import com.naufaldi_athallah_rifqi.todo_do.view.todo.TodoAdapter
import com.naufaldi_athallah_rifqi.todo_do.view.todo.TodoClickEvent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_profile.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener, DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private var day = 0
    private var month: Int = 0
    private var year: Int = 0
    private var hour: Int = 0
    private var minute: Int = 0
    private var textDate: String = ""
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val remote: FirestoreService by lazy { FirestoreService() }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "10001"
        private val default_notification_channel_id = "default"
    }

    private val adapter = TodoAdapter()
    private lateinit var cm: ConnectivityManager

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: PromptTodoBinding
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(bottom_app_bar)
        val user : User = getUserFromIntent()
        initGoogleSignInClient()
        initView(user)
        initConnectionManager()
        updateStatus(user)
        addTodoListListener(user)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.bottomappbar_menu, menu)
        return true
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(
            this@MainActivity, this@MainActivity, hour, minute,
            DateFormat.is24HourFormat(this)
        )
        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        updateDateInView()
    }

    private fun updateDateInView() {
        val format = "dd/MM/yyyy hh:mm a"
        val sdf = SimpleDateFormat(format, Locale.US)
        textDate = sdf.format(calendar.time)
        binding.tietTodoDate.text = SpannableStringBuilder(textDate)
    }

    private fun scheduleNotification(notification: Notification, delay: Long) {
        intent = Intent(this, MyNotificationPublisher::class.java)
        intent.putExtra(MyNotificationPublisher.NOTIFICATION_ID, 1)
        intent.putExtra(MyNotificationPublisher.NOTIFICATION, notification)
        val pendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, delay, pendingIntent)
    }

    private fun getNotification(content: String, title: String): Notification {
        val builder : NotificationCompat.Builder = NotificationCompat.Builder(this,
            default_notification_channel_id
        )
        builder.setContentTitle(title)
        builder.setContentText(content)
        builder.setSmallIcon(R.drawable.ic_notif)
        builder.setAutoCancel(true)
        builder.setChannelId(NOTIFICATION_CHANNEL_ID)
        return builder.build()
    }

    private fun initBinding() {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.prompt_todo, null, false
        )
    }

    private fun initConnectionManager() {
        cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private fun getUserFromIntent() : User {
        return intent.getSerializableExtra(Const.Collection.USER) as User
    }

    private fun initGoogleSignInClient() {
        val googleSignInOptions : GoogleSignInOptions = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private fun ImageView.load(url: String?){
        ImageLoader.load(url, this)
    }

    private fun initView(user: User) {
        adapter.setListener(object: TodoClickEvent {
            override fun onClickTodo(todo: Todo, action: String, position: Int) {
                when(action) {
                    TodoClickEvent.ACTION_COMPLETE -> toggleMarkAsComplete(todo, position)
                    TodoClickEvent.ACTION_DETAILS -> showDetails(todo)
                    TodoClickEvent.ACTION_EDIT -> editTodo(todo, position)
                    TodoClickEvent.ACTION_DELETE -> deleteTodo(todo, position)
                }
            }
        })

        rv_todo_list.layoutManager = LinearLayoutManager(this)
        rv_todo_list.adapter = adapter


        add_todo.setOnClickListener { addTodo(user) }
        swipe_refresh.setOnRefreshListener {
            Handler().postDelayed(Runnable {
                swipe_refresh.isRefreshing = false
            }
                , 4000)
            loadTodoList(user) }
    }

    private fun addTodo(user: User) {
        initBinding()
        binding.tietTodoDate.setOnClickListener {
            Log.d("TODO DATE", "Clicked")
            val calendar: Calendar = Calendar.getInstance()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog =
                DatePickerDialog(this@MainActivity, this@MainActivity, year, month, day)
            datePickerDialog.show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.label_add_todo)
            .setView(binding.root)
            .setPositiveButton(R.string.label_add_todo) {
                    _, _ ->
                swipe_refresh.isRefreshing = true

                val todoTitle = binding.tietTodoTitle.text.toString()
                val todoDate = binding.tietTodoDate.text.toString()
                val todo = Todo(
                    "", todoTitle, false, todoDate, user.uid!!, ""
                )

                remote.addTodo(todo, object: TodoCallback {
                    override fun onResponse(todo: Todo?, error: String?) {
                        swipe_refresh.isRefreshing = false
                        if(error == null) {
                            Toaster(this@MainActivity).showToast(getString(R.string.add_todo_success_message))
                            img_no_data.visibility = View.INVISIBLE
                            txt_nodata.visibility = View.INVISIBLE
                            rv_todo_list.visibility = View.VISIBLE
                            adapter.addTodo(todo!!)
                            scheduleNotification(getNotification(todoDate, todoTitle), calendar.time.time)
                        }else {
                            Toaster(this@MainActivity).showToast(error)
                        }
                    }
                })
            }
            .setNegativeButton(R.string.label_cancel) { _,_-> }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
        dialog.show()

         Validator.forceValidation(arrayOf(binding.tietTodoTitle, binding.tietTodoDate), dialog)
    }

    private fun addTodoListListener(user: User) {
        remote.addTodoListListener(user, object: TodoListCallback {
            override fun onResponse(todoList: ArrayList<Todo>?, error: String?) {
                if (error != null) {
                    Toaster(this@MainActivity).showToast(error)
                }else {
                    if(todoList!!.size > 0) {
                        img_no_data.visibility = View.INVISIBLE
                        txt_nodata.visibility = View.INVISIBLE
                        rv_todo_list.visibility = View.VISIBLE
                        adapter.setTodoList(todoList)
                        updateStatus(user)
                    }else {
                        img_no_data.visibility = View.VISIBLE
                        txt_nodata.visibility = View.VISIBLE
                        rv_todo_list.visibility = View.INVISIBLE
                    }
                }
            }
        })
    }

    private fun loadTodoList(user: User) {
        swipe_refresh.isRefreshing = true
        remote.getTodoList(user, object: TodoListCallback {
            override fun onResponse(todoList: ArrayList<Todo>?, error: String?) {
                swipe_refresh.isRefreshing = false
                if (error != null) {
                    Toaster(this@MainActivity).showToast(error)
                }else {
                    if(todoList!!.size > 0) {
                        img_no_data.visibility = View.INVISIBLE
                        txt_nodata.visibility = View.INVISIBLE
                        rv_todo_list.visibility = View.VISIBLE
                        adapter.setTodoList(todoList)
                        updateStatus(user)
                    }else {
                        img_no_data.visibility = View.VISIBLE
                        txt_nodata.visibility = View.VISIBLE
                        rv_todo_list.visibility = View.INVISIBLE
                    }
                }
            }
        })
    }

    private fun updateStatus(user: User) {

        container_profile.img_profile.load(user.image)
        container_profile.tv_name.text = user.name
        container_profile.visibility = View.VISIBLE

        var status = getString(R.string.label_no_todo_list_found)
        if(adapter.itemCount > 0) {
            status = "${adapter.itemCount} to-do(s) found"
        }

        container_profile.tv_status.text = status

        val calender = Calendar.getInstance()
        val day = calender.get(Calendar.DAY_OF_MONTH)

        container_profile.tv_dd.text = day.toString()
        container_profile.tv_MMM.text = FormatUtil().toMonth(calender.time)
        container_profile.tv_day.text = FormatUtil().toDay(calender.time)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val builder: NetworkRequest.Builder = NetworkRequest.Builder()
            cm.registerNetworkCallback(

                builder.build(),
                object : ConnectivityManager.NetworkCallback() {

                    override fun onAvailable(network: Network) {
                        lifecycleScope.launch {
                            Log.i("MainActivity", "onAvailable!")

                            // check if NetworkCapabilities has TRANSPORT_WIFI
                            val isWifi:Boolean = cm.getNetworkCapabilities(network).hasTransport(
                                NetworkCapabilities.TRANSPORT_WIFI)

                            doSomething(true, isWifi)
                        }
                    }

                    override fun onLost(network: Network) {
                        lifecycleScope.launch {
                            Log.i("MainActivity", "onLost!")
                            doSomething(false)
                        }
                    }
                }
            )
        }
    }

    private suspend fun doSomething(isConnected:Boolean, isWifi:Boolean= false){
        withContext(Dispatchers.Main){
            if(isConnected) {
                img_no_data.visibility = View.INVISIBLE
                txt_nodata.visibility = View.INVISIBLE
                rv_todo_list.visibility = View.VISIBLE
            }else {
                img_no_data.visibility = View.VISIBLE
                txt_nodata.visibility = View.VISIBLE
                txt_nodata.text = "Tidak ada koneksi internet!"
                rv_todo_list.visibility = View.INVISIBLE
            }
        }
    }

    private fun toggleMarkAsComplete(todo: Todo, position: Int) {

        swipe_refresh.isRefreshing = true

        var successMessage = R.string.task_marked_as_completed_success_message
        if(todo.completed) successMessage = R.string.task_marked_as_incomplete_success_message
        todo.completed = !todo.completed
        remote.markTodoListAsComplete(arrayListOf(todo), object: TodoListCallback {
            override fun onResponse(todoList: ArrayList<Todo>?, error: String?) {
                swipe_refresh.isRefreshing = false
                if(error == null) {
                    Toaster(this@MainActivity).showToast(getString(successMessage))
                }else {
                    Toaster(this@MainActivity).showToast(error)
                }
            }
        })
    }

    private fun showDetails(todo: Todo) {

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.d("ON OPTIONS ITEM", "CLICKED")
        when (item!!.itemId) {
            R.id.app_bar_delete_todo -> {
                deleteAllTodo()
                return true

            }
            R.id.app_bar_sign_out -> {
                signOut()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllTodo() {

    }

    private fun markAllAsCompletedTodo() {

    }

    private fun editTodo(todo: Todo, position: Int) {
        initBinding()
        val currentTitle = todo.todo
        val todoDate = todo.date
        binding.tietTodoDate.text = SpannableStringBuilder(todoDate)
        binding.tietTodoTitle.text = SpannableStringBuilder(currentTitle)
        binding.tietTodoTitle.setSelection(todo.todo.length)
        binding.tietTodoDate.setOnClickListener {
            Log.d("TODO DATE", "Clicked")
            val calendar: Calendar = Calendar.getInstance()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog =
                DatePickerDialog(this@MainActivity, this@MainActivity, year, month, day)
            datePickerDialog.show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.label_edit_todo)
            .setView(binding.root)
            .setPositiveButton(R.string.label_edit_todo) {
                    _, _ ->
                swipe_refresh.isRefreshing = true

                val todoTitle = binding.tietTodoTitle.text.toString()
                val todoDate = binding.tietTodoDate.text.toString()

                todo.todo = todoTitle
                todo.date = todoDate

                remote.updateTodo(todo, object: TodoCallback {
                    override fun onResponse(todo: Todo?, error: String?) {
                        swipe_refresh.isRefreshing = false
                        scheduleNotification(getNotification(todoDate, todoTitle), calendar.time.time)
                        if(error == null) {
                            Toaster(this@MainActivity).showToast(getString(R.string.update_todo_success_message))
//                            adapter.addTodo(todo!!)
                        }else {
                            Toaster(this@MainActivity).showToast(error)
                        }
                    }
                })
            }
            .setNegativeButton(R.string.label_cancel) { _,_-> }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
        dialog.show()

        Validator.forceValidation(arrayOf(binding.tietTodoTitle, binding.tietTodoDate), dialog)
    }

    private fun deleteTodo(todo: Todo, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Are you sure?")
            .setMessage("Warning: this task will be deleted permanently")
            .setPositiveButton(R.string.label_delete) {
                    _,_->
                swipe_refresh.isRefreshing = true
                remote.deleteTodo(todo, object: TodoCallback {
                    override fun onResponse(todo: Todo?, error: String?) {
                        swipe_refresh.isRefreshing = false
                        if(error == null) {
                            Toaster(this@MainActivity).showToast(getString(R.string.delete_todo_success_message))
                            adapter.getTodoList().remove(todo)
                            adapter.notifyDataSetChanged()
                        }else {
                            Toaster(this@MainActivity).showToast(error)
                        }
                    }
                })
            }
            .setNegativeButton(R.string.label_cancel) { _,_-> }
            .create()
            .show()
    }



    override fun onAuthStateChanged(p0: FirebaseAuth) {
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
        if (firebaseUser == null) {
            goToAuthInActivity()
        }
    }

    private fun goToAuthInActivity() {
        intent = Intent(this, IntroSliderActivity::class.java)
        startActivity(intent)
    }

    private fun signOut() {
        singOutFirebase()
        signOutGoogle()
        val i = baseContext.packageManager
            .getLaunchIntentForPackage(baseContext.packageName)
        i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
    }

    private fun singOutFirebase() {
        firebaseAuth.signOut()
    }

    private fun signOutGoogle() {
        googleSignInClient.signOut()
    }


}