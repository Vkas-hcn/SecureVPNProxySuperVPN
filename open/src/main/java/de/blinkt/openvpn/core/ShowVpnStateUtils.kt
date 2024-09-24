package de.blinkt.openvpn.core

import android.net.VpnService
import android.util.Log
import com.tencent.mmkv.MMKV

object ShowVpnStateUtils {
    private val mmkv by lazy {
        MMKV.mmkvWithID("EasyVPN", MMKV.MULTI_PROCESS_MODE)
    }

    private fun getFlowData(): Boolean {
        val data = mmkv.decodeBool("easy_vpn_flow_data", true)
        Log.e("TAG", "getFlowData: ==${data}", )
        return data
    }

    fun brand(builder:VpnService.Builder, myPackageName: String) {
        if(getFlowData()){
            //黑名单绕流
            (listOf(myPackageName) + listGmsPackages())
                .iterator()
                .forEachRemaining {
                    runCatching { builder.addDisallowedApplication(it) }
                }
        }
    }

    private fun listGmsPackages(): List<String> {
        return listOf(
            "com.google.android.gms",
            "com.google.android.ext.services",
            "com.google.process.gservices",
            "com.android.vending",
            "com.google.android.gms.persistent",
            "com.google.android.cellbroadcastservice",
            "com.google.android.packageinstaller",
            "com.google.android.gms.location.history",
        )
    }
    fun getSpeedData(upData: String, downData: String) {
        mmkv.encode("easy_dow_num", downData)
        mmkv.encode("easy_up_num", upData)
    }
}