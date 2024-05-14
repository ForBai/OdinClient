package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module

/**
 * @see me.odinclient.mixin.mixins.MixinMouseHelper
 */
object LockCursor: Module(
    name = "Lock Cursor",
    description = "Makes you completely unable to move your camera.",
    category = Category.RENDER
)