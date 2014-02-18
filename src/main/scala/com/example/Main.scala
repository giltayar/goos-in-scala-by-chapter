package com.example

import javax.swing._
import org.jivesoftware.smack.{Chat, XMPPConnection}
import java.awt.event.{WindowEvent, WindowAdapter}

object Main extends App with Logging {
  private val sniperId = this.args(0)
  private val auctionItems = this.args drop 1
  private val snipersModel = new SnipersTableModel()
  private var ui: MainWindow = null
  private val connection = createConnection()

  createAndWaitForMainWindow()

  auctionItems.foreach(joinAuction(_))

  disconnectWhenUiClose(connection)

  private def createAndWaitForMainWindow() {
    SwingUtilities.invokeAndWait(createRunnable {
      ui = new MainWindow(snipersModel, new UserRequestListener {
        def joinAuction(itemId: String) = {
          Main.joinAuction(itemId)
        }
      })
    })
  }

  private def createConnection() = {
    val connection = new XMPPConnection(AUCTION_XMPP_HOST)
    connection.connect()

    connection.login(sniperId, AUCTION_XMPP_USER_PASSWORD)

    connection
  }

  private def joinAuction(auctionItem: String) {
    snipersModel.addSniper(SniperSnapshot.joining(auctionItem))

    val chat = connection.getChatManager.createChat(
      s"${auctionItemUserName(auctionItem)}@${connection.getServiceName}/$AUCTION_XMPP_RESOURCE",
      null)

    val auction = new XMPPAuction(chat)
    val auctionSniper = new AuctionSniper(auctionItem, auction, new SwingThreadSniperListener(snipersModel))

    chat.addMessageListener(new AuctionMessageTranslator(sniperId, auctionSniper))

    auction.join()
  }

  private def disconnectWhenUiClose(connection: XMPPConnection) = {
    ui.addWindowListener(new WindowAdapter() {
      override def windowClosed(e: WindowEvent) {
        connection.disconnect()
      }
    })
  }
}

private class SwingThreadSniperListener(private val listener: SniperListener) extends SniperListener {
  def sniperStateChanged(sniperSnapshot: SniperSnapshot) = {
    SwingUtilities.invokeLater(createRunnable {
      listener.sniperStateChanged(sniperSnapshot)
    })
  }
}

private class XMPPAuction(private val chat: Chat) extends Auction {
  def join() = {
    chat.sendMessage("SOLVersion: 1.1; Event: JOIN;")
  }
  def bid(newPrice: Int) = {
    chat.sendMessage(s"SOLVersion: 1.1; Event: BID; Price: $newPrice;")
  }
}

