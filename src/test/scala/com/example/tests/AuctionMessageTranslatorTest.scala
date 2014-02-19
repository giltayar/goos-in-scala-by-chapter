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

    def sendMessage(messageBody: String) {
      val message = new Message()
      message.setBody(messageBody)

      translator.processMessage(null, message)
    }
  }

  "AuctionMessageTranslator" should {
    "notify auction closed when close message received" in new Context {

      sendMessage("SOLVersion: 1.1; Event: CLOSE;")

      there was one(mockAuctionEventListener).auctionClosed()
    }
    "notify of bid when bid message received from other bidder" in new Context {
      sendMessage("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;")

      there was one(mockAuctionEventListener).currentPrice(192, 7, PriceSource.FromOtherBidder)
    }

    "notify of bid when bid message received from sniper" in new Context {
      sendMessage(s"SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: ${SNIPER_ID};")

      there was one(mockAuctionEventListener).currentPrice(192, 7, PriceSource.FromSniper)
    }
    "not throw exception if message is not a new price or a close" in new Context {
      sendMessage(s"SOLVersion: 1.1; Event: WHATEVER; CurrentPrice: 192; Increment: 7; Bidder: ${SNIPER_ID};")
    }

    "notify of failed auction when getting a bad message" in new Context {
      sendMessage("a bad message")

      there was one(mockAuctionEventListener).auctionFailed()
    }

    "notify of failed auction when some fields are missing" in new Context {
      sendMessage("a bad message")

      sendMessage(s"SOLVersion: 1.1; Event: WHATEVER; CurrentPrice: 192; Bidder: ${SNIPER_ID};")

      there was one(mockAuctionEventListener).auctionFailed()
    }
  }
}
