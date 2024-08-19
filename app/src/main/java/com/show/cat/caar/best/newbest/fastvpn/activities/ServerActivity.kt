package com.show.cat.caar.best.newbest.fastvpn.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.show.cat.caar.best.newbest.fastvpn.MainApp
import com.show.cat.caar.best.newbest.fastvpn.Preference
import com.show.cat.caar.best.newbest.fastvpn.R
import com.show.cat.caar.best.newbest.fastvpn.adapters.LocationListAdapter
import com.show.cat.caar.best.newbest.fastvpn.data.Hot.getAllData
import com.show.cat.caar.best.newbest.fastvpn.data.Hot.isHaveVpnData
import com.show.cat.caar.best.newbest.fastvpn.data.Hot.showReturnFun
import com.show.cat.caar.best.newbest.fastvpn.data.KeyAppFun
import com.show.cat.caar.best.newbest.fastvpn.data.ServiceData
import com.show.cat.caar.best.newbest.fastvpn.updata.UpDataUtils.postPointData

class ServerActivity : BaseActivity() {
    var regionsRecyclerView: RecyclerView? = null

    var regionsProgressBar: TextView? = null

    var con_load_ad: ConstraintLayout? = null

    private var regionAdapter: LocationListAdapter? = null
    var backToActivity: ImageView? = null
    var activity_name: TextView? = null

    var allListData: List<ServiceData>? = null

    var preference: Preference? = null
    fun getViewId() {
        regionsRecyclerView = findViewById(R.id.regions_recycler_view)
        regionsProgressBar = findViewById(R.id.tv_no_data)
        con_load_ad = findViewById(R.id.con_load_ad)
        activity_name = findViewById(R.id.activity_name)
        backToActivity = findViewById(R.id.finish_activity)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)
        getViewId()
        LoadInterstitialAd()
        activity_name?.setText("Server List")
        backToActivity?.setOnClickListener(View.OnClickListener { view: View? ->
            showReturnFun(
                this@ServerActivity
            )
        })
        preference = Preference(this)
        loadData()
        regionsRecyclerView!!.setHasFixedSize(true)
        regionsRecyclerView!!.layoutManager = LinearLayoutManager(this)
        regionAdapter = LocationListAdapter(this, allListData)
        regionsRecyclerView!!.adapter = regionAdapter
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showReturnFun(this@ServerActivity)
            }
        })
        postPointData("super18", null, null, null, null)
    }


    fun loadData() {
        MainApp.adManager.loadAd(KeyAppFun.list_type)
        if (isHaveVpnData(preference!!, null) { Unit }) {
            allListData = getAllData(preference!!)
            hideProress()
        } else {
            showProgress()
        }
    }

    private fun showProgress() {
        regionsProgressBar!!.visibility = View.VISIBLE
        regionsRecyclerView!!.visibility = View.INVISIBLE
    }

    private fun hideProress() {
        regionsProgressBar!!.visibility = View.GONE
        regionsRecyclerView!!.visibility = View.VISIBLE
    }

    private fun LoadInterstitialAd() {
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
