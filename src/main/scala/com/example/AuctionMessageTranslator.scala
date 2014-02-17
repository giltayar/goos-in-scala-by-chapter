package com.example

import org.jivesoftware.smack.{Chat, MessageListener}
import org.jivesoftware.smack.packet.Message

object PriceSource extends Enumeration {
  type PriceSource = Value

  val FromSniper = Value
  val FromOtherBidder = Value
}

trait AuctionEventListener {
  def currentPrice(price: Int, increment: Int, priceSource: PriceSource.Value)

  def auctionClosed()
}

class AuctionMessageTranslator(private val sniperId: String,
                               private val listener: AuctionEventListener) extends MessageListener with Logging {

  def processMessage(chat: Chat, message: Message) = {
    val fields = packEventFrom(message.getBody)

    log.info(s"Processing message ${fields}} (${message.getBody})")

    fields("Event") match {
      case "CLOSE" => listener.auctionClosed
      case "PRICE" => listener.currentPrice(
        fields("CurrentPrice").toInt,
        fields("Increment").toInt,
        if (fields("Bidder") == sniperId) PriceSource.FromSniper else PriceSource.FromOtherBidder)
      case _ => ()

    }
  }

  private def packEventFrom(message: String) =
    message.split(";").map(_.trim).map(m => {val arr = m.split(":").map(_.trim); (arr(0), arr(1))}).toMap
}
