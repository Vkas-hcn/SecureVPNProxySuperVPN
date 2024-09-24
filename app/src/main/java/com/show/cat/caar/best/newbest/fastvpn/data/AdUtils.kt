package com.show.cat.caar.best.newbest.fastvpn.data

import android.util.Base64
import com.google.gson.Gson
import com.show.cat.caar.best.newbest.fastvpn.Preference
import com.show.cat.caar.best.newbest.fastvpn.updata.UpDataUtils
import android.content.Context
import com.show.cat.caar.best.newbest.fastvpn.MainApp
import java.io.BufferedReader
import java.io.InputStreamReader
object AdUtils {


    fun Context.loadJsonFromAssets(fileName: String): String? {
        return try {
            // Access the asset file
            val inputStream = assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            // Use StringBuilder to read the file line by line
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            // Close the reader
            bufferedReader.close()
            inputStream.close()
            // Return the JSON as a string
            stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getAdListData(preference: Preference): VpnAdBean {
        val onlineAdBean = preference.getStringpreference(KeyAppFun.o_ad_data)
        val localAdBean = MainApp.context.loadJsonFromAssets("ad_config.json")
        runCatching {
            if (onlineAdBean.isNotEmpty()) {
                return Gson().fromJson(base64Decode(onlineAdBean), VpnAdBean::class.java)
            } else {
                return Gson().fromJson(localAdBean, VpnAdBean::class.java)
            }
        }.getOrNull() ?: return Gson().fromJson(localAdBean, VpnAdBean::class.java)
    }

    fun base64Decode(base64Str: String): String {
        return String(Base64.decode(base64Str, Base64.DEFAULT))
    }


    fun getLjData(preference: Preference): AdLjBean {
        val adRefBean = preference.getStringpreference(KeyAppFun.o_me_data)
        val localAdBean = MainApp.context.loadJsonFromAssets("easy_lj.json")
        runCatching {
            if (adRefBean.isNotEmpty()) {
                return Gson().fromJson(base64Decode(adRefBean), AdLjBean::class.java)
            } else {
                return Gson().fromJson(localAdBean, AdLjBean::class.java)
            }
        }.getOrNull() ?: return Gson().fromJson(localAdBean, AdLjBean::class.java)
    }

    fun getIsOrNotRl(preference: Preference): Boolean {
        when (getLjData(preference).rrr_ll) {
            "1" -> {
                return true
            }

            "2" -> {
                return false
            }

            "3" -> {
                return !isItABuyingUser()
            }

            else -> {
                return true
            }
        }
    }


    fun getAdBlackData(preference: Preference): Boolean {
        val state = when (getLjData(preference).ccc_kk) {
            "1" -> {
                true
            }

            "2" -> {
                false
            }

            else -> {
                preference.getStringpreference(KeyAppFun.cloak_data) != "grady"
            }
        }
        val blackDataUpType = preference.getStringpreference(KeyAppFun.black_updata_state)
        if (!state && blackDataUpType != "1") {
            UpDataUtils.postPointData("super1")
            preference.setStringpreference(KeyAppFun.black_updata_state, "1")
        }
        return state
    }
    private fun getUserJson(): FlashUserBean {
        val preference = Preference(MainApp.context)
        val adRefBean = preference.getStringpreference(KeyAppFun.o_ml_data)
        val localAdBean = MainApp.context.loadJsonFromAssets("ref_model.json")
        runCatching {
            if (adRefBean.isNotEmpty()) {
                return Gson().fromJson(base64Decode(adRefBean), FlashUserBean::class.java)
            } else {
                return Gson().fromJson(localAdBean, FlashUserBean::class.java)
            }
        }.getOrNull() ?: return Gson().fromJson(localAdBean, FlashUserBean::class.java)
    }
    private fun isFacebookUser(): Boolean {
        var preference = Preference(MainApp.context)
        val data = getUserJson()
        val referrer = preference.getStringpreference(KeyAppFun.ref_data)
        val pattern = "fb4a|facebook".toRegex(RegexOption.IGNORE_CASE)
        return (pattern.containsMatchIn(referrer) && data.ass == "1")
    }

    fun isItABuyingUser(): Boolean {
        val preference = Preference(MainApp.context)
        val data = getUserJson()
        val referrer = preference.getStringpreference(KeyAppFun.ref_data)
        return isFacebookUser()
                || (data.xc == "1" && referrer.contains("gclid", true))
                || (data.vb == "1" && referrer.contains("not%20set", true))
                || (data.gk == "1" && referrer.contains(
            "youtubeads",
            true
        ))
                || (data.nm == "1" && referrer.contains("%7B%22", true))
                || (data.io == "1" && referrer.contains("adjust", true))
                || (data.we == "1" && referrer.contains("bytedance", true))
    }

    fun blockAdUsers(): Boolean {
        val preference = Preference(MainApp.context)

        val data = getLjData(preference).aaa_zz
        when (data) {
            "1" -> {
                return true
            }

            "2" -> {
                return isItABuyingUser()
            }

            "3" -> {
                return false
            }

            else -> {
                return true
            }
        }
    }
}