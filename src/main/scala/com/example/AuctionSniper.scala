package com.example

class AuctionSniper(private val itemId: String,
                    private val auction: Auction)
    extends AuctionEventListener {
  var snapshot = SniperSnapshot.joining(itemId)

  private var sniperListener: SniperListener = null

  def addSniperListener(sniperListener: SniperListener) = this.sniperListener = sniperListener

  def auctionClosed() = changeSnapshotTo(snapshot.closed())

  def currentPrice(price: Int, increment: Int, priceSource: PriceSource.Value) = priceSource match {
    case PriceSource.FromOtherBidder =>
      auction.bid(price + increment)
      changeSnapshotTo(snapshot.bidding(price, price + increment))
    case PriceSource.FromSniper =>
      changeSnapshotTo(snapshot.winning(price))
    case _ => throw new Exception(s"Bad priceSource $priceSource")
  }

  private def changeSnapshotTo(snapshot: SniperSnapshot) {
    this.snapshot = snapshot
    sniperListener.sniperStateChanged(snapshot)
  }

}

trait SniperListener {
  def sniperStateChanged(sniperState: SniperSnapshot)
}

object SniperState extends Enumeration {
  type SniperState = Value
  val Joining = Value
  val Bidding = Value
  val Winning = Value
  val Lost = Value
  val Won = Value
}
