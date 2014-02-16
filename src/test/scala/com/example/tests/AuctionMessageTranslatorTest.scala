package com.example.tests

import org.specs2.mutable.Specification
import org.jivesoftware.smack.packet.Message
import org.specs2.mock.Mockito
import com.example.{AuctionMessageTranslator, AuctionEventListener}
import org.specs2.specification.Scope

class AuctionMessageTranslatorTest extends Specification with Mockito {

  trait Context extends Scope {
    val mockAuctionEventListener = mock[AuctionEventListener]
    val translator = new AuctionMessageTranslator(mockAuctionEventListener)
  }

  "AuctionMessageTranslator" should {
    "notify auction closed when close message received" in new Context {

      val message = new Message()
      message.setBody("SOLVersion: 1.1; Event: CLOSE;")

      translator.processMessage(null, message)

      there was one(mockAuctionEventListener).auctionClosed()
    }
    "notify of bid when bid message received" in new Context {
      val message = new Message()

      message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;")

      translator.processMessage(null, message)

      there was one(mockAuctionEventListener).currentPrice(192, 7)
    }
  }
}
