package com.redshark.texas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redshark.ddz.PokerUtils;

/**
 * 牌型工具类
 * 该类负责定义牌型及判断牌型
 */
public class PokerTypeUtils {

    public static final  String RoyalFlush      = "RoyalFlush"; //皇家同花顺
    public static final  String StraightFlush  	= "StraightFlush"; //同花顺
    public static final  String FourOfAKind 	= "FourOfAKind"; //四条
    public static final  String FullHouse 		= "FullHouse"; //葫芦
    public static final  String Flush 			= "Flush"; //同花
    public static final  String Straight 		= "Straight"; //顺子
    public static final  String ThreeOfAKind 	= "ThreeOfAKind"; //三条
    public static final  String TwoPairs 		= "TwoPairs";
    public static final  String Pair 			= "Pair";
    public static final  String Highcard 		= "Highcard";

    /** 
     * 牌型的大小,值越大越强
     * */
    private static final Map<String, Integer> typeSortValues;
    static
    {
    	typeSortValues = new HashMap<String, Integer>();
    	typeSortValues.put("Highcard", new Integer(1));
    	typeSortValues.put("Pair", new Integer(2));
    	typeSortValues.put("TwoPairs", new Integer(3));
    	typeSortValues.put("ThreeOfAKind", new Integer(4));
    	typeSortValues.put("Straight", new Integer(5));
    	typeSortValues.put("Flush", new Integer(6));
    	typeSortValues.put("FullHouse", new Integer(7));
    	typeSortValues.put("FourOfAKind", new Integer(8));
    	typeSortValues.put("StraightFlush", new Integer(9));
    	typeSortValues.put("RoyalFlush", new Integer(10));
    }
    
    public static PokerType getType(List<Integer> pokerIds){
//        List<Poker> pokers = PokerUtils.parsePokers(pokerIds);
//        //先将牌从大到小排序
//        pokers.sort(new PokerComparatorDesc());
//        int orderValue ;
//        if((orderValue = isBoom(pokers)) != -1)
//            return new PokerType(Boom,orderValue);
//        if((orderValue = isKingBoom(pokers)) != -1)
//            return new PokerType(KingBoom, orderValue);
//        if((orderValue = isSingle(pokers)) != -1)
//            return new PokerType(Single, orderValue);
//        if((orderValue = isPair(pokers)) != -1)
//            return new PokerType(Pair, orderValue);
//        if((orderValue = isThree(pokers)) != -1)
//            return new PokerType(Three, orderValue);
//        if((orderValue = isThreeSingle(pokers)) != -1)
//            return new PokerType(ThreeSingle, orderValue);
//        if((orderValue = isThreePairs(pokers)) != -1)
//            return new PokerType(ThreePair, orderValue);
//        if((orderValue = isStraight(pokers)) != -1)
//            return new PokerType(Straight, orderValue);
//        if((orderValue = isStraightPairs(pokers)) != -1)
//            return new PokerType(StraightPairs, orderValue);
//        if((orderValue = isPlane(pokers)) != -1)
//            return new PokerType(Plane, orderValue);
//        if((orderValue = isPlane2Single(pokers)) != -1)
//            return new PokerType(Plane2Single, orderValue);
//        if((orderValue = isPlane2pairs(pokers)) != -1)
//            return new PokerType(Plane2Pairs, orderValue);
//        if((orderValue = isFour2Single(pokers)) != -1)
//            return new PokerType(Four2Single, orderValue);
//        if((orderValue = isFour2Pairs(pokers)) != -1)
//            return new PokerType(Four2Pairs, orderValue);
        return null;
    }
    
    /**
     * 取得一对
     * @param pokers
     * @return
     */
    private static List<Poker> getPair(List<Poker> pokers) {
    	List<Poker> pair = new ArrayList<Poker>();
    	for(int j = pokers.size() -1 ; j >= 1 ; j--){
			if(pokers.get(j).getRank() == pokers.get(j-1).getRank()) {
				pair.add(pokers.get(j));
				pair.add(pokers.get(j-1));
				break;
			}
		}
		return pair;
	}
    
