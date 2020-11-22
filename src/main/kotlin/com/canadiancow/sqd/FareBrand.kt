package com.canadiancow.sqd

enum class FareBrand(
    private val description: String
) {
    BA("Basic"),
    TG("Standard"),
    FL("Flex"),
    CO("Comfort"),
    LT("Latitude"),
    PL("Premium Economy (Lowest)"),
    PF("Premium Economy (Flexible)"),
    EL("Business Class (Lowest)"),
    EF("Business Class (Flexible)");

    companion object {
        fun generateHtmlList(): String {
            return values().joinToString("<br/>\n") { "${it.name} (${it.description})" }
        }
    }
}
