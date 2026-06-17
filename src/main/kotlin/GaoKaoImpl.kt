package dev.answer

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jsoup.Jsoup
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

// ==================== 数据类定义 ====================
data class ExamCandidate(
    val name: String,      // 姓名
    val examNo: String,    // 报考号/考生号
    val idCard: String     // 身份证号
)

data class ExamResult(
    val candidate: ExamCandidate,
    val scoreData: ScoreData? = null,   // 成绩数据，若获取失败则为 null
    val msg: String = "",                // 返回的提示信息
    val error: String? = null            // 异常信息
)

@Serializable
data class ScoreData(
    val ksh: String,            // 考生号
    val zkzh: String,           // 准考证号
    val xm: String,             // 姓名
    val wyyzmc: String,         // 外语语种
    val wgbz: String,           // 违规标志
    val yw: String,             // 语文
    val sx: String,             // 数学
    val wy: String,             // 外语
    val sxkmmc: String,         // 首选科目名称
    val sxkm: String,           // 首选科目成绩
    val zxkm1mc: String,        // 再选科目1名称
    val zxkm1: String,          // 再选科目1成绩
    val zxkm2mc: String,        // 再选科目2名称
    val zxkm2: String,          // 再选科目2成绩
    val jf: String,             // 加分
    val tzf: String,            // 总成绩（含加分）
    val pm: String              // 排名
)

// ==================== Excel 读取 ====================
fun readCandidateListFromExcel(inputPath: String): List<ExamCandidate> {
    val candidates = mutableListOf<ExamCandidate>()
    WorkbookFactory.create(File(inputPath)).use { workbook ->
        val sheet = workbook.getSheetAt(0)
        // 从第2行开始（假设第1行是表头）
        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue
            val name = row.getCell(0)?.toString()?.trim() ?: ""
            val examNo = when (row.getCell(1)?.cellType) {
                CellType.NUMERIC -> row.getCell(1).numericCellValue.toLong().toString()
                else -> row.getCell(1)?.toString()?.trim() ?: ""
            }
            val idCard = when (row.getCell(2)?.cellType) {
                CellType.NUMERIC -> row.getCell(2).numericCellValue.toLong().toString()
                else -> row.getCell(2)?.toString()?.trim() ?: ""
            }
            if (name.isNotBlank() && examNo.isNotBlank() && idCard.isNotBlank()) {
                candidates.add(ExamCandidate(name, examNo, idCard))
            }
        }
    }
    return candidates
}

// ==================== 成绩获取与解析 ====================
fun fetchScoreAndMsg(examNo: String, idCard: String): ExamResult {
    return try {
        val html = GaoKaoNet.require(examNo, idCard)   // 假设此函数已实现网络请求
        val scoreJson = extractScoreFromHtml(html)
        val msg = extractMsgFromHtml(html)
        val scoreData = if (scoreJson.isNotEmpty()) {
            parseScoreData(scoreJson)
        } else null
        ExamResult(ExamCandidate("", examNo, idCard), scoreData, msg)
    } catch (e: Exception) {
        ExamResult(ExamCandidate("", examNo, idCard), null, error = e.message)
    }
}

fun extractMsgFromHtml(html: String): String {
    val pattern = Regex("""var\s+msg\s*=\s*"([^"]*)";""")
    val match = pattern.find(html)
    return match?.groupValues?.get(1) ?: ""
}

fun extractScoreFromHtml(html: String): String {
    val doc = Jsoup.parse(html)
    val scripts = doc.select("script").map { it.html() }
    val regex = Regex("""var\s+_score\s*=\s*'([^']*)'(?:\s*\.toLowerCase\(\))?\s*;""")
    for (script in scripts) {
        val match = regex.find(script)
        if (match != null) {
            var jsonStr = match.groupValues[1]
                .replace("\\'", "'")
                .replace("\\\\", "\\")
            if (match.value.contains(".toLowerCase()")) {
                jsonStr = jsonStr.lowercase()
            }
            return jsonStr
        }
    }
    return ""
}

fun parseScoreData(jsonString: String): ScoreData {
    val json = Json { ignoreUnknownKeys = true }
    return json.decodeFromString<ScoreData>(jsonString)
}

// ==================== Excel 导出 ====================
fun writeResultsToExcel(results: List<ExamResult>, outputPath: String) {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("高考成绩")

    // 表头
    val headers = arrayOf(
        "姓名", "报考号", "准考证号", "语文", "数学", "外语", "外语语种",
        "首选科目名称", "首选科目成绩", "再选科目1名称", "再选科目1成绩",
        "再选科目2名称", "再选科目2成绩", "加分", "总分", "排名", "原始分之和",
        "提示信息", "错误信息"
    )
    val headerRow = sheet.createRow(0)
    headers.forEachIndexed { idx, header -> headerRow.createCell(idx).setCellValue(header) }

    // 数据行
    results.forEachIndexed { idx, result ->
        val row = sheet.createRow(idx + 1)
        if (result.scoreData != null) {
            val d = result.scoreData
            row.createCell(0).setCellValue(d.xm.trim())
            row.createCell(1).setCellValue(d.ksh.trim())
            row.createCell(2).setCellValue(d.zkzh.trim())
            row.createCell(3).setCellValue(d.yw.trim())
            row.createCell(4).setCellValue(d.sx.trim())
            row.createCell(5).setCellValue(d.wy.trim())
            row.createCell(6).setCellValue(d.wyyzmc.trim())
            row.createCell(7).setCellValue(d.sxkmmc.trim())
            row.createCell(8).setCellValue(d.sxkm.trim())
            row.createCell(9).setCellValue(d.zxkm1mc.trim())
            row.createCell(10).setCellValue(d.zxkm1.trim())
            row.createCell(11).setCellValue(d.zxkm2mc.trim())
            row.createCell(12).setCellValue(d.zxkm2.trim())
            row.createCell(13).setCellValue(d.jf.trim())
            row.createCell(14).setCellValue(d.tzf.trim())
            row.createCell(15).setCellValue(d.pm.trim())

            val sum = listOf(d.yw, d.sx, d.wy, d.sxkm, d.zxkm1, d.zxkm2)
                .sumOf { it.trim().toIntOrNull() ?: 0 }
            row.createCell(16).setCellValue(sum.toDouble())
            row.createCell(17).setCellValue(result.msg)
            row.createCell(18).setCellValue(result.error ?: "")
        } else {
            // 成绩获取失败，填充提示信息
            row.createCell(17).setCellValue(result.msg)
            row.createCell(18).setCellValue(result.error ?: "未获取到成绩数据")
        }
    }

    // 自动调整列宽
    for (i in headers.indices) {
        sheet.autoSizeColumn(i)
    }

    FileOutputStream(outputPath).use { fos -> workbook.write(fos) }
    println("结果已导出到：$outputPath")
}