package com.github.emulio.ui.screens.dialogs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.emulio.Emulio
import com.github.emulio.model.InputConfig
import com.github.emulio.ui.screens.*
import com.github.emulio.ui.screens.wizard.PlatformWizardScreen
import com.github.emulio.ui.screens.wizard.ScraperWizardScreen
import com.github.emulio.utils.translate

class MainMenuDialog(emulio: Emulio, private val backCallback: () -> EmulioScreen, screen: EmulioScreen, private val stg: Stage = screen.stage) : EmulioDialog("Main Menu".translate(), emulio, "main-menu") {

    // we need to cache this font!
    private val mainFont = FreeTypeFontGenerator(Gdx.files.internal("fonts/RopaSans-Regular.ttf")).generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
        size = 40
        color = Color.WHITE
    })

    private val listView: List<String>
    private val listScrollPane: ScrollPane

    private val menuItems = mapOf(
            "Scraper".translate() to {
                closeDialog(true)
                screen.switchScreen(ScraperWizardScreen(emulio, backCallback))
            },
            "General Settings".translate() to {
                InfoDialog("Not yet implemented", "Not yet implemented", emulio).show(stg)
            },
            "Input settings".translate() to {
                closeDialog(true)
                screen.switchScreen(InputConfigScreen(emulio, backCallback))
            },
            "Platform Config".translate() to {
                YesNoDialog("Platform Config".translate(), """
                        The platforms can be edited/configured using a Wizard or editing manually.  
                    """.trimIndent().translate(), emulio,
                    "Proceed to Wizard".translate(),
                    "Edit Manually".translate(),
                    cancelCallback = {
                        screen.launchPlatformConfigEditor()
                        screen.showReloadConfirmation()
                    },
                    confirmCallback = {
                        screen.switchScreen(PlatformWizardScreen(emulio, backCallback))
                    }).show(stg)
            },
            "Restart Emulio".translate() to {
                screen.showReloadConfirmation()
            },
            "Quit Emulio".translate() to {
                showExitConfirmation(emulio, stg)
            }
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
            menuItems.keys.forEach { items.add(it) }

            width = screenWidth / 2
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

            setSize(screenWidth / 2, screenHeight / 2)
        }
        contentTable.add(listScrollPane).fillX().expandX().maxHeight(screenHeight / 2).minWidth(screenWidth / 2)


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
        menuItems[listView.selected]!!.invoke()
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