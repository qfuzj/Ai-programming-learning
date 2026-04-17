import type { CommonRegionNode } from "@/api/common";
import type { ScenicItem, ScenicQuery } from "@/api/scenic";

export type ScenicRegionNode = CommonRegionNode;

export interface ScenicListState {
  loading: boolean;
  scenicList: ScenicItem[];
  total: number;
  query: ScenicQuery;
  regionTreeData: ScenicRegionNode[];
  categoryOptions: string[];
  levelOptions: string[];
  selectedProvinceId?: number;
  selectedCityId?: number;
  isCollapsed: boolean;
  expanded: boolean;
}
