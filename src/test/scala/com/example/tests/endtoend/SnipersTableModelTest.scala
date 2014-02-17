package com.example.tests.endtoend

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import com.example.{SniperState, SniperSnapshot, SnipersTableModel}
import javax.swing.event.{TableModelEvent, TableModelListener}
import com.example.SnipersTableModel.Column
import org.specs2.matcher.Hamcrest
import org.hamcrest.beans.SamePropertyValuesAs

class SnipersTableModelTest extends Specification with Mockito with Hamcrest {

  private trait Context extends Scope {
    val snipersTableModel = new SnipersTableModel
    val tableListener = mock[TableModelListener]

    snipersTableModel.addTableModelListener(tableListener)
  }

  "SnipersTableModel" should {
    "have enough columns" in new Context {
      snipersTableModel.getColumnCount must be equalTo Column.values.size
    }
    "set sniper values in columns" in new Context {
      snipersTableModel.sniperStateChanged(SniperSnapshot("item id", 555, 666, SniperState.Bidding))

      def aRowChangedEvent() = SamePropertyValuesAs.samePropertyValuesAs(new TableModelEvent(snipersTableModel, 0))

      there was atLeastOne(tableListener).tableChanged(anArgThat(aRowChangedEvent()))

      snipersTableModel.getValueAt(0, Column.ItemIdentifier.id) must be equalTo "item id"
      snipersTableModel.getValueAt(0, Column.LastPrice.id) must be equalTo "555"
      snipersTableModel.getValueAt(0, Column.LastBid.id) must be equalTo "666"
      snipersTableModel.getValueAt(0, Column.SniperStatus.id) must be equalTo "Bidding"
    }
  }
}
