package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.emulio.Emulio
import com.github.emulio.model.AnyInputConfig
import com.github.emulio.model.InputConfig
import com.github.emulio.model.Playstation
import com.github.emulio.model.Xbox
import com.github.emulio.process.ProcessException
import com.github.emulio.process.ProcessLauncher
import com.github.emulio.ui.screens.dialogs.InfoDialog
import com.github.emulio.ui.screens.dialogs.MainMenuDialog
import com.github.emulio.ui.screens.dialogs.OptionsMenuDialog
import com.github.emulio.ui.screens.dialogs.YesNoDialog
import com.github.emulio.utils.translate
import mu.KotlinLogging
import java.io.File
import java.math.BigInteger
import kotlin.system.exitProcess


abstract class EmulioScreen(open val emulio: Emulio) : Screen {

    private val logger = KotlinLogging.logger { }

	val stage: Stage = Stage()

	val screenWidth = Gdx.graphics.width.toFloat()
	val screenHeight = Gdx.graphics.height.toFloat()

	private val freeFontGeneratorCache = mutableMapOf<FileHandle, FreeTypeFontGenerator>()
	private val fontCache = mutableMapOf<Triple<FileHandle, Int, Color?>, BitmapFont>()

	val freeTypeFontGenerator = getFreeTypeFontGenerator(Gdx.files.internal("fonts/RopaSans-Regular.ttf"))

    var fadeAnimation = true

	fun getColor(rgba: String?): Color = when {
//        // TODO: Color are resources? we need to cache they like in SWT/Swing lib?
        rgba == null -> Color.BLACK
        rgba.length == 6 -> Color(BigInteger(rgba.toUpperCase() + "FF", 16).toInt())
        rgba.length == 8 -> Color(BigInteger(rgba.toUpperCase(), 16).toInt())
        else -> Color.BLACK
    }

    fun buildText(text: String, txtFont: BitmapFont, x: Float, y: Float): Label {
        return Label(text, Label.LabelStyle().apply {
            font = txtFont
        }).apply {
            setPosition(x, y)
            color = Color.WHITE
        }
    }

    open fun buildImage(imgPath: String, imgWidth: Float, imgHeight: Float, x: Float, y: Float): Image {
        val imgButtonStart = Image(Texture(Gdx.files.internal(imgPath), true).apply {
            setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
        })
        imgButtonStart.setSize(imgWidth, imgHeight)
        imgButtonStart.x = x
        imgButtonStart.y = y
        return imgButtonStart
    }

