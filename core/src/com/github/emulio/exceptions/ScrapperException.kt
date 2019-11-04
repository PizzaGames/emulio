package com.github.emulio.exceptions

import java.lang.RuntimeException

class ScrapperException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)