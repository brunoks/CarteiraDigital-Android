package br.com.ucl.cryptocoin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.ly_edit_pass.view.*
import org.cryptonode.jncryptor.AES256JNCryptor
import org.cryptonode.jncryptor.JNCryptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupView()
        if (CryptoPrefs.getData(this@LoginActivity, CryptoPrefs.PARAM_PUBLIC_KEY) == null || CryptoPrefs.getData(this@LoginActivity, CryptoPrefs.PARAM_PUBLIC_KEY) == "") {
            generateKeys()
        } else {
            button_login.isEnabled = true
            button_login.alpha = 1f
//            encrypt(CryptoPrefs.getData(this@LoginActivity, CryptoPrefs.PARAM_PRIVATE_KEY)!!)
        }
    }

    private fun setupView() {
        button_login.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun generateKeys() {
        RetrofitService.getInstanceService()
            .generateWallet()
            .enqueue(object : Callback<WalletKeys> {
                override fun onFailure(call: Call<WalletKeys>, t: Throwable) {
                    t.printStackTrace()
                }

                override fun onResponse(call: Call<WalletKeys>, response: Response<WalletKeys>) {
                    val res = response.body()
                    if (response.isSuccessful) {
                        CryptoPrefs.setData(this@LoginActivity, res?.publicKey!!, res.privateKey)
                        encrypt(res.privateKey)
                        button_login.isEnabled = true
                        button_login.alpha = 1f
                    }
                }
            })
    }

    private fun encrypt(privateKey: String) {
        val cryptor = AES256JNCryptor()

        val view = LayoutInflater.from(this).inflate(R.layout.ly_edit_pass, null)

        val editPass = view.edit_password

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Salvar senha e chave privada")
        dialog.setView(view)
        dialog.setCancelable(false)
        dialog.setPositiveButton("Salvar") { dialog, _ ->
            val senha = editPass.text.toString()
            if(senha.isNotEmpty()) {
                val bytesPrivateKey = privateKey.toByteArray()
                try {
                    val bytes = cryptor.encryptData(bytesPrivateKey, senha.toCharArray())
                    val data = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    CryptoPrefs.putPrivate(this, data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            dialog.dismiss()
        }
        dialog.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        dialog.create()
        dialog.show()
    }
}

data class WalletKeys(
    @SerializedName("private_key") val privateKey: String,
    @SerializedName("public_key") val publicKey: String
) {
    companion object {
        const val PARAM = "wallet_keys"
    }
}
