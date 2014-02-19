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

    startBiddingFor(None, auctions:_*)
  }

  def startBiddingWithStopPrice(stopPrice: Int, auctions: FakeAuctionServer*) {
    startSniperApplication()

    startBiddingFor(Some(stopPrice), auctions:_*)
  }

  private def startBiddingFor(stopPrice: Option[Int], auctions: FakeAuctionServer*) {
    auctions.foreach {
      fakeAuctionServer =>
        driver.startBiddingFor(fakeAuctionServer.item, stopPrice)
        driver.showsSniperStatus(fakeAuctionServer.item, 0, 0, "Joining")
    }
  }

def startSniperApplication() = {
    val thread = new Thread(createRunnable {
      Main.main(Array(ApplicationRunner.SNIPER_ID))
    })

    thread setDaemon true
    thread.start()

    driver = new AuctionSniperDriver(5000)
    driver.hasTitle(MainWindow.WINDOW_NAME)
    driver.hasColumnTitles()
  }

  def showsSniperHasLostAuction(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(auction.item, lastPrice, lastBid, "Lost")
  }

  def showsSniperHasWonAuction(auction: FakeAuctionServer, lastPrice: Int) {
    driver.showsSniperStatus(auction.item, lastPrice, lastPrice, "Won")
  }

  def hasShownSniperIsBidding(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(auction.item, lastPrice, lastBid, "Bidding")
  }

  def hasShownSniperIsLosing(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(auction.item, lastPrice, lastBid, "Losing")
  }

  def hasShownSniperIsWinning(auction: FakeAuctionServer, lastPrice: Int) {
    driver.showsSniperStatus(auction.item, lastPrice, lastPrice, "Winning")
  }

  def showsSniperHasFailed(auction: FakeAuctionServer) {
    driver.showsSniperStatus(auction.item, 0, 0, "Failed")
  }

  def reportsInvalidMessage(auction: FakeAuctionServer, brokenMessage: String) {
    // I didn't implement this part of chapter 19
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
