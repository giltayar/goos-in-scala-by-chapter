package com.example

import javax.swing.{JLabel, JFrame, SwingUtilities}
import javax.swing.border.LineBorder
import java.awt.Color
import org.jivesoftware.smack.{Chat, XMPPConnection}
import java.awt.event.{WindowEvent, WindowAdapter}

object Main extends App with Logging {
  val Array(auctionItem) = this.args
  var ui: MainWindow = null

  log.info("Main.main start")
  createAndWaitForMainWindow()
  joinAuction()
  log.info("Main.main end")

  def joinAuction() {
    log.info(s"start bidding $auctionItem")
    val connection = new XMPPConnection(CONNECTION_HOST)
    connection.connect()

    val itemUserName = auctionItemUserName(auctionItem)

    disconnectWhenUiClose(connection)
    connection.login(itemUserName, PASSWORD)
    log.info(s"creating chat $itemUserName@${connection.getServiceName}/$RESOURCE")

    val chat = connection.getChatManager.createChat(
      s"$itemUserName@${connection.getServiceName}/$RESOURCE",
      null)

    val auction = new XMPPAuction(chat)
    val auctionSniper = new AuctionSniper(auction, new SniperStatusDisplayer(ui))
    chat.addMessageListener(new AuctionMessageTranslator(auctionSniper))

    auction.join()
    log.info(s"created chat ${chat.getParticipant}")
  }

  private def createAndWaitForMainWindow() {
    log.info("Creating and waiting for main window")
    SwingUtilities.invokeAndWait(createRunnable {
      log.info("creating main window")
      ui = new MainWindow
      log.info("created main window")
    })
    log.info("The wait for the main window is over")
  }

  private def disconnectWhenUiClose(connection: XMPPConnection) = {
    ui.addWindowListener(new WindowAdapter() {
      override def windowClosed(e: WindowEvent) {
        connection.disconnect()
      }
    })
  }
}

class SniperStatusDisplayer(private val ui: MainWindow) extends SniperListener {
  def sniperLost() = {
    setStatus(MainWindow.STATUS_LOST)
  }

  def sniperBidding() = {
    setStatus(MainWindow.STATUS_BIDDING)
  }

  private def setStatus(statusText: String) = SwingUtilities.invokeLater(createRunnable {
    ui.showStatus(statusText)
  })
}

class XMPPAuction(private val chat: Chat) extends Auction {
  def join() = {
    chat.sendMessage("SOLVersion: 1.1; Event: JOIN;")
  }
  def bid(newPrice: Int) = {
    chat.sendMessage(s"SOLVersion: 1.1; Event: BID; Price: $newPrice;")
  }

}

class MainWindow extends JFrame("AuctionSniper") {
  var statusLabel : JLabel = null

  this setName MainWindow.WINDOW_NAME
  this setDefaultCloseOperation JFrame.EXIT_ON_CLOSE

  add({
    statusLabel = new JLabel()
    statusLabel setName MainWindow.SNIPER_STATUS_NAME
    statusLabel setBorder new LineBorder(Color.BLACK)
    statusLabel
  })
  showStatus(MainWindow.STATUS_JOINING)
  pack()
  setVisible(true)

  def showStatus(text: String) {
    statusLabel.setText(text)
  }
}

object MainWindow {
  val WINDOW_NAME = "Auction Sniper"
  val SNIPER_STATUS_NAME = "sniper status"

  val STATUS_LOST = "Lost"
  val STATUS_JOINING = "Joining"
  var STATUS_BIDDING = "Bidding"
}

