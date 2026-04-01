package com.campus.resourcesharing.vo.home;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class BannerVO {
    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer sort;

    public BannerVO() {}

    public BannerVO(Long id, String title, String imageUrl, String linkUrl, Integer sort) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.sort = sort;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
