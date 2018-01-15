package com.github.emulio.utils

import com.badlogic.gdx.Gdx
import com.github.emulio.Emulio
import com.github.emulio.yaml.YamlReaderHelper
import com.github.emulio.yaml.YamlUtils

object I18n {

    private val languageMap by lazy {
        val emulio = Gdx.app.applicationListener as Emulio

        YamlUtils.parse(emulio.getLanguageStream())
    }

    fun translate(key: String): String {
        return languageMap[key] as String? ?: "*$key"
    }

    fun containsKey(key: String): Boolean {
        return languageMap.containsKey(key)
    }

}

fun String.translate(): String {
    return I18n.translate(this)
}