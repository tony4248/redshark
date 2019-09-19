package com.redshark.texas;


import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redshark.ddz.GameLogic;

public class Pocket {
  // The 2 cards that make up the pocket.
  private List<Poker> pocket;
  
  // The category of the pocket.
  private Category category;
  
  public enum Category {
    UNSUITED,
    SUITED,
    PAIR
  }
  
 
  /**
   * Constructs a pocket with the given cards.
   * @param pocket The 2 cards that make up the pocket.
   */
  public Pocket(List<Poker> pocket) {
    this.pocket = pocket;
    computeCategory();
  }
  
  /**
   * @return the category of this pocket.
   */
  public Category getCategory() {
    return category;
  }
  
  public Poker getLowCard() {
    return pocket.get(0);
  }
  
  public Poker getHighCard() {
    return pocket.get(1);
  }
  
  public String toString() {
    String s = "[";
    for (int i = 0; i < pocket.size(); i++) {
      s += pocket.get(i).toString();
      if (i != pocket.size() - 1) {
        s += " ";
      }
    }
    return s + "]";
  }
  
  /**
   * Computes the category of this hand and reorders the pocket cards to place the
   * higher ranking card first.
   */
  private void computeCategory() {
    if (pocket.size() != 2) {
      return;
    }
    
    if (pocket.get(0).getRank() == pocket.get(1).getRank()) {
      category = Category.PAIR;
    } else if (pocket.get(0).getSuit() == pocket.get(1).getSuit()) {
      category = Category.SUITED;
    } else {
      category = Category.UNSUITED;
    }
    
    if (pocket.get(0).getRank() < pocket.get(1).getRank()) {
      Collections.reverse(pocket);
    }
  }
}
