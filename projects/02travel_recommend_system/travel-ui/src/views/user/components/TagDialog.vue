<template>
  <el-dialog v-model="visible" title="编辑偏好标签" width="560px" destroy-on-close>
    <div v-for="group in tagGroups" :key="group.type" class="tag-group">
      <div class="tag-group-title">{{ group.label }}</div>
      <div class="tag-group-list">
        <el-tag
          v-for="tag in group.tags"
          :key="tag.id"
          class="selectable-tag"
          :effect="selectedIds.includes(tag.id) ? 'dark' : 'plain'"
          round
          style="cursor: pointer; margin: 4px"
          @click="toggleTag(tag.id)"
        >
          {{ tag.name }}
        </el-tag>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="saving"
        class="btn-primary"
        @click="emit('save', selectedIds)"
      >
        保存（已选 {{ selectedIds.length }} 个）
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import type { CommonTagItem } from "@/api/common";

interface TagGroup {
  type: number;
  label: string;
  tags: CommonTagItem[];
}

interface Props {
  modelValue: boolean;
  allTags: CommonTagItem[];
  initialSelectedIds: number[];
  saving?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  save: [ids: number[]];
}>();

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const selectedIds = ref<number[]>([]);
watch(
  () => props.modelValue,
  (isOpen) => {
    if (isOpen) selectedIds.value = [...props.initialSelectedIds];
  }
);

const tagGroups = computed<TagGroup[]>(() =>
  [
    { type: 1, label: "景点标签", tags: props.allTags.filter((t) => t.type === 1) },
    { type: 2, label: "偏好标签", tags: props.allTags.filter((t) => t.type === 2) },
  ].filter((g) => g.tags.length > 0)
);

function toggleTag(id: number): void {
  const index = selectedIds.value.indexOf(id);
  if (index === -1) {
    selectedIds.value.push(id);
  } else {
    selectedIds.value.splice(index, 1);
  }
}
</script>

<style scoped>
.btn-primary {
  background-color: #34e0a1;
  border-color: #34e0a1;
}

.tag-group {
  margin-bottom: 20px;
}

.tag-group-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid #f0f0f0;
}

.tag-group-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.selectable-tag {
  transition: all 0.2s;
}

.selectable-tag:hover {
  opacity: 0.8;
}
</style>
