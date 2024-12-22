package com.example.lifecalendar


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.lifecalendar.databinding.ActivityMainBinding
import com.example.lifecalendar.ui.birthdayDialog.BirthdayDialogFragment
import com.example.lifecalendar.ui.lifespanDialog.lifeSpanFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity(), BirthdayDialogFragment.OnBirthdaySetListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_BIRTHDAY = "birthday"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_setting
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (!isBirthdaySet()) {
            showBirthdayDialog()
//            val birthday = getBirthday()
//            Log.d("dddd", "birthday: ${birthday.toString()}")
        } else {
            // User has set birthday, proceed to main app logic
            val birthday = getBirthday()
            Log.d("dddd", "birthday: ${birthday.toString()}")
            // Do something with the birthday
        }

        // 启动BirthdayService
        val serviceIntent = Intent(this, BirthdayService::class.java)
        startService(serviceIntent)
        // 查询是否成功存储时间字符串
        val contentResolver = contentResolver
        val projection = arrayOf(LifeCalendarProvider.LIFESPAN_COLUMN_TIME)
        val cursor = contentResolver.query(
            LifeCalendarProvider.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        if (cursor!= null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(LifeCalendarProvider.LIFESPAN_COLUMN_TIME)
            if (index!= -1) {
                // 获取存储的时间字符串
                val storedString = cursor.getString(index)
                Log.d("MainActivity", "Stored string: $storedString")
            }
            cursor.close()
        }

    }

    private fun isBirthdaySet(): Boolean {
        return sharedPreferences.contains(PREF_BIRTHDAY)
    }

    private fun showBirthdayDialog() {
        val dialog = BirthdayDialogFragment()
        dialog.show(supportFragmentManager, "birthday_dialog")
    }

    override fun onBirthdaySet(date: Date) {
        saveBirthday(date)
        // Proceed to main app logic
        val birthday = getBirthday()
        // Do something with the birthday
        if (!isLifespanSet()) {
            // 显示 lifeSpanFragment
            val lifeSpanFragment = lifeSpanFragment()
            lifeSpanFragment.show(supportFragmentManager, "life_span_dialog")
        }
    }



    private fun isLifespanSet(): Boolean {
        val uri = LifeCalendarProvider.CONTENT_URI
        val projection = arrayOf(LifeCalendarProvider.LIFESPAN_COLUMN_WEEKS)
        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        return cursor?.use {
            it.moveToFirst() // 如果有数据，返回 true
        } ?: false // 如果没有数据，返回 false
    }

    private fun saveBirthday(date: Date) {
        val editor = sharedPreferences.edit()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        editor.putString(PREF_BIRTHDAY, formatter.format(date))
        editor.apply()
    }

    private fun getBirthday(): Date? {
        val birthdayString = sharedPreferences.getString(PREF_BIRTHDAY, null)
        return if (birthdayString != null) {
            val formatter =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.parse(birthdayString)
        } else {
            null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}