package com.redshark.ddz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Poker {
	 /**
     * 牌ID
     */
    private int id;

    /**
     * 牌面
     */
    private String name;

    /**
     * 牌大小排序值 0-14  数值越小，表示牌越大
     */
    private int sort;
    
    
    /**
     * 构造函数
     * @param id
     * @param sort
     */
    public Poker (int id, int sort) {
        this.id = id;
        this.sort = sort;
    }
}
