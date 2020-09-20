package com.kekmech.di

import com.google.gson.*
import com.kekmech.di.factories.*
import com.kekmech.helpers.*
import com.kekmech.repository.*
import com.kekmech.repository.sources.*
import io.ktor.client.*
import io.netty.util.internal.logging.*
import okhttp3.logging.*
import org.koin.dsl.*
import java.util.*

class AppModule : ModuleProvider({
    single { GsonFactory.create() } bind Gson::class
    single { HttpClientFactory.create() } bind HttpClient::class
    single { Slf4JLoggerFactory.getInstance("MAP") } bind InternalLogger::class
    single { Locale.GERMAN } bind Locale::class

    single { MarkersSource(get(), get()) } bind MarkersSource::class
    single { MapRepository(get(), get()) } bind MapRepository::class
})

object Logger : HttpLoggingInterceptor.Logger {
    override fun log(message: String) = println(message)
}