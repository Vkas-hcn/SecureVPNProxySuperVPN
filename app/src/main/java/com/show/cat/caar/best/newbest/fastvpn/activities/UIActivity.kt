package com.show.cat.caar.best.newbest.fastvpn.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieAnimationView
import com.show.cat.caar.best.newbest.fastvpn.MainApp
import com.show.cat.caar.best.newbest.fastvpn.Preference
import com.show.cat.caar.best.newbest.fastvpn.R
import com.show.cat.caar.best.newbest.fastvpn.data.Hot
import com.show.cat.caar.best.newbest.fastvpn.data.KeyAppFun
import com.show.cat.caar.best.newbest.fastvpn.data.ServiceData

abstract class UIActivity : BaseActivity(), View.OnClickListener{
    var SKU_DELAROY_YEARLY: String = ""

    var connectionStateTextView: ImageView? = null

    var currentServerBtn: LinearLayout? = null

    var selectedServerTextView: TextView? = null

    var country_flag: ImageView? = null

    var uploading_speed_textview: TextView? = null

    var downloading_speed_textview: TextView? = null

    var connection_layout: ConstraintLayout? = null

    var lav_guide: LottieAnimationView? = null

    var view_guide_1: View? = null

    var con_loading: ConstraintLayout? = null

    var img_disconnect: ImageView? = null

    var img_yuan_1: ImageView? = null

    var img_yuan_2: ImageView? = null

    var img_yuan_3: ImageView? = null

    var tv_date: TextView? = null

    var ad_layout: ConstraintLayout? = null

    var img_oc_ad: ImageView? = null

    var ad_layout_admob: FrameLayout? = null

    var privacybtn: LinearLayout? = null
    var preference: Preference = Preference(MainApp.context)
    private val mUIHandler = Handler(Looper.getMainLooper())
    fun getViewId() {
         connectionStateTextView = findViewById(R.id.connection_state)
         currentServerBtn= findViewById(R.id.optimal_server_btn)

         selectedServerTextView = findViewById(R.id.selected_server)
         country_flag = findViewById(R.id.country_flag)

        uploading_speed_textview = findViewById(R.id.uploading_speed)
        downloading_speed_textview = findViewById(R.id.downloading_speed)
        connection_layout = findViewById(R.id.connection_layout)
        lav_guide = findViewById(R.id.lav_guide)
        view_guide_1 = findViewById(R.id.view_guide_1)
        con_loading = findViewById(R.id.con_loading)
        img_disconnect = findViewById(R.id.img_disconnect)
        img_yuan_1 = findViewById(R.id.img_yuan_1)
        img_yuan_2 = findViewById(R.id.img_yuan_2)
        img_yuan_3 = findViewById(R.id.img_yuan_3)
        tv_date = findViewById(R.id.tv_date)
        ad_layout = findViewById(R.id.ad_layout)
        img_oc_ad = findViewById(R.id.img_oc_ad)
        ad_layout_admob = findViewById(R.id.ad_layout_admob)
        privacybtn = findViewById(R.id.privacybtn)
    }
    val mUIUpdateRunnable: Runnable = object : Runnable {
        override fun run() {
            mUIHandler.postDelayed(this, 10000)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getViewId()
        preference = Preference(this)
        val bean = Hot.getCLickServiceData(this)
        if (bean == null) {
            Hot.initVPNSet(preference, null)
        } else {
            Hot.initVPNSet(preference, bean)
            setVpnUi(bean)
        }
    }


    fun setVpnUi(bean: ServiceData?) {
        country_flag!!.setImageResource(KeyAppFun.getFlagImageData(bean?.wIqcDNWy?:""))
        selectedServerTextView?.setText(bean?.wIqcDNWy)
    }

    override fun setTitle(title: CharSequence) {
        supportActionBar!!.title = title
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    companion object {
        protected val TAG: String = MainActivity::class.java.simpleName
    }
}