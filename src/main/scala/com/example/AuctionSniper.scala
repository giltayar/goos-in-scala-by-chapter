package com.example

class AuctionSniper(private val auction: Auction, private val sniperListener: SniperListener)
    extends AuctionEventListener {
  var isWinning = false

  def auctionClosed() = {
    if (isWinning)
      sniperListener.sniperWon()
    else
      sniperListener.sniperLost()
  }

  def currentPrice(price: Int, increment: Int, priceSource: PriceSource.Value) = {
    isWinning = priceSource == PriceSource.FromSniper

    priceSource match {
      case PriceSource.FromOtherBidder =>
        auction.bid(price + increment)
        sniperListener.sniperBidding()
      case PriceSource.FromSniper =>
        sniperListener.sniperWinning()
      case _ => throw new Exception(s"Bad priceSource $priceSource")
    }
  }
}

trait SniperListener {
  def sniperBidding()
  def sniperWinning()

  def sniperLost()
  def sniperWon()
}