	fun getFont(fileHandle: FileHandle, fontSize: Int, fontColor: Color? = null): BitmapFont {
		val triple = Triple(fileHandle, fontSize, fontColor)

        return if (fontCache.containsKey(triple)) {
            fontCache[triple]!!
        } else {
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = fontSize

                if (fontColor != null) {
                    color = fontColor
                    borderWidth = 0.4f
                    borderColor = fontColor
                }

                shadowColor = Color(0.2f, 0.2f, 0.2f, 0.2f)
                shadowOffsetX = 1
                shadowOffsetY = 1
            }

            getFreeTypeFontGenerator(fileHandle).generateFont(parameter).apply {
                fontCache[triple] = this
            }
        }

	}

	private fun getFreeTypeFontGenerator(fileHandle: FileHandle): FreeTypeFontGenerator {
        return if (freeFontGeneratorCache.containsKey(fileHandle)) {
            freeFontGeneratorCache[fileHandle]!!
        } else {
            FreeTypeFontGenerator(fileHandle).apply { freeFontGeneratorCache[fileHandle] = this  }
        }
	}

	override fun show() {
        logger.debug { "show" }
        stage.root.actions.forEach { it.reset() }

        if (fadeAnimation) {
            stage.root.color.a = 0f

            stage.root.addAction(SequenceAction(
                Actions.fadeIn(0.5f),
                Actions.run {
                    onScreenLoad()
                }
            ))
        } else {
            onScreenLoad()
        }
	}

    open fun onScreenLoad() {

    }

    abstract fun release()


	override fun dispose() {
        logger.trace { "dispose: ${javaClass.name} " }
		stage.dispose()
	}

    public fun restart() {
        switchScreen(DevSplashScreen(emulio))
    }

	fun switchScreen(newScreen: Screen) {
        logger.info { "Changing screen to: ${newScreen.javaClass.name}" }
		stage.root.color.a = 1f

        release()
        dispose()
        emulio.screen = newScreen
	}

    fun showCloseDialog() {
        showExitConfirmation(emulio, stage)
    }

    fun showInfoDialog(message: String) {
        InfoDialog("Info", message, emulio).show(stage)
    }

    fun showErrorDialog(message: String) {
        InfoDialog("Error", message, emulio).show(stage)
    }

    fun showMainMenu(screenCreatorOnBack: () -> EmulioScreen) {
        MainMenuDialog(emulio, screenCreatorOnBack, this).show(stage)
    }

    fun showOptionsMenu(screenCreatorOnBack: () -> EmulioScreen) {
        OptionsMenuDialog(emulio, screenCreatorOnBack, this).show(stage)
    }

    fun launchPlatformConfigEditor() {
        val editor = try {
            ProcessLauncher.executeProcess(listOf("cmd", "/c", "code", "-v").toTypedArray())
            "code"
        } catch (e: ProcessException) {
            // FIXME: if linux, use another command
            "notepad"
        }

        val platformsFile = emulio.options.platformsFile
        if (!platformsFile.exists()) {
            val templateFile = File(platformsFile.parent, platformsFile.nameWithoutExtension + "-template.yaml")
            if (templateFile.exists()) {
                templateFile.copyTo(platformsFile, true)
            } else {
                platformsFile.createNewFile()
            }
        }

        ProcessLauncher.executeProcess(listOf("cmd", "/c", editor, platformsFile.absolutePath).toTypedArray())
    }

    fun showReloadConfirmation() {
        YesNoDialog("Restart?".translate(), """
			Do you want to restart and reload configurations? (Yes: reload, No: quits)
			""".trimIndent().translate(), emulio,
                cancelCallback = {
                    logger.info { "Exiting Emulio." }
                    exitProcess(0)
                },
                confirmCallback = {
                    logger.info { "Reloading Emulio." }
                    restart()
                }).show(this.stage)
    }

    private var helpItems: HelpItems? = null

    fun initHelpHuds(initialY: Float, height: Float, newHelpItems: HelpItems, initialInputConfig: InputConfig = emulio.data["lastInput"] as InputConfig) {
        val calculatedHeight = height * 0.55f
        
        logger.debug { "initHelpHuds: ${initialInputConfig.name}" }

        val helpFont = freeTypeFontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = calculatedHeight.toInt()
            color = Color.WHITE
            color.a = 1f
        })

        // Calculate the size according resolution???
        val imgWidth = calculatedHeight
        val imgHeight = calculatedHeight
        val padding = 5f

        val lineHeight = helpFont.lineHeight

        val y = initialY - 2f + ((height - lineHeight) / 2)
        val imageY = (initialY - 2f + ((height - imgHeight) / 2)) + 2f

        var x = 10f


        newHelpItems.apply {

            val action = initialInputConfig.confirm
            if (txtConfirm != null) {
                buildHud(action, txtConfirm, initialInputConfig, imgWidth, imgHeight, x, imageY, padding, helpFont, y).apply {
                    imgConfirmButton = first
                    txtConfirmButton = second
                    x = third
                }
            }

            if (txtCancel != null) {
                buildHud(initialInputConfig.cancel, txtCancel, initialInputConfig, imgWidth, imgHeight, x, imageY, padding, helpFont, y).apply {
                    imgCancelButton = first
                    txtCancelButton = second
                    x = third
                }
            }

            if (txtFind != null) {
                buildHud(initialInputConfig.find, txtFind, initialInputConfig, imgWidth, imgHeight, x, imageY, padding, helpFont, y).apply {
                    imgFindButton = first
                    txtFindButton = second
                    x = third
                }
            }

            if (txtOptions != null) {
                buildHud(initialInputConfig.options, txtOptions, initialInputConfig, imgWidth, imgHeight, x, imageY, padding, helpFont, y).apply {
                    imgOptionsButton = first
                    txtOptionsButton = second
                    x = third
                }
            }

            if (txtSelect != null) {
                buildHud(initialInputConfig.select, txtSelect, initialInputConfig, imgWidth, imgHeight, x, imageY, padding, helpFont, y).apply {
                    imgSelectButton = first
                    txtSelectButton = second
                    x = third
                }
            }

            if (txtPageUp != null) {
                buildHud(initialInputConfig.pageUp, txtPageUp, initialInputConfig, imgWidth, imgHeight, x, imageY, padding, helpFont, y).apply {
                    imgPageUpButton = first
                    txtPageUpButton = second
                    x = third
                }
            }

            if (txtPageDown != null) {
                buildHud(initialInputConfig.pageDown, txtPageDown, initialInputConfig, imgWidth, imgHeight, x, imageY, padding, helpFont, y).apply {
                    imgPageDownButton = first
                    txtPageDownButton = second
                    x = third
                }
            }

            if (txtUpDown != null) {
                buildHud(HELP_HUD_UPDOWN, txtUpDown, initialInputConfig, imgWidth, imgHeight, x, imageY, padding, helpFont, y).apply {
                    imgUpDownButton = first
                    txtUpDownButton = second
                    x = third
                }
            }

            if (txtLeftRight != null) {
                buildHud(HELP_HUD_LEFTRIGHT, txtLeftRight, initialInputConfig, imgWidth, imgHeight, x, imageY, padding, helpFont, y).apply {
                    imgLeftRightButton = first
                    txtLeftRightButton = second
                    x = third
                }
            }

            if (txtAllDirection != null) {
                buildHud(HELP_HUD_ALL, txtAllDirection, initialInputConfig, imgWidth, imgHeight, x, imageY, padding, helpFont, y).apply {
                    imgAllDirectionButtons = first
                    txtAllDirectionButtons = second
                    x = third
                }
            }

            lastInputLoaded = initialInputConfig

        }

        this.helpItems = newHelpItems
    }

    private fun HelpItems.buildHud(action: Int, text: String, config: InputConfig, imgWidth: Float, imgHeight: Float, x: Float, imageY: Float, padding: Float, helpFont: BitmapFont, y: Float): Triple<Image, Label, Float> {
        var x1 = x
        val imgPath = getButtonImagePath(config.name, action)

        val imgConf = buildImage(imgPath, imgWidth, imgHeight, x1, imageY)
        imgConf.color.a = alpha
        stage.addActor(imgConf)

        x1 += imgWidth + padding


        val txtConf = buildText(text, helpFont, x1, y)
        txtConf.color = txtColor
        txtConf.color.a = alpha

        stage.addActor(txtConf)

        x1 += txtConf.width + (padding * 3)
        return Triple(imgConf, txtConf, x1)
    }

    fun updateHelp(textColor: Color?, outerAlpha: Float) {
        val helpHuds = this.helpItems ?: return

        helpHuds.apply {
            alpha = outerAlpha

            if (txtConfirm != null) {
                updateHelpItem(txtConfirmButton, textColor, imgConfirmButton)
            }
            if (txtCancel != null) {
                updateHelpItem(txtCancelButton, textColor, imgCancelButton)
            }
            if (txtUpDown != null) {
                updateHelpItem(txtUpDownButton, textColor, imgUpDownButton)
            }
            if (txtLeftRight != null) {
                updateHelpItem(txtLeftRightButton, textColor, imgLeftRightButton)
            }
            if (txtAllDirection != null) {
                updateHelpItem(txtAllDirectionButtons, textColor, imgAllDirectionButtons)
            }
            if (txtFind != null) {
                updateHelpItem(txtFindButton, textColor, imgFindButton)
            }
            if (txtOptions != null) {
                updateHelpItem(txtOptionsButton, textColor, imgOptionsButton)
            }
            if (txtSelect != null) {
                updateHelpItem(txtSelectButton, textColor, imgSelectButton)
            }
            if (txtPageUp != null) {
                updateHelpItem(txtPageUpButton, textColor, imgPageUpButton)
            }
            if (txtPageDown != null) {
                updateHelpItem(txtPageDownButton, textColor, imgPageDownButton)
            }

        }
    }

    private fun HelpItems.updateHelpItem(txt: Label?, textColor: Color?, img: Image?) {
        txt?.apply {
            if (textColor != null) {
                color = textColor
            }
            color.a = alpha
        }
        img?.apply {
            color.a = alpha
        }
    }

    fun updateHelp(config: InputConfig = emulio.data["lastInput"] as InputConfig) {
        val help = this.helpItems ?: return

        if (config == AnyInputConfig) {
            return
        }

        if (config.name == help.lastInputLoaded!!.name) {
            return
        }

        help.apply {

            lastInputLoaded = config
            val animationTime = 0.1f

            if (txtConfirm != null) {
                updateHelpItem(config, config.confirm, imgConfirmButton, animationTime)
            }
            if (txtCancel != null) {
                updateHelpItem(config, config.cancel, imgCancelButton, animationTime)
            }
            if (txtUpDown != null) {
                updateHelpItem(config, HELP_HUD_UPDOWN, imgUpDownButton, animationTime)
            }
            if (txtLeftRight != null) {
                updateHelpItem(config, HELP_HUD_LEFTRIGHT, imgLeftRightButton, animationTime)
            }
            if (txtAllDirection != null) {
                updateHelpItem(config, HELP_HUD_ALL, imgAllDirectionButtons, animationTime)
            }
            if (txtFind != null) {
                updateHelpItem(config, config.find, imgFindButton, animationTime)
            }
            if (txtOptions != null) {
                updateHelpItem(config, config.options, imgOptionsButton, animationTime)
            }
            if (txtSelect != null) {
                updateHelpItem(config, config.select, imgSelectButton, animationTime)
            }
            if (txtPageUp != null) {
                updateHelpItem(config, config.pageUp, imgPageUpButton, animationTime)
            }
            if (txtPageDown != null) {
                updateHelpItem(config, config.pageDown, imgPageDownButton, animationTime)
            }
        }
    }

    private fun HelpItems.updateHelpItem(config: InputConfig, action: Int, img: Image?, animationTime: Float) {
        val texture = Texture(Gdx.files.internal(getButtonImagePath(config.name, action)), true)
        texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
        img?.apply {
            addAction(SequenceAction(Actions.fadeOut(animationTime),
                    Actions.run {
                        drawable = TextureRegionDrawable(TextureRegion(texture))
                    },
                    Actions.alpha(alpha, animationTime)
            ))

        }
    }

}

