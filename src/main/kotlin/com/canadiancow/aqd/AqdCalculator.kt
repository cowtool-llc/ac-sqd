package com.canadiancow.aqd

import java.text.DecimalFormat

class AqdCalculator(
        private val ticket: String,
        private val altitudeStatus: String,
        private val hasBonusMilesPrivilege: Boolean,
        private val segments: String,
        private val baseFare: Double?,
        private val surcharges: Double?
) {

    fun calculate(): String {
        val builder = StringBuilder()
        builder.append(getHeader())

        if (segments.isNotBlank() && baseFare != null || surcharges != null) {
            try {
                builder.append(calculateAqdBreakdown())
            } catch (e: AqdCalculatorException) {
                builder.append("<div>Error:<br/>${e.message}</div><br/>\n")
            }
        }

        builder.append(buildForm())
        builder.append(getFooter())
        return builder.toString()
    }

    @Throws(AqdCalculatorException::class)
    private fun calculateAqdBreakdown(): String {
        val itinerary = Itinerary.parse(
                ticket = ticket,
                altitudeStatus = altitudeStatus,
                hasBonusMilesPrivilege = hasBonusMilesPrivilege,
                segmentsCsv = segments,
                baseFare = baseFare,
                surcharges = surcharges
        )

        val builder = StringBuilder()
        builder.append("<div>")
        builder.append("<table style=\"font-family: monospace; border-collapse: collapse;\" border=\"1\">\n")
        builder.append("<tr><th>Airline</th>")
        builder.append("<th>Flight</th>")
        builder.append("<th>Fare Class (Brand)</th>")
        builder.append("<th>Distance</th>")
        builder.append("<th>Distance Source</th>")
        builder.append("<th>AQM %</th>")
        builder.append("<th>AQM</th>")
        builder.append("<th>AQD</th>")
        builder.append("<th>Aeroplan %</th>")
        builder.append("<th>Aeroplan</th>")
        builder.append("<th>Bonus %</th>")
        builder.append("<th>Bonus</th>")
        builder.append("<th>Total Aeroplan</th>")
        builder.append("</tr>\n")

        itinerary.segments.forEachIndexed { index, segment ->
            builder.append("<tr>")
            builder.append("<td>${segment.airline}</td>")
            builder.append("<td>${"${segment.origin}-${segment.destination}"}</td>")
            builder.append("<td style=\"text-align:center\">${segment.fareClass}${if (segment.fareBrand.isNullOrBlank()) "" else " (${segment.fareBrand})"}</td>")
            builder.append("<td style=\"text-align:right\">${segment.distanceString}</td>")
            builder.append("<td style=\"text-align:center\">${segment.distanceSourceString}</td>")
            builder.append("<td style=\"text-align:right\">${segment.earningResult?.aqmPercent ?: "???"}</td>")
            builder.append("<td style=\"text-align:right\">${segment.aqmString}</td>")
            builder.append("<td style=\"text-align:right\">${itinerary.segmentAqd[index]?.toCurrencyString() ?: "???"}</td>")
            builder.append("<td style=\"text-align:right\">${segment.earningResult?.aeroplanPercent ?: "???"}</td>")
            builder.append("<td style=\"text-align:right\">${segment.aeroplanString}</td>")
            builder.append("<td style=\"text-align:right\">${segment.earningResult?.bonusPercent ?: "???"}</td>")
            builder.append("<td style=\"text-align:right\">${segment.bonusString}</td>")
            builder.append("<td style=\"text-align:right\">${segment.totalString}</td>")
            builder.append("</tr>\n")
        }

        // Total row
        builder.append("<tr>")
        builder.append("<th colspan=\"2\">Total</th>")
        builder.append("<th></th>")
        builder.append("<th style=\"text-align:right\">${itinerary.totalRow.distance ?: "???"}</th>")
        builder.append("<th></th>")
        builder.append("<th></th>")
        builder.append("<th style=\"text-align:right\">${itinerary.totalRow.aqm ?: "???"}</th>")
        builder.append("<th style=\"text-align:right\">${itinerary.totalRow.aqd?.toCurrencyString() ?: "???"}</th>")
        builder.append("<th></th>")
        builder.append("<th style=\"text-align:right\">${itinerary.totalRow.aeroplan ?: "???"}</th>")
        builder.append("<th></th>")
        builder.append("<th style=\"text-align:right\">${itinerary.totalRow.bonus ?: "???"}</th>")
        builder.append("<th style=\"text-align:right\">${itinerary.totalRow.total ?: "???"}</th>")
        builder.append("</tr>\n")

        builder.append("</table>")
        builder.append("</div><br/>\n")

        return builder.toString()
    }

    private fun getHeader(): String {
        val builder = StringBuilder()
        builder.append("<html>\n<head><title>AQD Calculator</title></head>\n<body>\n")
        builder.append("""<div><a href="https://www.flyertalk.com/forum/air-canada-aeroplan/1744575-new-improved-calculator-aqm-aeroplan-miles-aqd.html" target="_blank">FlyerTalk Thread</a></div><br/>""")
        return builder.toString()
    }

    private fun getFooter(): String {
        val builder = StringBuilder()
        builder.append("<div>Download <a href=\"?fetch=countries\">country data</a>, <a href=\"?fetch=airports\">airport data</a>, <a href=\"?fetch=distances\">Aeroplan distance data</a></div>\n")
        builder.append("</body>\n</html>")
        return builder.toString()
    }

    private fun buildForm(): String {
        return """
            <div><form method="get">
            <table border="0">
            <tr><td style="text-align:right"><label for="ticket">Ticket</label></td>
            <td><select name="ticket" id="ticket">
              <option value="014"${if (ticket.startsWith("014")) " selected=\"selected\"" else ""}>Air Canada (014)</option>
              <option value="000"${if (!ticket.startsWith("014")) " selected=\"selected\"" else ""}>Other</option>
            </select></td></tr>
            <tr><td style="text-align:right"><label for="altitude_status">Altitude Status</label></td>
            <td><select name="altitude_status" id="altitude_status">
              <option value=""${if (altitudeStatus !in setOf("25", "35", "50", "75", "100")) " selected=\"selected\"" else ""}>None</option>
              <option value="25"${if (altitudeStatus == "25") " selected=\"selected\"" else ""}>P25K</option>
              <option value="35"${if (altitudeStatus == "35") " selected=\"selected\"" else ""}>E35K</option>
              <option value="50"${if (altitudeStatus == "50") " selected=\"selected\"" else ""}>E50K</option>
              <option value="75"${if (altitudeStatus == "75") " selected=\"selected\"" else ""}>E75K</option>
              <option value="100"${if (altitudeStatus == "100") " selected=\"selected\"" else ""}>SE100K</option>
            </select></td></tr>
            <tr><td style="text-align:right"><label for="bonus_miles_privilege">Bonus Aeroplan Miles Select Privilege</label></td>
            <td><input type="checkbox" name="bonus_miles_privilege" id="bonus_miles_privilege" value="true"${if (hasBonusMilesPrivilege) """ checked="checked"""" else ""}"/></td></tr>
            <tr><td style="text-align:right"><label for="base_fare">Base Fare</label></td>
            <td><input type="number" min="0" step="0.01" name="base_fare" id="base_fare" required value="${baseFare?.toString() ?: "0"}"/></td></tr>
            <tr><td style="text-align:right"><label for="surcharges">AQD-eligible surcharges (YQ/YR)</label></td>
            <td><input type="number" min="0" step="0.01" name="surcharges" id="surcharges" required value="${surcharges?.toString() ?: "0"}"/></td></tr>
            <tr><td colspan="2"><label for="segments">Segments:<br/>Operating airline,Origin,Destination,Fare Class,Brand Code<br/>You can leave off fare brand for non-AC</label></td>
            <tr><td colspan="2"><textarea rows="16" cols="50" name="segments" required>${if (segments.isBlank()) defaultSegments else segments}</textarea></td></tr>
            <tr><td colspan="2"><button type="submit">Calculate AQM/AQD</button></td></tr>
            </table>
            </form></div>
            <div><b>Brand Codes:</b><br/>${FareBrand.generateHtmlList()}</div><br/>
        """.trimIndent()
    }
}

private val currencyFormat = DecimalFormat("#,##0.00")
private fun Double.toCurrencyString() = currencyFormat.format(this)

internal val defaultSegments = """
AC,SFO,YVR,K,CO
AC,YVR,LHR,P,EL
LH,LHR,MUC,P,
TG,MUC,BKK,M
CX,BKK,HKG,C
""".trim()
