package com.github.emulio.view.input

import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane

class EmlGDXScroll(listView: com.badlogic.gdx.scenes.scene2d.ui.List<*>): EmlScroll {
    private val list = listView
    val scroll = ScrollPane(listView, ScrollPane.ScrollPaneStyle()).apply {
        setFlickScroll(true)
        setScrollBarPositions(false, true)
        setScrollingDisabled(true, false)
        setSmoothScrolling(true)

        isTransform = true
    }

    override val size: Int
        get() = (scroll.height/list.itemHeight).toInt()

    override fun setScrollTop(top: Int){
        scroll.scrollY = top * list.itemHeight
    }
}