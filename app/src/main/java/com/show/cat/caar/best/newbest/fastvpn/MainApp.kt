package com.show.cat.caar.best.newbest.fastvpn

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.show.cat.caar.best.newbest.fastvpn.activities.MainActivity
import com.tencent.mmkv.MMKV
import com.show.cat.caar.best.newbest.fastvpn.data.Hot.isMainProcess
import com.show.cat.caar.best.newbest.fastvpn.data.Hot.registerAppLifeCallback
import com.show.cat.caar.best.newbest.fastvpn.data.KeyAppFun
import com.show.cat.caar.best.newbest.fastvpn.updata.UpDataUtils
import com.show.cat.caar.best.newbest.fastvpn.utils.AdManager
import com.show.cat.caar.best.newbest.fastvpn.utils.GlobalTimer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainApp : Application() {
    var referJobFlash: Job? = null

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        FirebaseApp.initializeApp(this)
        context = this
        appli =this
        initHydraSdk()
        if (isMainProcess(this)) {
            MobileAds.initialize(this)
            adManager = AdManager(this)
            registerAppLifeCallback(this)
            globalTimer = GlobalTimer()
            getReferInformation(this)
        }
    }
    private fun getReferInformation(context: Context) {
        referJobFlash?.cancel()
        var preference = Preference(MainApp.context)
        val ref = preference.getStringpreference(KeyAppFun.ref_data)

        referJobFlash = GlobalScope.launch {
            while (isActive) {
                if (ref.isNullOrEmpty()) {
                    UpDataUtils.haveRefData(context)
                } else {
                    cancel()
                    referJobFlash = null
                }
                delay(5000)
            }
        }
    }
    fun initHydraSdk() {
        MMKV.initialize(this)
        saveLoadManager =
            MMKV.mmkvWithID("EasyVPN", MMKV.MULTI_PROCESS_MODE)
    }


    companion object {
        lateinit var appli :Application
        lateinit var context: Context
        lateinit var adManager: AdManager
        lateinit var globalTimer: GlobalTimer
        lateinit var saveLoadManager: MMKV
    }
}
