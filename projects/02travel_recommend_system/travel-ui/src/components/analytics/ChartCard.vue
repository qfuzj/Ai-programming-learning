<template>
  <section class="chart-card" :class="{ 'is-empty': empty, 'is-loading': loading }">
    <header v-if="title || $slots.action" class="chart-card__head">
      <div class="chart-card__heading">
        <h3 v-if="title" class="chart-card__title">{{ title }}</h3>
        <p v-if="subtitle" class="chart-card__subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.action" class="chart-card__action">
        <slot name="action" />
      </div>
    </header>
    <div class="chart-card__body" :style="{ height: bodyHeight }">
      <div v-if="loading" class="chart-card__state">加载中...</div>
      <div v-else-if="empty" class="chart-card__state">{{ emptyText }}</div>
      <slot v-else />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title?: string;
  subtitle?: string;
  loading?: boolean;
  empty?: boolean;
  emptyText?: string;
  bodyHeight?: string;
}>();
</script>

<style scoped>
.chart-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 24px 24px;
  background: #fff;
  border: 1px solid #e7eaf0;
  border-radius: 12px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.chart-card:hover {
  border-color: #d3d8e0;
  box-shadow: 0 4px 12px rgba(16, 24, 40, 0.04);
}

.chart-card__head {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}

.chart-card__heading {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.chart-card__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #101828;
}

.chart-card__subtitle {
  margin: 0;
  font-size: 12px;
  color: #98a2b3;
}

.chart-card__action {
  flex-shrink: 0;
}

.chart-card__body {
  position: relative;
  width: 100%;
  min-height: 260px;
}

.chart-card__state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 13px;
  color: #98a2b3;
}

.chart-card.is-loading .chart-card__body,
.chart-card.is-empty .chart-card__body {
  background: #fafbfc;
  border-radius: 8px;
}
</style>
