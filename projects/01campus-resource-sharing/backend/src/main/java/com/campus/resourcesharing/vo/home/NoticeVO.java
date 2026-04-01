package com.campus.resourcesharing.vo.home;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class NoticeVO {
    private Long id;
    private String title;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    public NoticeVO() {}

    public NoticeVO(Long id, String title, String content, LocalDateTime createTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createTime = createTime;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
