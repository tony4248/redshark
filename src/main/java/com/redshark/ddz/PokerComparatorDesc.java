package com.redshark.ddz;

import java.util.Comparator;

public class PokerComparatorDesc implements Comparator<Poker> {

    public int compare(Poker p1,Poker p2){
        if(p1.getSort() > p2.getSort())
            return -1;
        else if(p1.getSort() < p2.getSort())
            return 1;
        else
            return 0;
    }
}
