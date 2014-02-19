package com.example


case class SniperSnapshot(itemId: String, lastPrice: Int, lastBid: Int, sniperState: SniperState.Value) {
  def losing(price: Int): SniperSnapshot = SniperSnapshot(itemId, price, lastBid, SniperState.Losing)

  def closed() = SniperSnapshot(itemId, lastPrice, lastBid,
    sniperState match {
      case SniperState.Winning  => SniperState.Won
      case SniperState.Joining | SniperState.Bidding | SniperState.Losing => SniperState.Lost
      case _ => throw new Exception("Auction is already closed")
    }
  )

  def winning(newLastPrice: Int) = SniperSnapshot(itemId, newLastPrice, lastBid, SniperState.Winning)

  def bidding(newLastPrice: Int, newLastBid: Int) =
    SniperSnapshot(itemId, newLastPrice, newLastBid, SniperState.Bidding)

  def failed() = SniperSnapshot(itemId, 0, 0, SniperState.Failed)
}

object SniperSnapshot {
  def joining(itemId: String) = SniperSnapshot(itemId, 0, 0, SniperState.Joining)
}
