package com.github.emulio.ui.input

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.github.czyzby.lml.parser.impl.AbstractLmlView

class ScraperWindow(stage: Stage, skin: Skin) {

    /*
    val actors: com.badlogic.gdx.utils.Array<Actor>
    val view: ScraperView

    init {

        //if (!VisUI.isLoaded()) VisUI.load()
        val parser = Lml.parser().skin(skin).build()

        val template = Gdx.files.internal("templates/ScraperWindow.lml")
        view = ScraperView(stage)

        actors = parser.createView(view, template)
    }

     */
}

class ScraperView(stage: Stage): AbstractLmlView(stage) {
    override fun getViewId(): String {
        return  "scraperViewId"
    }

    /*
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
     */
}