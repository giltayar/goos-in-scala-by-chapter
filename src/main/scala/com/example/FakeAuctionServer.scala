package com.example

import org.jivesoftware.smack.{MessageListener, Chat, XMPPConnection}
import org.jivesoftware.smack.packet.Message
import scala.collection.mutable.ArrayBuffer
import java.util.concurrent.{TimeUnit, ArrayBlockingQueue}

class FakeAuctionServer(val item: String) extends Logging {

  private val connection = new XMPPConnection(CONNECTION_HOST)

  private var currentChat: Chat = null
  private val messages = new ArrayBlockingQueue[Message](1)

  def startSellingItem() {
    log.info(s"start Selling Item $item")

    connection.connect()
    connection.login(auctionItemUserName(item), PASSWORD, RESOURCE)
    connection.getChatManager.addChatListener(createChatListener { (chat, b) =>
      log.info("got a bid")
      currentChat = chat
      currentChat.addMessageListener(createMessageListener { (chat, message) =>
        messages.add(message)
      })
    })
    log.info("Added chat listener")
  }

  def stop() {
    connection.disconnect()
    log.info("stopped connection")
  }

  def hasReceivedJoinRequestFromSniper() = {
    log.info("polling for 10 seconds to wait for a message")
    val res = messages.poll(10, TimeUnit.SECONDS) != null
    log.info("finished polling")

    res
  }

  def announceClosed() {
    if (currentChat != null)
      currentChat.sendMessage(new Message())
  }
}
