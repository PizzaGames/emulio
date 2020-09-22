package com.github.emulio.ui.screens.scraper

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.utils.Pools
import com.github.czyzby.lml.parser.impl.AbstractLmlView
import com.github.czyzby.lml.util.Lml
import com.github.emulio.Emulio
import com.github.emulio.model.InputConfig
import com.github.emulio.ui.screens.EmulioDialog
import com.github.emulio.ui.screens.addClickListener
import mu.KotlinLogging
import kotlin.reflect.KClass


val logger = KotlinLogging.logger { }

class EditGameInfoDialog(emulio: Emulio,
                         stage: Stage,
                         private val confirmCallback: (text: String) -> Unit = {}) : EmulioDialog("Game info", emulio) {


    private val view: EditGameInfoView

    private val btCancel: Button
    private val btConfirm: Button
//    private val btSpace: Button
//    private val btShift: Button
//    private val btBackspace: Button
//    private val buttons: List<Button>
//
//    private val textArea: TextArea
//
//    private var mapTraverseButtons: Map<Button, TraverseKey>

    var text: String? = null

    init {
        logger.debug { "initializing virtual keyboard" }
        val parser = Lml.parser().skin(skin).build()

        logger.debug { "reading keyboard lml file" }
        val template = Gdx.files.internal("templates/EditGameInfo.lml")
        view = EditGameInfoView(stage)

        logger.debug { "filling dialog with template read from lml file" }
        parser.createView(view, template).forEach { actor ->
            contentTable.add(actor).expand().fill()
        }


        logger.debug { "mapping buttons and keys" }
        btCancel = contentTable.findActor("cancel")
        btConfirm = contentTable.findActor("confirm")

        /*
        btSpace = contentTable.findActor("space")
        btShift = contentTable.findActor("shift")
        btBackspace = contentTable.findActor("backspace")

        textArea = contentTable.findActor("text")

        buttons = contentTable.findByType(Button::class)

        val buttonsMap = buttons.filterIsInstance(TextButton::class.java).map { it.text.toString().toUpperCase() to it }.toMap()

        fun key(letter: String): Actor {
            return buttonsMap[letter] ?: error("Key not found")
        }

        logger.debug { "creating traverse keys map" }
        mapTraverseButtons = buttons.map { button ->
            button to if (button is TextButton) {
                when (button.text.toString().toUpperCase()) {
                    "1" -> TraverseKey(button, key("0"), key("2"), btConfirm, key("Q"))
                    "2" -> TraverseKey(button, key("1"), key("3"), btConfirm, key("W"))
                    "3" -> TraverseKey(button, key("2"), key("4"), btConfirm, key("E"))
                    "4" -> TraverseKey(button, key("3"), key("5"), btConfirm, key("R"))
                    "5" -> TraverseKey(button, key("4"), key("6"), btConfirm, key("T"))
                    "6" -> TraverseKey(button, key("5"), key("7"), btConfirm, key("Y"))
                    "7" -> TraverseKey(button, key("6"), key("8"), btCancel, key("U"))
                    "8" -> TraverseKey(button, key("7"), key("9"), btCancel, key("I"))
                    "9" -> TraverseKey(button, key("8"), key("0"), btCancel, key("O"))
                    "0" -> TraverseKey(button, key("9"), key("1"), btCancel, key("P"))
                    "Q" -> TraverseKey(button, key("P"), key("W"), key("1"), key("A"))
                    "W" -> TraverseKey(button, key("Q"), key("E"), key("2"), key("S"))
                    "E" -> TraverseKey(button, key("W"), key("R"), key("3"), key("D"))
                    "R" -> TraverseKey(button, key("E"), key("T"), key("4"), key("F"))
                    "T" -> TraverseKey(button, key("R"), key("Y"), key("5"), key("G"))
                    "Y" -> TraverseKey(button, key("T"), key("U"), key("6"), key("H"))
                    "U" -> TraverseKey(button, key("Y"), key("I"), key("7"), key("J"))
                    "I" -> TraverseKey(button, key("U"), key("O"), key("8"), key("K"))
                    "O" -> TraverseKey(button, key("I"), key("P"), key("9"), key("K"))
                    "P" -> TraverseKey(button, key("O"), key("Q"), key("0"), key("L"))
                    "A" -> TraverseKey(button, key("L"), key("S"), key("Q"), key("Z"))
                    "S" -> TraverseKey(button, key("A"), key("D"), key("W"), key("Z"))
                    "D" -> TraverseKey(button, key("S"), key("F"), key("E"), key("X"))
                    "F" -> TraverseKey(button, key("D"), key("G"), key("R"), key("C"))
                    "G" -> TraverseKey(button, key("F"), key("H"), key("T"), key("V"))
                    "H" -> TraverseKey(button, key("G"), key("J"), key("Y"), key("B"))
                    "J" -> TraverseKey(button, key("H"), key("K"), key("U"), key("N"))
                    "K" -> TraverseKey(button, key("J"), key("L"), key("I"), key("M"))
                    "L" -> TraverseKey(button, key("K"), key("A"), key("O"), key("M"))
                    "Z" -> TraverseKey(button, key("M"), key("X"), key("S"), btShift)
                    "X" -> TraverseKey(button, key("Z"), key("C"), key("D"), key("@"))
                    "C" -> TraverseKey(button, key("X"), key("V"), key("F"), btSpace)
                    "V" -> TraverseKey(button, key("C"), key("B"), key("H"), btSpace)
                    "B" -> TraverseKey(button, key("V"), key("N"), key("J"), btSpace)
                    "N" -> TraverseKey(button, key("B"), key("M"), key("K"), key("/"))
                    "M" -> TraverseKey(button, key("N"), key("Z"), key("J"), btBackspace)
                    "@" -> TraverseKey(button, btShift, btSpace, key("Z"), btConfirm)
                    "/" -> TraverseKey(button, btSpace, btBackspace, key("M"), btCancel)
                    else -> when (button) {
                        btSpace -> TraverseKey(btSpace, key("@"), key("/"), key("C"), btConfirm)
                        btConfirm -> TraverseKey(btConfirm, btCancel, btCancel, btSpace, key("1"))
                        btCancel -> TraverseKey(btCancel, btConfirm, btConfirm, btSpace, key("1"))
                        else -> error("Key not found for $button")
                    }
                }
            } else {
                when (button) {
                    btShift -> TraverseKey(btShift, btBackspace, key("@"), key("Z"), btConfirm)
                    btBackspace -> TraverseKey(btBackspace, key("/"), btShift, key("M"), btCancel)
                    else -> error("Key not found for $button")
                }
            }
        }.toMap()

        logger.debug { "adding listeners" }
        for (button in buttons.filterIsInstance(TextButton::class.java).filter { it.name == null }) {
            button.addClickListener {
                val char = button.text[0].let {
                    if (btShift.isChecked) {
                        btShift.isChecked = false
                        it.toUpperCase()
                    } else {
                        it.toLowerCase()
                    }
                }

                textArea.text = textArea.text + char
            }
        }

         */



        /*
        btSpace.addClickListener {
            textArea.text = textArea.text + " "
        }

        btBackspace.addClickListener {
            if (!textArea.text.isBlank()) {
                textArea.text = textArea.text.substring(0, textArea.text.length - 1)
            }
        }

         */

        btConfirm.addClickListener {
            confirmAction()
        }

        btCancel.addClickListener {
            cancelAction()
        }

    }

    private fun cancelAction() {
        logger.debug { "cancelAction" }
        this.text = null
        closeDialog()
    }

    private fun confirmAction() {
//        this.text = textArea.text

        logger.info { "VirtualKeyboard confirmed (text: $text)" }
        this.confirmCallback(this.text ?: "")
        closeDialog()
    }

    enum class Traverse {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    data class TraverseKey(
            val key: Actor,
            val keyLeft: Actor,
            val keyRight: Actor,
            val keyUp: Actor,
            val keyDown: Actor)

    private fun traverseButtons(direction: Traverse) {
        logger.debug { "traverse buttons: $direction" }
        val focused = stage.keyboardFocus

//        if (focused !is Button) {
//            stage.keyboardFocus = buttons.first()
//            return
//        }
//
//        fun findTraverse(button: Button): TraverseKey {
//            return mapTraverseButtons[button] ?: error("Key not found")
//        }

//        when (direction) {
//            Traverse.LEFT -> stage.keyboardFocus = findTraverse(focused).keyLeft
//            Traverse.RIGHT -> stage.keyboardFocus = findTraverse(focused).keyRight
//            Traverse.UP -> stage.keyboardFocus = findTraverse(focused).keyUp
//            Traverse.DOWN -> stage.keyboardFocus = findTraverse(focused).keyDown
//        }

    }

    override fun onConfirmButton(input: InputConfig) {
        val focused = stage.keyboardFocus

        if (focused is Button) {
            fireClick(focused)
        }
    }

    private fun fireKey(actor: Actor, character: Char) {
        val inputEvent = Pools.obtain(InputEvent::class.java).apply {
            reset()
            this.button = 0
            this.relatedActor = actor
            this.character = character
        }
        try {
            inputEvent.type = InputEvent.Type.keyTyped
            actor.fire(inputEvent)
        } finally {
            Pools.free(inputEvent)
        }
    }

    private fun fireClick(button: Button) {
        val inputEvent = Pools.obtain(InputEvent::class.java).apply {
            reset()
            this.button = 0
            this.relatedActor = button
        }
        try {
            inputEvent.type = InputEvent.Type.touchDown
            button.fire(inputEvent)
            inputEvent.type = InputEvent.Type.touchUp
            button.fire(inputEvent)
        } finally {
            Pools.free(inputEvent)
        }
    }

    override fun onCancelButton(input: InputConfig) {
//        if (stage.keyboardFocus != textArea) {
//            cancelAction()
//        }
    }

    override fun onUpButton(input: InputConfig) {
        traverseButtons(Traverse.UP)
    }

    override fun onDownButton(input: InputConfig) {
        traverseButtons(Traverse.DOWN)
    }

    override fun onLeftButton(input: InputConfig) {
        traverseButtons(Traverse.LEFT)
    }

    override fun onRightButton(input: InputConfig) {
        traverseButtons(Traverse.RIGHT)
    }

    override fun onSelectButton(input: InputConfig) {
//        stage.keyboardFocus = textArea
    }

    override fun onExitButton(input: InputConfig) {
        cancelAction()
    }
}



private fun <T : Actor> Group.findByType(kClass: KClass<T>): List<T> {
    val found = ArrayList<T>()

    this.children.forEach { child ->
        if (kClass.java.isInstance(child)) {
            @Suppress("UNCHECKED_CAST")
            found.add(child as T)
        }

        if (child is Group) {
            found.addAll(child.findByType(kClass))
        }
    }

    return found
}

class EditGameInfoView(stage: Stage): AbstractLmlView(stage) {

    override fun getViewId(): String {
        return  "EditGameInfoId"
    }
}