package com.byagowi.persiancalendar.ui

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Display
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import cn.leancloud.LCObject
import cn.leancloud.LCQuery
import com.byagowi.persiancalendar.CALENDAR_READ_PERMISSION_REQUEST_CODE
import com.byagowi.persiancalendar.CHANGE_LANGUAGE_IS_PROMOTED_ONCE
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_HAS_EVER_VISITED
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET_SET_DATE
import com.byagowi.persiancalendar.PREF_LAST_APP_VISIT_VERSION
import com.byagowi.persiancalendar.PREF_NEW_INTERFACE
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.Variants.debugAssertNotNull
import com.byagowi.persiancalendar.core.dialog.PrivacyDialog
import com.byagowi.persiancalendar.core.dialog.PrivacyPolicyActivity
import com.byagowi.persiancalendar.core.dialog.TermsActivity
import com.byagowi.persiancalendar.core.utils.SharedPreferenceUtils
import com.byagowi.persiancalendar.core.utils.Utils
import com.byagowi.persiancalendar.databinding.ActivityMainBinding
import com.byagowi.persiancalendar.databinding.NavigationHeaderBinding
import com.byagowi.persiancalendar.entities.CalendarType
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.global.configureCalendarsAndLoadEvents
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.enableNewInterface
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.global.isIranHolidaysEnabled
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.ui.calendar.CalendarFragmentDirections
import com.byagowi.persiancalendar.ui.preferences.PreferencesFragment
import com.byagowi.persiancalendar.ui.utils.askForCalendarPermission
import com.byagowi.persiancalendar.ui.utils.bringMarketPage
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.makeStatusBarTransparent
import com.byagowi.persiancalendar.ui.utils.makeWallpaperTransparency
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.getAppFont
import com.byagowi.persiancalendar.utils.isRtl
import com.byagowi.persiancalendar.utils.overrideFont
import com.byagowi.persiancalendar.utils.putJdn
import com.byagowi.persiancalendar.utils.readAndStoreDeviceCalendarEventsOfTheDay
import com.byagowi.persiancalendar.utils.startEitherServiceOrWorker
import com.byagowi.persiancalendar.utils.supportedYearOfIranCalendar
import com.byagowi.persiancalendar.utils.update
import com.google.android.material.navigation.NavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.lang.Exception
import kotlin.system.exitProcess

/**
 * Program activity for android
 */
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener,
    NavigationView.OnNavigationItemSelectedListener, NavController.OnDestinationChangedListener,
    DrawerHost {

    private var creationDateJdn: Jdn? = null
    private var settingHasChanged = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var time: String

    private val onBackPressedCloseDrawerCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = binding.drawer.closeDrawer(GravityCompat.START)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)

        window?.makeStatusBarTransparent()

        onBackPressedDispatcher.addCallback(this, onBackPressedCloseDrawerCallback)
        initGlobal(this)

        // Don't apply font override to non Arabic script languages
        if (language.isArabicScript) overrideFont("SANS_SERIF", getAppFont(applicationContext))

        startEitherServiceOrWorker(this)

        readAndStoreDeviceCalendarEventsOfTheDay(applicationContext)
        update(applicationContext, false)

        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        ensureDirectionality()

        if (enableNewInterface &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            getSystemService<ActivityManager>()?.isLowRamDevice == false
        ) {
            window?.makeWallpaperTransparency()
            binding.drawer.fitsSystemWindows = false
            binding.drawer.background = MaterialShapeDrawable().also {
                it.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(16.dp)
            }
            binding.drawer.clipToOutline = true
            binding.drawer.alpha = 0.96f
            binding.root.fitsSystemWindows = false
        }

        binding.drawer.addDrawerListener(createDrawerListener())

        navHostFragment?.navController?.addOnDestinationChangedListener(this)
        when (intent?.action) {
            "COMPASS" -> R.id.compass
            "LEVEL" -> R.id.level
            "CONVERTER" -> R.id.converter
            "SETTINGS" -> R.id.settings
            "DEVICE" -> R.id.deviceInformation
            else -> null // unsupported action. ignore
        }?.also {
            navigateTo(it)
            // So it won't happen again if the activity is restarted
            intent?.action = ""
        }

        appPrefs.registerOnSharedPreferenceChangeListener(this)

        if (isShowDeviceCalendarEvents && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) askForCalendarPermission()

        binding.navigation.setNavigationItemSelectedListener(this)

        NavigationHeaderBinding.bind(binding.navigation.getHeaderView(0))
            .seasonImage.setImageResource(run {
                var season = (Jdn.today.toPersianCalendar().month - 1) / 3

                // Southern hemisphere
                if ((coordinates?.latitude ?: 1.0) < .0) season = (season + 2) % 4

                when (season) {
                    0 -> R.drawable.spring
                    1 -> R.drawable.summer
                    2 -> R.drawable.fall
                    else -> R.drawable.winter
                }
            })

        if (!appPrefs.getBoolean(CHANGE_LANGUAGE_IS_PROMOTED_ONCE, false)) {
            showChangeLanguageSnackbar()
            appPrefs.edit { putBoolean(CHANGE_LANGUAGE_IS_PROMOTED_ONCE, true) }
        }

        creationDateJdn = Jdn.today

        if (mainCalendar == CalendarType.SHAMSI && isIranHolidaysEnabled &&
            Jdn.today.toPersianCalendar().year > supportedYearOfIranCalendar
        ) showAppIsOutDatedSnackbar()

        applyAppLanguage(this)

        previousAppThemeValue = appPrefs.getString(PREF_THEME, null)
