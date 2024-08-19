package com.show.cat.caar.best.newbest.fastvpn.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.show.cat.caar.best.newbest.fastvpn.data.Hot

open class BaseActivity:AppCompatActivity() {
    private val adTask = Runnable { hotLaunched = true }
    private var hotLaunched = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Hot.registerTask(adTask)
    }
    override fun onDestroy() {
        super.onDestroy()
        Hot.unregisterTask(adTask)
    }
}