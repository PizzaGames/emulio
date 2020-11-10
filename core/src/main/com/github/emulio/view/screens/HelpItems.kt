package com.github.emulio.view.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.github.emulio.model.config.InputConfig

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