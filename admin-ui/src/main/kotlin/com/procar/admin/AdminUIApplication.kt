package com.procar.admin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AdminUIApplication

fun main(args: Array<String>) {
    runApplication<AdminUIApplication>(*args)
}
