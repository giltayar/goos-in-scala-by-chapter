package com.example

import com.example.xmpp.{AuctionEventListener, PriceSource}

class AuctionSniper(private val item: Item,
                    private val auction: Auction)
    extends AuctionEventListener {
  var snapshot = SniperSnapshot.joining(item.id)
  val stopPrice = item.stopPrice

  private var sniperListener: SniperListener = null

  def addSniperListener(sniperListener: SniperListener) = this.sniperListener = sniperListener

  def auctionClosed() = changeSnapshotTo(snapshot.closed())

  def currentPrice(price: Int, increment: Int, priceSource: PriceSource.Value) = priceSource match {
    case PriceSource.FromOtherBidder =>
      val bid = price + increment
      if (item.allowsBid(bid)) {
        changeSnapshotTo(snapshot.bidding(price, bid))
        auction.bid(bid)
      }
      else
        changeSnapshotTo(snapshot.losing(price))
    case PriceSource.FromSniper =>
      changeSnapshotTo(snapshot.winning(price))
    case _ => throw new Exception(s"Bad priceSource $priceSource")
  }

  private def changeSnapshotTo(snapshot: SniperSnapshot) {
    this.snapshot = snapshot
    sniperListener.sniperStateChanged(snapshot)
  }

  def auctionFailed() = {
    changeSnapshotTo(snapshot.failed())
  }
}

trait SniperListener {
  def sniperStateChanged(sniperState: SniperSnapshot)
}

object SniperState extends Enumeration {
  val Joining = Value

  val Bidding = Value
  val Winning = Value
  val Losing = Value

  val Lost = Value
  val Won = Value

  val Failed = Value
}
