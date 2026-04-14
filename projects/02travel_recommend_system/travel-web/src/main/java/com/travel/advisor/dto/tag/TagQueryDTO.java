package com.travel.advisor.dto.tag;

import lombok.Data;

@Data
public class TagQueryDTO {
    private String name;
    private Integer type;
    private String category;
    private Integer status;
}
