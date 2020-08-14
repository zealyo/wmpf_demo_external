package com.eyuan.wmpf.screen.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.eyuan.wmpf.screen.R
import com.eyuan.wmpf.screen.ui.DocumentActivity
import com.eyuan.wmpf.screen.ui.FastExperienceActivity
import com.eyuan.wmpf.screen.ui.MainActivity
import com.eyuan.wmpf.screen.ui.QAActivity

class GuideActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_guide)

        findViewById<Button>(R.id.btn_fast_experience).setOnClickListener {
            startActivity(Intent(this, FastExperienceActivity::class.java))
        }

        findViewById<Button>(R.id.btn_detail).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<Button>(R.id.btn_document).setOnClickListener {
            startActivity(Intent(this, DocumentActivity::class.java))
        }

        findViewById<Button>(R.id.btn_qa).setOnClickListener {
            startActivity(Intent(this, QAActivity::class.java))
        }
    }

}