package com.github.emulio.view.screens.dialogs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.github.emulio.Emulio
import com.github.emulio.model.config.InputConfig
import com.github.emulio.service.i18n.translate
import com.github.emulio.view.screens.EmulioDialog
import com.github.emulio.view.screens.createColorTexture

class JumpToLetterMenuDialog(
        emulio: Emulio,
        private val backCallback: (letter: Char) -> Unit) : EmulioDialog("Letter".translate(), emulio, "main-menu") {

    // we need to cache this font!
    private val mainFont = FreeTypeFontGenerator(Gdx.files.internal("fonts/RopaSans-Regular.ttf")).generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
        size = 40
        color = Color.WHITE
    })

    private val listView: List<String>
    private val listScrollPane: ScrollPane

    private val menuItems = mapOf(
        "A" to { closeDialog(true); backCallback('A') },
        "B" to { closeDialog(true); backCallback('B') },
        "C" to { closeDialog(true); backCallback('C') },
        "D" to { closeDialog(true); backCallback('D') },
        "E" to { closeDialog(true); backCallback('E') },
        "F" to { closeDialog(true); backCallback('F') },
        "G" to { closeDialog(true); backCallback('G') },
        "H" to { closeDialog(true); backCallback('H') },
        "I" to { closeDialog(true); backCallback('I') },
        "J" to { closeDialog(true); backCallback('J') },
        "K" to { closeDialog(true); backCallback('K') },
        "L" to { closeDialog(true); backCallback('L') },
        "M" to { closeDialog(true); backCallback('M') },
        "N" to { closeDialog(true); backCallback('N') },
        "O" to { closeDialog(true); backCallback('O') },
        "P" to { closeDialog(true); backCallback('P') },
        "Q" to { closeDialog(true); backCallback('Q') },
        "R" to { closeDialog(true); backCallback('R') },
        "S" to { closeDialog(true); backCallback('S') },
        "T" to { closeDialog(true); backCallback('T') },
        "U" to { closeDialog(true); backCallback('U') },
        "V" to { closeDialog(true); backCallback('V') },
        "W" to { closeDialog(true); backCallback('W') },
        "X" to { closeDialog(true); backCallback('X') },
        "Y" to { closeDialog(true); backCallback('Y') },
        "Z" to { closeDialog(true); backCallback('Z') },
        "#" to { closeDialog(true); backCallback('#') }
    )

    init {
        val title = titleLabel.text
        titleLabel.remove()
        titleTable.reset()

        titleTable.add(Label(title, emulio.skin, "title").apply {
            color.a = 0.8f
        })

        listView = List<String>(List.ListStyle().apply {
            font = mainFont
            fontColorSelected = Color.WHITE
            fontColorUnselected = Color(0x878787FF.toInt())
            val selectorTexture = createColorTexture(0x878787FF.toInt())
            selection = TextureRegionDrawable(TextureRegion(selectorTexture))


        }).apply {

            setAlignment(Align.center)

            menuItems.keys.forEach {
                items.add(it)
            }

            width = 50f
            height = 100f

            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    performClick()
                }
            })

            selectedIndex = 0
        }

        contentTable.reset()
        listScrollPane = ScrollPane(listView, ScrollPane.ScrollPaneStyle()).apply {

            setFlickScroll(true)
            setScrollBarPositions(false, true)

            setScrollingDisabled(true, false)
            setSmoothScrolling(true)

            isTransform = true

            setSize(50f, screenHeight / 2)
        }
        contentTable.add(listScrollPane).fillX().expandX().maxHeight(screenHeight / 2).minWidth(50f)


    }

    private var lastSelected: Int = -1

    private fun performClick() {
        if (lastSelected == listView.selectedIndex) {
            performSelectItem()
        }

        lastSelected = listView.selectedIndex
    }

    private fun performSelectItem() {
        closeDialog()
        (menuItems[listView.selected] ?: error("")).invoke()
    }

    private fun selectNext(amount: Int = 1) {
        val nextIndex = listView.selectedIndex + amount

        if (amount < 0) {
            if (nextIndex < 0) {
                listView.selectedIndex = listView.items.size + amount
            } else {
                listView.selectedIndex = nextIndex
            }
        }

        if (amount > 0) {
            if (nextIndex >= listView.items.size) {
                listView.selectedIndex = 0
            } else {
                listView.selectedIndex = nextIndex
            }
        }

        lastSelected = listView.selectedIndex

        checkVisible(nextIndex)
    }

    private fun checkVisible(index: Int) {
        val itemHeight = listView.itemHeight

        val selectionY = index * itemHeight
        val selectionY2 = selectionY + itemHeight

        val minItemsVisible = itemHeight * 5

        val itemsPerView = listScrollPane.height / itemHeight


        if (listView.selectedIndex > (menuItems.size - itemsPerView)) {
            listScrollPane.scrollY = listView.height - listScrollPane.height
            return
        }

        if (listView.selectedIndex == 0) {
            listScrollPane.scrollY = 0f
            return
        }

        if ((selectionY2 + minItemsVisible) > listScrollPane.height) {
            listScrollPane.scrollY = (selectionY2 - listScrollPane.height) + minItemsVisible
        }

        val minScrollY = Math.max(selectionY - minItemsVisible, 0f)

        if (minScrollY < listScrollPane.scrollY) {
            listScrollPane.scrollY = minScrollY
        }
    }


    override fun onDownButton(input: InputConfig) {
        selectNext(1)
    }

    override fun onUpButton(input: InputConfig) {
        selectNext(-1)
    }

    override fun onPageDownButton(input: InputConfig) {
        selectNext(5)
    }

    override fun onPageUpButton(input: InputConfig) {
        selectNext(-5)
    }


    override fun onConfirmButton(input: InputConfig) {
        performSelectItem()
    }

    override fun onCancelButton(input: InputConfig) {
        closeDialog()
    }

}