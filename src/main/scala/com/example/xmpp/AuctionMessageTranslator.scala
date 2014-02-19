package com.example.xmpp

import org.jivesoftware.smack.{Chat, MessageListener}
import org.jivesoftware.smack.packet.Message
import java.util.EventListener
import com.example.Logging

object PriceSource extends Enumeration {
  type PriceSource = Value

  val FromSniper = Value
  val FromOtherBidder = Value
}

trait AuctionEventListener extends EventListener {
  def currentPrice(price: Int, increment: Int, priceSource: PriceSource.Value)

  def auctionClosed()

  def auctionFailed()
}

class AuctionMessageTranslator(private val sniperId: String,
                               private val listener: AuctionEventListener) extends MessageListener with Logging {

  def processMessage(chat: Chat, message: Message) = {
    try {
      val fields = packEventFrom(message.getBody)

      log.info(s"Got message ${message.getBody}, and I am $sniperId")

      fields("Event") match {
        case "CLOSE" => listener.auctionClosed()
        case "PRICE" => listener.currentPrice(
          fields("CurrentPrice").toInt,
          fields("Increment").toInt,
          if (fields("Bidder") == sniperId) PriceSource.FromSniper else PriceSource.FromOtherBidder)
        case _ => ()

      }
    }
    catch {
      case e: Exception =>
        log.info(s"Exception when processing ${message.getBody}: e=$e")
        listener.auctionFailed()
    }
  }

  private def packEventFrom(message: String) =
    message.split(";").map(_.trim).map(m => {val arr = m.split(":").map(_.trim); (arr(0), arr(1))}).toMap
}