fun getButtonImagePath(controllerName: String, button: Int): String {
    return when {
        isXboxController(controllerName) -> getXboxImagePath(button)
        isPlaystationController(controllerName) -> getPlaystationImagePath(button)
        isKeyboard(controllerName) -> getKeyboardImagePath(button)
        else -> getGenericImagePath(button)
    }

}

fun isKeyboard(controllerName: String): Boolean {
    return controllerName.toLowerCase().contains("keyboard")
}

fun isXboxController(controllerName: String): Boolean {
    return Xbox.isXboxController(controllerName)
}

fun isPlaystationController(controllerName: String): Boolean {
    return Playstation.isPlaystationController(controllerName)
}

fun getXboxImagePath(button: Int): String {
    return when (button) {
        Xbox.A -> "images/help/xbox/360_A.png"
        Xbox.B -> "images/help/xbox/360_B.png"
        Xbox.X -> "images/help/xbox/360_X.png"
        Xbox.Y -> "images/help/xbox/360_Y.png"
        Xbox.BACK -> "images/help/xbox/360_Back.png"
        Xbox.START -> "images/help/xbox/360_Start.png"
        Xbox.L_BUMPER -> "images/help/xbox/360_LB.png"
        Xbox.R_BUMPER -> "images/help/xbox/360_RB.png"
        HELP_HUD_ALL -> "images/help/xbox/360_Dpad.png"
        HELP_HUD_UPDOWN -> "images/help/xbox/360_Dpad_UpDown.png"
        HELP_HUD_LEFTRIGHT -> "images/help/xbox/360_Dpad_LeftRight.png"
        else -> "images/help/generic/unknown.png"
    }
}

