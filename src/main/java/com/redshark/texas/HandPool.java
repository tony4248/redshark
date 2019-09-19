package com.redshark.texas;


import java.util.ArrayList;
import java.util.List;

/**
 * Represents a pool of at least 5 cards that a best hand of 5 cards is chosen from.
 *
 * @author andyehou@gmail.com (Andy Hou)
 */
public class HandPool {
  // The list of cards to choose from.
  private List<Poker> cards;
  // The list of every possible hand. Order of cards does not matter.
  private List<Hand> hands;
  // A hand with the best value.
  private Hand bestHand;
  
  public HandPool(List<Poker> cards) {
    this.cards = cards;
  }
  
  /**
   * @return A best hand of 5 cards that can be created from the pool of cards.
   */
  public Hand getBestHand() {
    if (bestHand == null) {
      listHands();
      
      bestHand = hands.get(0);
      for (int i = 1; i < hands.size(); i++) {
        Hand hand = hands.get(i);
        if (hand.compare(bestHand) > 0) {
          bestHand = hand;
        }
      }
    }
    return bestHand;
  }
  
  private void listHands() {
    hands = new ArrayList<Hand>();
    getHandsRec(cards, new ArrayList<Poker>());
  }
  
  private void getHandsRec(List<Poker> available, List<Poker> partialHand) {
    int cardsToGo = 5 - partialHand.size();
    if (cardsToGo == 0) {
      hands.add(new Hand(partialHand));
      return;
    }
    
    for (int i = 0; i <= available.size() - cardsToGo; i++) {
      Poker card = available.get(i);
      partialHand.add(card);
      getHandsRec(available.subList(i + 1, available.size()), partialHand);
      partialHand.remove(partialHand.size() - 1);
    }
  }
}
