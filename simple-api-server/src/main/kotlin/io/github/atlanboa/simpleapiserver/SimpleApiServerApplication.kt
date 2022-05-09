package io.github.atlanboa.simpleapiserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SimpleApiServerApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<SimpleApiServerApplication>(*args)
        }
    }

}
