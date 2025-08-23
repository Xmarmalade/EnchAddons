package net.skymoe.enchaddons.impl.feature.awesomemap.features.dungeon

import net.minecraft.entity.Entity
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.item.ItemSkull
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos
import net.skymoe.enchaddons.EA
import net.skymoe.enchaddons.feature.awesomemap.AwesomeMapEvent
import net.skymoe.enchaddons.impl.feature.awesomemap.features.dungeon.ScanUtils.getRoomFromPos
import net.skymoe.enchaddons.util.LogLevel
import net.skymoe.enchaddons.util.MC
import net.skymoe.enchaddons.util.modMessage
import java.util.*

const val MIMIC_SKULL_ID = "ae55953f-605e-3c71-a813-310c028de150"

object MimicDetector {
    var mimicOpenTime = 0L
    var mimicPos: BlockPos? = null

    fun setMimicKilled() {
        RunInformation.mimicKilled = true
        AwesomeMapEvent
            .MimicKilled(RunInformation.timeElapsed)
            .also(EA.eventDispatcher)
    }

    fun setPrinceKilled() {
        RunInformation.princeKilled = true
        AwesomeMapEvent
            .PrinceKilled(RunInformation.timeElapsed)
            .also(EA.eventDispatcher)
    }

    fun isMimic(entity: Entity): Boolean {
        if (entity !is EntityZombie || !entity.isChild) return false

        val isInstantKill =
            entity.getCurrentArmor(3)?.let { item ->
                if (item.item !is ItemSkull || !item.hasTagCompound()) {
                    return@let false
                }
                item.tagCompound?.run {
                    if (!hasKey("SkullOwner")) {
                        return@run false
                    }
                    val skull = getCompoundTag("SkullOwner")
                    if (!skull.hasKey("Id")) {
                        return@run false
                    }
                    val uuid = skull.getString("Id")
                    uuid == MIMIC_SKULL_ID
                }
            } ?: false

        val isNormalKill =
            (0..3).all { i ->
                entity.getCurrentArmor(i) === null
            }

        if (isInstantKill || isNormalKill) {
            modMessage("Mimic was killed ${if (isInstantKill) "INSTANTLY" else ""}.", LogLevel.INFO)
        }
        return isInstantKill || isNormalKill
    }

    fun findMimic(): String? {
        MC.theWorld.loadedTileEntityList
            .filter { it is TileEntityChest && it.chestType == 1 }
            .groupingBy { getRoomFromPos(it.pos)?.data?.name }
            .eachCount()
            .forEach { (room, trappedChests) ->
                Dungeon.Info.uniqueRooms
                    .find { it.name == room && it.mainRoom.data.trappedChests < trappedChests }
                    ?.let {
                        it.hasMimic = true
                        MapRenderList.renderUpdated = true
                        return it.name
                    }
            }
        return null
    }
}
