package com.example.xmpp

import org.jivesoftware.smack.XMPPConnection
import com.example._


private[example] class XMPPAuction(private val sniperId: String,
                                   private val connection: XMPPConnection,
                                   auctionItem: Item) extends Auction {
  private val auctionEventListeners = Announcer.to(classOf[AuctionEventListener])

  private val chat = connection.getChatManager.createChat(
    s"${auctionItemUserName(auctionItem.id)}@${connection.getServiceName}/$AUCTION_XMPP_RESOURCE",
    null)

  listenToChat

  private def listenToChat {
    val translator = new AuctionMessageTranslator(sniperId, auctionEventListeners.announce())

    chat.addMessageListener(translator)

    def disconnectOnFailListener =
      new AuctionEventListener {
        def auctionClosed() = ()

        def currentPrice(price: Int, increment: Int, priceSource: PriceSource.Value) = ()

        def auctionFailed() = chat.removeMessageListener(translator)
      }

    auctionEventListeners.addListener(disconnectOnFailListener)
  }

  def addAuctionEventListener(listener: AuctionEventListener) = {
    auctionEventListeners.addListener(listener)
  }

  def join() = {
    chat.sendMessage("SOLVersion: 1.1; Event: JOIN;")
  }

  def bid(newPrice: Int) = {
    chat.sendMessage(s"SOLVersion: 1.1; Event: BID; Price: $newPrice;")
  }
}
