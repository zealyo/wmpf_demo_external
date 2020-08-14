package com.eyuan.wmpf.screen.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.eyuan.wmpf.screen.R
import com.eyuan.wmpf.screen.RequestsRepo

class PushMsgQuickStartActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PushMsgActivity"
    }

    private val consoleView: TextView by lazy {
        findViewById<TextView>(R.id.console)
    }

    private val delayView by lazy {
        findViewById<EditText>(R.id.et_delay)
    }

    private val appIdView: EditText by lazy {
        findViewById<EditText>(R.id.et_app_id)
    }

    private val msgView: EditText by lazy {
        findViewById<EditText>(R.id.et_msg)
    }

    private val emitterView: Button by lazy {
        findViewById<Button>(R.id.emitter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push_msg_quick_start)
        emitterView.setOnClickListener {
            try {
                if (pushToken.isNotBlank() && accessToken.isNotBlank()) {
                    RequestsRepo.postMsg(accessToken, pushToken, msgView.text.toString(), 0) { success, ret ->
                        printlnToView("3. 推送请求: isSuccess = $success, ret = $ret")
                        printlnToView("4. 等待推送消息返回...")
                    }
                } else {
                    start()
                }
            } catch (e: Exception) {
                Log.d(TAG, "onCreate: ")
            }
        }
    }

    private var pushToken = ""
    private var accessToken = ""

    private fun start() {
        val appId = appIdView.text.toString()
        val msg = msgView.text.toString()
        var delay = Integer.valueOf(delayView.text.toString())
        if (delay < 0) delay = 0

        if (appId.isBlank()) {
            toast("appId can not be blank")
            return
        }
        printlnToView("**Test Only: 该示例没有维护状态, 不应该多次获取token**")
        printlnToView("1. 获取access_token...")
        RequestsRepo.getAccessToken { success, ret ->
            var accessToken = ""
            if (success) {
                accessToken = ret
                this.accessToken = accessToken
                printlnToView("result: $ret")
                printlnToView("2. 获取push_token...")
                RequestsRepo.getPushToken(appId) { success, ret, expireTime, errMsg ->
                    if (success) {
                        printlnToView("result: token = [$ret], expireTime = [$expireTime]")
                    } else {
                        printlnToView("result: $errMsg")
                        return@getPushToken
                    }
                    val pushToken = ret
                    this.pushToken = pushToken

                    printlnToView("使用获取push_token = $pushToken, " +
                            "accessToken = $accessToken, msg = ${msgView.text}, delay = $delay 推送")
                    RequestsRepo.postMsg(accessToken, pushToken, msgView.text.toString(), delay) { success, ret ->
                        printlnToView("3. 推送请求: isSuccess = $success, ret = $ret")
                        printlnToView("4. 等待推送消息返回...")
                        RequestsRepo.setMsgCallback(this@PushMsgQuickStartActivity)
                    }
                }
            } else {
                printlnToView("result: $ret")
            }
        }

    }

    @SuppressLint("SetTextI18n")
    fun printlnToView(content: String) {
        runOnUiThread {
            val temp = consoleView.text.toString()
            consoleView.text = temp + "\n" + content
        }
    }

}

fun Activity.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
