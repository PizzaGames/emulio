package com.github.emulio.service.scrapers.tgdb_old.model

data class Platform(
        val id: Int ,
        val name: String,
        val alias: String?,
        val icon: String?,
        val console: String?,
        val controller: String?,
        val developer: String?,
        val manufacturer: String?,
        val media: String?,
        val cpu: String?,
        val memory: String?,
        val graphics: String?,
        val sound: String?,
        val maxcontrollers: Int?,
        val display: String?,
        val overview: String?,
        val youtube: String?
)