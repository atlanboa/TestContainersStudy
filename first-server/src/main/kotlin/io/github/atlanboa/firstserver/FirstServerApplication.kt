package io.github.atlanboa.firstserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FirstServerApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<FirstServerApplication>(*args)
        }
    }
}

