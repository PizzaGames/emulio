package com.github.emulio.ui.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.github.czyzby.lml.annotation.LmlActor
import com.github.czyzby.lml.parser.impl.AbstractLmlView
import com.github.czyzby.lml.util.Lml
import com.github.emulio.model.theme.Theme

class ScraperWindow(stage: Stage, skin: Skin) {

    val actors: com.badlogic.gdx.utils.Array<Actor>
    val view: ScraperView

    init {

        //if (!VisUI.isLoaded()) VisUI.load()
        val parser = Lml.parser().skin(skin).build()

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

    fun updatePlatformTheme(theme: Theme){
        platformImage.name = theme.platform?.platformName
        platformImage.drawable = theme.getDrawableFromPlatformTheme()
        window.titleLabel.setText(theme.platform?.name)
    }
}