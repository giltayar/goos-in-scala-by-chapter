package com.example.tests

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.mock.Mockito
import com.example.{Auction, SniperListener, AuctionSniper}

class AuctionSniperTest extends Specification with Mockito {

  trait Context extends Scope {
    val mockAuction = mock[Auction]
    val mockSniperListener = mock[SniperListener]
    val auctionSniper = new AuctionSniper(mockAuction, mockSniperListener)
  }

  "AuctionSnipe" should {
    "fire event SniperLost when auction event close is fired" in new Context {

      auctionSniper.auctionClosed

      there was one(mockSniperListener).sniperLost()
    }

    "sends an incremented bid to the auction (and sends listener a bid event) " +
      "when it receives a price update" in new Context {
      val PRICE = 1001
      val INCREMENT = 25

      auctionSniper.currentPrice(PRICE, INCREMENT)

      there was one(mockAuction).bid(PRICE + INCREMENT)
      there was atLeast(1)(mockSniperListener).sniperBidding
    }
  }

}
