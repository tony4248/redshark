package com.redshark.ddz;

import java.util.ArrayList;
import java.util.List;

/**
 * 牌型工具类
 * 该类负责定义牌型及判断牌型
 */
public class PokerTypeUtils {

    public static final  String Boom           = "Boom";
    public static final  String KingBoom  		= "typeKingBoom";
    public static final  String Single 		= "Single";
    public static final  String Pair 			= "Pair";
    public static final  String Three 			= "Three";
    public static final  String ThreeSingle 	= "ThreeSingle";
    public static final  String ThreePair 		= "ThreePair";
    public static final  String Straight 		= "Straight";
    public static final  String StraightPairs 	= "StraightPairs";
    public static final  String Plane 			= "Plane";
    public static final  String Plane2Single 	= "Plane2Single";
    public static final  String Plane2Pairs 	= "Plane2Pairs";
    public static final  String Four2Single 	= "Four2Single";
    public static final  String Four2Pairs 	= "Four2Pairs";


    public static PokerType getType(List<Integer> pokerIds){
        List<Poker> pokers = PokerUtils.parsePokers(pokerIds);
        //先将牌从大到小排序
        pokers.sort(new PokerComparatorDesc());
        int orderValue ;
        if((orderValue = isBoom(pokers)) != -1)
            return new PokerType(Boom,orderValue);
        if((orderValue = isKingBoom(pokers)) != -1)
            return new PokerType(KingBoom, orderValue);
        if((orderValue = isSingle(pokers)) != -1)
            return new PokerType(Single, orderValue);
        if((orderValue = isPair(pokers)) != -1)
            return new PokerType(Pair, orderValue);
        if((orderValue = isThree(pokers)) != -1)
            return new PokerType(Three, orderValue);
        if((orderValue = isThreeSingle(pokers)) != -1)
            return new PokerType(ThreeSingle, orderValue);
        if((orderValue = isThreePairs(pokers)) != -1)
            return new PokerType(ThreePair, orderValue);
        if((orderValue = isStraight(pokers)) != -1)
            return new PokerType(Straight, orderValue);
        if((orderValue = isStraightPairs(pokers)) != -1)
            return new PokerType(StraightPairs, orderValue);
        if((orderValue = isPlane(pokers)) != -1)
            return new PokerType(Plane, orderValue);
        if((orderValue = isPlane2Single(pokers)) != -1)
            return new PokerType(Plane2Single, orderValue);
        if((orderValue = isPlane2pairs(pokers)) != -1)
            return new PokerType(Plane2Pairs, orderValue);
        if((orderValue = isFour2Single(pokers)) != -1)
            return new PokerType(Four2Single, orderValue);
        if((orderValue = isFour2Pairs(pokers)) != -1)
            return new PokerType(Four2Pairs, orderValue);
        return null;
    }
    
