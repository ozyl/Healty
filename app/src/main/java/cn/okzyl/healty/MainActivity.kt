package cn.okzyl.healty

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import cn.okzyl.healty.service.BaseAccessibilityService
import cn.vove7.andro_accessibility_api.AccessibilityApi
import cn.vove7.andro_accessibility_api.AppScope
import cn.vove7.andro_accessibility_api.api.*
import cn.vove7.andro_accessibility_api.viewfinder.SF
import cn.vove7.andro_accessibility_api.viewfinder.text
import kotlinx.coroutines.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                waitAccessibility(180000, AccessibilityApi.BASE_SERVICE_CLS)
                runOnUiThread {
                    startActivity(Intent().apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse("alipays://platformapi/startapp?appId=2021001135679870&page=pages/home/index")
                    })
                }
                val isPage = waitForPage(AppScope("com.eg.android.AlipayGphone",
                    "com.alipay.mobile.nebulax.integration.mpaas.activity.NebulaActivity\$Lite1"))

                if (isPage){
                    repeat(300){
                        if (SF.text("温馨提示 ③").findFirst()!=null){
                            runOnUiThread{
                                startActivity(Intent().apply {
                                    action = Intent.ACTION_VIEW
                                    data = Uri.parse("alipays://platformapi/startapp?appId=2021001135679870&page=pages/codeScanning/index")
                                })
                            }
                        }
                        delay(100)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity,"发生错误${e.message}",Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}