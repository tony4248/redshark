package com.redshark.texas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PokerType {
	/**
     * 牌型
     */
    private String type;
    /**
     * 牌型内最大牌的值
     */
    private int sort;
    
    /**
     * 不同牌型的大小顺序
     */
    private int typeSort;
}
