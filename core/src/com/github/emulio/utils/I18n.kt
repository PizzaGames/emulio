package com.github.emulio.utils

import com.badlogic.gdx.Gdx
import com.github.emulio.Emulio
import com.github.emulio.yaml.YamlReaderHelper
import com.github.emulio.yaml.YamlUtils
import mu.KotlinLogging

object I18n {

    val logger = KotlinLogging.logger { }

    private val languageMap by lazy {
        val emulio = Gdx.app.applicationListener as Emulio

        YamlUtils.parse(emulio.getLanguageStream())
    }

    fun translate(key: String): String {
        return languageMap[key] as String? ?: markedText(key)
    }

    private fun markedText(key: String): String {
        logger.debug { "*** Translation not found, include in the i18n file: \"$key\": \"$key\"" }
        return "*$key"
    }

    fun containsKey(key: String): Boolean {
        return languageMap.containsKey(key)
    }

}

fun String.translate(): String {
    return I18n.translate(this)
}