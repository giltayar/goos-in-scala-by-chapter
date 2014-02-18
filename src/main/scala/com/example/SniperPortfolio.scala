package com.example

class SniperPortfolio extends SniperCollector {

  var auctionSnipers = Set[AuctionSniper]()
  var portfolioListener: PortfolioListener = null

  def addSniper(auctionSniper: AuctionSniper) = {
    auctionSnipers += auctionSniper
    portfolioListener.sniperAdded(auctionSniper)
  }

  def addPortfolioListener(portfolioListener: PortfolioListener) = {
    this.portfolioListener = portfolioListener
  }
}

trait PortfolioListener {
  def sniperAdded(auctionSniper: AuctionSniper)
}
