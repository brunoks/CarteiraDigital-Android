package br.com.ucl.cryptocoin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import androidx.core.app.ActivityCompat
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.gson.JsonObject
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.ly_edit_pass.view.*
//import com.google.zxing.integration.android.IntentIntegrator
import me.sudar.zxingorient.ZxingOrient
import me.sudar.zxingorient.ZxingOrientResult
import org.cryptonode.jncryptor.AES256JNCryptor
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity() {

    private val REQUEST_CAMERA = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupView()
        requestBalance()
        generateQrCode()
    }

    private fun setupView() {
        text_valor_conta.setOnClickListener {
            requestBalance()
        }

        button_pagar.setOnClickListener {
            if(Build.VERSION.SDK_INT>=23) {
                if(ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.CAMERA
                        ), REQUEST_CAMERA
                    )
                } else {
                    /*val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, REQUEST_CAMERA)*/
                    scan()
                }
            }
        }

        image_qrcode.setOnClickListener {
            encrypt()
        }
    }

    private fun encrypt() {
        val cryptor = AES256JNCryptor()

        val view = LayoutInflater.from(this).inflate(R.layout.ly_edit_pass, null)

        val editPass = view.edit_password

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Gerar nova chave publica")
        dialog.setView(view)
        dialog.setCancelable(false)
        dialog.setPositiveButton("Gerar") { dialog, _ ->
            val senha = editPass.text.toString()
            if(senha.isNotEmpty()) {
                try {
                    val d = Base64.decode(CryptoPrefs.getData(this, CryptoPrefs.PARAM_PRIVATE), Base64.NO_WRAP)
                    val dataByte = cryptor.decryptData(d, senha.toCharArray())
                    val c = String(dataByte, StandardCharsets.UTF_8)

                    val json = JsonObject()
                    json.addProperty("private_key", c)

                    RetrofitService.getInstanceService().getNewPublicKey(json)
                            .enqueue(object : Callback<GeneratePublicKeyTO> {
                                override fun onFailure(call: Call<GeneratePublicKeyTO>, t: Throwable) {
                                    t.printStackTrace()
                                }

                                override fun onResponse(call: Call<GeneratePublicKeyTO>, response: Response<GeneratePublicKeyTO>) {
                                    if(response.isSuccessful) {
                                        CryptoPrefs.updatePublicKey(this@MainActivity, response.body()?.publicKey!!)
                                        generateQrCode()
                                    }
                                }
                            })
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            dialog.dismiss()
        }
        dialog.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        dialog.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        /*val res = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(res != null) {
            if(res.contents == null) {

            } else {
                System.out.println(res.contents)
            }
        } else {

        }*/

        val scanResult = ZxingOrient.parseActivityResult(requestCode, resultCode, data)

        if (scanResult != null) {
            val publicKeyUserToPay = scanResult.contents
            if (publicKeyUserToPay != null && publicKeyUserToPay.isNotEmpty()) {
                val intent = Intent(this, PagarActivity::class.java)
                intent.putExtra("pay", publicKeyUserToPay)
                startActivity(intent)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    fun scan() {
        /*val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
        integrator.setPrompt("Pagar")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()*/

        ZxingOrient(this)
            .showInfoBox(false)
            .setBeep(false)
            .setVibration(false)
            .setToolbarColor("#00000000")
            .initiateScan()
    }

    override fun onStart() {
        super.onStart()
        requestBalance()
    }

    fun generateQrCode() {
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitmapMatrix = qrCodeWriter.encode(CryptoPrefs.getData(this@MainActivity, CryptoPrefs.PARAM_PUBLIC_KEY).toString(), BarcodeFormat.QR_CODE, 180, 180)
            val width = bitmapMatrix.width
            val height = bitmapMatrix.height
            val bit = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bit.setPixel(x, y, if (bitmapMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            image_qrcode.setImageBitmap(bit)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun requestBalance() {
        progress_load_balance.visibility = View.VISIBLE
        val json = JsonObject()
        json.addProperty("public_key", CryptoPrefs.getData(this@MainActivity, CryptoPrefs.PARAM_PUBLIC_KEY).toString())

        RetrofitService.getInstanceService()
            .getBalance(json)
            .enqueue(object : Callback<BalanceTO> {
                override fun onFailure(call: Call<BalanceTO>, t: Throwable) {
                    t.printStackTrace()
                    progress_load_balance.visibility = View.INVISIBLE
                }

                override fun onResponse(call: Call<BalanceTO>, response: Response<BalanceTO>) {
                    if (response.isSuccessful) {
                        text_valor_conta.text = "UC ${response.body()?.balance}"
                    }
                    progress_load_balance.visibility = View.INVISIBLE
                }
            })
    }
}

data class BalanceTO(
    @SerializedName("balance") val balance: Float
) {
    companion object {
        const val PARAM = "balance_to"
    }
}

data class GeneratePublicKeyTO(
    @SerializedName("public_key") val publicKey: String
) {
    companion object {
        const val PARAM = "generate_public_key"
    }
}