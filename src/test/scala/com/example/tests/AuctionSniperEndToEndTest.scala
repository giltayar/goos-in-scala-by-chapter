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

    def joinAuction(stopPrice: Option[Int] = None) = {
      auction.startSellingItem()
      stopPrice match {
        case Some(x) => application.startBiddingWithStopPrice(x, auction)
        case None => application.startBiddingIn(auction)
      }

      auction.hasReceivedJoinRequestFromSniper()
    }
  }

  trait RunnerAndTwoAuctionServers extends RunnerAndAuctionServer {
    val auction2 = new FakeAuctionServer("4567")

    override def after = {
      auction2.stop()
      super.after
    }

    override def joinAuction(stopPrice: Option[Int] = None) = {
      auction.startSellingItem()
      auction2.startSellingItem()
      stopPrice match {
        case Some(x) =>
          application.startBiddingWithStopPrice(x, auction, auction2)
        case None =>
          application.startBiddingIn(auction, auction2)
      }
      auction.hasReceivedJoinRequestFromSniper()
      auction2.hasReceivedJoinRequestFromSniper()
    }
  }

  "The auction sniper" should {
    sequential

    "receive join request from sniper and show sniper has lost auction" in new RunnerAndAuctionServer {
      joinAuction()

      auction.announceClosed()
      application.showsSniperHasLostAuction(auction, 0, 0)
    }

    "join, bid, and lose" in new RunnerAndAuctionServer {
      joinAuction()

      auction.reportPrice(1000, 98, "other bidder")
      application.hasShownSniperIsBidding(auction, 1000, 1098)

      auction.hasReceivedBid(1098)

      auction.announceClosed()
      application.showsSniperHasLostAuction(auction, 1000, 1098)
    }

    "join, bid, and win" in new RunnerAndAuctionServer {
      joinAuction()

      auction.reportPrice(1000, 98, "other bidder")
      application.hasShownSniperIsBidding(auction, 1000, 1098)

      auction.hasReceivedBid(1098)

      auction.reportPrice(1098, 2, ApplicationRunner.SNIPER_ID)
      application.hasShownSniperIsWinning(auction, 1098)

      auction.announceClosed()
      application.showsSniperHasWonAuction(auction, 1098)
    }

    "bid for multiple items" in new RunnerAndTwoAuctionServers {
      joinAuction()

      auction.reportPrice(1000, 98, "other bidder")
      auction.hasReceivedBid(1098)
      application.hasShownSniperIsBidding(auction, 1000, 1098)

      auction2.reportPrice(500, 21, "other bidder")
      auction2.hasReceivedBid(521)
      application.hasShownSniperIsBidding(auction2, 500, 521)

      auction.reportPrice(1098, 97, ApplicationRunner.SNIPER_ID)
      auction2.reportPrice(521, 22, ApplicationRunner.SNIPER_ID)

      application.hasShownSniperIsWinning(auction, 1098)
      application.hasShownSniperIsWinning(auction2, 521)

      auction.announceClosed()
      auction2.announceClosed()

      application.showsSniperHasWonAuction(auction, 1098)
      application.showsSniperHasWonAuction(auction, 1098)
    }

    "lose auction when the price is too high" in new RunnerAndAuctionServer {
      joinAuction(Some(1100))

      auction.reportPrice(1000, 98, "other bidder")
      auction.hasReceivedBid(1098)
      application.hasShownSniperIsBidding(auction, 1000, 1098)

      auction.reportPrice(1197, 10, "third party")
      application.hasShownSniperIsLosing(auction, 1197, 1098)

      auction.reportPrice(1207, 10, "fourth party")
      application.hasShownSniperIsLosing(auction, 1207, 1098)

      auction.announceClosed()
      application.showsSniperHasLostAuction(auction, 1207, 1098)
    }
  }
}

