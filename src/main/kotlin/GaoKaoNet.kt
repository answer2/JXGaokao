package dev.answer

import com.mmg.ddddocr4j.utils.DDDDOcrUtil
import me.patamon.lz.LZString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 *
 * @author AnswerDev
 * @date 2026/6/9 22:21
 * @description GaoKaoNet
 */
object GaoKaoNet {

    val hostName : String = "jxcf.jxeea.cn"
    fun require(key1 : String,
                key2 : String,
                key3 : String = "",
                 _cap_id  : String = "") : String{
        val client =createUnsafeOkHttpClient()

        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body = "key1=${LZString.compressToBase64(key1)}&key2=${LZString.compressToBase64(key2)}&key3=${LZString.compressToBase64(key3)}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://$hostName")
            .post(body)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edg/137.0.0.0")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Cache-Control", "max-age=0")
            .addHeader("sec-ch-ua", "\"Microsoft Edge\";v=\"137\", \"Chromium\";v=\"137\", \"Not/A)Brand\";v=\"24\"")
            .addHeader("sec-ch-ua-mobile", "?0")
            .addHeader("sec-ch-ua-platform", "\"Windows\"")
            .addHeader("Origin", "https://$hostName")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("Sec-Fetch-Site", "same-origin")
            .addHeader("Sec-Fetch-Mode", "navigate")
            .addHeader("Sec-Fetch-User", "?1")
            .addHeader("Sec-Fetch-Dest", "document")
            .addHeader("Referer", "https://$hostName/")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
            .addHeader("Cookie", "_cap_id=$_cap_id")
            .build()

        val response = client.newCall(request).execute()
        val bodyString = response.body.string()
        return bodyString
    }


    data class CaptchaResult(
        val capId: String,
        val imageBase64: String
    )

    fun getCaptcha(
        cookie: String = ""
    ): CaptchaResult? {
        val client =createUnsafeOkHttpClient()
        val t = System.currentTimeMillis()
        val url = "https://$hostName/captcha/getcode?t=$t"

        val request = Request.Builder()
            .url(url)
            .get()
            .header("Connection", "keep-alive")
            .header("Pragma", "no-cache")
            .header("Cache-Control", "no-cache")
            .header("sec-ch-ua-platform", "\"Windows\"")
            .header("X-Requested-With", "XMLHttpRequest")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36 Edg/149.0.0.0")
            .header("Accept", "application/json, text/javascript, */*; q=0.01")
            .header("sec-ch-ua", "\"Microsoft Edge\";v=\"149\", \"Chromium\";v=\"149\", \"Not)A;Brand\";v=\"24\"")
            .header("sec-ch-ua-mobile", "?0")
            .header("Sec-Fetch-Site", "same-origin")
            .header("Sec-Fetch-Mode", "cors")
            .header("Sec-Fetch-Dest", "empty")
            .header("Referer", "http://$hostName/")
            .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
            .apply { if (cookie.isNotEmpty()) header("Cookie", cookie) }
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    System.err.println("请求失败: HTTP ${response.code}")
                    return null
                }

                // 从响应头提取 _cap_id
                val capId = response.headers("Set-Cookie")
                    .firstOrNull { it.startsWith("_cap_id=") }
                    ?.substringAfter("_cap_id=")
                    ?.substringBefore(";")
                    ?: run {
                        System.err.println("响应头中未找到 _cap_id")
                        return null
                    }

                // 解析响应体，提取 img
                val body = response.body?.string() ?: run {
                    System.err.println("响应体为空")
                    return null
                }
                val json = JSONObject(body)
                if (json.getInt("code") != 1) {
                    System.err.println("业务错误: ${json.optString("msg")}")
                    return null
                }
                val imageBase64 = json.getJSONObject("data").getString("img")

                CaptchaResult(capId = capId, imageBase64 = imageBase64)
            }
        } catch (e: Exception) {
            System.err.println("请求异常: ${e.message}")
            null
        }
    }

    fun getGrade(key1 : String,
                 key2 : String): String {

        val captcha =  getCaptcha();
        if (captcha != null) {
            println(captcha.capId)
          return require(
                key1,
                key2,
                DDDDOcrUtil.getCode(captcha.imageBase64),
                captcha.capId);
        }
        return ""
    }

    // 创建一个信任所有证书和主机名的客户端（危险！）
    fun createUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) = Unit
            override fun getAcceptedIssuers(): Array<out X509Certificate?>? = arrayOf()
        })
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val hostnameVerifier = HostnameVerifier { _, _ -> true }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(hostnameVerifier)
            .build()
    }
}