    /**
     * 判断一手牌是否有对子
     * 如果不是，返回空; 如果是，返回该牌型的值
     * 输入的参数必须是一个对子
     */
    private static List<Integer> getPairMaxValues(List<Poker> pokers) {
    	List<Integer> maxValues = new ArrayList<Integer>();
		maxValues.add(pokers.get(0).getRank());
		return maxValues;
	}
    
    /**
     * 取得牌中的两个对子
     * 如果不是，返回空; 如果是，返回该牌型的值
     */
    private static List<Poker> getTwoPairs(List<Poker> pokers) {
    	List<Poker> twoPairs = new ArrayList<Poker>();
    	int pairsNum = 0;
    	for(int j = pokers.size() -1 ; j >= 1 ; j--){
			if(pokers.get(j).getRank() == pokers.get(j).getRank()) {
				twoPairs.add(pokers.get(j));
				twoPairs.add(pokers.get(j-1));
				pairsNum ++;
				 {break;}
			}
		}
    	if(pairsNum == 2) {return twoPairs;}
    	else {
    		return twoPairs = new ArrayList<Poker>();
		}
    }
    
    /**
     * 取得两对中的值
     */
    private static List<Integer> getTwoPairsMaxValues(List<Poker> pokers) {
    	List<Integer> maxValues = new ArrayList<Integer>();
		maxValues.add(pokers.get(0).getRank());
		maxValues.add(pokers.get(2).getRank());
		return maxValues;
    }
    
    /**
     * 取得ThreeOfAKind的牌
     */
    public static List<Poker> getThreeOfAKind(List<Poker> pokers){
    	int index = 0;
    	List<Poker> threeOfAKind = new ArrayList<Poker>();
		for(int j = pokers.size() -1 ; j >= 2 ; j--){
			if(pokers.get(j).getRank() == pokers.get(j-1).getRank() 
				&& pokers.get(j).getRank() == pokers.get(j-2).getRank())
			{
				index = j;
				threeOfAKind.add(pokers.get(j));
				threeOfAKind.add(pokers.get(j-1));
				threeOfAKind.add(pokers.get(j-2));
				break;
			}
		}
		//没找到
		if(index == 0){
			return threeOfAKind;
		}else if(index == pokers.size() -1){
			threeOfAKind.add(pokers.get(index-3));
		}else{
			threeOfAKind.add(pokers.get(pokers.size() -1));
		}
		return threeOfAKind;
    }
    
    /**
     * 取得FourOfAKind的牌
     */
    public static List<Poker> getFourOfAKind(List<Poker> pokers){
    	int index = 0;
    	List<Poker> threeOfAKind = new ArrayList<Poker>();
		for(int j = pokers.size() -1 ; j >= 3 ; j--){
			if(pokers.get(j).getRank() == pokers.get(j-1).getRank() 
				&& pokers.get(j).getRank() == pokers.get(j-2).getRank()
				&& pokers.get(j).getRank() == pokers.get(j-3).getRank())
			{
				index = j;
				threeOfAKind.add(pokers.get(j));
				threeOfAKind.add(pokers.get(j-1));
				threeOfAKind.add(pokers.get(j-2));
				threeOfAKind.add(pokers.get(j-3));
				break;
			}
		}
		//没找到
		if(index == 0){
			return threeOfAKind;
		}else if(index == pokers.size() -1){
			threeOfAKind.add(pokers.get(index-4));
		}else{
			threeOfAKind.add(pokers.get(pokers.size() -1));
		}
		return threeOfAKind;
    }
    
    /**
     * 取得两对中的值
     */
    private static List<Integer> getThreeOfAKindMaxValues(List<Poker> pokers) {
    	List<Integer> maxValues = new ArrayList<Integer>();
    	maxValues.add(pokers.get(1).getRank());
		return maxValues;
    }
    

    /**
     * 判断一手牌是否是炸弹（不含王炸）
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isFourOfAKind(List<Poker> list){
        if(list.get(1).getRank() == list.get(0).getRank()
                && list.get(2).getRank() == list.get(0).getRank()
                && list.get(3).getRank() == list.get(0).getRank()){
            return list.get(0).getRank();
        }else{
            return -1;
        }
    }
    
}
