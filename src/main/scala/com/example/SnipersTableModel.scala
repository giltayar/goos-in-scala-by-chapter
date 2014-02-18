package com.example
import javax.swing.table.AbstractTableModel

private[example] class SnipersTableModel extends AbstractTableModel with SniperListener with Logging {
  var sniperStates = Array[SniperSnapshot]()

  def getColumnCount = SnipersTableModel.Column.values.size
  def getRowCount = sniperStates.size

  def getValueAt(rowIndex: Int, columnIndex: Int) = {
    val sniperState = sniperStates(rowIndex)
    SnipersTableModel.Column(columnIndex) match {
      case SnipersTableModel.Column.ItemIdentifier => sniperState.itemId
      case SnipersTableModel.Column.LastPrice => sniperState.lastPrice.toString
      case SnipersTableModel.Column.LastBid => sniperState.lastBid.toString
      case SnipersTableModel.Column.SniperStatus => SnipersTableModel.SniperStateToText(sniperState.sniperState)
    }
  }

  override def getColumnName(column: Int) = SnipersTableModel.ColumnTitles(SnipersTableModel.Column(column))

  def sniperStateChanged(sniperState: SniperSnapshot) = {
    val rowIndex = sniperStates indexWhere (_.itemId == sniperState.itemId)
    sniperStates(rowIndex) = sniperState
    fireTableRowsUpdated(rowIndex, rowIndex)
  }

  def addSniper(sniper: SniperSnapshot) = {
    sniperStates :+= sniper
    fireTableRowsInserted(sniperStates.size - 1, sniperStates.size - 1)
  }
}

private[example] object SnipersTableModel {
  object Column extends Enumeration {
    val ItemIdentifier = Value
    val LastPrice = Value
    val LastBid = Value
    val SniperStatus = Value
  }

  val ColumnTitles = Map(
    Column.ItemIdentifier -> "Item",
    Column.LastPrice -> "Last Price",
    Column.LastBid -> "Last Bid",
    Column.SniperStatus -> "State"
  )

  val SniperStateToText = Map(
    SniperState.Bidding -> "Bidding",
    SniperState.Joining -> "Joining",
    SniperState.Winning -> "Winning",
    SniperState.Lost -> "Lost",
    SniperState.Won -> "Won"
  )
}
