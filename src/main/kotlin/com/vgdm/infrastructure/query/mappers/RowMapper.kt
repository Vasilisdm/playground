package com.vgdm.infrastructure.query.mappers

import kotliquery.Row

object RowMapper {
    fun mapFromRow(row: Row) : Map<String, Any?> {
        return row.underlying.metaData
            .let { (1..it.columnCount).map(it::getColumnName) }
            .map { it to row.anyOrNull(it) }
            .toMap()
    }
}