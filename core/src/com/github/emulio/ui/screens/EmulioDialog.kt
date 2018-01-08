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
import com.github.emulio.Emulio
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

        inputController = InputManager(this, emulio.config, stage)

        Gdx.input.inputProcessor = inputController
        overlay = Image(createColorTexture(0x000000AA)).apply {
            setFillParent(true)
            color.a = 0f
            addAction(Actions.fadeIn(0.5f))
        }
        stage.addActor(overlay)
        return super.show(stage)
    }

    fun closeDialog() {
        hide()
        remove()
        overlay.actions.forEach { reset() }
        overlay.actions.clear()

        overlay.addAction(SequenceAction(Actions.fadeOut(0.5f), Actions.run { overlay.remove() }))

        inputController.dispose()
        if (oldProcessor is InputManager) {
            (oldProcessor as InputManager).resume()
        }
        Gdx.input.inputProcessor = oldProcessor
    }

    override fun onUpButton(): Boolean {
        return false
    }

    override fun onDownButton(): Boolean {
        return false
    }

    override fun onLeftButton(): Boolean {
        return false
    }

    override fun onRightButton(): Boolean {
        return false
    }

    override fun onFindButton(): Boolean {
        return false
    }

    override fun onOptionsButton(): Boolean {
        return false
    }

    override fun onSelectButton(): Boolean {
        return false
    }

    override fun onPageUpButton(): Boolean {
        return false
    }

    override fun onPageDownButton(): Boolean {
        return false
    }

    override fun onExitButton(): Boolean {
        return false
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
            add(Label(dialogMessage, emulio.skin)).minHeight(100f).expand()
            row()
            add(ImageTextButton("Yes".translate(), emulio.skin, "confirm").apply {
                addClickListener {
                    onConfirmButton()
                }
            }).expandX().right()
            add(ImageTextButton("No".translate(), emulio.skin, "cancel").apply {
                addClickListener {
                    onCancelButton()
                }
            }).expandX().left()
        }).expand().fill()
    }

    override fun onConfirmButton(): Boolean {
        onConfirmDialog()
        closeDialog()
        return false
    }

    override fun onCancelButton(): Boolean {
        onCancelDialog()
        closeDialog()
        return false
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
                    onConfirmButton()
                }
            }).expandX()
        }).expand().fill()
    }

    override fun onConfirmButton(): Boolean {
        closeDialog()
        return false
    }

    override fun onCancelButton(): Boolean {
        closeDialog()
        return false
    }
}

class MainMenuDialog(emulio: Emulio, val stg: Stage) : EmulioDialog("Main Menu".translate().toUpperCase(), emulio, "naked") {

    // we need to cache this font!
    val mainFont = FreeTypeFontGenerator(Gdx.files.internal("fonts/RopaSans-Regular.ttf")).generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
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
                InfoDialog("Not yet implemented", "Not yet implemented", emulio).show(stg)
            },
            "Quit Emulio" to {
                showExitConfirmation(emulio, stg)
            }
    )

    init {
        val title = titleLabel.text
        titleLabel.remove()
        titleTable.reset()

        titleTable.add(Label(title, emulio.skin, "huge").apply {
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


    override fun onDownButton(): Boolean {
        selectNext(1)
        return true
    }

    override fun onUpButton(): Boolean {
        selectNext(-1)
        return true
    }

    override fun onPageDownButton(): Boolean {
        selectNext(5)
        return true
    }

    override fun onPageUpButton(): Boolean {
        selectNext(-5)
        return true
    }


    override fun onConfirmButton(): Boolean {
        performSelectItem()
        return true
    }

    override fun onCancelButton(): Boolean {
        closeDialog()
        return true
    }

}

