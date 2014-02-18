package com.example

class SniperLauncher(private val sniperId: String,
                     private val sniperCollector: SniperCollector,
                     private val auctionHouse: XMPPAuctionHouse) extends UserRequestListener {

  def joinAuction(itemId: String) = {
    val auction = auctionHouse.joinAuction(sniperId, itemId)
    val auctionSniper = new AuctionSniper(itemId, auction)

    auction.addAuctionEventListener(auctionSniper)

    sniperCollector.addSniper(auctionSniper)

    auction.join()
  }
}
