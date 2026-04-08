package com.travel.advisor.dto;

import lombok.Data;

/**
 * 字典VO对象，用于封装字典项的代码和值描述
 */
@Data
public class DictVO {
    private Object code;
    private String desc;

    public DictVO(Object code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}