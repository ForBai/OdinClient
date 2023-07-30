package me.odinclient.features.impl.qol

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.Utils.containsOneOf
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Reminders : Module(
    "Reminders",
    category = Category.QOL
) {
    private val ultReminder: Boolean by BooleanSetting("Ult Reminder")
    private val dragReminder: Boolean by BooleanSetting("Drag Reminder")
    private val readyReminder: Boolean by BooleanSetting("Ready Reminder")

    private var firstLaser = false
    private var playerReady = false

    private val alertMap = mapOf(
        "[BOSS] Wither King: You.. again?" to "&3Swap to edrag!",
        "[BOSS] Maxor: YOU TRICKED ME!" to "&3Use ult!",
        "[BOSS] Maxor: THAT BEAM! IT HURTS! IT HURTS!!" to "&3Use ult!",
        "[BOSS] Goldor: You have done it, you destroyed the factory…" to "&3Use ult!",
        "[BOSS] Sadan: My giants! Unleashed!" to "&3Use ult!"
        // Add more pairs here as needed
    )

    @SubscribeEvent
    fun onClientChatReceived(event: ClientChatReceivedEvent) {
        if (!DungeonUtils.inDungeons) return
        val msg = event.message.unformattedText.noControlCodes

        if (msg == "${mc.thePlayer.name} is now ready!") {
            playerReady = true
            mc.thePlayer.closeScreen()
            return
        }

        if (msg in alertMap) {
            val alert = alertMap[msg] ?: return

            if (msg.startsWith("[BOSS] Maxor:")) if (!firstLaser) firstLaser = true else return
            if (msg.startsWith("[BOSS] Wither King:") && !dragReminder) return
            if (!ultReminder && msg.containsOneOf("Maxor", "Goldor", "Sadan")) return

            PlayerUtils.alert(alert)
            modMessage(alert)
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        playerReady = false
        firstLaser = false
    }

    init {
        execute(10000, 0) {
            if (!readyReminder || !DungeonUtils.inDungeons) return@execute
            if (playerReady) return@execute

            PlayerUtils.alert("&3Ready up!")
            modMessage("Ready up!")
        }
    }
}