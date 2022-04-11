package cn.okzyl.healty

import android.app.Application
import cn.okzyl.healty.service.BaseAccessibilityService
import cn.okzyl.healty.service.GestureAccessibilityService
import cn.vove7.andro_accessibility_api.AccessibilityApi

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AccessibilityApi.apply {
            BASE_SERVICE_CLS = BaseAccessibilityService::class.java
            GESTURE_SERVICE_CLS = GestureAccessibilityService::class.java
        }
    }
}