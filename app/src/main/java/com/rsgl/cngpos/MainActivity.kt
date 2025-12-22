package com.rsgl.cngpos

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import com.phonepe.intent.sdk.api.PhonePeKt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val isInitSuccess = PhonePeKt.init(
            context = this,
            merchantId = BuildConfig.PHONEPE_MERCHANT_ID,
            flowId = "FLOW_${System.currentTimeMillis()}", // any unique string
            phonePeEnvironment = PhonePeEnvironment.RELEASE, // or SANDBOX
            enableLogging = true,
            appId = BuildConfig.PHONEPE_APP_ID // optional, can be null
        )

        if (!isInitSuccess) {
            Log.e("PhonePe", "SDK Init Failed")
        }
    }

}
