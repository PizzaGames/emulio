package com.github.emulio.ui.input

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.emulio.ui.screens.createColorTexture

class EmlGDXList<T>(private val list: List<T>,
                    mainFont: BitmapFont?,
                    listWidth: Float,
                    getDescription: (T) -> String) : EmlList {

    val listView = com.badlogic.gdx.scenes.scene2d.ui.List<String>(com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle().apply {
        font = mainFont
        fontColorSelected = Color.WHITE
        fontColorUnselected = Color(0x878787FF.toInt())
        val selectorTexture = createColorTexture(0x878787FF.toInt())
        selection = TextureRegionDrawable(TextureRegion(selectorTexture))

    })

    init {
        this.listView.apply {
            val descriptionsArray = list.map(getDescription).toTypedArray()
            setItems(com.badlogic.gdx.utils.Array(descriptionsArray))

            width = listWidth
            height = 100f
            selectedIndex = 0
        }
    }

    override val size: Int
        get() = list.size

    override var selectedIndex: Int
        get() = listView.selectedIndex
        set(value) {
            listView.selectedIndex = value
        }
}