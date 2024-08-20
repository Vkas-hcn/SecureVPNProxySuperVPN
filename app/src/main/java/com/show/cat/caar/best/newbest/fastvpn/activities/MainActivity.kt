package com.show.cat.caar.best.newbest.fastvpn.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.os.RemoteException
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ActivityUtils

import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.show.cat.caar.best.newbest.fastvpn.MainApp
import com.show.cat.caar.best.newbest.fastvpn.MainApp.Companion.globalTimer
import com.show.cat.caar.best.newbest.fastvpn.R
import com.show.cat.caar.best.newbest.fastvpn.data.AdUtils
import com.show.cat.caar.best.newbest.fastvpn.data.Hot
import com.show.cat.caar.best.newbest.fastvpn.data.Hot.clickGuide
import com.show.cat.caar.best.newbest.fastvpn.data.Hot.isHaveVpnData
import com.show.cat.caar.best.newbest.fastvpn.data.Hot.setVpnStateData
import com.show.cat.caar.best.newbest.fastvpn.data.KeyAppFun
import com.show.cat.caar.best.newbest.fastvpn.data.RetrofitClient
import com.show.cat.caar.best.newbest.fastvpn.data.ServiceData
import com.show.cat.caar.best.newbest.fastvpn.data.VpnStateData
import com.show.cat.caar.best.newbest.fastvpn.updata.UpDataUtils.super10
import com.show.cat.caar.best.newbest.fastvpn.updata.UpDataUtils.postPointData
import com.show.cat.caar.best.newbest.fastvpn.utils.ImageRotator
import de.blinkt.openvpn.api.ExternalOpenVPNService
import de.blinkt.openvpn.api.IOpenVPNAPIService
import de.blinkt.openvpn.api.IOpenVPNStatusCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.exitProcess

