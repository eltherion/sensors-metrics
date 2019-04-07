package com.datart.sensors.metrics.mapping

import com.datart.sensors.metrics.model._

trait RowMapper {
  def toRow(rawRow: String): Row
}

class RowMapperImpl extends RowMapper {
  override def toRow(rawRow: String): Row = ???
}
