package com.kekmech

import com.google.gson.Gson
import com.kekmech.di.*
import com.kekmech.dto.*
import com.kekmech.gson.*
import com.kekmech.helpers.*
import com.kekmech.repository.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.core.context.*
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import java.text.*
import java.time.*
import java.util.*
import kotlin.system.exitProcess

val mapRepository by inject(MapRepository::class.java)
val gson by inject(Gson::class.java)

data class Entry(val name: String, val address: String, val location: Location)
data class Content(val content: List<Entry>)

fun main(args: Array<String>) {
    initKoin()
    val server = embeddedServer(Netty, port = GlobalConfig.port) {
        install(DefaultHeaders)
        install(Compression)
        install(CallLogging)
        install(ContentNegotiation) {
            gson {
                setDateFormat(DateFormat.LONG)
                registerTypeAdapter(LocalDate::class.java, LocalDateSerializer())
                registerTypeAdapter(LocalTime::class.java, LocalTimeSerializer())
                registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeSerializer())
            }
        }
        install(StatusPages) {
            exception<ExternalException> { cause ->
                call.respond(HttpStatusCode.ServiceUnavailable, cause.message.orEmpty())
            }
            exception<LogicException> { cause ->
                cause.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, cause.message.orEmpty())
            }
            exception<ValidationException> { cause ->
                call.respond(HttpStatusCode.BadRequest, cause.message.orEmpty())
            }
            exception<Exception> { cause ->
                cause.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, cause.message.orEmpty())
            }
        }
        routing {
            getMapMarkers()
            get("/") { call.respond(HttpStatusCode.OK, "Hello world") }
        }
    }
    server.start(wait = true)
}

fun initKoin() = startKoin {
    modules(
        AppModule(),
        PostgresModule
    )
}

fun Route.getMapMarkers() = get(Endpoint.getMapMarkers) {
    val markers = mapRepository.getMarkers()
    call.respond(HttpStatusCode.OK, GetMapMarkersResponse(markers.orEmpty()))
}