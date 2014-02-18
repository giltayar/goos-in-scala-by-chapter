package com.example.tests

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import com.example.{SniperState, SniperSnapshot, SnipersTableModel}
import javax.swing.event.{TableModelEvent, TableModelListener}
import com.example.SnipersTableModel.Column
import org.specs2.matcher.{MatchResult, Hamcrest}
import org.hamcrest.beans.SamePropertyValuesAs

class SnipersTableModelTest extends Specification with Mockito with Hamcrest {

  private trait Context extends Scope {
    val snipersTableModel = new SnipersTableModel
  }

  "SnipersTableModel" should {
    "have enough columns" in new Context {
      snipersTableModel.getColumnCount must be equalTo Column.values.size
    }

    "add sniper snapshot correctly" in new Context {
      val joining = SniperSnapshot.joining("1234")

      snipersTableModel.addSniper(joining)

      snipersTableModel.getRowCount must be equalTo 1
      assertRowMatchesSnapshot(snipersTableModel, 0, joining)
    }

    "change sniper state" in new Context {
      snipersTableModel.addSniper(SniperSnapshot.joining("item id"))

      private val snapshot = SniperSnapshot("item id", 555, 666, SniperState.Bidding)

      snipersTableModel.sniperStateChanged(snapshot)

      assertRowMatchesSnapshot(snipersTableModel, 0, snapshot)
    }

    "add two sniper snapshots correctly" in new Context{
      val joining = SniperSnapshot.joining("1234")
      val joining2 = SniperSnapshot.joining("12345678")

      snipersTableModel.addSniper(joining)
      snipersTableModel.addSniper(joining2)

      snipersTableModel.getRowCount must be equalTo 2
      assertRowMatchesSnapshot(snipersTableModel, 0, joining)
      assertRowMatchesSnapshot(snipersTableModel, 1, joining2)
    }
  }

  private def assertRowMatchesSnapshot(snipersTableModel: SnipersTableModel,
                                       rowIndex: Int,
                                       snapshot: SniperSnapshot): Unit = {
    snipersTableModel.getValueAt(rowIndex, Column.ItemIdentifier.id) must be equalTo snapshot.itemId
    snipersTableModel.getValueAt(rowIndex, Column.LastPrice.id) must be equalTo snapshot.lastPrice.toString
    snipersTableModel.getValueAt(rowIndex, Column.LastBid.id) must be equalTo snapshot.lastBid.toString
    snipersTableModel.getValueAt(rowIndex, Column.SniperStatus.id) must be equalTo SnipersTableModel.SniperStateToText(snapshot.sniperState)
  }
}
