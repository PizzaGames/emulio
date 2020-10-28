package com.github.emulio.sound

import com.badlogic.gdx.files.FileHandle
import ozmod.OZMod

object MusicManager {
    fun play() {
        val ozm = OZMod()

        val player = ozm.getPlayer(FileHandle(""))
        player.play()
    }
}

fun main(args: Array<String>) {
    MusicManager.play()
}