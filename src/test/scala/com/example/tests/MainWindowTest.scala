package com.example.tests

import org.specs2.mutable.{After, Specification}
import com.example.{UserRequestListener, SnipersTableModel, MainWindow}
import org.specs2.specification.Scope
import com.example.tests.endtoend.AuctionSniperDriver
import com.objogate.wl.swing.probe.ValueMatcherProbe
import org.hamcrest.Matchers

class MainWindowTest extends Specification {
  trait Context extends Scope with After {
    val snipersTableModel = new SnipersTableModel()
    val driver = new AuctionSniperDriver(100)

    def after = {
      if (driver != null)
        driver.dispose()
    }
  }
  "MainWindow" should {
    "make user request when join button clicked" in new Context {
      val buttonProbe = new ValueMatcherProbe[String](Matchers.equalTo("12345"), "join request")
      val mainWindow = new MainWindow(snipersTableModel, new UserRequestListener {
          def joinAuction(itemId: String) = {
            buttonProbe.setReceivedValue(itemId)
          }
        }
      )

      driver.startBiddingFor("12345")
      driver.check(buttonProbe)
    }
  }
}
