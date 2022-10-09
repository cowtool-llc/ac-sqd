package com.cowtool.acsqd

import java.text.DecimalFormat

class SqdCalculator(
    private val ticket: String,
    private val aeroplanStatus: String,
    private val hasBonusPointsPrivilege: Boolean,
    private val segments: String,
    private val baseFare: Double?,
    private val surcharges: Double?
) {

    fun calculate(): SqdResult {
        val builder = StringBuilder()

        if (segments.isNotBlank() && baseFare != null || surcharges != null) {
            try {
                builder.append(calculateSqdBreakdown())
            } catch (e: SqdCalculatorException) {
                builder.append("<div>Error:<br/>${e.message}</div><br/>\n")
            }
        }

        return SqdResult(
            results = builder.toString(),
        )
    }

    @Throws(SqdCalculatorException::class)
    private fun calculateSqdBreakdown(): String {
        val itinerary = Itinerary.parse(
            ticket = ticket,
            aeroplanStatus = aeroplanStatus,
            hasBonusPointsPrivilege = hasBonusPointsPrivilege,
            segmentsCsv = segments,
            baseFare = baseFare,
            surcharges = surcharges
        )

        val builder = StringBuilder()
        builder.append("<div>")

        builder.append("<table style=\"border-collapse: collapse;\" border=\"1\">\n")
        builder.append("""<tr><td style="background-color:#FFFF00">Yellow is until the switchover to dollar-based earning</td></tr>""")
        builder.append("""<tr><td style="background-color:#FF0000"><b>Red is speculative (no facts available)</b></td></tr>""")
        builder.append("</table>\n")

        builder.append("<table style=\"font-family: monospace; border-collapse: collapse;\" border=\"1\">\n")
        builder.append("<tr><th>Airline</th>")
        builder.append("<th>Flight</th>")
        builder.append("<th>Fare (Brand)</th>")
        builder.append("<th>Distance</th>")
        builder.append("<th>Distance Source</th>")

        builder.append("""<th>SQM %</th>""")
        builder.append("""<th>SQM</th>""")
        builder.append("""<th>SQD</th>""")
        builder.append("""<th style="background-color:#FFFF00">Aeroplan %</th>""")
        builder.append("""<th style="background-color:#FFFF00">Aeroplan</th>""")
        builder.append("""<th style="background-color:#FFFF00">Bonus %</th>""")
        builder.append("""<th style="background-color:#FFFF00">Bonus</th>""")
        builder.append("""<th style="background-color:#FFFF00">Aeroplan Points</th>""")
        builder.append("""<th>Base</th>""")
        builder.append("""<th>Status</th>""")
        builder.append("""<th style="background-color:#FF0000">Bonus</th>""")
        builder.append("""<th style="background-color:#FF0000">Total Rate</th>""")
        builder.append("""<th>Aeroplan Points</th>""")
        builder.append("</tr>\n")

        itinerary.segments.forEach { segment ->
            builder.append("<tr>")
            builder.append("<td>${segment.airline}</td>")
            builder.append("<td>${"${segment.origin}-${segment.destination}"}</td>")
            builder.append("<td style=\"text-align:center\">${segment.fareClass}${if (segment.fareBrand.isNullOrBlank()) "" else " (${segment.fareBrand})"}</td>")
            builder.append("<td style=\"text-align:right\">${segment.distanceString}</td>")
            builder.append("<td style=\"text-align:center\">${segment.distanceSourceString}</td>")

            builder.append("""<td style="text-align:right">${segment.earningResult?.sqmPercent ?: "???"}</td>""")
            builder.append("""<td style="text-align:right">${segment.sqmString}</td>""")
            builder.append("""<td style="text-align:right">${segment.earningResult?.sqd?.toCurrencyString() ?: "???"}</td>""")
            builder.append("""<td style="text-align:right">${segment.earningResult?.aeroplanPointsPercent ?: "???"}</td>""")
            builder.append("""<td style="text-align:right">${segment.aeroplanMilesString}</td>""")
            builder.append("""<td style="text-align:right">${segment.earningResult?.bonusPointsPercent ?: "???"}</td>""")
            builder.append("""<td style="text-align:right">${segment.bonusPointsString}</td>""")
            builder.append("""<td style="text-align:right">${segment.totalMilesString}</td>""")
            builder.append("""<td style="text-align:right">${segment.baseRateString}</td>""")
            builder.append("""<td style="text-align:right">${segment.statusRateString}</td>""")
            builder.append("""<td style="text-align:right">${segment.bonusRateString}</td>""")
            builder.append("""<td style="text-align:right">${segment.totalRateString}</td>""")
            builder.append("""<td style="text-align:right">${segment.totalPointsString}</td>""")

            builder.append("</tr>\n")
        }

        // Total row
        builder.append("<tr>")
        builder.append("<th colspan=\"2\">Total</th>")
        builder.append("<th></th>")
        builder.append("<th style=\"text-align:right\">${itinerary.totalRow.distance ?: "???"}</th>")
        builder.append("<th></th>")

        builder.append("""<th></th>""")
        builder.append("""<th>${itinerary.totalRow.sqm ?: "???"}</th>""")
        builder.append("""<th>${itinerary.totalRow.sqd?.toCurrencyString() ?: "???"}</th>""")
        builder.append("""<th></th>""")
        builder.append("""<th style="text-align:right">${itinerary.totalRow.aeroplanMiles ?: "???"}</th>""")
        builder.append("""<th></th>""")
        builder.append("""<th style="text-align:right">${itinerary.totalRow.bonusPoints ?: "???"}</th>""")
        builder.append("""<th style="text-align:right">${itinerary.totalRow.totalMiles ?: "???"}</th>""")
        builder.append("""<th></th>""")
        builder.append("""<th></th>""")
        builder.append("""<th></th>""")
        builder.append("""<th></th>""")
        builder.append("""<th>${itinerary.totalRow.totalPoints ?: "???"}</th>""")

        builder.append("</tr>\n")

        builder.append("</table>")
        builder.append("</div><br/>\n")

        return builder.toString()
    }
}

private val currencyFormat = DecimalFormat("#,##0")
private fun Int.toCurrencyString() = currencyFormat.format(this)

internal val defaultSegments =
    """
AC,SFO,YVR,K,CO
AC,YVR,LHR,P,EL
LH,LHR,MUC,P,
TG,MUC,BKK,M
CX,BKK,HKG,C
""".trim()
