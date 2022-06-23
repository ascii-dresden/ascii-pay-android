package org.ascii.asciiPayCompanion

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.api.json.writeObject

class UUID (val rep : java.util.UUID){
    val uuidAdapter = object : Adapter<UUID> {
        override fun fromJson(
            reader: JsonReader,
            customScalarAdapters: CustomScalarAdapters
        ): UUID {
            reader.beginObject()
            val res = UUID(java.util.UUID.fromString(reader.nextString()))
            reader.endObject()
            return res
        }

        override fun toJson(
            writer: JsonWriter,
            customScalarAdapters: CustomScalarAdapters,
            value: UUID
        ) {
            writer.beginObject()
            writer.name("UUID").value(value.rep.toString())
            writer.endObject()
        }

    }
}