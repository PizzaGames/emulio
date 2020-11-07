package com.github.emulio.ui.input

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.czyzby.lml.parser.impl.AbstractLmlView

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