package com.example

import com.example.xmpp.XMPPAuctionHouse
import com.example.ui.UserRequestListener

class SniperLauncher(private val sniperId: String,
                     private val sniperCollector: SniperCollector,
                     private val auctionHouse: XMPPAuctionHouse) extends UserRequestListener {

  def joinAuction(item: Item) = {
    val auction = auctionHouse.joinAuction(sniperId, item)
    val auctionSniper = new AuctionSniper(item, auction)

    auction.addAuctionEventListener(auctionSniper)

    sniperCollector.addSniper(auctionSniper)

    auction.join()
  }
}
