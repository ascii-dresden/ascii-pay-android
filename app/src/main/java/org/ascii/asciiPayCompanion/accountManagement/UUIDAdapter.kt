package org.ascii.asciiPayCompanion.accountManagement

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import java.util.*

class UUIDAdapter : Adapter<UUID> {
    override fun fromJson(
        reader: JsonReader,
        customScalarAdapters: CustomScalarAdapters
    ): UUID {
        reader.beginObject()
        val res = UUID.fromString(reader.nextString())
        reader.endObject()
        return res
    }

    override fun toJson(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        value: UUID
    ) {
        writer.beginObject()
        writer.name("UUID").value(value.toString())
        writer.endObject()
    }
}