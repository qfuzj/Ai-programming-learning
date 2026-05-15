import { onBeforeUnmount, ref, watch, type Ref } from "vue";
import * as echarts from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { BarChart, LineChart, PieChart, GaugeChart, FunnelChart } from "echarts/charts";
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
  DatasetComponent,
  TransformComponent,
} from "echarts/components";
import { LabelLayout, UniversalTransition } from "echarts/features";
import type { EChartsType } from "echarts/core";
import type { EChartsOption } from "echarts";

let registered = false;
function ensureRegistered(): void {
  if (registered) return;
  echarts.use([
    CanvasRenderer,
    BarChart,
    LineChart,
    PieChart,
    GaugeChart,
    FunnelChart,
    GridComponent,
    TooltipComponent,
    LegendComponent,
    TitleComponent,
    DatasetComponent,
    TransformComponent,
    LabelLayout,
    UniversalTransition,
  ]);
  registered = true;
}

export function useChart(option: Ref<EChartsOption | null>) {
  ensureRegistered();
  const containerRef = ref<HTMLElement>();
  let instance: EChartsType | null = null;
  let resizeObserver: ResizeObserver | null = null;

  function dispose(): void {
    resizeObserver?.disconnect();
    resizeObserver = null;
    instance?.dispose();
    instance = null;
  }

  watch(
    containerRef,
    (el) => {
      dispose();
      if (!el) return;
      instance = echarts.init(el);
      if (option.value) {
        instance.setOption(option.value, { notMerge: true });
      }
      resizeObserver = new ResizeObserver(() => instance?.resize());
      resizeObserver.observe(el);
    },
    { flush: "post", immediate: true }
  );

  watch(
    option,
    (value) => {
      if (instance && value) {
        instance.setOption(value, { notMerge: true });
      }
    },
    { deep: true }
  );

  onBeforeUnmount(dispose);

  return { containerRef };
}

export type { EChartsOption };
