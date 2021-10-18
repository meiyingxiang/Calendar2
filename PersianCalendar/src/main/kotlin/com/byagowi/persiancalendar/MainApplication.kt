package com.byagowi.persiancalendar

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import cn.leancloud.LeanCloud
import com.byagowi.persiancalendar.global.initGlobal

class MainApplication : Application() {


    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()
        Variants.mainApplication(this)
        initGlobal(applicationContext)

        // 提供 this、App ID 和 App Key 作为参数
        LeanCloud.initialize(
            this, "PIttCj5zTLO6iNQIdnzoplzf-MdYXbMMI",
            "FeLiE0Vfk0r2phM06stM3XVO"
        )
    }

    // Can I haz these resources not removed?!
    // Workaround for weird AGP 4.1.0 >= used resource removal issues
    @Keep
    private val heyAndroidBuildToolsWeNeedTheseAndItIsUnbelievableYouAreRemovingThem = listOf(
        R.drawable.blue_shade_background, R.raw.abdulbasit
    )
}
