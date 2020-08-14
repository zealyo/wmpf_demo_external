package com.eyuan.wmpf.screen.experience

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.*
import com.tencent.luggage.demo.wxapi.DeviceInfo
import com.eyuan.wmpf.screen.Api
import com.eyuan.wmpf.screen.R
import com.eyuan.wmpf.screen.RequestsRepo
import com.eyuan.wmpf.screen.ui.DocumentActivity
import com.eyuan.wmpf.screen.utils.InvokeTokenHelper
import com.tencent.wxapi.test.OpenSdkTestUtil
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by complexzeng on 2020/6/17 2:59 PM.
 */
@SuppressLint("LongLogTag", "SetTextI18n")
class ExperienceActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "MicroMsg.ExperienceActivity"
    }

    private var landscapeMode = 0

    private lateinit var pathTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_experience)
        val appIdEditView = findViewById<EditText>(R.id.et_launch_app_id)
        val ticketEditView = findViewById<EditText>(R.id.et_ticket)
        val respTextView = findViewById<TextView>(R.id.resp_tv)
        val landscapeSwitch = findViewById<Switch>(R.id.switch_landscape)
        landscapeSwitch.setOnCheckedChangeListener { view, isClicked ->
            landscapeMode = if (isClicked) {
                2
            } else {
                0
            }
        }

        findViewById<Button>(R.id.btn_launch_wxa_app_quickly).setOnClickListener {
            launchWxa(respTextView, appIdEditView, ticketEditView, 0)
        }

        findViewById<Button>(R.id.btn_launch_wxa_dev_app).setOnClickListener {
            launchWxa(respTextView, appIdEditView, ticketEditView, 1)
        }

        findViewById<Button>(R.id.btn_launch_wxa_pre_app).setOnClickListener {
            launchWxa(respTextView, appIdEditView, ticketEditView, 2)
        }

        pathTv = findViewById(R.id.et_path)
    }

    private fun launchWxa(respTextView: TextView, appIdEditView: EditText, ticketEditView: EditText, versionType: Int) {
        respTextView.text = ""
        val appId = appIdEditView.text.toString().trim()
        val ticket = ticketEditView.text.toString().trim()

        RequestsRepo.getTestDeviceInfo(ticket, appId, DeviceInfo.APP_ID) {
            respTextView.post {
                respTextView.text = it
                val temp = it
                if (temp.toLowerCase(Locale.ROOT).contains("error")) {
                    DeviceInfo.reset()
                    return@post
                }
                var consoleText = respTextView.text.toString() + "\n" + "--------激活设备中--------\n"
                respTextView.text = consoleText
                Api.activateDevice(DeviceInfo.productId, DeviceInfo.keyVersion,
                        DeviceInfo.deviceId, DeviceInfo.signature, DeviceInfo.APP_ID)
                        .subscribe({
                            Log.i(TAG, "success: $it")
                            respTextView.post {
                                consoleText += String.format("init finish, err %d",
                                        it?.baseResponse?.errCode)
                                if (it.invokeToken == null) {
                                    consoleText += "\nactivate device fail for a null token, may ticket is expired\n"
                                    respTextView.text = consoleText
                                } else {
                                    val invokeToken = it.invokeToken
                                    InvokeTokenHelper.initInvokeToken(this, invokeToken)
                                    if (versionType == 0) {
                                        consoleText += "\ninvoke authorizeNoLogin\n"
                                        respTextView.text = consoleText

                                        Api.launchWxaApp(optLaunchAppId(), optPath(), landsapeMode = landscapeMode).subscribe({
                                            Log.i(DocumentActivity.TAG, "success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}")
                                        }, {
                                            Log.e(DocumentActivity.TAG, "error: $it")
                                        })

                                    } else {
                                        consoleText += "\ninvoke authorize\n"
                                        respTextView.text = consoleText
                                        Api.authorize()
                                                .subscribe({
                                                    runOnUiThread {
                                                        consoleText += "\ninvoke authorize result: ${it.baseResponse.errCode} ${it.baseResponse.errMsg} \n"
                                                        respTextView.text = consoleText
                                                        respTextView.text = "$consoleText\n--------启动小程序--------\n"
                                                    }

                                                    Api.launchWxaApp(optLaunchAppId(), optPath(), appType = versionType, landsapeMode = landscapeMode).subscribe({}, {})
                                                    Log.i(DocumentActivity.TAG, "success: ${it.baseResponse.errCode} ${it.baseResponse.errMsg}")
                                                }, {
                                                    Log.e(DocumentActivity.TAG, "error: $it")
                                                })
                                    }
                                }
                            }
                        }, {
                            Log.e(TAG, "error: $it")
                            respTextView.post {
                                var errorMsg = it.message ?: ""
                                if (errorMsg.contains("bridge not found")) {
                                    errorMsg += ", 确认WMPF框架处于运行状态"
                                }
                                Toast.makeText(this, "激活设备失败, error: $errorMsg", Toast.LENGTH_SHORT).show()
                                consoleText += "激活设备失败, error: $errorMsg"
                                respTextView.text = consoleText

                            }
                        })
            }
        }
    }

    private fun optPath(): String {
        val path = pathTv.text?.toString()
        return if (path == null || path.isBlank()) {
            ""
        } else {
            path
        }
    }

    private fun optLaunchAppId(): String {
        var launchAppId = findViewById<EditText>(R.id.et_launch_app_id).text.toString()
        if (launchAppId.isEmpty()) {
            launchAppId = "wxe5f52902cf4de896"
        }
        return launchAppId
    }
}
