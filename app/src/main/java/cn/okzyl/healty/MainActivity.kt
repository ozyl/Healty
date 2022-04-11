package cn.okzyl.healty

import android.app.Activity
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
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.lifecycle.lifecycleScope
import cn.okzyl.healty.service.BaseAccessibilityService
import cn.vove7.andro_accessibility_api.AccessibilityApi
import cn.vove7.andro_accessibility_api.AppScope
import cn.vove7.andro_accessibility_api.api.*
import cn.vove7.andro_accessibility_api.utils.NeedAccessibilityException
import cn.vove7.andro_accessibility_api.utils.jumpAccessibilityServiceSettings
import cn.vove7.andro_accessibility_api.utils.whileWaitTime
import cn.vove7.andro_accessibility_api.viewfinder.SF
import cn.vove7.andro_accessibility_api.viewfinder.matchText
import cn.vove7.andro_accessibility_api.viewfinder.text
import kotlinx.coroutines.*
import kotlin.concurrent.thread
import kotlin.math.min
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.open).setOnClickListener {
            start()
        }
        findViewById<Button>(R.id.openAli).setOnClickListener {
            jumpMP()
        }
        act = this
    }

    override fun onDestroy() {
        act = null
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (AccessibilityApi.isBaseServiceEnable) {
            start()
        }
    }


    private fun start() {
        job?.cancel()
        job = lifecycleScope.launch(Dispatchers.IO) {
            try {
                waitAccessibility(180000, AccessibilityApi.BASE_SERVICE_CLS)
                runOnUiThread {
                    jumpMP()
                }
                isOpen = true
                tryOpen()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    companion object {
        var isOpen = false

        var job: Job? = null
        var bossJob: Job? = null

        var act: Activity? = null

        var inCarm = false

        fun tryOpen() {
            inCarm = AccessibilityApi.currentScope?.pageName?.startsWith("com.alipay.mobile.scan.as.tool.ToolsCaptureActivity") == true
            if (AccessibilityApi.currentScope?.pageName?.startsWith("com.alipay.mobile.nebulax.integration.mpaas.activity.NebulaActivity") == true && isOpen && bossJob?.isActive != true) {
                bossJob?.cancel()
                bossJob = CoroutineScope(Dispatchers.IO).launch {
                    try {
                        repeat(20) {
                            if (!isOpen) return@launch
                            openCarm()
                            delay(300)
                        }
                        isOpen = false
                    } catch (e: Exception) {
                    }
                }
            }
        }

        suspend fun openCarm() {
            val node = SF.matchText("本人信息扫码登记").findFirst() ?: return

            val result = node.tryClick()
            if (result) {
                delay(1000)
                if (inCarm){
                    bossJob?.cancel()
                    isOpen = false
                    act?.finish()
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
                        ActivityManager.MOVE_TASK_WITH_HOME
                    )
                    return
                }
            }
        }
    }


    private fun jumpMP() {
        AutoSceneUtil.moveTaskToFront(this)
        startActivity(Intent().apply {
            action = Intent.ACTION_VIEW
            data =
                Uri.parse("alipays://platformapi/startapp?appId=2021001135679870&page=pages/home/index")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
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