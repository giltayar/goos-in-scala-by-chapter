package com.example.ui

import javax.swing._
import java.awt.{BorderLayout, Container, FlowLayout}
import java.util.EventListener
import java.awt.event.{ActionEvent, ActionListener}
import com.example.{Item, SniperPortfolio}

private[example] class MainWindow(private val sniperPortfolio: SniperPortfolio)
    extends JFrame(MainWindow.WINDOW_NAME) {
  var userRequestListener: UserRequestListener = null

  val snipersTableModel = new SnipersTableModel()

  sniperPortfolio.addPortfolioListener(snipersTableModel)

  this setName MainWindow.WINDOW_NAME
  this setDefaultCloseOperation JFrame.EXIT_ON_CLOSE

  fillContentPane(makeSnipersTable(), makeControls())
  pack()

  setVisible(true)

  def addUserRequestListener(userRequestListener: UserRequestListener) =
    this.userRequestListener = userRequestListener

  private def makeSnipersTable() = {
    val snipersTable = new JTable(snipersTableModel)

    snipersTable.setName(MainWindow.SNIPERS_TABLE_NAME)

    snipersTable
  }

  private def makeControls() = {
    val controls = new JPanel(new FlowLayout())
    val itemIdField = new JTextField()
    itemIdField.setColumns(25)
    itemIdField.setName(MainWindow.NEW_ITEM_ID_NAME)
    controls.add(itemIdField)

    val stopPriceField = new JTextField()
    stopPriceField.setColumns(9)
    stopPriceField.setName(MainWindow.STOP_PRICE_NAME)
    controls.add(stopPriceField)


    val joinAuctionButton = new JButton("Join Auction")
    joinAuctionButton.setName(MainWindow.JOIN_BUTTON_NAME)
    joinAuctionButton.addActionListener(new ActionListener(){
      def actionPerformed(e: ActionEvent) = {
        userRequestListener.joinAuction(Item(itemIdField.getText,
          if (!stopPriceField.getText.trim.isEmpty) Some(stopPriceField.getText.toInt) else None))
      }
    })
    controls.add(joinAuctionButton)

    controls
  }

  private def fillContentPane(table: JTable, controlPanel: JPanel) = {
    val contentPane: Container = getContentPane

    contentPane.setLayout(new BorderLayout)

    contentPane.add(controlPanel, BorderLayout.PAGE_START)
    contentPane.add(new JScrollPane(table), BorderLayout.CENTER)
  }
}

private[example] object MainWindow {
  val JOIN_BUTTON_NAME = "JoinButton"

  val NEW_ITEM_ID_NAME = "NewItem"
  val STOP_PRICE_NAME = "StopPrice"

  val WINDOW_NAME = "Auction Sniper"
  val SNIPERS_TABLE_NAME = "SnipersTable"
}

trait UserRequestListener extends EventListener {
  def joinAuction(item: Item)
}
