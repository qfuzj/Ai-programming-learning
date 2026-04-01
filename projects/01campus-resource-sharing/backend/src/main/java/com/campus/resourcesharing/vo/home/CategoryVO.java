package com.campus.resourcesharing.vo.home;

public class CategoryVO {
    private Long id;
    private String name;
    private Integer sort;

    public CategoryVO() {}

    public CategoryVO(Long id, String name, Integer sort) {
        this.id = id;
        this.name = name;
        this.sort = sort;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
