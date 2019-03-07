package cn.tursom.techtest

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import cn.tursom.tools.*

class EncryptionActivity : Activity() {
    lateinit var encryptionAnswerEditText: EditText
    lateinit var encryptionStrEditText: EditText
    val text: String
        inline get() = encryptionStrEditText.text.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encryption)

        initView()
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.encryptionMD5Button -> {
                encryptionAnswerEditText.setText(text.md5())
            }
            R.id.encryptionSHAButton -> {
                encryptionAnswerEditText.setText(text.sha())
            }
            R.id.encryptionSHA1Button -> {
                encryptionAnswerEditText.setText(text.sha1())
            }
            R.id.encryptionSHA256Button -> {
                encryptionAnswerEditText.setText(text.sha256())
            }
            R.id.encryptionSHA384Button -> {
                encryptionAnswerEditText.setText(text.sha384())
            }
            R.id.encryptionSHA512Button -> {
                encryptionAnswerEditText.setText(text.sha512())
            }
        }
    }

    private fun initView() {
        encryptionAnswerEditText = findViewById(R.id.encryptionAnswerEditText)
        encryptionStrEditText = findViewById(R.id.encryptionStrEditText)
    }

}
