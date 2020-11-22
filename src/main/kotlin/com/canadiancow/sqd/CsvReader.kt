package com.canadiancow.sqd

fun <T> parseResourceToCsv(
    resourcePath: String,
    keyParser: (List<String>) -> String = { it.first() },
    lineParser: (Int, String, List<String>) -> T
): Map<String, T> {
    val csvText = ::SqdCalculator.javaClass.getResource(resourcePath).readText()

    return csvText.lines().filter { it.isNotBlank() }.mapIndexedNotNull { index, s ->
        if (index == 0) {
            // Header
            null
        } else {
            val values = s.split(",")
            keyParser(values) to lineParser(index + 1, s, values)
        }
    }.toMap()
}
