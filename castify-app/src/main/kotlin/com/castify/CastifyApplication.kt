package com.castify

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.castify"])
class CastifyApplication

fun main(args: Array<String>) {
    runApplication<CastifyApplication>(*args)
}
