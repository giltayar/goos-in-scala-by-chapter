package com.example

import javax.swing._
import java.awt.BorderLayout
import org.jivesoftware.smack.{Chat, XMPPConnection}
import java.awt.event.{WindowEvent, WindowAdapter}
import javax.swing.table.AbstractTableModel

object Main extends App with Logging {
  private val Array(auctionItem, sniperId) = this.args
  private val snipersModel = new SnipersTableModel()
  private var ui: MainWindow = null
  private val connection = createConnection()

  createAndWaitForMainWindow()
  joinAuction(connection)
  disconnectWhenUiClose(connection)

  private def createAndWaitForMainWindow() {
    SwingUtilities.invokeAndWait(createRunnable {
      ui = new MainWindow(snipersModel)
    })
  }

  private def createConnection() = {
    val connection = new XMPPConnection(CONNECTION_HOST)
    connection.connect()

    connection.login(auctionItemUserName(auctionItem), PASSWORD)

    connection
  }

  private def joinAuction(connection: XMPPConnection) {

    val chat = connection.getChatManager.createChat(
      s"${auctionItemUserName(auctionItem)}@${connection.getServiceName}/$RESOURCE",
      null)

    val auction = new XMPPAuction(chat)
    val auctionSniper = new AuctionSniper(auctionItem, auction, new SwingThreadSniperListener(snipersModel))

    chat.addMessageListener(new AuctionMessageTranslator(sniperId, auctionSniper))

    auction.join()
  }

  private def disconnectWhenUiClose(connection: XMPPConnection) = {
    ui.addWindowListener(new WindowAdapter() {
      override def windowClosed(e: WindowEvent) {
        connection.disconnect()
      }
    })
  }
}

private class SwingThreadSniperListener(private val listener: SniperListener) extends SniperListener {
  def sniperStateChanged(sniperSnapshot: SniperSnapshot) = {
    SwingUtilities.invokeLater(createRunnable {
      listener.sniperStateChanged(sniperSnapshot)
    })
  }
}

private class XMPPAuction(private val chat: Chat) extends Auction {
  def join() = {
    chat.sendMessage("SOLVersion: 1.1; Event: JOIN;")
  }
  def bid(newPrice: Int) = {
    chat.sendMessage(s"SOLVersion: 1.1; Event: BID; Price: $newPrice;")
  }
}

private class MainWindow(snipersTableModel: SnipersTableModel) extends JFrame(MainWindow.WINDOW_NAME) {
  this setName MainWindow.WINDOW_NAME
  this setDefaultCloseOperation JFrame.EXIT_ON_CLOSE

  fillContentPane(makeSnipersTable())
  pack()

  snipersTableModel.sniperStateChanged(SniperSnapshot("", 0, 0, SniperState.Joining))
  setVisible(true)

  def makeSnipersTable() = {
    val snipersTable = new JTable(snipersTableModel)

    snipersTable.setName(MainWindow.SNIPERS_TABLE_NAME)

    snipersTable
  }

  def fillContentPane(table: JTable) = {
    val contentPane = getContentPane

    contentPane.setLayout(new BorderLayout())

    contentPane.add(new JScrollPane(table), BorderLayout.CENTER)
  }
}

object MainWindow {
  val WINDOW_NAME = "Auction Sniper"
  val SNIPERS_TABLE_NAME = "SnipersTable"

  val STATUS_JOINING = "Joining"
  var STATUS_BIDDING = "Bidding"
  val STATUS_WINNING = "Winning"
  val STATUS_LOST = "Lost"
  val STATUS_WON = "Won"
}

private class SnipersTableModel extends AbstractTableModel with SniperListener {
  var sniperState : SniperSnapshot = null

  def getColumnCount = SnipersTableModel.Column.values.size
  def getRowCount = 1

  def getValueAt(rowIndex: Int, columnIndex: Int) = {
    SnipersTableModel.Column(columnIndex) match {
      case SnipersTableModel.Column.ItemIdentifier => sniperState.itemId
      case SnipersTableModel.Column.LastPrice => sniperState.lastPrice.toString
      case SnipersTableModel.Column.LastBid => sniperState.lastBid.toString
      case SnipersTableModel.Column.SniperStatus => SnipersTableModel.SniperStateToText(sniperState.sniperState)
    }
  }

  override def getColumnName(column: Int) = SnipersTableModel.ColumnTitles(SnipersTableModel.Column(column))

  def sniperStateChanged(sniperState: SniperSnapshot) = {
    this.sniperState = sniperState
    fireTableRowsUpdated(0, 0)
  }
}

private object SnipersTableModel {
  object Column extends Enumeration {
    val ItemIdentifier = Value
    val LastPrice = Value
    val LastBid = Value
    val SniperStatus = Value
  }

  val ColumnTitles = Map(
    Column.ItemIdentifier -> "Item",
    Column.LastPrice -> "Last Price",
    Column.LastBid -> "Last Bid",
    Column.SniperStatus -> "State"
  )

  val SniperStateToText = Map(
    SniperState.Bidding -> "Bidding",
    SniperState.Joining -> "Joining",
    SniperState.Winning -> "Winning",
    SniperState.Lost -> "Lost",
    SniperState.Won -> "Won"
  )
}
