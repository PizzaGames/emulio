package com.github.emulio.service.i18n

fun String.translate(): String {
    return I18n.translate(this)
}