class MainActivity : UIActivity() {
    var vpnCODJob: Job? = null
    var vpnStateMi = VpnStateData.DISCONNECTED
    private val endPageLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    private lateinit var imageRotator: ImageRotator
    private var handle: Handler = Handler()
    private val TAG = "MainActivity"
    private var speedJob: Job? = null
    private var jobMainJdo: Job? = null
    private var killAppState = false
    var adShown = false
    private lateinit var requestPermissionForResultVPN: ActivityResultLauncher<Intent?>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        liveVpnState()
        imageRotator = ImageRotator()
        backFun()
        showHomeAd()
        showDueDialog()
        MainApp.saveLoadManager.encode(
            KeyAppFun.easy_vpn_flow_data, AdUtils.getIsOrNotRl(preference)
        )
        if (clickGuide) {
            cloneGuide()
        }
        view_guide_1?.setOnClickListener { }
        requestPermissionForResultVPN =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                requestPermissionForResult(it)
            }
    }

    private fun requestPermissionForResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            if (preference.getStringpreference(KeyAppFun.pmm_fast) != "1") {
                preference.setStringpreference(KeyAppFun.pmm_fast, "1")
                postPointData("super8")
            }
            get14Per()
        } else {
            Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                clickButTOVpn()
                postPointData("super4")
            } else {
                Snackbar.make(
                    findViewById(R.id.main_layout),
                    "Notification permission denied. Please enable it in settings.",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Settings") {
                        openAppSettings()
                    }
                    .show()
            }
        }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun backFun() {
        onBackPressedDispatcher.addCallback(this) {
            if (lav_guide?.isVisible == true) {
                cloneGuide()
                return@addCallback
            }
            if (vpnStateMi == VpnStateData.DISCONNECTING) {
                stopDisConnectFun()
                return@addCallback
            }
            if (vpnStateMi == VpnStateData.CONNECTING) {
                Toast.makeText(
                    this@MainActivity,
                    "Unable to operate during connection process!",
                    Toast.LENGTH_SHORT
                ).show()
                return@addCallback
            }
            onBackPressedFun()
        }
    }

    private fun showDueDialog(): Boolean {
//        if (RetrofitClient.shouldBlockAccess(preference)) {
//            Hot.illegalUserDialog(this) {
//                moveTaskToBack(true)
//                Process.killProcess(Process.myPid())
//                finish()
//            }
//            return true
//        }
        return false
    }

    private fun stopDisConnectFun() {
        cancelCOD()
        updateUI(Hot.vpnStateHotData)
    }

    override fun onStart() {
        super.onStart()
        connection_layout?.setOnClickListener {
            initVpnSet()
        }
        lav_guide?.setOnClickListener {
            initVpnSet()
        }
        currentServerBtn?.setOnClickListener {
            Log.e(TAG, "initVpnSet: ${vpnStateMi}")

            if (vpnStateMi == VpnStateData.CONNECTING) {
                return@setOnClickListener
            }
            if (vpnStateMi == VpnStateData.DISCONNECTING) {
                stopDisConnectFun()
                return@setOnClickListener
            }
            isHaveVpnData(preference, con_loading) {
                startActivityForResult(Intent(this, ServerActivity::class.java), 3000)
            }
        }
        privacybtn?.setOnClickListener {
            if (vpnStateMi == VpnStateData.CONNECTING) {
                return@setOnClickListener
            }
            if (vpnStateMi == VpnStateData.DISCONNECTING) {
                stopDisConnectFun()
                return@setOnClickListener
            }
            startActivity(
                Intent(
                    "android.intent.action.VIEW", Uri.parse("https://maxisoftapps.blogspot.com/")
                )
            )
        }
    }

    private var lastExecutionTime: Long = 0

    private fun initVpnSet() {
        Log.e(TAG, "initVpnSet: ${vpnStateMi}")
        if (Hot.vpnStateHotData != VpnStateData.CONNECTED) {
            postPointData("super6")
        }
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastExecutionTime < 2000) {
            return
        }
        lastExecutionTime = currentTime

        if (vpnStateMi == VpnStateData.CONNECTING) {
            return
        }
        if (vpnStateMi == VpnStateData.DISCONNECTING) {
            stopDisConnectFun()
            return
        }
        RetrofitClient.detectCountry(preference)
        cloneGuide()
        if (isHaveVpnData(preference, con_loading) {}) {
            if (checkVPNPermission()) {
                get14Per()
            } else {
                if (preference.getStringpreference(KeyAppFun.pmm_state) != "1") {
                    preference.setStringpreference(KeyAppFun.pmm_state, "1")
                    postPointData("super7")
                }
                VpnService.prepare(this).let {
                    requestPermissionForResultVPN.launch(it)
                }
            }
        }
    }

    private fun get14Per() {
        if (showDueDialog()) return
        if (!Hot.isNetworkConnected(this)) {
            Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show()
            return
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU && Hot.vpnStateHotData != VpnStateData.CONNECTED) {
            postPointData("super5")
        }
        clickButTOVpn()
    }

    private fun checkVPNPermission(): Boolean {
        VpnService.prepare(this).let {
            return it == null
        }
    }

    private fun cancelCOD() {
        vpnCODJob?.cancel()
        vpnCODJob = null
        adShown = true
    }

    private fun clickButTOVpn() {
        killAppState = true
        cancelCOD()
        vpnCODJob = lifecycleScope.launch {
            MainApp.adManager.loadAd(KeyAppFun.cont_type)
            Hot.clickStateHotData = Hot.vpnStateHotData
            if (Hot.vpnStateHotData == VpnStateData.DISCONNECTED) {
                updateUI(VpnStateData.CONNECTING)
                delay(2000)
                postPointData("super9")
                openVTool()
            }
            if (Hot.vpnStateHotData == VpnStateData.CONNECTED) {
                MainApp.adManager.loadAd(KeyAppFun.ba_type)
                MainApp.adManager.loadAd(KeyAppFun.result_type)
                updateUI(VpnStateData.DISCONNECTING)
                delay(2000)
                postPointData("super13")
                showConnectAd {
                    mService?.disconnect()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(300)
            val state = lifecycle.currentState == Lifecycle.State.RESUMED
            if (state) {
                postPointData("super2")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (vpnStateMi == VpnStateData.CONNECTING && Hot.vpnStateHotData != VpnStateData.CONNECTED) {
            stopDisConnectFun()
        }
        if (vpnStateMi == VpnStateData.DISCONNECTING && Hot.vpnStateHotData != VpnStateData.DISCONNECTED) {
            stopDisConnectFun()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3000) {
            if (resultCode == RESULT_OK) {
                onRegionSelected()
            }
            if (resultCode == RESULT_CANCELED) {
                initVpnSet()
            }
        }
        if (requestCode == 4000) {
            if (resultCode == RESULT_OK) {
                val bean = Hot.getCLickServiceData(this)
                Hot.initVPNSet(preference, bean)
                setVpnUi(bean)
            }
        }
    }


    private fun onRegionSelected() {
        val bean = Hot.getCLickServiceData(this)
        Hot.initVPNSet(preference, bean)
        setVpnUi(bean)
        initVpnSet()
    }


    override fun onClick(view: View) {
    }


    private fun onBackPressedFun() {
        val alertDialog = AlertDialog.Builder(this@MainActivity)
        alertDialog.setTitle("Leave Application?")
        alertDialog.setMessage("Are you sure you want to leave the application?")
        alertDialog.setIcon(R.mipmap.ic_launcher)
        alertDialog.setPositiveButton("YES") { dialog: DialogInterface?, which: Int ->
            mService?.disconnect()
            ActivityUtils.finishAllActivities()
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }
        alertDialog.setNegativeButton("NO", null)
        alertDialog.show()
    }

    private fun setVpnState(state: String?) {
        lifecycleScope.launch {
            when (state) {
                "CONNECTED" -> {
                    Log.e(TAG, "VPN连接成功=${vpnStateMi}")
                    if (!killAppState) {
                        Log.e(TAG, "同步UI=${vpnStateMi}")
                        updateUI(Hot.vpnStateHotData)
                    }
                    setVpnStateData(VpnStateData.CONNECTED)
                    if (vpnStateMi == VpnStateData.CONNECTING) {
                        showConnectAd {
                            lifecycleScope.launch {
                                endPageLiveData.postValue(true)
                                delay(300)
                                updateUI(Hot.vpnStateHotData)
                            }
                        }
                        MainApp.adManager.loadAd(KeyAppFun.ba_type)
                        MainApp.adManager.loadAd(KeyAppFun.result_type)
                    }
                    super10()
                }

                "CONNECTING" -> {
                    Log.e(TAG, "VPN连接中")
                    setVpnStateData(VpnStateData.CONNECTING)
                }

                "Stopping" -> {
                    Log.e(TAG, "VPN断开中")
                    setVpnStateData(VpnStateData.DISCONNECTING)
                }

                "NOPROCESS" -> {
                    Log.e(TAG, "VPN断开=${vpnStateMi}")
                    setVpnStateData(VpnStateData.DISCONNECTED)
                    if (vpnStateMi == VpnStateData.DISCONNECTING) {
                        endPageLiveData.postValue(true)
                        delay(300)
                    }
                    updateUI(Hot.vpnStateHotData)
                }
            }
        }

    }

    var mService: IOpenVPNAPIService? = null

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName?,
            service: IBinder?,
        ) {
            mService = IOpenVPNAPIService.Stub.asInterface(service)
            try {
                mService?.registerStatusCallback(mCallback)
            } catch (e: Exception) {
            }
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            mService = null
        }
    }

    private fun liveVpnState() {
        killAppState = false
        bindService(
            Intent(this, ExternalOpenVPNService::class.java),
            mConnection,
            AppCompatActivity.BIND_AUTO_CREATE
        )
        endPageLiveData.observe(this) {
            if (it) {
                endPageLiveData.postValue(false)
                startActivityForResult(Intent(this, EndActivity::class.java), 4000)
                Log.e(TAG, "跳转结果页")
            }
        }
        val updateUITimer = object : Runnable {
            override fun run() {
                tv_date?.text = globalTimer.getFormattedTime()
                handle.postDelayed(this, 1000)
            }
        }
        handle.post(updateUITimer)
    }

    private fun showConnectAd(jumpFun: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        var attemptCount = 0
        if (MainApp.adManager.canShowAd(KeyAppFun.cont_type) == KeyAppFun.ad_jump_over) {
            jumpFun()
            return
        }
        adShown = false
        MainApp.adManager.loadAd(KeyAppFun.cont_type)
        val checkConditionAndPreloadAd = object : Runnable {
            override fun run() {
                if (adShown) return
                attemptCount++
                if (attemptCount < 20) {
                    handler.postDelayed(this, 500)
                } else {
                    Log.e("TAG", "等待CONNECT广告超时。。。 ")
                    jumpFun()
                }
                Log.e("TAG", "等待CONNECT广告中。。。 ")
                if (MainApp.adManager.canShowAd(KeyAppFun.cont_type) == KeyAppFun.ad_show) {
                    adShown = true
                    MainApp.adManager.showAd(KeyAppFun.cont_type, this@MainActivity) {
                        jumpFun()
                    }
                }
            }
        }
        handler.postDelayed(checkConditionAndPreloadAd, 500)
    }

    //同步UI
    private fun syncUiFun(vpnStateData: String) {
        if (vpnStateData == "Connected") {
            cloneGuide()
            updateUI(VpnStateData.CONNECTED)
        }
    }

    private fun cloneGuide() {
        lav_guide?.visibility = View.GONE
        view_guide_1?.visibility = View.GONE
        clickGuide = true
    }

    private val mCallback = object : IOpenVPNStatusCallback.Stub() {
        override fun newStatus(uuid: String?, state: String?, message: String?, level: String?) {
            Log.e(TAG, "stateChanged: " + state)

            setVpnState(state)
            state?.let { syncUiFun(it) }

            when (state) {
                "CONNECTED" -> {

                }

                "CONNECTING" -> {

                }

                "RECONNECTING" -> {
                }

                "NOPROCESS" -> {

                }


                else -> {}
            }

        }
    }

    fun openVTool() {
        MainScope().launch(Dispatchers.IO) {
            val serviceString = preference.getStringpreference(KeyAppFun.l_service_now_data)
            val clickBean = Gson().fromJson(serviceString, ServiceData::class.java)
            preference.setStringpreference(KeyAppFun.tba_vpn_ip_type, clickBean.DCzDBHwKl)
            preference.setStringpreference(KeyAppFun.tba_vpn_name_type, clickBean.RLhLoQLm)
            runCatching {
                val conf = this@MainActivity.assets.open("fast_265.ovpn")
                val br = BufferedReader(InputStreamReader(conf))
                val config = StringBuilder()
                var line: String?
                while (true) {
                    line = br.readLine()
                    if (line == null) break
                    if (line.contains("remote 195", true)) {
                        line = "remote ${clickBean.DCzDBHwKl} ${clickBean.eOEwSU}"
                    }
                    config.append(line).append("\n")
                }
                Log.e("TAG", "openVTool: $config")
                br.close()
                conf.close()
                mService?.startVPN(config.toString())
            }.onFailure {
            }
        }
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {
    }


    private fun updateUI(vpnStateData: VpnStateData) {
        vpnStateMi = vpnStateData
        when (vpnStateData) {
            VpnStateData.DISCONNECTED -> {
                connectionStateTextView?.setImageResource(R.drawable.disc)
                img_disconnect?.setImageResource(R.drawable.ic_home_off)
                img_yuan_1?.visibility = View.VISIBLE
                img_yuan_2?.visibility = View.GONE
                img_yuan_3?.visibility = View.GONE
                globalTimer.reset()
                img_yuan_2?.let { imageRotator.stopRotating(it) }
            }

            VpnStateData.DISCONNECTING -> {
                connectionStateTextView?.setImageResource(R.drawable.disc)
                img_disconnect?.setImageResource(R.drawable.bg_connecting)
                img_yuan_1?.visibility = View.GONE
                img_yuan_2?.visibility = View.VISIBLE
                img_yuan_3?.visibility = View.GONE
                img_yuan_2?.let { imageRotator.startRotating(it) }
            }

            VpnStateData.CONNECTED -> {
                connectionStateTextView?.setImageResource(R.drawable.conne)
                img_disconnect?.setImageResource(R.drawable.bg_connected)
                img_yuan_1?.visibility = View.GONE
                img_yuan_2?.visibility = View.GONE
                img_yuan_3?.visibility = View.VISIBLE
                globalTimer.start()
                img_yuan_2?.let { imageRotator.stopRotating(it) }
                showVpnSpeed()
            }

            VpnStateData.CONNECTING -> {
                connectionStateTextView?.setImageResource(R.drawable.connecting)
                img_disconnect?.setImageResource(R.drawable.bg_connecting)
                img_yuan_1?.visibility = View.GONE
                img_yuan_2?.visibility = View.VISIBLE
                img_yuan_3?.visibility = View.GONE
                img_yuan_2?.let { imageRotator.startRotating(it) }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun showVpnSpeed() {
        speedJob?.cancel()
        speedJob = lifecycleScope.launch {
            while (Hot.vpnStateHotData == VpnStateData.CONNECTED) {
                delay(1000)
                uploading_speed_textview?.text = MainApp.saveLoadManager.getString(
                    "easy_up_num", "0B/s"
                )
                downloading_speed_textview?.text = MainApp.saveLoadManager.getString(
                    "easy_dow_num", "0B/s"
                )
            }
        }
    }


    private fun showHomeAd() {
        jobMainJdo?.cancel()
        jobMainJdo = null
        if (AdUtils.getAdBlackData(preference)) {
            ad_layout?.isVisible = false
            return
        }
        ad_layout?.isVisible = true
        if (MainApp.adManager.canShowAd(KeyAppFun.home_type) == KeyAppFun.ad_jump_over) {
            img_oc_ad?.isVisible = true
            ad_layout_admob?.isVisible = false
            return
        }
        jobMainJdo = lifecycleScope.launch {
            delay(300)
            while (isActive) {
                if (MainApp.adManager.canShowAd(KeyAppFun.home_type) == KeyAppFun.ad_show) {
                    MainApp.adManager.showAd(KeyAppFun.home_type, this@MainActivity) {}
                    jobMainJdo?.cancel()
                    jobMainJdo = null
                    break
                }
                delay(500L)
            }
        }
    }
}
