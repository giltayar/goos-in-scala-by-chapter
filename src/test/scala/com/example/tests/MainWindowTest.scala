package com.example.tests

import org.specs2.mutable.{After, Specification}
import com.example.{Item, SniperPortfolio}
import org.specs2.specification.Scope
import com.example.tests.endtoend.AuctionSniperDriver
import com.objogate.wl.swing.probe.ValueMatcherProbe
import org.hamcrest.Matchers
import com.example.ui.{SnipersTableModel, UserRequestListener, MainWindow}

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
      val snipersPortfolio = new SniperPortfolio()
      val mainWindow = new MainWindow(snipersPortfolio)
      mainWindow.addUserRequestListener(new UserRequestListener {
        def joinAuction(item: Item) = {
          buttonProbe.setReceivedValue(item.id)
        }
      })

      driver.startBiddingFor("12345")
      driver.check(buttonProbe)
    }
  }
}
