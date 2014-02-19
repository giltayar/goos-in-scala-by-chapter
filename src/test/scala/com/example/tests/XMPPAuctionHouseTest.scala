package com.example.tests

import org.specs2.mutable.{After, Specification}
import org.specs2.matcher.Scope
import com.example._
import com.example.tests.endtoend.FakeAuctionServer
import java.util.concurrent.{TimeUnit, CountDownLatch}
import com.example.xmpp.{AuctionEventListener, PriceSource, XMPPAuctionHouse}

class XMPPAuctionHouseTest extends Specification {
  trait Context extends Scope with After {
    val auctionHouse = XMPPAuctionHouse.connect(AUCTION_XMPP_HOST, "sniper", AUCTION_XMPP_USER_PASSWORD)
    val auctionServer = new FakeAuctionServer("1234")
    val auction = auctionHouse.joinAuction("sniper", Item(auctionServer.item, None))

    auctionServer.startSellingItem()

    def after = {
      auctionServer.stop()
      auctionHouse.disconnect()
    }
  }
  "XMPPAuction" should {
    "receive events from auction server after joining" in new Context {
      val auctionWasClosedCountdown = new CountDownLatch(1)

      auction.addAuctionEventListener(new AuctionEventListener {
        def auctionClosed() = {
          auctionWasClosedCountdown.countDown()
        }

        def currentPrice(price: Int, increment: Int, priceSource: PriceSource.Value) = ()
      })

      auction.join()
      auctionServer.hasReceivedJoinRequestFromSniper()
      auctionServer.announceClosed()

      auctionWasClosedCountdown.await(2, TimeUnit.SECONDS) must beTrue
    }
  }
}
