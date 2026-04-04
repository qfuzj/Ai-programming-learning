package com.travel.advisor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 地区实体类
 */
@Data
@TableName("region")
public class Region {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父级ID，顶级地区的 parentId 为 0，其他地区的 parentId 指向其上级地区的 ID，实现树形结构。
     */
    private Long parentId;

    /**
     * 地区名称
     */
    private String name;

    /**
     * 地区简称
     */ 
    private String shortName;

    /**
     * 层级：1 省 2 市 3 区县
     */
    private Integer level;

    /**
     * 地区编码
     */
    private String code;

    /**
     * 拼音
     */
    private String pinyin;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 排序字段
     */
    private Integer sortOrder;

    /**
     * 是否热门：0 否 1 是，供前端展示使用。
     */
    private Integer isHot;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除：0 否 1 是
     */
    @TableLogic
    private Integer isDeleted;
}
