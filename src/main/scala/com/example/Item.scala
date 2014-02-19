package com.example

case class Item(id: String, stopPrice: Option[Int]) {
  def allowsBid(bid: Int) = stopPrice match {
    case Some(x) => bid < x
    case None => true
  }
}
