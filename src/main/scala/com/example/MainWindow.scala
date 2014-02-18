package com.example

import javax.swing._
import java.awt.{BorderLayout, Container, FlowLayout}
import java.util.EventListener
import java.awt.event.{ActionEvent, ActionListener}

private[example] class MainWindow(private val snipersTableModel: SnipersTableModel,
                                  private val userRequestListener: UserRequestListener)
    extends JFrame(MainWindow.WINDOW_NAME) {
  this setName MainWindow.WINDOW_NAME
  this setDefaultCloseOperation JFrame.EXIT_ON_CLOSE

  fillContentPane(makeSnipersTable(), makeControls())
  pack()

  setVisible(true)

  def makeSnipersTable() = {
    val snipersTable = new JTable(snipersTableModel)

    snipersTable.setName(MainWindow.SNIPERS_TABLE_NAME)

    snipersTable
  }

  def makeControls() = {
    val controls = new JPanel(new FlowLayout())
    val itemIdField = new JTextField()
    itemIdField.setColumns(25)
    itemIdField.setName(MainWindow.NEW_ITEM_ID_NAME)
    controls.add(itemIdField)

    val joinAuctionButton = new JButton("Join Auction")
    joinAuctionButton.setName(MainWindow.JOIN_BUTTON_NAME)
    joinAuctionButton.addActionListener(new ActionListener(){
      def actionPerformed(e: ActionEvent) = {
        userRequestListener.joinAuction(itemIdField.getText)
      }
    })
    controls.add(joinAuctionButton)

    controls
  }

  def fillContentPane(table: JTable, controlPanel: JPanel) = {
    val contentPane: Container = getContentPane

    contentPane.setLayout(new BorderLayout)

    contentPane.add(controlPanel, BorderLayout.PAGE_START)
    contentPane.add(new JScrollPane(table), BorderLayout.CENTER)
  }
}

private[example] object MainWindow {
  val JOIN_BUTTON_NAME = "JoinButton"

  val NEW_ITEM_ID_NAME = "NewItem"

  val WINDOW_NAME = "Auction Sniper"
  val SNIPERS_TABLE_NAME = "SnipersTable"

  val STATUS_JOINING = "Joining"
  var STATUS_BIDDING = "Bidding"
  val STATUS_WINNING = "Winning"
  val STATUS_LOST = "Lost"
  val STATUS_WON = "Won"
}

trait UserRequestListener extends EventListener {
  def joinAuction(itemId: String)
}
