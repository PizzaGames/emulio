package com.github.emulio.model

import java.io.File

data class EmulioOptions(
    val workdir: File,
    val minimizeApplication: () -> Unit,
    val restoreApplication: () -> Unit,
    val screenSize: Pair<Int, Int>?,
    val fullscreen: Boolean,
    val platformsFile: File = File(workdir, "emulio-platforms.yaml")
)