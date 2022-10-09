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

    fun calculate(): String {
        val builder = StringBuilder()
        builder.append(getHeader())

        if (segments.isNotBlank() && baseFare != null || surcharges != null) {
            try {
                builder.append(calculateSqdBreakdown())
            } catch (e: SqdCalculatorException) {
                builder.append("<div>Error:<br/>${e.message}</div><br/>\n")
            }
        }

        builder.append(buildForm())
        builder.append(getFooter())
        return builder.toString()
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

    private fun getHeader(): String {
        val builder = StringBuilder()
        builder.append("<html>\n<head><title>SQD Calculator</title></head>\n<body>\n")
        builder.append("""<div><a href="https://www.flyertalk.com/forum/air-canada-aeroplan/1744575-new-improved-calculator-aqm-aeroplan-miles-aqd.html">FlyerTalk Thread</a><br/>""")
        builder.append("""<a href="https://github.com/scottkennedy/ac-aqd">GitHub Repository</a></div><br/>""")
        return builder.toString()
    }

    private fun getFooter(): String {
        val builder = StringBuilder()
        builder.append("<div>Download <a href=\"?fetch=countries\">country data</a>, <a href=\"?fetch=airports\">airport data</a>, <a href=\"?fetch=distances\">Aeroplan distance data</a></div>\n")
        builder.append("</body>\n</html>")
        return builder.toString()
    }

    private fun buildForm(): String {
        val statuses = setOf("25", "35", "50", "75", "100")
        return """
            <div><form method="get">
            <table border="0">
            <tr><td style="text-align:right"><label for="ticket">Ticket</label></td>
            <td><select name="ticket" id="ticket">
              <option value="014"${if (ticket.startsWith("014")) " selected=\"selected\"" else ""}>Air Canada (014)</option>
              <option value="000"${if (!ticket.startsWith("014")) " selected=\"selected\"" else ""}>Other</option>
            </select></td></tr>
            <tr><td style="text-align:right"><label for="aeroplan_status">Aeroplan Elite Status</label></td>
            <td><select name="aeroplan_status" id="aeroplan_status">
              <option value=""${if (aeroplanStatus !in statuses) """ selected="selected"""" else ""}>None</option>
              <option value="25"${if (aeroplanStatus == "25") " selected=\"selected\"" else ""}>25K</option>
              <option value="35"${if (aeroplanStatus == "35") " selected=\"selected\"" else ""}>35K</option>
              <option value="50"${if (aeroplanStatus == "50") " selected=\"selected\"" else ""}>50K</option>
              <option value="75"${if (aeroplanStatus == "75") " selected=\"selected\"" else ""}>75K</option>
              <option value="100"${if (aeroplanStatus == "100") " selected=\"selected\"" else ""}>SE</option>
            </select></td></tr>
            <tr><td style="text-align:right"><label for="bonus_points_privilege">Bonus Aeroplan Points Select Privilege</label></td>
            <td><input type="checkbox" name="bonus_points_privilege" id="bonus_points_privilege" value="true"${if (hasBonusPointsPrivilege) """ checked="checked"""" else ""}"/></td></tr>
            <tr><td style="text-align:right"><label for="base_fare">Base Fare</label></td>
            <td><input type="number" min="0" step="0.01" name="base_fare" id="base_fare" required value="${baseFare?.toString() ?: "0"}"/></td></tr>
            <tr><td style="text-align:right"><label for="surcharges">SQD-eligible surcharges (YQ/YR)</label></td>
            <td><input type="number" min="0" step="0.01" name="surcharges" id="surcharges" required value="${surcharges?.toString() ?: "0"}"/></td></tr>
            <tr><td colspan="2"><label for="segments">Segments:<br/>Operating airline,Origin,Destination,Fare Class,Brand Code<br/>You can leave off fare brand for non-AC</label></td>
            <tr><td colspan="2"><textarea rows="16" cols="50" name="segments" required>${if (segments.isBlank()) defaultSegments else segments}</textarea></td></tr>
            <tr><td colspan="2"><button type="submit">Calculate SQM/SQD</button></td></tr>
            </table>
            </form></div>
            <div><b>Brand Codes:</b><br/>${FareBrand.generateHtmlList()}</div><br/>
        """.trimIndent()
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
