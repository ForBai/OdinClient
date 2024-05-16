package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting


object Waypoints : Module(
    name = "Waypoints",
    category = Category.RENDER,
    description = "Custom Waypoints! /wp gui."
) {
    private val fromParty: Boolean by BooleanSetting("From Party Chat", false, description = "Adds waypoints from party chat.")
    private val fromAll: Boolean by BooleanSetting("From All Chat", false, description = "Adds waypoints from all chat.")
    val onlyBox: Boolean by BooleanSetting("Only shows the box", false, description = "Only shows the box, not the name")
    val onlyDistance: Boolean by BooleanSetting("Only shows the distance as name", false, description = "Only shows the distance as name")

    // https://regex101.com/r/EXc7mQ/1
    init {
        onMessage(Regex("Party > (\\[.+?]) ?(.{1,16}): x: (-?\\d+), y: (-?\\d+), z: (-?\\d+)(.*)"), { fromParty && enabled }) {
            val matchResult = Regex("Party > (\\[.+?]) ?(.{1,16}): x: (-?\\d+), y: (-?\\d+), z: (-?\\d+)(.*)").find(it) ?: return@onMessage
            val (rank, name, x, y, z) = matchResult.destructured
            WaypointManager.addTempWaypoint(getColorFromRank(rank) + name, x.toIntOrNull() ?: return@onMessage, y.toIntOrNull() ?: return@onMessage, z.toIntOrNull() ?: return@onMessage)
        }

        onMessage(Regex("(?:\\[\\d+])? ?(\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)"), { fromAll && enabled }) { // greatest regex of all time!
            val matchResult = Regex("(?:\\[\\d+])? ?(\\[.+])? (.{0,16}): x: (-?\\d+),? y: (-?\\d+),? z: (-?\\d+)").find(it) ?: return@onMessage
            val (rank, name, x, y, z) = matchResult.destructured
            WaypointManager.addTempWaypoint(getColorFromRank(rank) + name, x.toIntOrNull() ?: return@onMessage, y.toIntOrNull() ?: return@onMessage, z.toIntOrNull() ?: return@onMessage)
        }
    }

    private fun getColorFromRank(rank: String): String {
        return when (rank) {
            "[VIP]", "[VIP+]" -> "§a"
            "[MVP]", "[MVP+]" -> "§b"
            "[MVP++]" -> "§6"
            else -> "§7"
        }
    }
}