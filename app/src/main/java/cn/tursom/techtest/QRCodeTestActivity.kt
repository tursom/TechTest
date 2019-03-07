package cn.tursom.techtest

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import cn.tursom.tools.hideTitle
import com.google.zxing.integration.android.IntentIntegrator

class QRCodeTestActivity : AppCompatActivity() {
    private lateinit var outputTextView: TextView

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            runOnUiThread {
                if (result.contents == null) {
                    outputTextView.text = "Canceled"
                } else {
                    outputTextView.text = "Scanned: " + result.contents
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_test)
        hideTitle()

        outputTextView = findViewById(R.id.QRCodeTest_outputTextView)
    }

    fun onClick(@Suppress("UNUSED_PARAMETER") view: View) {
        val intentIntegrator = IntentIntegrator(this)
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
//				.setBeepEnabled(false)  //不开启扫码提示音
            .initiateScan() // 开始扫描
    }
}
