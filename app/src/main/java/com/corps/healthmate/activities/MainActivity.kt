package com.corps.healthmate.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.corps.healthmate.R
import com.corps.healthmate.fragment.*
import com.corps.healthmate.navigation.NavigationManager
import com.corps.healthmate.utils.BottomNavIndicator
import com.corps.healthmate.utils.MedicineDataLoader
import com.corps.healthmate.utils.NetworkUtils
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val NOTIFICATION_PERMISSION_CODE = 101
    }

    private var currentFragmentId: Int = R.id.nav_ai_assist
    private var isNetworkAvailable = true
    private var selectedTab: View? = null
    private lateinit var bottomNavIndicator: BottomNavIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Make navigation bar transparent and extend behind it
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupStatusBar()
        setupBottomNavigation()
        requestPermissions()
        setupNetworkMonitoring()
        setupEmergencyButton()

        // Select home tab by default
        findViewById<View>(R.id.nav_home).performClick()

        handleFragmentNavigation(intent)
    }

    private fun setupBottomNavigation() {
        // Initialize bottom navigation indicator
        val indicator = findViewById<View>(R.id.bottom_nav_indicator)
        val bottomNavContainer = findViewById<LinearLayout>(R.id.bottom_navigation_container)
        bottomNavIndicator = BottomNavIndicator(indicator, bottomNavContainer)

        // Setup click listeners for navigation items
        findViewById<LinearLayout>(R.id.nav_home).setOnClickListener {
            updateNavigation(it, AiAssistFragment(), 0)
        }

        findViewById<LinearLayout>(R.id.nav_inbox).setOnClickListener {
            updateNavigation(it, DoctorsFragment(), 1)
        }

        findViewById<LinearLayout>(R.id.nav_calendar).setOnClickListener {
            updateNavigation(it, ChatFragmentPatient(), 3)
        }

        findViewById<LinearLayout>(R.id.nav_profile).setOnClickListener {
            updateNavigation(it, GamificationFragment(), 4)
        }

        // Setup emergency call button
        findViewById<LinearLayout>(R.id.nav_call).setOnClickListener {
            // Handle emergency call action
            handleEmergencyCall()
        }

        // Set home as default selection
        findViewById<LinearLayout>(R.id.nav_home).performClick()
    }

    private fun updateNavigation(view: View, fragment: Fragment, position: Int) {
        if (selectedTab != view) {
            selectedTab = view
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
            
            // Update colors
            updateNavigationColors(view.id)
            
            // Update indicator
            bottomNavIndicator.updateIndicatorPosition(position)
        }
    }

    private fun updateNavigationColors(selectedId: Int) {
        val navItems = listOf(
            R.id.nav_home,
            R.id.nav_inbox,
            R.id.nav_calendar,
            R.id.nav_profile
        )

        navItems.forEach { itemId ->
            val item = findViewById<LinearLayout>(itemId)
            val icon = item.getChildAt(0) as ImageView
            val text = item.getChildAt(1) as TextView
            
            if (itemId == selectedId) {
                icon.setColorFilter(getColor(R.color.tab_selected)) 
                text.setTextColor(getColor(R.color.tab_selected))
            } else {
                icon.setColorFilter(getColor(R.color.tab_unselected))
                text.setTextColor(getColor(R.color.tab_unselected))
            }
        }
    }

    private fun handleEmergencyCall() {
        // Implement emergency call functionality
        Toast.makeText(this, "Emergency Call Initiated", Toast.LENGTH_SHORT).show()
        // Add your emergency call implementation here
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
        startActivity(Intent(this, EmergencyHandlerActivity::class.java))
    }

    private fun setupNetworkMonitoring() {
        lifecycleScope.launch {
            NetworkUtils.getNetworkStatus(applicationContext)
                .distinctUntilChanged()
                .collect { isConnected ->
                    isNetworkAvailable = isConnected
                    if (isConnected) {
                        loadAppropriateFragment(currentFragmentId)
                    } else {
                        showNoInternetFragment()
                    }
                }
        }
    }

    private fun setupEmergencyButton() {
        val fabEmergency = findViewById<ImageView>(R.id.fabEmergency)
        
        fabEmergency.setOnClickListener {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
            startActivity(Intent(this, EmergencyHandlerActivity::class.java))
        }
    }

    private fun loadAppropriateFragment(itemId: Int) {
        if (!isNetworkAvailable) {
            showNoInternetFragment()
            return
        }

        val fragment: Fragment = when (itemId) {
            R.id.nav_ai_assist -> AiAssistFragment()
            R.id.nav_doctor -> DoctorsFragment()
            R.id.nav_chat_patient -> ChatFragmentPatient()
            R.id.nav_gamification -> GamificationFragment()
            else -> AiAssistFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showNoInternetFragment() {
        val noInternetFragment = NoInternetFragment.newInstance {
            if (isNetworkAvailable) {
                loadAppropriateFragment(currentFragmentId)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, noInternetFragment)
            .commit()
    }

    private fun requestPermissions() {
        // Request notification permission for Android 13+ if required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM)
        } else {
            emptyArray() // Empty array for devices below API 31
        }

        // Check if permissions are already granted
        var isPermissionGranted = true
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                isPermissionGranted = false
                break
            }
        }

        // Request permissions only if not already granted
        if (!isPermissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleFragmentNavigation(intent)
    }

    private fun handleFragmentNavigation(intent: Intent?) {
        intent?.getStringExtra("openFragment")?.let { fragmentToOpen ->
            if (fragmentToOpen == "home") {
                openHomeFragment()
            }
        }
    }

    private fun openHomeFragment() {
        val homeFragment = AiAssistFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, homeFragment)
            .commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (i in permissions.indices) {
                if (permissions[i] == Manifest.permission.SCHEDULE_EXACT_ALARM) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        // Permission granted
                        Toast.makeText(this, "Alarm permission granted.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Permission denied
                        Toast.makeText(this, "Alarm permission denied.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    private fun setupStatusBar() {
        // Make system bars work better with edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Make status bar and navigation bar transparent
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)

        // Get the WindowInsetsController
        val decorView = window.decorView
        val insetsController = WindowInsetsControllerCompat(window, decorView)

        // Light status bar and navigation bar icons for light theme
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true
    }

    private fun forceMedicineReload() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MedicineDataLoader(applicationContext).forceReloadMedicines()
            } catch (e: Exception) {
                Timber.e(e, "Failed to reload medicines")
            }
        }
    }
}
