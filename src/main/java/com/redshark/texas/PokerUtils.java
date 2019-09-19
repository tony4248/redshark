package com.redshark.texas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokerUtils {
	/**
     * 牌大小,数值越高, 牌值越大
     */
    private static final List<Integer> pokerSortValues = Arrays.asList(
            13, 13, 13, 13,//A
    		1, 1, 1, 1,//2
            2, 2, 2, 2,//3
            3, 3, 3, 3,//4
            4, 4, 4, 4,//5
            5, 5, 5, 5,//6
            6, 6, 6, 6,//7
            7, 7, 7, 7,//8
            8, 8, 8, 8,//9
            9, 9, 9, 9,//10
            10, 10, 10, 10,//J
            11, 11, 11, 11,//Q
            12, 12, 12, 12//K
    );

    private static final List<String> pokerNames = Arrays.asList(
    		"SPADES-A","HEARTS-A","CLUBS-A", "DIAMONDS-A", //A
    		"SPADES-2","HEARTS-2","CLUBS-2", "DIAMONDS-2", //2
            "SPADES-3","HEARTS-3","CLUBS-3", "DIAMONDS-3", //3
            "SPADES-4","HEARTS-4","CLUBS-4", "DIAMONDS-4", //4
            "SPADES-5","HEARTS-5","CLUBS-5", "DIAMONDS-5", //5
            "SPADES-6","HEARTS-6","CLUBS-6", "DIAMONDS-6", //6
            "SPADES-7","HEARTS-7","CLUBS-7", "DIAMONDS-7", //7
            "SPADES-8","HEARTS-8","CLUBS-8", "DIAMONDS-8", //8
            "SPADES-9","HEARTS-9","CLUBS-9", "DIAMONDS-9", //9
            "SPADES-10","HEARTS-10", "CLUBS-10", "DIAMONDS-10", //10
            "SPADES-J","HEARTS-J", "CLUBS-J","DIAMONDS-J", //J
            "SPADES-Q","HEARTS-Q", "CLUBS-Q","DIAMONDS-Q", //Q
            "SPADES-K","HEARTS-K", "CLUBS-K","DIAMONDS-K"); //K
    
    
    /**
     * A的大小值
     */
    public static final Integer AValue = 13;


    /**
     * 牌面的id
     */
    private static final List<Integer> pokerIds = Arrays.asList(
            1, 2, 3, 4,//A
            5, 6, 7, 8,//2
            9, 10, 11, 12,//3
            13, 14, 15, 16,//4
            17, 18, 19, 20,//5
            21, 22, 23, 24,//6
            25, 26, 27, 28,//7
            29, 30, 31, 32,//8
            33, 34, 35, 36,//9
            37, 38, 39, 40,//10
            41, 42, 43, 44,//J
            45, 46, 47, 48,//Q
            49, 50, 51, 52//K
    		);//King
    
     /**
     * 获取牌的花色
     * */
//    private static final SuitType getSuitType(int pokerId) {
//    	SuitType suitType = null;
//    	switch (pokerId % 4) {
//			case 1:
//				suitType = SuitType.SPADES;
//				break;
//			case 2:
//				suitType = SuitType.HEARTS;
//				break;
//			case 3:
//				suitType = SuitType.CLUBS;
//				break;
//			case 4:
//				suitType = SuitType.DIAMONDS;
//				break;
//		}
//    	return suitType;
//    }

    /**
     * 随机生成一副牌
     */
    public static List<Integer> getRandomPokerIds(){

        int length = pokerIds.size();
        List<Integer> activityIdList = new ArrayList<>(pokerIds);
        List<Integer> idList = new ArrayList<>();

        for(int i = 0 ; i < pokerIds.size() ; i ++){
            int index = (int)Math.floor(Math.random() * length);
            int pokerIndex = activityIdList.get(index);
            idList.add(pokerIndex);
            List<Integer> list = new ArrayList<>();

            for (int j = 0 ; j < activityIdList.size() ; j ++){
                if(j != index){
                    list.add(activityIdList.get(j));
                }
            }
            activityIdList = new ArrayList<>(list);
            length --;
        }
        return idList;
    }

    public static List<Poker> parsePokers(List<Integer> pokerIdList){
        List<Poker> pokers = new ArrayList<>();
        for(Integer index : pokerIdList){
//            pokers.add(new Poker(
//                    pokerIds.get(index -1),
//                    getSuitType(index),
//                    pokerSortValues.get(index -1)
//            ));
        }
        return pokers;
    }
    /**
     * 获取分配好的一副扑克牌
     * @return
     */
    public static Map<Integer,List<Integer>> getSplitPokerIds(){
        List<Integer> list = getRandomPokerIds();
        Map<Integer,List<Integer>> map = new HashMap<>();
        //按照座位号
        map.put(1,new ArrayList<>(list.subList(0,17)));
        map.put(2,new ArrayList<>(list.subList(17,34)));
        map.put(3,new ArrayList<>(list.subList(34,51)));
        map.put(4,new ArrayList<>(list.subList(51,54)));
        return map;
    }
}
