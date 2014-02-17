package com.example.tests.endtoend

import com.objogate.wl.swing.driver.{JTableHeaderDriver, JTableDriver, ComponentDriver, JFrameDriver}
import com.objogate.wl.swing.matcher.{IterableComponentsMatcher, JLabelTextMatcher}

import com.objogate.wl.swing.gesture.GesturePerformer
import com.objogate.wl.swing.AWTEventQueueProber
import com.example._
import javax.swing.table.JTableHeader

class ApplicationRunner extends Logging {
  var driver : AuctionSniperDriver = null
  var itemId : String = null

  def startBiddingIn(auction: FakeAuctionServer) {
    itemId = auction.item

    val thread = new Thread(createRunnable {
      Main.main(List(auction.item, ApplicationRunner.SNIPER_ID).toArray)
    })

    thread setDaemon true
    thread.start()

    driver = new AuctionSniperDriver(1000)
    driver.hasTitle(MainWindow.WINDOW_NAME)
    driver.hasColumnTitles()
    driver.showsSniperStatus("", 0, 0, MainWindow.STATUS_JOINING)
  }

  def showsSniperHasLostAuction(lastPrice: Int) {
    driver.showsSniperStatus(itemId, lastPrice, lastPrice, MainWindow.STATUS_LOST)
  }

  def showsSniperHasWonAuction(lastPrice: Int) {
    driver.showsSniperStatus(itemId, lastPrice, lastPrice, MainWindow.STATUS_WON)
  }

  def hasShownSniperIsBidding(lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(itemId, lastPrice, lastBid, MainWindow.STATUS_BIDDING)
  }

  def hasShownSniperIsWinning(lastPrice: Int) {
    driver.showsSniperStatus(itemId, lastPrice, lastPrice, MainWindow.STATUS_WINNING)
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
    new AWTEventQueueProber(timeoutMillis, 100)) {

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
