package com.example.xmpp

import org.jivesoftware.smack.XMPPConnection
import com.example.Item

class XMPPAuctionHouse(private val connection: XMPPConnection) {

  def joinAuction(sniperId: String, item: Item) = {
    new XMPPAuction(sniperId, connection, item)
  }

  def disconnect() = connection.disconnect()
}

object XMPPAuctionHouse {
  def connect(hostName: String, user: String, password: String) = {
    val connection = new XMPPConnection(hostName)

    connection.connect()
    connection.login(user, password)

    new XMPPAuctionHouse(connection)
  }
}
