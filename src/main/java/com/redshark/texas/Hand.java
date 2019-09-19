package com.redshark.texas;


import java.util.ArrayList;
import java.util.List;

/**
 * Represents a hand of 5 cards.
 *
 * @author andyehou@gmail.com (Andy Hou)
 */
public class Hand {
  // The 5 cards that make up the hand.
  private List<Poker> hand;
  
  // The sorting orders of the hand, higher is better, lower index is higher priority.
  private List<Integer> sortingOrders;
  
  // The category of the hand.
  private Category category;
  
  // The possible categories in order from lowest to highest value.
  public enum Category {
    HIGH_CARD(new int[]{39, 37, 22, 6, 27}),
    ONE_PAIR(new int[]{8, 21, 51, 36, 3}),
    TWO_PAIR(new int[]{46, 33, 1, 14, 2}),
    THREE_OF_A_KIND(new int[]{4, 17, 30, 47, 19}),
    STRAIGHT(new int[]{20, 32, 18, 4, 42}),
    FLUSH(new int[]{26, 37, 36, 34, 32}),
    FULL_HOUSE(new int[]{9, 22, 48, 31, 5}),
    FOUR_OF_A_KIND(new int[]{45, 19, 32, 6, 13}),
    STRAIGHT_FLUSH(new int[]{8, 7, 6, 5, 4}),
    ROYAL_FLUSH(new int[]{39, 51, 50, 49, 48});
    
    private final Hand exampleHand;
    
    private Category(int[] exampleHandCards) {
      exampleHand = new Hand(exampleHandCards);
    }
    
    public Hand exampleHand() {
      return exampleHand;
    }
  }

  /**
   * Constructs a hand with the given cards.
   * @param cards The 5 cards that make up the hand.
   */
  public Hand(List<Poker> hand) {
    this.hand = new ArrayList<Poker>(hand);
    computeCategory();
  }
  
  public Hand(int[] cardIndexes) {
    hand = new ArrayList<Poker>();
    for (int i = 0; i < cardIndexes.length; i++) {
      hand.add(Poker.fromIndex(cardIndexes[i]));
    }
    computeCategory();
  }
  
  /**
   * @return the category of this hand.
   */
  public Category getCategory() {
    return category;
  }
  
  /**
   * @param card The card to check.
   * @return true if this hand contains the card.
   */
  public boolean contains(Poker card) {
    return hand.contains(card);
  }
  
  public List<Poker> getCards() {
    return hand;
  }
  
  /**
   * Compares the value of this hand with another.
   * @param other The hand to compare with.
   * @return -1 if this hand is lower, 0 if they are equal in value, and 1 if this hand is higher.
   */
  public int compare(Hand other) {
    if (category.ordinal() < other.category.ordinal()) {
      return -1;
    } else if (category.ordinal() > other.category.ordinal()) {
      return 1;
    }
    
    // Hands of the same category are further sorted by the sorting orders list.
    for (int i = 0; i < sortingOrders.size(); i++) {
      if (sortingOrders.get(i) < other.sortingOrders.get(i)) {
        return -1;
      } else if (sortingOrders.get(i) > other.sortingOrders.get(i)) {
        return 1;
      }
    }
    
    return 0;
  }
  
  public String toString() {
    String s = "[";
    for (int i = 0; i < hand.size(); i++) {
      s += hand.get(i).toString();
      if (i != hand.size() - 1) {
        s += " ";
      }
    }
    s += "]\n" + category + "\n[";
    for (int i = 0; i < sortingOrders.size(); i++) {
      s += Poker.getRankChar(sortingOrders.get(i));
      if (i != sortingOrders.size() - 1) {
        s += " ";
      }
    }
    return s + "]";
  }
  
  /**
   * Computes the category and sorting orders of this hand.
   */
  private void computeCategory() {
    if (hand.size() != 5) {
      return;
    }
    
    // Compute the rank counts.
    int rankCounts[] = new int[13];
    for (Poker card : hand) {
      rankCounts[card.getRank()]++;
    }
    
    // Check for any pair, 3 of a kind, 4 of a kind combos.
    int rankCountCounts[] = new int[6];
    for (int i = 12; i >= 0; i--) {
      rankCountCounts[rankCounts[i]]++;
    }
    
    // Check for flush.
    boolean hasFlush = true;
    for (int i = 1; i < hand.size(); i++) {
      if (hand.get(i).getSuit() != hand.get(i - 1).getSuit()) {
        hasFlush = false;
        break;
      }
    }
    
    // Check for straight.
    boolean hasStraight = true;
    boolean isAceHigh = false;
    if (rankCountCounts[1] != 5) {
      hasStraight = false;
    } else {
      int consecutiveCount = 0;
      // Check for K - A connection.
      if (rankCounts[0] == 1 && rankCounts[12] == 1) {
        consecutiveCount++;
        isAceHigh = true;
      }
      for (int i = 1; i < rankCounts.length; i++) {
        if (rankCounts[i] == 1 && rankCounts[i - 1] == 1) {
          consecutiveCount++;
        }
      }
      if (consecutiveCount != 4) {
        hasStraight = false;
      // Check for no wrap-around straight (ace cannot be both high and low).
      } else if (isAceHigh && rankCounts[9] == 0) {
        hasStraight = false;
      }
    }
    
    // Get the highest rank for the straight. Ace high straight is 13. Ace low straight is 4.
    int straightHighRank = 13;
    if (rankCounts[0] == 0) {
      for (straightHighRank = 12; straightHighRank > 0; straightHighRank--) {
        if (rankCounts[straightHighRank] > 0) {
          break;
        }
      }
    } else if (!isAceHigh) {
      straightHighRank = 4;
    }
    
    // Compute the sorting orders.
    sortingOrders = new ArrayList<Integer>();
    for (int i = 4; i >= 1; i--) {
      for (int j = 0; j < rankCountCounts[i]; j++) {
        int countdown = j + 1;
        for (int k = 13; k > 0; k--) {
          if (rankCounts[k % 13] == i) {
            if (--countdown == 0) {
              sortingOrders.add(k);
              break;
            }
          }
        }
      }
    }
    
    if (hasFlush && hasStraight) {
      if (isAceHigh) {
        category = Category.ROYAL_FLUSH;
        sortingOrders.clear();
      } else {
        category = Category.STRAIGHT_FLUSH;
        sortingOrders.clear();
        sortingOrders.add(straightHighRank);
      }
      
    } else if (rankCountCounts[4] == 1) {
      category = Category.FOUR_OF_A_KIND;
    
    } else if (rankCountCounts[3] == 1 && rankCountCounts[2] == 1) {
      category = Category.FULL_HOUSE;
      
    } else if (hasFlush) {
      category = Category.FLUSH;
    
    } else if (hasStraight) {
      category = Category.STRAIGHT;
      sortingOrders.clear();
      sortingOrders.add(straightHighRank);
    
    } else if (rankCountCounts[3] == 1) {
      category = Category.THREE_OF_A_KIND;
    
    } else if (rankCountCounts[2] == 2) {
      category = Category.TWO_PAIR;
    
    } else if (rankCountCounts[2] == 1) {
      category = Category.ONE_PAIR;
      
    } else {
      category = Category.HIGH_CARD;
    }
  }
}
