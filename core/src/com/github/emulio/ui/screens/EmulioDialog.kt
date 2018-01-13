package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.github.emulio.Emulio
import com.github.emulio.model.AnyInputConfig
import com.github.emulio.model.InputConfig
import com.github.emulio.ui.input.InputManager
import com.github.emulio.utils.translate

abstract class EmulioDialog(title: String, open val emulio: Emulio, styleName: String = "default") : Dialog(title, emulio.skin, styleName), com.github.emulio.ui.input.InputListener {

    private lateinit var oldProcessor: InputProcessor
    private lateinit var inputController: InputManager

    private lateinit var overlay: Image

    val screenWidth = Gdx.graphics.width.toFloat()
    val screenHeight = Gdx.graphics.height.toFloat()

    override fun show(stage: Stage): Dialog {
        oldProcessor = Gdx.input.inputProcessor
        if (oldProcessor is InputManager) {
            (oldProcessor as InputManager).pause()
        }

        inputController = InputManager(this, emulio, stage)

        Gdx.input.inputProcessor = inputController
        overlay = Image(createColorTexture(0x000000AA)).apply {
            setFillParent(true)
            color.a = 0f
            addAction(Actions.fadeIn(0.5f))
        }
        stage.addActor(overlay)
        return super.show(stage)
    }

    fun closeDialog(skipAnimation: Boolean = false) {
        hide()
        remove()
        overlay.actions.forEach { reset() }
        overlay.actions.clear()

        if (skipAnimation) {
            overlay.remove()
        } else {
            overlay.addAction(SequenceAction(Actions.fadeOut(0.5f), Actions.run { overlay.remove() }))
        }


        inputController.dispose()
        if (oldProcessor is InputManager) {
            (oldProcessor as InputManager).resume()
        }
        Gdx.input.inputProcessor = oldProcessor
    }

    override fun onUpButton(input: InputConfig) {
    }

    override fun onDownButton(input: InputConfig) {
    }

    override fun onLeftButton(input: InputConfig) {
    }

    override fun onRightButton(input: InputConfig) {
    }

    override fun onFindButton(input: InputConfig) {
    }

    override fun onOptionsButton(input: InputConfig) {
    }

    override fun onSelectButton(input: InputConfig) {
    }

    override fun onPageUpButton(input: InputConfig) {
    }

    override fun onPageDownButton(input: InputConfig) {
    }

    override fun onExitButton(input: InputConfig) {
    }

}

abstract class YesNoDialog(title: String, val dialogMessage: String, emulio: Emulio) : EmulioDialog(title, emulio) {

    init {
        initGUI()
    }

    abstract fun onConfirmDialog()
    abstract fun onCancelDialog()

    private fun initGUI() {
        contentTable.add(Table().apply {
            add(Label(dialogMessage, emulio.skin)).align(Align.center).minHeight(100f).expandX()
            row()
            add(ImageTextButton("Yes".translate(), emulio.skin, "confirm").apply {
                addClickListener {
                    onConfirmButton(AnyInputConfig)
                }
            }).expandX().right()
            add(ImageTextButton("No".translate(), emulio.skin, "cancel").apply {
                addClickListener {
                    onCancelButton(AnyInputConfig)
                }
            }).expandX().left()
        }).expand().fill()
    }

    override fun onConfirmButton(input: InputConfig) {
        onConfirmDialog()
        closeDialog()
    }

    override fun onCancelButton(input: InputConfig) {
        onCancelDialog()
        closeDialog()
    }
}

class InfoDialog(title: String, val dialogMessage: String, emulio: Emulio) : EmulioDialog(title, emulio) {

    init {
        initGUI()
    }

    private fun initGUI() {
        contentTable.add(Table().apply {
            add(Label(dialogMessage, emulio.skin)).minHeight(100f).expand().fill()
            row()
            add(ImageTextButton("Ok".translate(), emulio.skin, "confirm").apply {
                addClickListener {
                    onConfirmButton(AnyInputConfig)
                }
            }).expandX()
        }).expand().fill()
    }

    override fun onConfirmButton(input: InputConfig) {
        closeDialog()
    }

    override fun onCancelButton(input: InputConfig) {
        closeDialog()
    }
}

class MainMenuDialog(emulio: Emulio, val backCallback: () -> EmulioScreen, screen: EmulioScreen, private val stg: Stage = screen.stage) : EmulioDialog("Main Menu".translate(), emulio, "main-menu") {

    // we need to cache this font!
    private val mainFont = FreeTypeFontGenerator(Gdx.files.internal("fonts/RopaSans-Regular.ttf")).generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
        size = 40
        color = Color.WHITE
    })

    private val listView: List<String>
    private val listScrollPane: ScrollPane

    private val menuItems = mapOf(
            "Scraper" to {
                InfoDialog("Not yet implemented", "Not yet implemented", emulio).show(stg)


            },
            "Sound Settings" to {
                InfoDialog("Not yet implemented", "Not yet implemented", emulio).show(stg)
            },
            "UI Settings" to {
                InfoDialog("Not yet implemented", "Not yet implemented", emulio).show(stg)
            },
            "Other settings" to {
                InfoDialog("Not yet implemented", "Not yet implemented", emulio).show(stg)
            },
            "Input settings" to {
                closeDialog(true)
                screen.switchScreen(InputConfigScreen(emulio, backCallback))
            },
            "Quit Emulio" to {
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

