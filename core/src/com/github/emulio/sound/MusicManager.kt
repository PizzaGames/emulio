package com.github.emulio.sound

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import ozmod.OZMod

object MusicManager {

    init {

    }

    fun play() {
        val ozm = OZMod()

        val player = ozm.getPlayer(FileHandle("C:\\Users\\vntmafr\\workspace\\emulio\\core\\assets\\music\\music-rm.mod"))
        player.play()


    }


}

fun main(args: Array<String>) {
    MusicManager.play()
}