    /**
     * 判断一手牌是否是炸弹（不含王炸）
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isBoom(List<Poker> list){
        if(list.size() == 4 && list.get(1).getSort() == list.get(0).getSort()
                && list.get(2).getSort() == list.get(0).getSort()
                && list.get(3).getSort() == list.get(0).getSort()){
            return list.get(0).getSort();
        }else{
            return -1;
        }
    }
    /**
     * 判断一手牌是否是王炸
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isKingBoom(List<Poker> list){
        if(list.size() == 2 && list.get(0).getSort() == PokerUtils.BigKingValue && list.get(1).getSort() == PokerUtils.SmallKingValue){
            return 0;
        }else{
            return -1;
        }
    }
    /**
     * 判断一手牌是否是单张
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isSingle(List<Poker> list){
        if(list.size() == 1)  return list.get(0).getSort();
        return -1;
    }

    /**
     * 判断一手牌是否是对子
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isPair(List<Poker> list){
        if(list.size() == 2 && list.get(0).getSort() == list.get(1).getSort())
            return list.get(0).getSort();
        return -1;

    }

    /**
     * 判断一手牌是否是顺子
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isStraight(List<Poker> list){
        if(list.size() < 5 || list.size() >12) return -1;//少于5张或多余12张，牌形不正确
        if(list.get(0).getSort() > PokerUtils.AValue) return -1;//如果最大的牌大于A，牌形不正确
        for(int i = 0 ; i < list.size() - 1; i ++){
            if(list.get(i).getSort()  != list.get(i + 1).getSort() + 1) return -1;//后一张牌不比前一张牌递次小1，牌形不正确
        }
        return list.get(0).getSort();
    }

    /**
     * 判断一手牌是否是三不带
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isThree(List<Poker> list){
        if(list.size() != 3) return -1;//不是三张
        if(list.get(1).getSort() == list.get(0).getSort()
                && list.get(2).getSort() == list.get(0).getSort())
            return  list.get(0).getSort();//三张一样的牌
        return -1;
    }
    /**
     * 判断一手牌是否是三带一
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isThreeSingle(List<Poker> list){
        if(list.size() != 4) return -1;//不是三张也不是四张，不是三带
        if(list.get(1).getSort() == list.get(0).getSort()
                && list.get(2).getSort() == list.get(0).getSort()
                && list.get(3).getSort() != list.get(0).getSort())
            return list.get(0).getSort();//前三张一样后一张不一样
        if(list.get(1).getSort() == list.get(3).getSort()
                && list.get(2).getSort() == list.get(3).getSort()
                && list.get(0).getSort() != list.get(3).getSort())
            return list.get(1).getSort();//前一张不一样后三张一样
        return -1;
    }
    /**
     * 判断一手牌是否是三带二
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isThreePairs(List<Poker> list){
        if(list.size() != 5) return -1;//不是三张也不是四张，不是三带
        if(list.get(1).getSort() == list.get(0).getSort()
                && list.get(2).getSort() == list.get(0).getSort()
                && list.get(3).getSort() != list.get(0).getSort()
                && list.get(3).getSort() == list.get(4).getSort())
            return list.get(0).getSort();//前三张一样后一张不一样   //前三张一样，后两张一样
        if(list.get(0).getSort() == list.get(1).getSort()
                && list.get(2).getSort() == list.get(3).getSort()
                && list.get(3).getSort() == list.get(4).getSort()
                && list.get(0).getSort() != list.get(2).getSort())
            return list.get(2).getSort();//前两张一样，后三张一样
        return -1;
    }
    /**
     * 判断一手牌是否是连对
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isStraightPairs(List<Poker> list){
        if(list.size() < 6 || list.size() > 20 || list.size() % 2 != 0) return -1;
        if(list.get(0).getSort() < 3) return -1;//如果最大的牌大于A，牌形不正确

        for(int i = 0 ; i < list.size() - 1 ; i += 2){
            if(list.get(i).getSort() != list.get(i + 1).getSort()) return -1;
        }
        for(int i = 0 ; i < list.size() - 2; i += 2){
            if(list.get(i).getSort() != list.get(i + 2).getSort() +1 ) return -1;
        }
        return list.get(0).getSort();
    }
    /**
     * 判断一手牌是否是飞机不带翅膀
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isPlane(List<Poker> list){
        if(list.size() != 6) return -1;
        if(list.get(0).getSort() < 3) return -1;//如果最大的牌大于A，牌形不正确(不能三个2 三个A)
        if(list.get(0).getSort() == list.get(1).getSort() && list.get(1).getSort() == list.get(2).getSort()
                && list.get(3).getSort() == list.get(4).getSort() && list.get(4).getSort() == list.get(5).getSort()
                && list.get(0).getSort() == list.get(3).getSort() + 1)
            return list.get(0).getSort();
        return -1;
    }
    /**
     * 判断一手牌是否是飞机带两张单牌
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isPlane2Single(List<Poker> list){
        List<Poker> a= new ArrayList<>();
        if(list.size() != 8) return -1;
        //判断有没有三张相同的牌
        for(int i = 0; i < list.size() -2; i++){
            if(list.get(i).getSort() == list.get(i + 1).getSort()
                    && list.get(i).getSort() == list.get(i + 2).getSort()){
                a.add(list.get(i));
            }
        }
        if(a.size() != 2) return -1;//没有两个三张，牌型不正确
        if(a.get(0).getSort() > PokerUtils.AValue) return -1;//如果最大的牌大于A，牌形不正确(不能三个2 三个A)
        if(a.get(0).getSort() - 1 == a.get(1).getSort())
            return a.get(0).getSort();//如果两个三张是连续的，牌型正确
        return -1;
    }
    /**
     * 判断一手牌是否是飞机带两对
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isPlane2pairs(List<Poker> list){
        if(list.size() != 10) return -1;
        List<Poker>  newList = new ArrayList<>(list);
        List<Poker>  three = new ArrayList<>();

        List<Poker> threes = new ArrayList<>();
        for(int i = 0; i < list.size() -2; i++){
            if(list.get(i).getSort() == list.get(i + 1).getSort()
                    && list.get(i).getSort() == list.get(i + 2).getSort()){
                three.add(list.get(i));

                threes.add(list.get(i));
                threes.add(list.get(i +1));
                threes.add(list.get(i +2));
            }

            newList.removeAll(threes);
        }
        if(three.size() != 2) return -1;
        for(int i = 0 ; i < newList.size() -1; i += 2){
            if(newList.get(i).getSort() != newList.get(i + 1).getSort()) return -1;
        }
        if(three.get(0).getSort() > PokerUtils.AValue) return -1;//如果最大的牌大于A，牌形不正确(不能三个2 三个A)
        if(three.get(0).getSort() - 1 == three.get(1).getSort()) return three.get(0).getSort();
        return -1;
    }
    /**
     * 判断一手牌是否是四带二
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isFour2Single(List<Poker> list){
        if(list.size() != 6) return -1;
        for(int i = 0 ; i < list.size() - 3 ; i ++){
            if(list.get(i).getSort() == list.get(i + 1).getSort()
                    && list.get(i).getSort() == list.get(i + 2).getSort()
                    && list.get(i).getSort() == list.get(i + 3).getSort())
                return list.get(i).getSort();
        }
        return -1;
    }
    /**
     * 判断一手牌是否是四带两对
     * 如果不是，返回-1; 如果是，返回该牌型的大小排序值
     */
    public static int isFour2Pairs(List<Poker> list){
        List<Poker>  newList = new ArrayList<>(list);
        Poker four = null;
        if(list.size() != 8) return -1;
        for(int i = 0 ; i < list.size() -3 ; i ++){
            if(list.get(i).getSort() == list.get(i + 1).getSort()
                    && list.get(i).getSort() ==list.get(i + 2).getSort()
                    && list.get(i).getSort() == list.get(i + 3).getSort()){
                List<Poker>  fours = new ArrayList<>();
                fours.add(list.get(i));
                fours.add(list.get(i +1));
                fours.add(list.get(i +2));
                fours.add(list.get(i +4));
                newList.removeAll(fours);
                four = list.get(i);
            }

        }
        for(int i = 0 ; i < newList.size() -1; i += 2){
            if(newList.get(i).getSort() != newList.get(i + 1).getSort()) return -1;
        }
        return four.getSort();
    }
}