fun getPlaystationImagePath(button: Int): String {
    return when (button) {
        Playstation.CIRCLE -> "images/help/playstation/circle.png"
        Playstation.CROSS -> "images/help/playstation/cross.png"
        Playstation.TRIANGLE -> "images/help/playstation/triangle.png"
        Playstation.SQUARE -> "images/help/playstation/square.png"
        Playstation.START -> "images/help/playstation/start.png"
        Playstation.SELECT -> "images/help/playstation/select.png"
        Playstation.L1 -> "images/help/playstation/l1.png"
        Playstation.L2 -> "images/help/playstation/l2.png"
        Playstation.R1 -> "images/help/playstation/r1.png"
        Playstation.R2 -> "images/help/playstation/r2.png"
        HELP_HUD_ALL -> "images/help/playstation/all.png"
        HELP_HUD_UPDOWN -> "images/help/playstation/updown.png"
        HELP_HUD_LEFTRIGHT -> "images/help/playstation/leftright.png"
        else -> "images/help/generic/unknown.png"
    }
}

fun getGenericImagePath(button: Int): String {
    return when (button) {
        0 -> "images/help/generic/1.png"
        1 -> "images/help/generic/2.png"
        2 -> "images/help/generic/3.png"
        3 -> "images/help/generic/4.png"
        4 -> "images/help/generic/5.png"
        5 -> "images/help/generic/6.png"
        6 -> "images/help/generic/7.png"
        7 -> "images/help/generic/8.png"
        8 -> "images/help/generic/9.png"
        9 -> "images/help/generic/10.png"
        10 -> "images/help/generic/11.png"
        11 -> "images/help/generic/12.png"
        12 -> "images/help/generic/13.png"
        13 -> "images/help/generic/14.png"
        14 -> "images/help/generic/15.png"
        15 -> "images/help/generic/16.png"
        16 -> "images/help/generic/17.png"
        17 -> "images/help/generic/18.png"
        18 -> "images/help/generic/19.png"
        19 -> "images/help/generic/20.png"
        HELP_HUD_ALL -> "images/help/generic/all.png"
        HELP_HUD_UPDOWN -> "images/help/generic/updown.png"
        HELP_HUD_LEFTRIGHT -> "images/help/generic/leftright.png"
        else -> "images/help/generic/unknown.png"
    }
}

