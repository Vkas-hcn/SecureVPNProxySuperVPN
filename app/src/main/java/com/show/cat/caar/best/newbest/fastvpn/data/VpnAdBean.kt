package com.show.cat.caar.best.newbest.fastvpn.data

import androidx.annotation.Keep

@Keep
data class VpnAdBean(
    val easy_esc: Int,
    val easy_kfv: Int,
    val ope_easy: List<AdEasy>,
    val home_easy: List<AdEasy>,
    val resu_easy: List<AdEasy>,
    val cont_easy: List<AdEasy>,
    val list_easy: List<AdEasy>,
    val ba_easy: List<AdEasy>,

    )
@Keep
data class AdEasy(
    val easy_isd: String,
    val easy_no: Int,
    val easy_pt: String,
    val easy_ty: String,

    var maxx_load_ip: String = "",
    var maxx_load_city: String = "",
    var maxx_show_ip: String = "",
    var maxx_show_city: String = ""
)

@Keep
data class AdLjBean(
    val aaa_zz:String,
    val ccc_kk: String,
    val rrr_ll: String,
)

@Keep
data class FlashUserBean(
    val ass: String,
    val xc: String,
    val vb: String,
    val gk: String,
    val nm: String,
    val io: String,
    val we: String
)
