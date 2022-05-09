package io.github.atlanboa.simpleapiserver

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/simple")
class Controller {

    @Value("\${akmj.name}")
    private lateinit var author: String

    @GetMapping
    fun getAnyRequest(@RequestParam objectName: String): ResponseEntity<String> {
        return ResponseEntity.ok("this is any response with $objectName by $author")
    }
}