package com.google.ar.core.examples.kotlin.helloar

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Kakao SDK 초기화
        KakaoSdk.init(this, "6c13cc9adb36b083aeb2a73823cd6559")
    }
}
