package cn.okzyl.healty

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import cn.okzyl.healty.service.BaseAccessibilityService
import cn.vove7.andro_accessibility_api.AccessibilityApi
import cn.vove7.andro_accessibility_api.AppScope
import cn.vove7.andro_accessibility_api.api.*
import cn.vove7.andro_accessibility_api.utils.NeedAccessibilityException
import cn.vove7.andro_accessibility_api.utils.jumpAccessibilityServiceSettings
import cn.vove7.andro_accessibility_api.utils.whileWaitTime
import cn.vove7.andro_accessibility_api.viewfinder.SF
import cn.vove7.andro_accessibility_api.viewfinder.text
import kotlinx.coroutines.*
import kotlin.concurrent.thread
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start()
        findViewById<Button>(R.id.open).setOnClickListener {
            start()
        }
    }

    var job :Job?=null

    private fun start() {
        job?.cancel()
        job = lifecycleScope.launch(Dispatchers.IO) {
            try {
                waitAccessibility(180000, AccessibilityApi.BASE_SERVICE_CLS)
                runOnUiThread {
                    startActivity(Intent().apply {
                        action = Intent.ACTION_VIEW
                        data =
                            Uri.parse("alipays://platformapi/startapp?appId=2021001135679870&page=pages/home/index")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                val isPage = waitForPage(AppScope("com.eg.android.AlipayGphone",
                    "com.alipay.mobile.nebulax.integration.mpaas.activity.NebulaActivity\$Lite1"))

                if (isPage) {
                    run outside@{repeat(300) {
                        if (SF.text("欢迎您").findFirst() != null) {
                            AutoSceneUtil.moveTaskToFront(this@MainActivity)
                            runOnUiThread {
                                startActivity(Intent().apply {
                                    action = Intent.ACTION_VIEW
                                    data =
                                        Uri.parse("alipays://platformapi/startapp?appId=2021001135679870&page=pages/codeScanning/index")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            }
                            return@outside
                        }
                        delay(50)
                    }}
                }
                delay(3000)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "发生错误${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}

object AutoSceneUtil {

    fun moveTaskToFront(context: Context) {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (runningTaskInfo in activityManager.getRunningTasks(3)) {
            if (context.packageName.equals(runningTaskInfo.topActivity?.packageName)) {
                activityManager.moveTaskToFront(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) runningTaskInfo.taskId else runningTaskInfo.id,
                    ActivityManager.MOVE_TASK_WITH_HOME)
                return
            }
        }
    }
}

fun waitAccessibility(waitMillis: Long = 30000, cls: Class<*>): Boolean {

    val se = if (cls == AccessibilityApi.BASE_SERVICE_CLS) AccessibilityApi.isBaseServiceEnable
    else AccessibilityApi.isGestureServiceEnable

    if (se) return true
    else jumpAccessibilityServiceSettings(cls)

    return whileWaitTime(waitMillis) {
        if (AccessibilityApi.isBaseServiceEnable) true
        else {
            Thread.sleep(500)
            null
        }
    } ?: throw NeedAccessibilityException(cls.name)
}