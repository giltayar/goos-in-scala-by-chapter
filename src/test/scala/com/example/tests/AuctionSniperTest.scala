package com.example.tests

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import com.example._
import org.specs2.matcher.Matcher
import com.example.SniperState.SniperState
import com.example.SniperState

class AuctionSniperTest extends Specification with Mockito {

  trait Context extends Scope {
    val AN_ITEM_ID = "anItemId"
    val mockAuction = mock[Auction]
    val mockSniperListener = mock[SniperListener]
    val auctionSniper = new AuctionSniper(AN_ITEM_ID, mockAuction, mockSniperListener)
  }

  def isInState(state: SniperState) : Matcher[SniperSnapshot] =
    ((_:SniperSnapshot).sniperState == state, s"isn't in state $state")

  "AuctionSnipe" should {
    "fire event SniperLost when auction event close is fired" in new Context {

      auctionSniper.auctionClosed()

      there was one(mockSniperListener).sniperStateChanged(isInState(SniperState.Lost))
    }

    "send an incremented bid to the auction (and sends listener a bid event) " +
      "when it receives a price update from other bidder" in new Context {
      val PRICE = 1001
      val INCREMENT = 25

      auctionSniper.currentPrice(PRICE, INCREMENT, PriceSource.FromOtherBidder)

      there was one(mockAuction).bid(PRICE + INCREMENT)
      there was atLeast(1)(mockSniperListener).sniperStateChanged(
        SniperSnapshot(AN_ITEM_ID, PRICE, PRICE + INCREMENT, SniperState.Bidding))
    }
    "sends listener a winning event " +
      "when it receives a price update from sniper" in new Context {
      val PRICE = 1001
      val INCREMENT = 25

      auctionSniper.currentPrice(PRICE, INCREMENT, PriceSource.FromOtherBidder)
      auctionSniper.currentPrice(PRICE + INCREMENT, INCREMENT, PriceSource.FromSniper)

      there was atLeast(1)(mockSniperListener).sniperStateChanged(
          SniperSnapshot(AN_ITEM_ID, 1026, 1026, SniperState.Winning))
    }

    "report lost if auction closes immediately" in new Context {
      auctionSniper.auctionClosed()

      there was one(mockSniperListener).sniperStateChanged(isInState(SniperState.Lost))
    }

    "reports lost if auction closes after bidding from other bidder" in new Context {
      val PRICE = 1001
      val INCREMENT = 25

      auctionSniper.currentPrice(PRICE, INCREMENT, PriceSource.FromOtherBidder)
      auctionSniper.auctionClosed()

      there was atLeastOne(mockSniperListener).sniperStateChanged(isInState(SniperState.Bidding)) andThen
        one(mockSniperListener).sniperStateChanged(isInState(SniperState.Lost))
    }

    "reports won if auction closes after bidding from sniper" in new Context {
      val PRICE = 1001
      val INCREMENT = 25

      auctionSniper.currentPrice(PRICE, INCREMENT, PriceSource.FromSniper)
      auctionSniper.auctionClosed()

      there was atLeastOne(mockSniperListener).sniperStateChanged(
          SniperSnapshot(AN_ITEM_ID, PRICE, 0, SniperState.Winning)) andThen
        one(mockSniperListener).sniperStateChanged(isInState(SniperState.Won))
    }
  }

}
