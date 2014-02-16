package com.example

import org.jivesoftware.smack.{Chat, XMPPConnection}
import org.jivesoftware.smack.packet.Message
import java.util.concurrent.{TimeUnit, ArrayBlockingQueue}
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo

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

  def reportPrice(price: Int, increment: Int, bidder: String) = {
    currentChat.sendMessage(s"SOLVersion: 1.1; Event: PRICE; CurrentPrice: $price; Bidder: $bidder")
  }

  def stop() {
    connection.disconnect()
    log.info("stopped connection")
  }

  def hasReceivedJoinRequestFromSniper() = {
    val message = messages.poll(5, TimeUnit.SECONDS)

    assertThat(message.getBody, equalTo(Main.getJoinXmppCommand))
  }

  def hasReceivedBid(bid: Int) = {
    val message = messages.poll(5, TimeUnit.SECONDS)

    assertThat(message.getBody, equalTo("asdfsadfasdf".format(bid)))
  }

  def announceClosed() {
    if (currentChat != null)
      currentChat.sendMessage("SOLVersion: 1.1; Event: CLOSE;")
  }
}