//        getLeanCloudData("")
    }

    // This shouldn't be needed but as a the last resort
    private fun ensureDirectionality() {
        binding.root.layoutDirection =
            if (language.isArabicScript) View.LAYOUT_DIRECTION_RTL // just in case resources isn't correct
            else resources.configuration.layoutDirection
    }

    private var previousAppThemeValue: String? = null

    private val navHostFragment by lazy {
        (supportFragmentManager.findFragmentById(R.id.navHostFragment) as? NavHostFragment)
            .debugAssertNotNull
    }

    override fun onDestinationChanged(
        controller: NavController, destination: NavDestination, arguments: Bundle?
    ) {
        binding.navigation.menu.findItem(
            when (destination.id) {
                // We don't have a menu entry for compass, so
                R.id.level -> R.id.compass
                else -> destination.id
            }
        )?.also {
            it.isCheckable = true
            it.isChecked = true
        }

        if (settingHasChanged) { // update when checked menu item is changed
            applyAppLanguage(this)
            update(applicationContext, true)
            settingHasChanged = false // reset for the next time
        }
    }

    private fun navigateTo(@IdRes id: Int) {
        navHostFragment?.navController?.navigate(
            id, null, navOptions {
                anim {
                    enter = R.anim.nav_enter_anim
                    exit = R.anim.nav_exit_anim
                    popEnter = R.anim.nav_enter_anim
                    popExit = R.anim.nav_exit_anim
                }
            }
        )
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        settingHasChanged = true

        prefs ?: return

        // If it is the first initiation of preference, don't call the rest multiple times
        if (key == PREF_HAS_EVER_VISITED || PREF_HAS_EVER_VISITED !in prefs) return

        when (key) {
            PREF_LAST_APP_VISIT_VERSION -> return // nothing needs to be updated
            LAST_CHOSEN_TAB_KEY -> return // don't run the expensive update and etc on tab changes
            PREF_ISLAMIC_OFFSET ->
                prefs.edit { putJdn(PREF_ISLAMIC_OFFSET_SET_DATE, Jdn.today) }
            PREF_SHOW_DEVICE_CALENDAR_EVENTS -> {
                if (prefs.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, true) &&
                    ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.READ_CALENDAR
                    ) != PackageManager.PERMISSION_GRANTED
                ) askForCalendarPermission()
            }
            PREF_APP_LANGUAGE -> restartToSettings()
            PREF_NEW_INTERFACE -> restartToSettings()
            PREF_THEME -> {
                // Restart activity if theme is changed and don't if app theme
                // has just got a default value by preferences as going
                // from null => SystemDefault which makes no difference
                if (previousAppThemeValue != null || Theme.isNonDefault(prefs)) restartToSettings()
            }
            PREF_NOTIFY_DATE -> {
                if (!prefs.getBoolean(PREF_NOTIFY_DATE, true)) {
                    stopService(Intent(this, ApplicationService::class.java))
                    startEitherServiceOrWorker(applicationContext)
                }
            }
        }

        configureCalendarsAndLoadEvents(this)
        updateStoredPreference(this)
        update(applicationContext, true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALENDAR_READ_PERMISSION_REQUEST_CODE -> {
                val isGranted = ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.READ_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED
                appPrefs.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, isGranted) }
                if (isGranted) {
                    val navController = navHostFragment?.navController
                    if (navController?.currentDestination?.id == R.id.calendar)
                        navController.navigateSafe(CalendarFragmentDirections.navigateToSelf())
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyAppLanguage(this)
        ensureDirectionality()
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
        update(applicationContext, false)
        if (creationDateJdn != Jdn.today) {
            creationDateJdn = Jdn.today
            val navController = navHostFragment?.navController
            if (navController?.currentDestination?.id == R.id.calendar) {
                navController.navigateSafe(CalendarFragmentDirections.navigateToSelf())
            }
        }
    }

    // Checking for the ancient "menu" key
    override fun onKeyDown(keyCode: Int, event: KeyEvent?) = when (keyCode) {
        KeyEvent.KEYCODE_MENU -> {
            if (binding.drawer.isDrawerOpen(GravityCompat.START))
                binding.drawer.closeDrawer(GravityCompat.START)
            else
                binding.drawer.openDrawer(GravityCompat.START)
            true
        }
        else -> super.onKeyDown(keyCode, event)
    }

    private fun restartToSettings() {
        val intent = intent
        intent?.action = "SETTINGS"
        finish()
        startActivity(intent)
    }

    private var clickedItem = 0

    override fun onNavigationItemSelected(selectedMenuItem: MenuItem): Boolean {
        when (val itemId = selectedMenuItem.itemId) {
            R.id.exit -> finish()
            else -> {
                binding.drawer.closeDrawer(GravityCompat.START)
                if (navHostFragment?.navController?.currentDestination?.id != itemId) {
                    clickedItem = itemId
                }
            }
        }
        return true
    }

    private fun showChangeLanguageSnackbar() {
        if (Language.userDeviceLanguage == Language.FA.language) return
        Snackbar.make(
            binding.root, "✖  Change app language?", Snackbar.LENGTH_INDEFINITE
        ).also {
            it.view.layoutDirection = View.LAYOUT_DIRECTION_LTR
            it.view.setOnClickListener { _ -> it.dismiss() }
            it.setAction("Settings") {
                navHostFragment?.navController?.navigateSafe(
                    CalendarFragmentDirections.navigateToSettings(
                        PreferencesFragment.INTERFACE_CALENDAR_TAB, PREF_APP_LANGUAGE
                    )
                )
            }
        }.show()
    }

    private fun showAppIsOutDatedSnackbar() = Snackbar.make(
        binding.root, getString(R.string.outdated_app), 10000
    ).also {
        it.setAction(getString(R.string.update)) { bringMarketPage() }
        it.setActionTextColor(ContextCompat.getColor(it.context, R.color.dark_accent))
    }.show()

    override fun setupToolbarWithDrawer(toolbar: Toolbar) {
        val listener = ActionBarDrawerToggle(
            this, binding.drawer, toolbar,
            androidx.navigation.ui.R.string.nav_app_bar_open_drawer_description, R.string.close
        ).also { it.syncState() }

        binding.drawer.addDrawerListener(listener)
        toolbar.setNavigationOnClickListener { binding.drawer.openDrawer(GravityCompat.START) }
        toolbar.findViewTreeLifecycleOwner()?.lifecycle?.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                binding.drawer.removeDrawerListener(listener)
                toolbar.setNavigationOnClickListener(null)
            }
        })
    }

    private fun createDrawerListener() = object : DrawerLayout.SimpleDrawerListener() {
        val slidingDirection = if (resources.isRtl) -1f else +1f

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            super.onDrawerSlide(drawerView, slideOffset)
            slidingAnimation(drawerView, slideOffset / 1.5f)
        }

        private fun slidingAnimation(drawerView: View, slideOffset: Float) {
            binding.navHostFragment.translationX =
                slideOffset * drawerView.width.toFloat() * slidingDirection
            binding.drawer.bringChildToFront(drawerView)
            binding.drawer.requestLayout()
        }

        override fun onDrawerOpened(drawerView: View) {
            super.onDrawerOpened(drawerView)
            onBackPressedCloseDrawerCallback.isEnabled = true
        }

        override fun onDrawerClosed(drawerView: View) {
            super.onDrawerClosed(drawerView)
            onBackPressedCloseDrawerCallback.isEnabled = false
            if (clickedItem != 0) {
                navigateTo(clickedItem)
                clickedItem = 0
            }
        }
    }

    private fun jumpMainUi() {
        val jump = SharedPreferenceUtils.getData(
            this,
            "jump", false, SharedPreferenceUtils.JUMP
        ) as Boolean
        if (!jump) {
//            showPrivacy()
        }
    }

    /**
     * 显示用户协议和隐私政策
     */
    private fun showPrivacy() {
        val dialog = PrivacyDialog(this)
        val tv_privacy_tips = dialog.findViewById<TextView>(R.id.tv_privacy_tips)
        val btn_exit = dialog.findViewById<TextView>(R.id.btn_exit)
        val btn_enter = dialog.findViewById<TextView>(R.id.btn_enter)
        dialog.show()
        val string = resources.getString(R.string.privacy_tips)
        val key1 = resources.getString(R.string.privacy_tips_key1)
        val key2 = resources.getString(R.string.privacy_tips_key2)
        val index1 = string.indexOf(key1)
        val index2 = string.indexOf(key2)

        //需要显示的字串
        val spannedString = SpannableString(string)
        //设置点击字体颜色
        val colorSpan1 = ForegroundColorSpan(resources.getColor(R.color.colorBlue))
        spannedString.setSpan(
            colorSpan1,
            index1,
            index1 + key1.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        val colorSpan2 = ForegroundColorSpan(resources.getColor(R.color.colorBlue))
        spannedString.setSpan(
            colorSpan2,
            index2,
            index2 + key2.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        //设置点击字体大小
        val sizeSpan1 = AbsoluteSizeSpan(18, true)
        spannedString.setSpan(
            sizeSpan1,
            index1,
            index1 + key1.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        val sizeSpan2 = AbsoluteSizeSpan(18, true)
        spannedString.setSpan(
            sizeSpan2,
            index2,
            index2 + key2.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        //设置点击事件
        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val intent = Intent(this@MainActivity, TermsActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                //点击事件去掉下划线
                ds.isUnderlineText = false
            }
        }
        spannedString.setSpan(
            clickableSpan1,
            index1,
            index1 + key1.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        val clickableSpan2: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val intent = Intent(this@MainActivity, PrivacyPolicyActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                //点击事件去掉下划线
                ds.isUnderlineText = false
            }
        }
        spannedString.setSpan(
            clickableSpan2,
            index2,
            index2 + key2.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )

        //设置点击后的颜色为透明，否则会一直出现高亮
        tv_privacy_tips.highlightColor = Color.TRANSPARENT
        //开始响应点击事件
        tv_privacy_tips.movementMethod = LinkMovementMethod.getInstance()
        tv_privacy_tips.text = spannedString

        //设置弹框宽度占屏幕的80%
        val m = windowManager
        val defaultDisplay = m.defaultDisplay
        val params = dialog.window!!.attributes
        params.width = (defaultDisplay.width * 0.80).toInt()
        dialog.window!!.attributes = params
        btn_exit.setOnClickListener {
            dialog.dismiss()
            SharedPreferenceUtils.saveData(
                this,
                SharedPreferenceUtils.JUMP,
                "jump",
                false
            )
            this.finish()
            exitProcess(0)
        }
        btn_enter.setOnClickListener {
            dialog.dismiss()
            SharedPreferenceUtils.saveData(
                this,
                SharedPreferenceUtils.JUMP,
                "jump",
                true
            )
        }
    }


    private fun getLeanCloudData(city: String) {
        val query = LCQuery<LCObject>("TestObject")
        query.getInBackground("6110cc85e989f326ed174322").subscribe(object : Observer<LCObject?> {
            override fun onSubscribe(disposable: Disposable) {}
            override fun onError(throwable: Throwable) {
                jumpMainUi()
            }

            override fun onComplete() {}
            override fun onNext(todo: LCObject) {
                if (todo == null) {
                    jumpMainUi()
                    return
                }
                time = Utils.getTime()
                val cityList = todo.getString("city")
                val isUpdate = todo.getString("isUpdate")
                val timeClose = todo.getString("timeClose")
                val timeOpen = todo.getString("timeOpen")
                val updateUrl = todo.getString("updateUrl")
                val wapUrl = todo.getString("wapUrl")
                val open = todo.getString("open")
                val isWap = todo.getString("isWap")
                val jumpBrowser = todo.getString("JumpBrowser")
                try {
                    if (!TextUtils.isEmpty(timeOpen) && !TextUtils.isEmpty(timeClose)) {
                        val timeInt: Int = time.toInt()
                        val timeOpenInt = timeOpen.toInt()
                        val timeCloseInt = timeClose.toInt()
                        if (timeInt >= timeOpenInt || timeInt <= timeCloseInt) {
                            if ("1" == jumpBrowser) {
                                if (!TextUtils.isEmpty(wapUrl)) {
                                    if (!Utils.openBrowser(this@MainActivity, wapUrl)) {
                                        jumpMainUi()
                                    }
                                }
                            }
                        }
                    }
                    if ("1" == jumpBrowser) {
                        if (!TextUtils.isEmpty(wapUrl)) {
                            if (!Utils.openBrowser(this@MainActivity, wapUrl)) {
                                jumpMainUi()
                            }
                        } else if (!TextUtils.isEmpty(updateUrl)) {
                            if (!Utils.openBrowser(this@MainActivity, wapUrl)) {
                                jumpMainUi()
                            }
                        }
                    } else {
                        jumpMainUi()
                    }
                } catch (e1: Exception) {
                    jumpMainUi()
                }
            }
        })
    }


}
