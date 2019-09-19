package com.redshark.ddz;

import java.util.List;

/**
 * 牌型大小比较工具类
 */
public class PokerCompareUtils {
    /**
     * 比较两手牌的大小
     * a b ,当a大于b时返回true
     * 王炸通吃
     * 炸弹仅次于王炸
     * 其他牌必须牌型相等才能比较
     */
    public static boolean comparePokers(List<Integer> aIds, List<Integer> bIds){
        List<Poker> a = PokerUtils.parsePokers(aIds);
        List<Poker> b = PokerUtils.parsePokers(bIds);
        a.sort(new PokerComparatorDesc());
        b.sort(new PokerComparatorDesc());
        if(a.size() < 1 || b.size() < 1) return false;//空
        if(PokerTypeUtils.isKingBoom(b) != -1) return false;//b是王炸
        if(PokerTypeUtils.isKingBoom(a) != -1) return true;//a是王炸
        if(PokerTypeUtils.isBoom(b) != -1){//b是炸弹
            if(PokerTypeUtils.isBoom(a) != -1) return compareSingle(a.get(0),b.get(0));//a 也是炸弹
            return false;//a 不是炸弹
        }
        if(PokerTypeUtils.isBoom(a) != -1) return true;//a 是炸弹
        if(a.size() != b.size()) return false;//已经排除了炸弹的可能，长度不相等,不能比较

        PokerType aType = PokerTypeUtils.getType(aIds);
        PokerType bType= PokerTypeUtils.getType(bIds);
        if(aType == null || bType == null || aType.getType() != bType.getType()) return false;//牌型不相等不能比较
        return aType.getSort() > bType.getSort();
    }
    /**
     * 比较单牌的大小
     */
    private static boolean compareSingle(Poker a,Poker b){
        if(a.getSort() > b.getSort()){
            return true;
        }else{
            return false;
        }
    }
}
