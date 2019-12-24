package com.github.emulio.ui.screens.keyboard

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.github.czyzby.lml.annotation.LmlActor
import com.github.czyzby.lml.parser.impl.AbstractLmlView
import com.github.czyzby.lml.vis.util.VisLml
import com.github.emulio.Emulio
import com.github.emulio.model.AnyInputConfig
import com.github.emulio.model.InputConfig
import com.github.emulio.model.theme.Theme
import com.github.emulio.ui.input.ScraperView
import com.github.emulio.ui.screens.EmulioDialog
import com.github.emulio.ui.screens.addClickListener
import com.github.emulio.ui.screens.getButtonImagePath
import com.github.emulio.utils.translate
import com.kotcrab.vis.ui.VisUI
import java.util.*

class VirtualKeyboardDialog(title: String,
                            private val dialogMessage: String,
                            emulio: Emulio,
                            stage: Stage,
                            private val confirmCallback: () -> Unit = {}) : EmulioDialog(title, emulio) {

    //val actors: com.badlogic.gdx.utils.Array<Actor>
    val view: VirtualKeyboardView

    init {
        if (!VisUI.isLoaded()) VisUI.load()
        val parser = VisLml.parser().skin(skin).build()

        val template = Gdx.files.internal("templates/VirtualKeyboard.lml")
        view = VirtualKeyboardView(stage)

        parser.createView(view, template).forEach { actor ->
            contentTable.add(actor).expand().fill()
        }



        /*
        contentTable.add(Table().apply {
            add(Label(dialogMessage, emulio.skin)).minHeight(100f).expand().fill()
            row()

            add(Button(emulio.skin).apply {
                val lastConfig = emulio.data["lastInput"] as InputConfig
                val imgSize = 25f
                add(Image(Texture(Gdx.files.internal(getButtonImagePath(lastConfig.name, lastConfig.confirm)), true).apply {
                    setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
                }).apply {
                    setScaling(Scaling.fit)
                    setAlign(Align.center)
                }).height(imgSize).width(imgSize)
                add(Label("Ok".translate(), emulio.skin, "title-small"))

                addClickListener {
                    onConfirmButton(AnyInputConfig)
                }
            })
        }).expand().fill()
         */
    }



    override fun onConfirmButton(input: InputConfig) {
        this.confirmCallback()
        closeDialog()
    }

    override fun onCancelButton(input: InputConfig) {
        this.confirmCallback()
        closeDialog()
    }
}

class VirtualKeyboardView(stage: Stage): AbstractLmlView(stage) {

    override fun getViewId(): String {
        return  "VirtualKeyboardViewId"
    }

    /*
    fun updatePlatformTheme(theme: Theme){
        platformImage.name = theme.platform?.platformName
        platformImage.drawable = theme.getDrawableFromPlatformTheme()
        window.titleLabel.setText(theme.platform?.name)
    }
    */
}