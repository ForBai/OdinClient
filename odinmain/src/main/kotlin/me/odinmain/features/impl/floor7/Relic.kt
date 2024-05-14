package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.config.Config
import me.odinmain.features.impl.floor7.WitherDragons.colors
import me.odinmain.features.impl.floor7.WitherDragons.relicAnnounceTime
import me.odinmain.features.impl.floor7.WitherDragons.selected
import me.odinmain.features.impl.kuudra.KuudraSplits.unaryPlus
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.round
import me.odinmain.utils.skyblock.*
import net.minecraft.event.ClickEvent
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

object Relic {
    private val greenPB = +NumberSetting("Green PB", 999.0, increment = 0.001, hidden = true)
    private val purplePB = +NumberSetting("Purple PB", 999.0, increment = 0.001, hidden = true)
    private val bluePB = +NumberSetting("Blue PB", 999.0, increment = 0.001, hidden = true)
    private val orangePB = +NumberSetting("Orange PB", 999.0, increment = 0.001, hidden = true)
    private val redPB = +NumberSetting("Red PB", 999.0, increment = 0.001, hidden = true)

    val currentRelic get() = mc.thePlayer?.heldItem?.itemID ?: ""


    enum class Relics (
        val id: String,
        val pbTime: NumberSetting<Double>,
        val colorCode: String
    ) {
        Green("GREEN_KING_RELIC", greenPB, "§a"),
        Purple("PURPLE_KING_RELIC", purplePB, "§5"),
        Blue("BLUE_KING_RELIC", bluePB, "§b"),
        Orange("ORANGE_KING_RELIC", orangePB, "§6"),
        Red("RED_KING_RELIC", redPB, "§c")
    }

    private var timer = 0L

    fun relicsOnMessage(){
        if (WitherDragons.relicAnnounce) partyMessage("${colors[selected]} Relic")
        timer = System.currentTimeMillis()
    }

    fun relicsBlockPlace(packet: C08PacketPlayerBlockPlacement) {
        if (timer == 0L) return
        val block = mc.theWorld?.getBlockState(packet.position)?.block ?: return
        if (!block.equalsOneOf(Blocks.cauldron, Blocks.anvil) || !currentRelic.equalsOneOf("GREEN_KING_RELIC", "PURPLE_KING_RELIC", "BLUE_KING_RELIC", "ORANGE_KING_RELIC", "RED_KING_RELIC")) return
        val relic = Relics.entries.find { it.id == currentRelic } ?: return modMessage("Relic not found")
        val hasPassed = (System.currentTimeMillis() - timer) / 1000.0

        if (relicAnnounceTime) modMessage("${relic.colorCode}${relic.name}§f took ${hasPassed}s ${if (hasPassed < relic.pbTime.value) "(§dNew PB)" else ""}",
            chatStyle = createClickStyle(ClickEvent.Action.SUGGEST_COMMAND, Relics.entries.joinToString { "${it.colorCode}${it.name}§f ${it.pbTime.value.round(3)}" }))

        if (hasPassed < relic.pbTime.value) relic.pbTime.value = hasPassed
        timer = 0L
        Config.save()
    }
}