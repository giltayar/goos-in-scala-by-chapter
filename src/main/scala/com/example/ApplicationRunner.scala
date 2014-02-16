package com.example

import com.objogate.wl.swing.driver.{ComponentDriver, JLabelDriver, JFrameDriver}
import com.objogate.wl.swing.gesture.GesturePerformer
import com.objogate.wl.swing.AWTEventQueueProber
import org.hamcrest.Matchers.equalTo

class ApplicationRunner extends Logging {

  var driver : AuctionSniperDriver = null

  def startBiddingIn(auction: FakeAuctionServer) {
    log.info("startBiddingIn starts")
    val thread = new Thread(createRunnable {
      Main.main(List(auction.item).toArray)
    })

    thread setDaemon true
    thread.start()

    driver = new AuctionSniperDriver(1000)
    driver.showsSniperStatus(MainWindow.STATUS_JOINING)
    log.info("startBiddingIn ended")
  }

  def showsSniperHasLostAuction() {
    log.info(s"waiting for status lost $driver")
    driver.showsSniperStatus(MainWindow.STATUS_LOST)
    log.info("got status lost")
  }

  def hasShownSniperIsBidding() {
    log.info(s"waiting for status lost $driver")
    driver.showsSniperStatus(MainWindow.STATUS_BIDDING)
    log.info("got status lost")
  }

  def stop() {
    if (driver != null)
      driver.dispose()
  }
}

class AuctionSniperDriver(timeoutMillis: Int) extends
  JFrameDriver(
    new GesturePerformer(),
    JFrameDriver.topLevelFrame(
      ComponentDriver.named(MainWindow.WINDOW_NAME), ComponentDriver.showingOnScreen()),
    new AWTEventQueueProber(timeoutMillis, 100)) {

    def showsSniperStatus(statusText: String) {
      new JLabelDriver(this, ComponentDriver.named(MainWindow.SNIPER_STATUS_NAME)).
        hasText(equalTo(statusText))
  }

}