package com.example

import org.specs2.Specification
import org.specs2.specification.Step
import org.specs2.matcher.Hamcrest

class AuctionSniperEndToEndTest extends Specification with Logging with Hamcrest {
  val application = new ApplicationRunner()
  val auction = new FakeAuctionServer("1234")

  def is = s2"""
  $sequential
  When an auction is selling an item,
   And an auction sniper has started to bid in that auction,
   Then the auction will receive a join request from sniper $e1
  And when the auction announces that it is closed,
   Then the application will show that the sniper has lost auction $e2
  ${Step(auction.stop())}
  ${Step(application.stop())}
  """
  def e1 = {
    log.info("e1 start")
    auction.startSellingItem()
    application.startBiddingIn(auction)
    val res = auction.hasReceivedJoinRequestFromSniper must beTrue
    log.info("e1 done")
    res
  }
  def e2 = {
    log.info("e2 start")
    auction.announceClosed()
    application.showsSniperHasLostAuction()
    log.info("e2 end")
    success
  }
}