fun getKeyboardImagePath(button: Int): String {
    return when (button) {
        HELP_HUD_ALL -> "images/help/keyboard/Keyboard_White_Arrow_All.png"
        HELP_HUD_UPDOWN -> "images/help/keyboard/Keyboard_White_Arrow_UpDown.png"
        HELP_HUD_LEFTRIGHT -> "images/help/keyboard/Keyboard_White_Arrow_LeftRight.png"
        Keys.NUMPAD_0 -> "images/help/keyboard/Keyboard_White_0.png"
        Keys.NUMPAD_1 -> "images/help/keyboard/Keyboard_White_1.png"
        Keys.NUMPAD_2 -> "images/help/keyboard/Keyboard_White_2.png"
        Keys.NUMPAD_3 -> "images/help/keyboard/Keyboard_White_3.png"
        Keys.NUMPAD_4 -> "images/help/keyboard/Keyboard_White_4.png"
        Keys.NUMPAD_5 -> "images/help/keyboard/Keyboard_White_5.png"
        Keys.NUMPAD_6 -> "images/help/keyboard/Keyboard_White_6.png"
        Keys.NUMPAD_7 -> "images/help/keyboard/Keyboard_White_7.png"
        Keys.NUMPAD_8 -> "images/help/keyboard/Keyboard_White_8.png"
        Keys.NUMPAD_9 -> "images/help/keyboard/Keyboard_White_9.png"
        Keys.NUM_0 -> "images/help/keyboard/Keyboard_White_0.png"
        Keys.NUM_1 -> "images/help/keyboard/Keyboard_White_1.png"
        Keys.NUM_2 -> "images/help/keyboard/Keyboard_White_2.png"
        Keys.NUM_3 -> "images/help/keyboard/Keyboard_White_3.png"
        Keys.NUM_4 -> "images/help/keyboard/Keyboard_White_4.png"
        Keys.NUM_5 -> "images/help/keyboard/Keyboard_White_5.png"
        Keys.NUM_6 -> "images/help/keyboard/Keyboard_White_6.png"
        Keys.NUM_7 -> "images/help/keyboard/Keyboard_White_7.png"
        Keys.NUM_8 -> "images/help/keyboard/Keyboard_White_8.png"
        Keys.NUM_9 -> "images/help/keyboard/Keyboard_White_9.png"
        Keys.A -> "images/help/keyboard/Keyboard_White_A.png"
        Keys.ALT_RIGHT -> "images/help/keyboard/Keyboard_White_Alt.png"
        Keys.ALT_LEFT -> "images/help/keyboard/Keyboard_White_Alt.png"
        Keys.DOWN -> "images/help/keyboard/Keyboard_White_Arrow_Down.png"
        Keys.LEFT -> "images/help/keyboard/Keyboard_White_Arrow_Left.png"
        Keys.RIGHT -> "images/help/keyboard/Keyboard_White_Arrow_Right.png"
        Keys.UP -> "images/help/keyboard/Keyboard_White_Arrow_Up.png"
        Keys.B -> "images/help/keyboard/Keyboard_White_B.png"
        Keys.BACKSPACE -> "images/help/keyboard/Keyboard_White_Backspace.png"
        Keys.LEFT_BRACKET -> "images/help/keyboard/Keyboard_White_Bracket_Left.png"
        Keys.RIGHT_BRACKET -> "images/help/keyboard/Keyboard_White_Bracket_Right.png"
        Keys.C -> "images/help/keyboard/Keyboard_White_C.png"
        Keys.CONTROL_LEFT -> "images/help/keyboard/Keyboard_White_Ctrl.png"
        Keys.CONTROL_RIGHT -> "images/help/keyboard/Keyboard_White_Ctrl.png"
        Keys.D -> "images/help/keyboard/Keyboard_White_D.png"
//            Keys.DEL -> "images/help/keyboard/Keyboard_White_Del.png"
        Keys.E -> "images/help/keyboard/Keyboard_White_E.png"
        Keys.END -> "images/help/keyboard/Keyboard_White_End.png"
        Keys.ENTER -> "images/help/keyboard/Keyboard_White_Enter.png"
        Keys.ESCAPE -> "images/help/keyboard/Keyboard_White_Esc.png"
        Keys.F -> "images/help/keyboard/Keyboard_White_F.png"
        Keys.F1 -> "images/help/keyboard/Keyboard_White_F1.png"
        Keys.F10 -> "images/help/keyboard/Keyboard_White_F10.png"
        Keys.F11 -> "images/help/keyboard/Keyboard_White_F11.png"
        Keys.F12 -> "images/help/keyboard/Keyboard_White_F12.png"
        Keys.F2 -> "images/help/keyboard/Keyboard_White_F2.png"
        Keys.F3 -> "images/help/keyboard/Keyboard_White_F3.png"
        Keys.F4 -> "images/help/keyboard/Keyboard_White_F4.png"
        Keys.F5 -> "images/help/keyboard/Keyboard_White_F5.png"
        Keys.F6 -> "images/help/keyboard/Keyboard_White_F6.png"
        Keys.F7 -> "images/help/keyboard/Keyboard_White_F7.png"
        Keys.F8 -> "images/help/keyboard/Keyboard_White_F8.png"
        Keys.F9 -> "images/help/keyboard/Keyboard_White_F9.png"
        Keys.G -> "images/help/keyboard/Keyboard_White_G.png"
        Keys.H -> "images/help/keyboard/Keyboard_White_H.png"
        Keys.HOME -> "images/help/keyboard/Keyboard_White_Home.png"
        Keys.I -> "images/help/keyboard/Keyboard_White_I.png"
        Keys.INSERT -> "images/help/keyboard/Keyboard_White_Insert.png"
        Keys.J -> "images/help/keyboard/Keyboard_White_J.png"
        Keys.K -> "images/help/keyboard/Keyboard_White_K.png"
        Keys.L -> "images/help/keyboard/Keyboard_White_L.png"
        Keys.M -> "images/help/keyboard/Keyboard_White_M.png"
        Keys.MINUS -> "images/help/keyboard/Keyboard_White_Minus.png"
        Keys.N -> "images/help/keyboard/Keyboard_White_N.png"
        Keys.O -> "images/help/keyboard/Keyboard_White_O.png"
        Keys.P -> "images/help/keyboard/Keyboard_White_P.png"
        Keys.PAGE_DOWN -> "images/help/keyboard/Keyboard_White_Page_Down.png"
        Keys.PAGE_UP -> "images/help/keyboard/Keyboard_White_Page_Up.png"
        Keys.PLUS -> "images/help/keyboard/Keyboard_White_Plus.png"
        Keys.Q -> "images/help/keyboard/Keyboard_White_Q.png"
        Keys.R -> "images/help/keyboard/Keyboard_White_R.png"
        Keys.S -> "images/help/keyboard/Keyboard_White_S.png"
        Keys.SEMICOLON -> "images/help/keyboard/Keyboard_White_Semicolon.png"
        Keys.SHIFT_LEFT -> "images/help/keyboard/Keyboard_White_Shift.png"
        Keys.SHIFT_RIGHT -> "images/help/keyboard/Keyboard_White_Shift.png"
        Keys.SLASH -> "images/help/keyboard/Keyboard_White_Slash.png"
        Keys.SPACE -> "images/help/keyboard/Keyboard_White_Space.png"
        Keys.T -> "images/help/keyboard/Keyboard_White_T.png"
        Keys.TAB -> "images/help/keyboard/Keyboard_White_Tab.png"
        Keys.GRAVE -> "images/help/keyboard/Keyboard_White_Tilda.png"
        Keys.U -> "images/help/keyboard/Keyboard_White_U.png"
        Keys.V -> "images/help/keyboard/Keyboard_White_V.png"
        Keys.W -> "images/help/keyboard/Keyboard_White_W.png"
        Keys.X -> "images/help/keyboard/Keyboard_White_X.png"
        Keys.Y -> "images/help/keyboard/Keyboard_White_Y.png"
        Keys.Z -> "images/help/keyboard/Keyboard_White_Z.png"
        else -> "images/help/generic/unknown.png"
    }
}

