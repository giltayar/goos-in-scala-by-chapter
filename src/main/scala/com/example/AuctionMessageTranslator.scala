package com.example

import org.jivesoftware.smack.{Chat, MessageListener}
import org.jivesoftware.smack.packet.Message

case class AuctionMessageTranslator(private val listener: AuctionEventListener) extends MessageListener {

  def processMessage(chat: Chat, message: Message) = {
    val fields = packEventFrom(message.getBody)

    fields("Event") match {
      case "CLOSE" => listener.auctionClosed
      case "PRICE" => listener.currentPrice(fields("CurrentPrice").toInt, fields("Increment").toInt)
      case _ => throw new Exception("Invalid Event Message ${message.getBody}: Event unknown")

    }
  }

  private def packEventFrom(message: String) =
    message.split(";").map(_.trim).map(m => {val arr = m.split(":").map(_.trim); (arr(0), arr(1))}).toMap
}

trait AuctionEventListener {
  def currentPrice(price: Int, increment: Int)

  def auctionClosed()
}