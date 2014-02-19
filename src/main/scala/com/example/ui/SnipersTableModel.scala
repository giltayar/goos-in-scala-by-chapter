package com.example.ui

import javax.swing.table.AbstractTableModel
import javax.swing.SwingUtilities
import com.example._

private[example] class SnipersTableModel extends AbstractTableModel
    with SniperListener with PortfolioListener with Logging {
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

  def addSniperSnapshot(sniper: SniperSnapshot) = {
    sniperStates :+= sniper
    fireTableRowsInserted(sniperStates.size - 1, sniperStates.size - 1)
  }

  def sniperAdded(auctionSniper: AuctionSniper) = {
    addSniperSnapshot(auctionSniper.snapshot)

    auctionSniper.addSniperListener(new SwingThreadSniperListener(this))
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
    SniperState.Losing -> "Losing",
    SniperState.Lost -> "Lost",
    SniperState.Won -> "Won"
  )
}

private class SwingThreadSniperListener(private val listener: SniperListener) extends SniperListener {
  def sniperStateChanged(sniperSnapshot: SniperSnapshot) = {
    SwingUtilities.invokeLater(createRunnable {
      listener.sniperStateChanged(sniperSnapshot)
    })
  }
}