fun showExitConfirmation(emulio: Emulio, stage: Stage, cancelCallback: () -> Unit = {}, confirmCallback: () -> Unit = {}) {
    YesNoDialog("Quit Emulio?".translate(), "Are you sure you want to quit emulio?".translate(), emulio,
            cancelCallback = cancelCallback,
            confirmCallback = {
                confirmCallback()
                Gdx.app.exit()
            }).show(stage)
}

fun createColorTexture(rgba: Int, width: Int = 1, height: Int = 1): Texture {
    val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888).apply {
        setColor(rgba)
        fillRectangle(0, 0, width, height)
    }
    val texture = Texture(pixmap)

    pixmap.dispose()
    return texture
}

fun Button.addClickListener(clickListener: () -> Unit) {
    addListener(object : ClickListener() {
        override fun clicked(event: InputEvent?, x: Float, y: Float) {
            clickListener()
        }
    })
}


const val HELP_HUD_UPDOWN: Int = -10
const val HELP_HUD_LEFTRIGHT: Int = -11
const val HELP_HUD_ALL: Int = -12

data class HelpItems(
    val txtConfirm: String? = null,
    var imgConfirmButton: Image? = null,
    var txtConfirmButton: Label? = null,
    val txtCancel: String? = null,
    var imgCancelButton: Image? = null,
    var txtCancelButton: Label? = null,
    val txtUpDown: String? = null,
    var imgUpDownButton: Image? = null,
    var txtUpDownButton: Label? = null,
    val txtLeftRight: String? = null,
    var imgLeftRightButton: Image? = null,
    var txtLeftRightButton: Label? = null,
    val txtAllDirection: String? = null,
    var imgAllDirectionButtons: Image? = null,
    var txtAllDirectionButtons: Label? = null,
    val txtFind: String? = null,
    var imgFindButton: Image? = null,
    var txtFindButton: Label? = null,
    val txtOptions: String? = null,
    var imgOptionsButton: Image? = null,
    var txtOptionsButton: Label? = null,
    val txtSelect: String? = null,
    var imgSelectButton: Image? = null,
    var txtSelectButton: Label? = null,
    val txtPageUp: String? = null,
    var imgPageUpButton: Image? = null,
    var txtPageUpButton: Label? = null,
    val txtPageDown: String? = null,
    var imgPageDownButton: Image? = null,
    var txtPageDownButton: Label? = null,
    var lastInputLoaded: InputConfig? = null,
    var alpha: Float = 0.8f,
    var txtColor: Color = Color.WHITE
)