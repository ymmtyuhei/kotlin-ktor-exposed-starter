package web

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import service.EchoService

fun Route.echo(service: EchoService) {

    route("/echo") {

        get("/") {
            call.respond(service.getEcho())
        }
    }

}
