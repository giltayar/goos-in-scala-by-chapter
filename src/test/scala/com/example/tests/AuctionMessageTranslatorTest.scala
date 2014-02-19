package com.example.tests

import org.specs2.mutable.Specification
import org.jivesoftware.smack.packet.Message
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import com.example.xmpp.{AuctionEventListener, PriceSource, AuctionMessageTranslator}

class AuctionMessageTranslatorTest extends Specification with Mockito {

  private val SNIPER_ID = "sniper"

  trait Context extends Scope {
    val mockAuctionEventListener = mock[AuctionEventListener]
    val translator = new AuctionMessageTranslator(SNIPER_ID, mockAuctionEventListener)
  }

  "AuctionMessageTranslator" should {
    "notify auction closed when close message received" in new Context {

      val message = new Message()
      message.setBody("SOLVersion: 1.1; Event: CLOSE;")

      translator.processMessage(null, message)

      there was one(mockAuctionEventListener).auctionClosed()
    }
    "notify of bid when bid message received from other bidder" in new Context {
      val message = new Message()

      message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;")

      translator.processMessage(null, message)

      there was one(mockAuctionEventListener).currentPrice(192, 7, PriceSource.FromOtherBidder)
    }

    "notify of bid when bid message received from sniper" in new Context {
      val message = new Message()

      message.setBody(s"SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: ${SNIPER_ID};")

      translator.processMessage(null, message)

      there was one(mockAuctionEventListener).currentPrice(192, 7, PriceSource.FromSniper)
    }
    "not throw exception if message is not a new price or a close" in new Context {
      val message = new Message()

      message.setBody(s"SOLVersion: 1.1; Event: WHATEVER; CurrentPrice: 192; Increment: 7; Bidder: ${SNIPER_ID};")

      translator.processMessage(null, message)
    }
  }
}
