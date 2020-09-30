package com.kekmech.repository.sources

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kekmech.LogicException
import com.kekmech.dto.Location
import com.kekmech.dto.MapMarker
import com.kekmech.dto.MarkerType
import com.kekmech.repository.DataSource
import io.netty.util.internal.logging.InternalLogger
import org.intellij.lang.annotations.Language
import org.jooq.DSLContext
import java.time.Duration

class MarkersSource(
    private val log: InternalLogger,
    private val dsl: DSLContext
) : DataSource<Unit, List<MapMarker>>() {

    override val cache: Cache<Unit, List<MapMarker>> = Caffeine.newBuilder()
        .maximumSize(1L)
        .expireAfterAccess(Duration.ofHours(1L))
        .build<Unit, List<MapMarker>>()

    /**
     * Get static data from DB
     */
    override fun getFromRemote(k: Unit): List<MapMarker>? = try {
        @Language("SQL")
        val records = dsl.fetch("""select * from map_markers;""".trimIndent())
        assert(records.isNotEmpty)
        val markers = records
            .map { record -> MapMarker(
                uid = record["place_uid"].toString(),
                address = record["address"].toString(),
                location = Location(record["lat"].asFloat(), record["lng"].asFloat()),
                name = record["place_name"].toString(),
                type = valueOfTypeOfNull(record["place_type"].toString()) ?: MarkerType.UNDEFINED,
                icon = record["icon"].toString(),
                tag = record["tag"]?.toString().orEmpty()
            ) }
        markers
    } catch (e: Exception) {
        throw LogicException("Error while loading map markers from DB: ${e.message}")
    }

    private fun Any.asFloat() = toString().toDouble()

    private fun valueOfTypeOfNull(string: String) = try {
        MarkerType.valueOf(string)
    } catch (e: Exception) {
        null
    }
}