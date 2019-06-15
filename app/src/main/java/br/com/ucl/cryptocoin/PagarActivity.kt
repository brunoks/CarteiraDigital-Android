package br.com.ucl.cryptocoin

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.ly_edit_pass.view.*
import kotlinx.android.synthetic.main.ly_edit_to_pay.*
import org.cryptonode.jncryptor.AES256JNCryptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.charset.StandardCharsets

class PagarActivity: AppCompatActivity() {

    private val cryptor = AES256JNCryptor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ly_edit_to_pay)

        button_pagar.setOnClickListener {
            if(text_valor_conta.text.toString().isNotEmpty()) {
                val valorPagamento = text_valor_conta.text.toString()

                encrypt(valorPagamento)
            }
        }
    }

    private fun encrypt(valorPagamento: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.ly_edit_pass, null)

        val editPass = view.edit_password

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Gerar nova chave publica")
        dialog.setView(view)
        dialog.setCancelable(false)
        dialog.setPositiveButton("Gerar") { dialog, _ ->
            val senha = editPass.text.toString()
            if(senha.isNotEmpty()) {
                pagament(senha, valorPagamento)
            }
            dialog.dismiss()
        }
        dialog.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        dialog.create()
        dialog.show()
    }

    fun pagament(senha: String, valorPagamento: String) {
        var c = ""
        try {
            val d = Base64.decode(CryptoPrefs.getData(this, CryptoPrefs.PARAM_PRIVATE), Base64.NO_WRAP)
            val dataByte = cryptor.decryptData(d, senha.toCharArray())
            c = String(dataByte, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val jsonObject = JsonObject()
        jsonObject.addProperty("public_key", intent.extras?.getString("pay"))
        jsonObject.addProperty("private_key", c)
        jsonObject.addProperty("value", valorPagamento.toFloat())

        progress_payment.visibility = View.VISIBLE

        RetrofitService.getInstanceService()
                .makePayment(jsonObject)
                .enqueue(object : Callback<MensagemRetorno> {
                    override fun onFailure(call: Call<MensagemRetorno>, t: Throwable) {
                        t.printStackTrace()
                        progress_payment.visibility = View.INVISIBLE
                        finish()
                    }

                    override fun onResponse(call: Call<MensagemRetorno>, response: Response<MensagemRetorno>) {
                        Toast.makeText(applicationContext, response.body()?.message, Toast.LENGTH_LONG).show()
                        finish()
                    }
                })
    }

}

data class MensagemRetorno(
        @SerializedName("message") val message: String
)