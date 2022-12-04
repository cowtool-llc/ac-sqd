package com.canadiancow.sqd

class SqdCalculator {
    fun calculate(): String {
        val builder = StringBuilder()
        builder.append(getHeader())
        return builder.toString()
    }

    private fun getHeader(): String {
        val builder = StringBuilder()
        builder.append("<html>\n<head><title>SQD Calculator</title></head>\n<body>\n")
        builder.append("""<div style="color: red;">This calculator has moved to a new location: Please update your bookmarks. <a href="https://acsqd.cowtool.com/">https://acsqd.cowtool.com/</a></div><br/>""")
        builder.append("""<div><a href="https://www.flyertalk.com/forum/air-canada-aeroplan/1744575-new-improved-calculator-aqm-aeroplan-miles-aqd.html">FlyerTalk Thread</a><br/>""")
        builder.append("""<a href="https://github.com/scottkennedy/ac-aqd">GitHub Repository</a></div><br/>""")
        return builder.toString()
    }
}
