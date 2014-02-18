package com

import org.jivesoftware.smack.{MessageListener, ChatManagerListener, Chat}
import org.jivesoftware.smack.packet.Message

package object example {
  val AUCTION_XMPP_USER_PASSWORD = "auction"
  val AUCTION_XMPP_RESOURCE = "Auction"
  val AUCTION_XMPP_HOST = "localhost"

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
