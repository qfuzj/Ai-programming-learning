package com.campus.resourcesharing.vo.home;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GoodsHomeVO {
    private Long id;
    private String title;
    private Long categoryId;
    private BigDecimal price;
    private String mainImage;
    private String conditionLevel;
    private Integer favoriteCount;
    private Integer viewCount;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    public GoodsHomeVO() {}

    public GoodsHomeVO(Long id, String title, Long categoryId, BigDecimal price, String mainImage,
                       String conditionLevel, Integer favoriteCount, Integer viewCount, 
                       String status, LocalDateTime createTime) {
        this.id = id;
        this.title = title;
        this.categoryId = categoryId;
        this.price = price;
        this.mainImage = mainImage;
        this.conditionLevel = conditionLevel;
        this.favoriteCount = favoriteCount;
        this.viewCount = viewCount;
        this.status = status;
        this.createTime = createTime;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }

    public String getConditionLevel() { return conditionLevel; }
    public void setConditionLevel(String conditionLevel) { this.conditionLevel = conditionLevel; }

    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
