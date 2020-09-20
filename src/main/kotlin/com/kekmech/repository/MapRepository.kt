package com.kekmech.repository

import com.kekmech.dto.MapMarker
import com.kekmech.repository.sources.MarkersSource
import org.intellij.lang.annotations.Language
import org.jooq.DSLContext
import java.util.*

class MapRepository(
    private val markersSource: MarkersSource,
    private val dsl: DSLContext
) {

    fun getMarkers() = markersSource.get(Unit)

    @Language("SQL")
    fun addMarker(marker: MapMarker) =
        dsl.fetch("insert into map_markers (place_uid, address, lat, lng, place_name, place_type, icon) values" +
                "('${UUID.randomUUID()}', '${marker.address}', ${marker.location.lat}, ${marker.location.lng}," +
                "'${marker.name}', '${marker.type}', '')")

}