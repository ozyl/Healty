package cn.okzyl.healty.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import cn.vove7.andro_accessibility_api.AccessibilityApi

class GestureAccessibilityService : AccessibilityService() {
    override fun onCreate() {
        super.onCreate()
        //must call
        AccessibilityApi.gestureService = this
    }
    override fun onDestroy() {
        super.onDestroy()
        //must call
        AccessibilityApi.gestureService = null
    }
    override fun onInterrupt() {}
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
}