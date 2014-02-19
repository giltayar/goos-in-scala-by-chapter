package com.example.tests.endtoend

import com.objogate.wl.swing.matcher.{IterableComponentsMatcher, JLabelTextMatcher}

import com.objogate.wl.swing.gesture.GesturePerformer
import com.objogate.wl.swing.AWTEventQueueProber
import com.example._
import javax.swing.table.JTableHeader
import javax.swing.{JButton, JTextField}
import com.objogate.wl.swing.driver._
import com.example.ui.MainWindow

class ApplicationRunner extends Logging {
  var driver : AuctionSniperDriver = null

  def startBiddingIn(auctions: FakeAuctionServer*) {

    startSniperApplication()

    auctions.foreach {fakeAuctionServer =>
      driver.startBiddingFor(fakeAuctionServer.item, None)
      driver.showsSniperStatus(fakeAuctionServer.item, 0, 0, MainWindow.STATUS_JOINING)
    }
  }
  def startBiddingWithStopPrice(stopPrice: Int, auctions: FakeAuctionServer*) {
    startSniperApplication()

    auctions.foreach {fakeAuctionServer =>
      driver.startBiddingFor(fakeAuctionServer.item, Some(stopPrice))
      driver.showsSniperStatus(fakeAuctionServer.item, 0, 0, MainWindow.STATUS_JOINING)
    }
  }

  def startSniperApplication() = {
    val thread = new Thread(createRunnable {
      Main.main(Array(ApplicationRunner.SNIPER_ID))
    })

    thread setDaemon true
    thread.start()

    driver = new AuctionSniperDriver(1000)
    driver.hasTitle(MainWindow.WINDOW_NAME)
    driver.hasColumnTitles()
  }

  def showsSniperHasLostAuction(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(auction.item, lastPrice, lastBid, MainWindow.STATUS_LOST)
  }

  def showsSniperHasWonAuction(auction: FakeAuctionServer, lastPrice: Int) {
    driver.showsSniperStatus(auction.item, lastPrice, lastPrice, MainWindow.STATUS_WON)
  }

  def hasShownSniperIsBidding(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(auction.item, lastPrice, lastBid, MainWindow.STATUS_BIDDING)
  }

  def hasShownSniperIsLosing(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(auction.item, lastPrice, lastBid, MainWindow.STATUS_LOSING)
  }

  def hasShownSniperIsWinning(auction: FakeAuctionServer, lastPrice: Int) {
    driver.showsSniperStatus(auction.item, lastPrice, lastPrice, MainWindow.STATUS_WINNING)
  }

  def stop() {
    if (driver != null)
      driver.dispose()
  }
}

object ApplicationRunner {
  val SNIPER_ID = "sniper"
}

class AuctionSniperDriver(timeoutMillis: Int) extends
  JFrameDriver(
    new GesturePerformer(),
    JFrameDriver.topLevelFrame(
      ComponentDriver.named(MainWindow.WINDOW_NAME), ComponentDriver.showingOnScreen()),
    new AWTEventQueueProber(timeoutMillis, 100)) with Logging {

  private def textField(id: String) = new JTextFieldDriver(this, classOf[JTextField], ComponentDriver.named(id))

  def itemIdField = textField(MainWindow.NEW_ITEM_ID_NAME)
  def stopPriceField = textField(MainWindow.STOP_PRICE_NAME)
  def bidButton = new JButtonDriver(this, classOf[JButton], ComponentDriver.named(MainWindow.JOIN_BUTTON_NAME))

  def startBiddingFor(auctionItem: String, stopPrice: Option[Int] = None) = {
    itemIdField.focusWithMouse()
    itemIdField.replaceAllText(auctionItem)

    stopPrice foreach {x =>
      stopPriceField.focusWithMouse()
      stopPriceField.replaceAllText(x.toString)
    }

    bidButton.click()
  }

  def hasColumnTitles() = {
    val headers = new JTableHeaderDriver(this, classOf[JTableHeader])
    headers.hasHeaders(IterableComponentsMatcher.matching(
      JLabelTextMatcher.withLabelText("Item"),
      JLabelTextMatcher.withLabelText("Last Price"),
      JLabelTextMatcher.withLabelText("Last Bid"),
      JLabelTextMatcher.withLabelText("State")
    ))

  }

  def showsSniperStatus(itemId: String, lastPrice: Int, lastBid: Int, statusText: String) {
    new JTableDriver(this).hasRow(IterableComponentsMatcher.matching(
      JLabelTextMatcher.withLabelText(itemId),
      JLabelTextMatcher.withLabelText(lastPrice.toString),
      JLabelTextMatcher.withLabelText(lastBid.toString),
      JLabelTextMatcher.withLabelText(statusText)
    ))
  }
}
