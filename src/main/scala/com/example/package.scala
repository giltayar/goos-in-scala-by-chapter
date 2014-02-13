package com

import org.jivesoftware.smack.{MessageListener, ChatManagerListener, Chat}
import org.jivesoftware.smack.packet.Message

package object example {
  val PASSWORD = "auction"
  val RESOURCE = "Auction"
  val CONNECTION_HOST = "localhost"

  private[example] def createRunnable(f: => Unit) = new Runnable {
    override def run(): Unit = f
  }

  private[example] def createChatListener(f: (Chat, Boolean) => Unit) = {
    new ChatManagerListener {
      def chatCreated(c: Chat, b: Boolean) = f(c, b)
    }
  }

  private[example] def createMessageListener(f: (Chat, Message) => Unit) = new MessageListener {
    def processMessage(c: Chat, m: Message) = f(c, m)
  }

  def auctionItemUserName(item: String): String = {
    s"auction-$item"
  }

}
