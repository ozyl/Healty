package cn.okzyl.healty.service

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityEvent
import cn.okzyl.healty.MainActivity
import cn.vove7.andro_accessibility_api.AccessibilityApi
import cn.vove7.andro_accessibility_api.AppScope
import cn.vove7.andro_accessibility_api.viewfinder.SF
import cn.vove7.andro_accessibility_api.viewfinder.matchText
import cn.vove7.andro_accessibility_api.viewfinder.text
import kotlinx.coroutines.*

class BaseAccessibilityService : AccessibilityApi() {

    //启用 页面更新 回调
    override val enableListenAppScope: Boolean = true

    //页面更新回调
    override fun onPageUpdate(currentScope: AppScope) {
        Log.d("TAG", "onPageUpdate: ${currentScope.packageName} ${currentScope.pageName}")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        super.onAccessibilityEvent(event)
            MainActivity.tryOpen()
    }


}