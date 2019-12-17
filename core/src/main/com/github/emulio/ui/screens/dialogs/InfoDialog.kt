package com.github.emulio.ui.screens.dialogs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.github.emulio.Emulio
import com.github.emulio.model.AnyInputConfig
import com.github.emulio.model.InputConfig
import com.github.emulio.ui.screens.EmulioDialog
import com.github.emulio.ui.screens.addClickListener
import com.github.emulio.ui.screens.getButtonImagePath
import com.github.emulio.utils.translate

class InfoDialog(title: String, private val dialogMessage: String, emulio: Emulio,
                 private val confirmCallback: () -> Unit = {}) : EmulioDialog(title, emulio) {

    init {
        initGUI()
    }

    private fun initGUI() {
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