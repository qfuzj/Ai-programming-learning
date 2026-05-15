<template>
  <div class="stat-card" :class="`stat-card--${tone || 'default'}`">
    <div class="stat-card__label">{{ label }}</div>
    <div class="stat-card__value">{{ value }}</div>
    <div v-if="hint" class="stat-card__hint">{{ hint }}</div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string;
  value: string | number;
  hint?: string;
  tone?: "default" | "primary" | "accent" | "warning" | "danger";
}>();
</script>

<style scoped>
.stat-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 20px 22px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e7eaf0;
  border-radius: 12px;
  transition:
    border-color 0.2s,
    box-shadow 0.2s,
    transform 0.2s;
}

.stat-card::before {
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  content: "";
  background: transparent;
  transition: background 0.2s;
}

.stat-card:hover {
  border-color: #d3d8e0;
  box-shadow: 0 4px 12px rgba(16, 24, 40, 0.04);
  transform: translateY(-1px);
}

.stat-card--primary::before {
  background: #00c46a;
}

.stat-card--accent::before {
  background: #5b8def;
}

.stat-card--warning::before {
  background: #ffae00;
}

.stat-card--danger::before {
  background: #f56c6c;
}

.stat-card--default::before {
  background: #d3d8e0;
}

.stat-card__label {
  font-size: 13px;
  color: #667085;
}

.stat-card__value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  color: #101828;
  font-variant-numeric: tabular-nums;
}

.stat-card__hint {
  font-size: 12px;
  color: #98a2b3;
}
</style>
