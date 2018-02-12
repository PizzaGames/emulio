package com.github.emulio.ui.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.github.czyzby.lml.annotation.LmlActor
import com.github.czyzby.lml.parser.impl.AbstractLmlView
import com.github.czyzby.lml.vis.util.VisLml
import com.github.emulio.model.Platform
import com.kotcrab.vis.ui.VisUI

class ScraperWindow(stage: Stage, skin: Skin) {

    val actors: com.badlogic.gdx.utils.Array<Actor>
    val view: ScraperView

    init {
        VisUI.load()
        val parser = VisLml.parser().skin(skin).build()

        val template = Gdx.files.internal("templates/ScraperWindow.lml")
        view = ScraperView(stage)

        actors = parser.createView(view, template)
    }
}

class ScraperView(stage: Stage): AbstractLmlView(stage) {
    @LmlActor("scraperPlatformImage") lateinit var platformImage: Image
    @LmlActor("scrapperWindow") lateinit var window: Window

    override fun getViewId(): String {
        return  "scraperViewId"
    }

    fun setPlatform(platform: Platform){
        window.titleLabel.setText(platform.name)
        platformImage.name = platform.platformName
    }
}