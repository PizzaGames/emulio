package com.github.emulio.ui.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.github.czyzby.lml.vis.util.VisLml
import com.kotcrab.vis.ui.VisUI

class ScraperWindow(skin: Skin) {

    val actors: com.badlogic.gdx.utils.Array<Actor>

    init {
        VisUI.load()
        val parser = VisLml.parser().skin(skin).build()

        actors = parser.parseTemplate(Gdx.files.internal("templates/ScraperWindow.lml"))
    }
}