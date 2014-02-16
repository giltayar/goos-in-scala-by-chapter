package com.example

class AuctionSniper(private val auction: Auction, private val sniperListener: SniperListener)
    extends AuctionEventListener {

  def auctionClosed() = {
    sniperListener.sniperLost
  }

  def currentPrice(price: Int, increment: Int) = {
    auction.bid(price + increment)
    sniperListener.sniperBidding()
  }
}

trait SniperListener {
  def sniperLost()

  def sniperBidding()
}
