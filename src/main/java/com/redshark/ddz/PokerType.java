package com.redshark.ddz;

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
     * 牌型大小排序值
     */
    private int sort;
}
