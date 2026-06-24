package dev.answer

import com.mmg.ddddocr4j.utils.DDDDOcrUtil
/**
 *
 * @author AnswerDev
 * @date 2026/6/24 18:24
 * @description ocr
 */
class Ocr {
companion object {
    @JvmStatic
    fun main(args : Array<String>) {
       println(GaoKaoNet.getGrade("123456", "123456"))
    }

    @JvmStatic
    fun getCode(base64 : String ): String {
        return DDDDOcrUtil.getCode(base64)
    }
}

}