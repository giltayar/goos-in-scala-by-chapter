package com.example

import org.specs2.mutable.{After, Specification}
import org.specs2.specification.Scope


class AuctionSniperEndToEndTest extends Specification {

  trait RunnerAndAuctionServer extends Scope with After {
    val application = new ApplicationRunner()
    val auction = new FakeAuctionServer("1234")

    def after = {
      auction.stop()
      application.stop()
    }
  }
  
  "The auction sniper" should {
    "receive join request from sniper and show sniper has lost auction" in new RunnerAndAuctionServer {
      auction.startSellingItem()
      application.startBiddingIn(auction)
      auction.hasReceivedJoinRequestFromSniper must beTrue
      auction.announceClosed()
      application.showsSniperHasLostAuction()
    }
  }
}

