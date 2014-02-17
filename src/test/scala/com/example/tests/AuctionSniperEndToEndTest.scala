package com.example.tests

import org.specs2.mutable.{After, Specification}
import org.specs2.specification.Scope
import com.example.tests.endtoend.{FakeAuctionServer, ApplicationRunner}
import com.example.Logging


class AuctionSniperEndToEndTest extends Specification with Logging {

  trait RunnerAndAuctionServer extends Scope with After {
    val application = new ApplicationRunner()
    val auction = new FakeAuctionServer("1234")

    def after = {
      auction.stop()
      application.stop()
    }

    def joinAuction() = {
      auction.startSellingItem()
      application.startBiddingIn(auction)
      auction.hasReceivedJoinRequestFromSniper()
    }
  }

  "The auction sniper" should {
    sequential

    "receive join request from sniper and show sniper has lost auction" in new RunnerAndAuctionServer {
      joinAuction()

      auction.announceClosed()
      application.showsSniperHasLostAuction()
    }

    "join, bid, and lose" in new RunnerAndAuctionServer {
      joinAuction()

      auction.reportPrice(1000, 98, "other bidder")
      application.hasShownSniperIsBidding()

      auction.hasReceivedBid(1098)

      auction.announceClosed()
      application.showsSniperHasLostAuction()
    }

    "join, bid, and win" in new RunnerAndAuctionServer {
      joinAuction()

      auction.reportPrice(1000, 98, "other bidder")
      application.hasShownSniperIsBidding()

      auction.hasReceivedBid(1098)

      auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_ID)
      application.hasShownSniperIsWinning()

      auction.announceClosed()
      application.showsSniperHasWonAuction
    }
  }
}

