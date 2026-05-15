/**
 * 管理后台数据分析模块共享的设计 token：配色板、统一字体、阴影。
 * 与极简风格保持一致：黑/白/绿三色系 + 中性灰阶，图表里禁用炫彩色渐变。
 */

export const chartPalette = {
  primary: "#00c46a",
  primarySoft: "rgba(0, 196, 106, 0.12)",
  primaryEdge: "rgba(0, 196, 106, 0.4)",
  accent: "#5b8def",
  accentSoft: "rgba(91, 141, 239, 0.12)",
  warning: "#ffae00",
  warningSoft: "rgba(255, 174, 0, 0.12)",
  danger: "#f56c6c",
  dangerSoft: "rgba(245, 108, 108, 0.12)",
  ink: "#101828",
  text: "#475467",
  textMuted: "#98a2b3",
  border: "#e7eaf0",
  borderSoft: "#f2f4f7",
  background: "#ffffff",
  panel: "#fafbfc",
} as const;

export const chartSeries = [
  chartPalette.primary,
  chartPalette.accent,
  chartPalette.warning,
  chartPalette.danger,
  "#7c3aed",
  "#06b6d4",
] as const;

export const chartFontFamily =
  '-apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", "Helvetica Neue", Arial, sans-serif';

export const chartTextStyle = {
  fontFamily: chartFontFamily,
  color: chartPalette.text,
  fontSize: 12,
};

export const chartTooltipStyle = {
  backgroundColor: chartPalette.background,
  borderColor: chartPalette.border,
  borderWidth: 1,
  padding: 12,
  textStyle: {
    color: chartPalette.ink,
    fontFamily: chartFontFamily,
    fontSize: 12,
  },
  extraCssText: "box-shadow: 0 8px 24px rgba(16, 24, 40, 0.08); border-radius: 8px;",
};

export const chartGrid = {
  top: 32,
  right: 24,
  bottom: 32,
  left: 48,
  containLabel: true,
} as const;

export const chartAxisLine = {
  axisLine: { lineStyle: { color: chartPalette.border } },
  axisTick: { show: false },
  axisLabel: { color: chartPalette.textMuted, fontFamily: chartFontFamily },
  splitLine: { lineStyle: { color: chartPalette.borderSoft, type: "dashed" as const } },
};
