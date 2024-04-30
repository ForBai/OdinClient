package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.events.impl.EnteredDungeonRoomEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.puzzlesolvers.WaterSolver.waterInteract
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.profile
import me.odinmain.utils.render.Color
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object PuzzleSolvers : Module(
    name = "Puzzle Solvers",
    category = Category.DUNGEON,
    description = "Dungeon puzzle solvers."
) {
    private val waterSolver: Boolean by BooleanSetting("Water Board", true, description = "Shows you the solution to the water puzzle.")
    val showOrder: Boolean by BooleanSetting("Show Order", true, description = "Shows the order of the levers to click.").withDependency { waterSolver }
    val showTracer: Boolean by BooleanSetting("Show Tracer", true, description = "Shows a tracer to the next lever.").withDependency { waterSolver }
    val tracerColorFirst: Color by ColorSetting("Tracer Color First", Color.GREEN, true, description = "Color for the first tracer").withDependency { showTracer }
    val tracerColorSecond: Color by ColorSetting("Tracer Color Second", Color.ORANGE, true, description = "Color for the second tracer").withDependency { showTracer }
    val reset: () -> Unit by ActionSetting("Reset", description = "Resets the solver.") {
        WaterSolver.reset()
    }.withDependency { waterSolver }

    private val tpMaze: Boolean by BooleanSetting("Teleport Maze", true, description = "Shows you the solution for the TP maze puzzle")
    val solutionThroughWalls: Boolean by BooleanSetting("Solution through walls", false, description = "Renders the final solution through walls").withDependency { tpMaze }
    val mazeColorOne: Color by ColorSetting("Color for one solution", Color.GREEN.withAlpha(.5f), true, description = "Color for when there is a single solution").withDependency { tpMaze }
    val mazeColorMultiple: Color by ColorSetting("Color for multiple solutions", Color.ORANGE.withAlpha(.5f), true, description = "Color for when there are multiple solutions").withDependency { tpMaze }
    val mazeColorVisited: Color by ColorSetting("Color for visited", Color.RED.withAlpha(.5f), true, description = "Color for the already used TP pads").withDependency { tpMaze }

    private val tttSolver: Boolean by BooleanSetting("Tic Tac Toe", true, description = "Shows you the solution for the TTT puzzle")

    private val iceFillSolver: Boolean by BooleanSetting("Ice Fill Solver", true, description = "Solver for the ice fill puzzle")
    private val iceFillColor: Color by ColorSetting("Ice Fill Color", Color.PINK, true, description = "Color for the ice fill solver").withDependency { iceFillSolver }
    val action: () -> Unit by ActionSetting("Reset", description = "Resets the solver.") {
        IceFillSolver.reset()
    }.withDependency { iceFillSolver }

    init {
        execute(500) {
            if (tpMaze) TPMaze.scan()
        }

        onPacket(S08PacketPlayerPosLook::class.java) {
            if (tpMaze) TPMaze.tpPacket(it)
        }

        onPacket(C08PacketPlayerBlockPlacement::class.java) {
            if (waterSolver) waterInteract(it)
        }

        onWorldLoad {
            WaterSolver.reset()
            TPMaze.reset()
            TicTacToe.reset()
            IceFillSolver.reset()
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        profile("Puzzle Solvers") {
            if (waterSolver) WaterSolver.waterRender()
            if (tpMaze) TPMaze.tpRender()
            if (tttSolver) TicTacToe.tttRender()
            if (iceFillSolver) IceFillSolver.onRenderWorldLast(iceFillColor)
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (tttSolver) TicTacToe.tttTick(event)
    }

    @SubscribeEvent
    fun onRoomEnter(event: EnteredDungeonRoomEvent) {
        WaterSolver.scan(event)

        IceFillSolver.enterDungeonRoom(event)
        //BlazeSolver.getRoomType()
    }
}