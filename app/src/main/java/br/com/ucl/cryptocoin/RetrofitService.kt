package br.com.ucl.cryptocoin

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

object RetrofitService {

    @JvmStatic
    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://uclcriptocoin.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @JvmStatic
    fun getInstanceService(): CryptoService {
        return getInstance().create(CryptoService::class.java)
    }

}

interface CryptoService {

    /**
     * Requisicao para gerar chave publica e privada
     */
    @GET("generate_wallet")
    fun generateWallet(): Call<WalletKeys>

    /**
     * Verifica quantidade de moeadas do usuario
     */
    @POST("balance")
    fun getBalance(@Body publicKey: JsonObject): Call<BalanceTO>

    /**
     * Gerar chave publica a partir da privada
     */
    @POST("generate_public_key")
    fun getNewPublicKey(@Body publicKey: JsonObject): Call<GeneratePublicKeyTO>

    /**
     * Gerar uma nova transacao
     */
    @POST("transaction")
    fun makePayment(@Body jsonObject: JsonObject): Call<MensagemRetorno>

}

