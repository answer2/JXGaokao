package dev.answer

import me.patamon.lz.LZString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 *
 * @author AnswerDev
 * @date 2026/6/9 22:21
 * @description GaoKaoNet
 */
object GaoKaoNet {
    fun require(key1 : String, key2 : String) : String{
        val client = OkHttpClient()

        val mediaType = "application/x-www-form-urlencoded".toMediaType()
        val body = "key1=${LZString.compressToBase64(key1)}&key2=${LZString.compressToBase64(key2)}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://jxcf.jxeea.cn")
            .post(body)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edg/137.0.0.0")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Cache-Control", "max-age=0")
            .addHeader("sec-ch-ua", "\"Microsoft Edge\";v=\"137\", \"Chromium\";v=\"137\", \"Not/A)Brand\";v=\"24\"")
            .addHeader("sec-ch-ua-mobile", "?0")
            .addHeader("sec-ch-ua-platform", "\"Windows\"")
            .addHeader("Origin", "https://jxcf.jxeea.cn")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("Sec-Fetch-Site", "same-origin")
            .addHeader("Sec-Fetch-Mode", "navigate")
            .addHeader("Sec-Fetch-User", "?1")
            .addHeader("Sec-Fetch-Dest", "document")
            .addHeader("Referer", "https://jxcf.jxeea.cn/")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
            //.addHeader("Cookie", "SLBServerPool78=0000.66.78.182.1751110098; _cap_id=1BC1M65eFOff6f5Z5tjfCeil3e4xQtvlRkks3651ucfmAT63quSVLA%3D%3D")
            .build()

        val response = client.newCall(request).execute()
        val bodyString = response.body.string()
        return bodyString
    }
}