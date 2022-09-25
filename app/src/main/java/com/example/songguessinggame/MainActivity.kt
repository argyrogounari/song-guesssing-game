package com.example.songguessinggame

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*
import java.util.concurrent.TimeUnit

const val WORK_REQUEST_HOUR_OF_DAY = 12
const val WORK_REQUEST_MINUTES = 0
const val WORK_REQUEST_SECONDS = 0
const val WORK_REQUEST_HOUR_OF_DAY_AMOUNT = 12

/**
 * The Main Activity of the app.
 *
 * All the fragments are attached to this.
 *
 * @constructor Creates an activity.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAppFirstRun()
        setUpBottomNavigation()
        defineWorkRequestUpdatesMarkers()
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.white)))
    }

    /**
     * Sets app the navigation through the BottomNavigationBar.
     */
    private fun setUpBottomNavigation() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        setupActionBarWithNavController(navController)
        navView.setupWithNavController(navController)
    }

    /**
     * Adds a boolean indication if this app was
     * run for the first time in SharedPreferences.
     */
    private fun checkAppFirstRun() {
        val prefs = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val notExist = -1
        val savedVersionCode = prefs.getInt(getString(R.string.version_code), notExist)

        if (savedVersionCode == notExist) {
                prefs.edit().putBoolean(getString(R.string.first_time_app_used), true).apply()
        }

        val currentVersionCode = BuildConfig.VERSION_CODE
        prefs.edit().putInt(getString(R.string.version_code), currentVersionCode).apply()
    }

    /**
     * Define work request to update the location of
     * the markers in the map everyday.
     */
    private fun defineWorkRequestUpdatesMarkers() {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()

        dueDate.set(Calendar.HOUR_OF_DAY, WORK_REQUEST_HOUR_OF_DAY)
        dueDate.set(Calendar.MINUTE, WORK_REQUEST_MINUTES)
        dueDate.set(Calendar.SECOND, WORK_REQUEST_SECONDS)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, WORK_REQUEST_HOUR_OF_DAY_AMOUNT)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val updateMarkersWorkRequest = OneTimeWorkRequestBuilder<UpdateMarkers>()
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag(getString(R.string.new_markers))
            .build()

        WorkManager.getInstance(this).enqueue(updateMarkersWorkRequest)
    }

    /**
     * Define back button functionality to comply
     * with added fragments.
     */
    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }
}
