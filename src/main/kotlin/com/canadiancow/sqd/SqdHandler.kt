package com.canadiancow.sqd

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler

data class SqdInput @JvmOverloads constructor(
    var baseFare: String = "",
    var surcharges: String = "",
    var segments: String = "",
    var ticket: String = "",
    var aeroplanStatus: String = "",
    var bonusPointsPrivilege: String = "",
    var fetch: String? = ""
)

class SqdHandler : RequestHandler<SqdInput, String> {
    override fun handleRequest(input: SqdInput, context: Context): String {
        return SqdCalculator().calculate()
    }
}
