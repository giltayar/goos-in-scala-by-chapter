package com.example


case class SniperSnapshot(itemId: String, lastPrice: Int, lastBid: Int, sniperState: SniperState.Value) {
  def closed() = SniperSnapshot(itemId, lastBid, lastBid,
    sniperState match {
      case SniperState.Winning  => SniperState.Won
      case SniperState.Joining | SniperState.Bidding => SniperState.Lost
      case _ => throw new Exception("Auction is already closed")
    }
  )

  def winning(newLastPrice: Int) = SniperSnapshot(itemId, newLastPrice, lastBid, SniperState.Winning)

  def bidding(newLastPrice: Int, newLastBid: Int) =
    SniperSnapshot(itemId, newLastPrice, newLastBid, SniperState.Bidding)
}

object SniperSnapshot
{
  def joining(itemId: String) = SniperSnapshot(itemId, 0, 0, SniperState.Joining)
}
