package com.example.lifecalendar


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.example.lifecalendar.ui.home.HomeFragment
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
            // 检查当前是否在 HomeFragment
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
            val currentFragment = navHostFragment?.childFragmentManager?.fragments?.find { it.isVisible }
            
            if (currentFragment is HomeFragment) {
                // 如果当前在 HomeFragment，直接滚动
                currentFragment.scrollToCurrentWeek()
            } else {
                // 如果不在 HomeFragment，先导航再滚动
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.nav_home)
                
                view.post {
                    val homeFragment = navHostFragment?.childFragmentManager?.fragments?.find { 
                        it is HomeFragment 
                    } as? HomeFragment
                    homeFragment?.scrollToCurrentWeek()
                }
            }
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

        val serviceIntent = Intent(this, BirthdayService::class.java)
        startService(serviceIntent)

        val navController2 = findNavController(R.id.nav_host_fragment_content_main)
        navController2.addOnDestinationChangedListener { _, destination, _ ->
            invalidateOptionsMenu() // This will trigger onPrepareOptionsMenu
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
        val selection = "${LifeCalendarProvider.LIFESPAN_COLUMN_WEEKS} IS NOT NULL"
        val cursor: Cursor? = contentResolver.query(uri, projection, selection, null, null)
        return cursor?.use {
            it.moveToFirst()
        } ?: false
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
        menuInflater.inflate(R.menu.main, menu)
        
        // 从 SharedPreferences 读取显示周数的设置
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val showNumbers = sharedPreferences.getBoolean("show_numbers", true)
        menu.findItem(R.id.action_show_numbers).isChecked = showNumbers
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_numbers -> {
                item.isChecked = !item.isChecked
                // 保存设置到 SharedPreferences
                getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("show_numbers", item.isChecked)
                    .apply()
                
                // 更新 HomeFragment
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
                val homeFragment = navHostFragment?.childFragmentManager?.fragments?.find { 
                    it is HomeFragment 
                } as? HomeFragment
                homeFragment?.refreshRecyclerView()
                
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.find { it.isVisible }

        // 仅在 HomeFragment 时显示菜单
        menu.findItem(R.id.action_show_numbers).isVisible = currentFragment is HomeFragment

        return super.onPrepareOptionsMenu(menu)
    }
}