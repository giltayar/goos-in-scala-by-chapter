package com.example

import javax.swing._
import java.awt.event.{WindowEvent, WindowAdapter}
import com.example.xmpp.XMPPAuctionHouse
import com.example.ui.MainWindow

object Main extends App with Logging {
  private val sniperId = this.args(0)
  private val sniperPortfolio = new SniperPortfolio()
  private val auctionHouse = XMPPAuctionHouse.connect(AUCTION_XMPP_HOST, sniperId, AUCTION_XMPP_USER_PASSWORD)
  private var ui: MainWindow = null

  createMainWindow()

  private def createMainWindow() {
    SwingUtilities.invokeAndWait(createRunnable {
      ui = new MainWindow(sniperPortfolio)
      ui.addUserRequestListener(new SniperLauncher(sniperId, sniperPortfolio, auctionHouse))
      ui.addWindowListener(new WindowAdapter() {
        override def windowClosed(e: WindowEvent) {
          auctionHouse.disconnect()
        }
      })
    })
  }
}

