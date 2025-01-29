package com.example.breeze


import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        InternetChecker().checkInternet(this, lifecycle)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        //createNotificationChannel()
        replaceWithFragment(Home())


        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> replaceWithFragment(Home()) // Show Home Fragment
                R.id.search_action -> replaceWithFragment(SearchFragment()) // Show Search Fragment
                R.id.bookmarks -> replaceWithFragment(Bookmarks()) // Show Bookmarks Fragment
            }
            true
        }
    }



    private fun replaceWithFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commitAllowingStateLoss()
    }

    /*import android.app.NotificationChannel
    import android.app.NotificationManager
    import android.os.Build

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default_channel_id"
            val channelName = "Default Channel"
            val channelDescription = "Channel for general notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            /
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

*/

}

