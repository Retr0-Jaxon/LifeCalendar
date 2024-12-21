package com.example.lifecalendar

import android.content.Context
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.lifecalendar.databinding.ActivityMainBinding
import com.example.lifecalendar.ui.birthdayDialog.BirthdayDialogFragment
import java.util.Date
import java.util.Locale
import kotlin.text.format
import android.content.Intent

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
        } else {
            // User has set birthday, proceed to main app logic
            val birthday = getBirthday()
            Log.d(birthday.toString(), "birthday: ${birthday.toString()}")
            // Do something with the birthday
        }

        // 启动BirthdayService
        val serviceIntent = Intent(this, BirthdayService::class.java)
        startService(serviceIntent)
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