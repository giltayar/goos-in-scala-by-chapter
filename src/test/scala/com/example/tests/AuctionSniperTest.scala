package com.example.tests

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import com.example.{PriceSource, Auction, SniperListener, AuctionSniper}

class AuctionSniperTest extends Specification with Mockito {

  trait Context extends Scope {
    val mockAuction = mock[Auction]
    val mockSniperListener = mock[SniperListener]
    val auctionSniper = new AuctionSniper(mockAuction, mockSniperListener)
  }

  "AuctionSnipe" should {
    "fire event SniperLost when auction event close is fired" in new Context {

      auctionSniper.auctionClosed()

      there was one(mockSniperListener).sniperLost()
    }

    "send an incremented bid to the auction (and sends listener a bid event) " +
      "when it receives a price update from other bidder" in new Context {
      val PRICE = 1001
      val INCREMENT = 25

      auctionSniper.currentPrice(PRICE, INCREMENT, PriceSource.FromOtherBidder)

      there was one(mockAuction).bid(PRICE + INCREMENT)
      there was atLeast(1)(mockSniperListener).sniperBidding()
    }
    "sends listener a winning event " +
      "when it receives a price update from sniper" in new Context {
      val PRICE = 1001
      val INCREMENT = 25

      auctionSniper.currentPrice(PRICE, INCREMENT, PriceSource.FromSniper)

      there was atLeast(1)(mockSniperListener).sniperWinning()
    }

    "report lost if auction closes immediately" in new Context {
      auctionSniper.auctionClosed()

      there was atLeast(1)(mockSniperListener).sniperLost()
    }

    "reports lost if auction closes after bidding from other bidder" in new Context {
      val PRICE = 1001
      val INCREMENT = 25

      auctionSniper.currentPrice(PRICE, INCREMENT, PriceSource.FromOtherBidder)
      auctionSniper.auctionClosed()

      there was atLeastOne(mockSniperListener).sniperBidding() andThen
        atLeastOne(mockSniperListener).sniperLost()
    }

    "reports won if auction closes after bidding from sniper" in new Context {
      val PRICE = 1001
      val INCREMENT = 25

      auctionSniper.currentPrice(PRICE, INCREMENT, PriceSource.FromSniper)
      auctionSniper.auctionClosed()

      there was atLeastOne(mockSniperListener).sniperWinning() andThen
        atLeastOne(mockSniperListener).sniperWon()
    }
  }

}
