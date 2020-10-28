package com.github.emulio.model.config

open class InputConfig {
	lateinit var type: InputType
	var name: String = "Generic"

	var confirm: Int = -1
	var cancel: Int = -1

	var usePov = false
	var up: Int = -1
	var down: Int = -1
	var left: Int = -1
	var right: Int = -1

	var axisX: Int = -1
	var axisY: Int = -1

    var axisLeftTrigger: Int = -1
    var axisRightTrigger: Int = -1

	var find: Int = -1

	var options: Int = -1
	var select: Int = -1

	var pageUp: Int = -1
	var pageDown: Int = -1

	var exit: Int = -1

    var configImages: Map<Int, String> = emptyMap()
}