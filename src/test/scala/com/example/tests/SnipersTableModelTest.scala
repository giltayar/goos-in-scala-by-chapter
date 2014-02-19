package com.example.tests

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import com.example.{SniperState, SniperSnapshot}
import com.example.ui.SnipersTableModel
import SnipersTableModel.Column
import org.specs2.matcher.Hamcrest

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

      snipersTableModel.addSniperSnapshot(joining)

      snipersTableModel.getRowCount must be equalTo 1
      assertRowMatchesSnapshot(snipersTableModel, 0, joining)
    }

    "change sniper state" in new Context {
      snipersTableModel.addSniperSnapshot(SniperSnapshot.joining("item id"))

      val snapshot = SniperSnapshot("item id", 555, 666, SniperState.Bidding)

      snipersTableModel.sniperStateChanged(snapshot)

      assertRowMatchesSnapshot(snipersTableModel, 0, snapshot)
    }

    "add two sniper snapshots correctly" in new Context{
      val joining = SniperSnapshot.joining("1234")
      val joining2 = SniperSnapshot.joining("12345678")

      snipersTableModel.addSniperSnapshot(joining)
      snipersTableModel.addSniperSnapshot(joining2)

      snipersTableModel.getRowCount must be equalTo 2
      assertRowMatchesSnapshot(snipersTableModel, 0, joining)
      assertRowMatchesSnapshot(snipersTableModel, 1, joining2)
    }

    "has text strings for all SniperState values" in new Context {
      snipersTableModel.addSniperSnapshot(SniperSnapshot.joining("item id"))

      SniperState.values.foreach {state =>

        val snapshot = SniperSnapshot("item id", 555, 666, state)

        snipersTableModel.sniperStateChanged(snapshot)

        snipersTableModel.getValueAt(0, Column.SniperStatus.id) must not be empty

      }
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
