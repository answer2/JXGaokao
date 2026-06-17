package dev.answer

// ==================== 主函数 ====================
fun main() {
    // 请根据实际情况修改输入输出路径
    val inputExcelPath = "D:/test.xlsx"      // 输入文件（包含姓名、报考号、身份证号）
    val outputExcelPath = "D:/成绩结果.xlsx"  // 输出文件路径

    println("正在读取考生列表...")
    val candidates = readCandidateListFromExcel(inputExcelPath)
    println("共读取到 ${candidates.size} 名考生")

    val results = mutableListOf<ExamResult>()
    for (candidate in candidates) {
        println("正在查询：${candidate.name} (${candidate.examNo})")
        val result = fetchScoreAndMsg(candidate.examNo, candidate.idCard.takeLast(4))
        // 补充考生姓名（因为 fetchScoreAndMsg 中临时构造的 candidate 没有姓名）
        val finalResult = result.copy(candidate = candidate)
        results.add(finalResult)
        // 打印简要信息
        if (finalResult.scoreData != null) {
            println("  姓名：${finalResult.scoreData.xm}")
        } else {
            println("  查询失败：${finalResult.error ?: finalResult.msg}")
        }
    }

    writeResultsToExcel(results, outputExcelPath)
    println("全部处理完成！")
}