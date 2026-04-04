package com.travel.advisor.vo.scenic;

import com.travel.advisor.vo.region.RegionTreeVO;
import lombok.Data;

import java.util.List;

@Data
public class ScenicFilterOptionsVO {

    private List<RegionTreeVO> regions;

    private List<String> categories;

    private List<String> levels;
}