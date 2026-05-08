<template>
  <div class="admin-page">
    <el-form :model="query" class="filter-panel" inline>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="配置键 / 说明"
          @keyup.enter="search"
        />
      </el-form-item>
      <el-form-item label="配置分组">
        <el-select v-model="query.configGroup" clearable placeholder="全部分组">
          <el-option
            v-for="item in configGroupOptions"
            :key="item.code"
            :label="item.desc"
            :value="String(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item class="form-actions">
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="configKey" label="配置键" min-width="220" show-overflow-tooltip />
      <el-table-column prop="configValue" label="配置值" min-width="240" show-overflow-tooltip />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ dictText(configTypeOptions, row.configType) }}</template>
      </el-table-column>
      <el-table-column label="分组" width="140">
        <template #default="{ row }">{{ dictText(configGroupOptions, row.configGroup) }}</template>
      </el-table-column>
      <el-table-column label="前端可见" width="110">
        <template #default="{ row }">
          <el-tag :type="row.isPublic === 1 ? 'success' : 'info'">
            {{ dictText(yesNoOptions, row.isPublic) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
      <el-table-column prop="updateTime" label="更新时间" min-width="170" />
      <el-table-column fixed="right" label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="loadData"
        @size-change="search"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="编辑配置" width="680px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="配置键">
          <el-input v-model="form.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值" required>
          <el-input
            v-if="form.configType === 'json'"
            v-model="form.configValue"
            type="textarea"
            :rows="8"
            placeholder="JSON 配置"
          />
          <el-input v-else v-model="form.configValue" placeholder="配置值" />
        </el-form-item>
        <el-form-item label="值类型" required>
          <el-select v-model="form.configType">
            <el-option
              v-for="item in configTypeOptions"
              :key="item.code"
              :label="item.desc"
              :value="String(item.code)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="配置分组" required>
          <el-select v-model="form.configGroup">
            <el-option
              v-for="item in configGroupOptions"
              :key="item.code"
              :label="item.desc"
              :value="String(item.code)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="配置说明">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="前端可见" required>
          <el-radio-group v-model="form.isPublic">
            <el-radio-button
              v-for="item in yesNoOptions"
              :key="item.code"
              :value="Number(item.code)"
            >
              {{ item.desc }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { getConfigGroupDict, getConfigTypeDict, getYesNoFlagDict, type DictItem } from "@/api/dict";
import {
  getSystemConfigByKey,
  getSystemConfigList,
  updateSystemConfig,
  type SystemConfigItem,
  type SystemConfigQuery,
  type SystemConfigUpdatePayload,
} from "@/api/system-config";
import { findDictDesc } from "@/composables/useDictOptions";

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const list = ref<SystemConfigItem[]>([]);
const total = ref(0);
const configGroupOptions = ref<DictItem[]>([]);
const configTypeOptions = ref<DictItem[]>([]);
const yesNoOptions = ref<DictItem[]>([]);

const query = reactive<SystemConfigQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  configGroup: undefined,
});
const form = reactive<SystemConfigUpdatePayload & { configKey: string }>({
  configKey: "",
  configValue: "",
  configType: "string",
  configGroup: "default",
  description: "",
  isPublic: 0,
});

function dictText(options: DictItem[], code: unknown): string {
  return findDictDesc(options, code, "-");
}

async function loadDictionaries(): Promise<void> {
  [configGroupOptions.value, configTypeOptions.value, yesNoOptions.value] = await Promise.all([
    getConfigGroupDict(),
    getConfigTypeDict(),
    getYesNoFlagDict(),
  ]);
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getSystemConfigList({
      ...query,
      keyword: query.keyword?.trim() || undefined,
    });
    list.value = res.records || [];
    total.value = res.total || 0;
  } finally {
    loading.value = false;
  }
}

function search(): void {
  query.pageNum = 1;
  void loadData();
}

function resetQuery(): void {
  query.pageNum = 1;
  query.keyword = "";
  query.configGroup = undefined;
  void loadData();
}

async function openEdit(item: SystemConfigItem): Promise<void> {
  const detail = await getSystemConfigByKey(item.configKey);
  Object.assign(form, {
    configKey: detail.configKey,
    configValue: detail.configValue || "",
    configType: detail.configType || "string",
    configGroup: detail.configGroup || "default",
    description: detail.description || "",
    isPublic: detail.isPublic ?? 0,
  });
  dialogVisible.value = true;
}

async function save(): Promise<void> {
  if (!form.configValue.trim()) {
    ElMessage.warning("请输入配置值");
    return;
  }
  if (form.configType === "json") {
    try {
      JSON.parse(form.configValue);
    } catch {
      ElMessage.warning("JSON 配置值格式不正确");
      return;
    }
  }
  saving.value = true;
  try {
    await updateSystemConfig(form.configKey, {
      configValue: form.configValue,
      configType: form.configType,
      configGroup: form.configGroup,
      description: form.description || undefined,
      isPublic: form.isPublic,
    });
    ElMessage.success("保存成功");
    dialogVisible.value = false;
    await loadData();
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  await loadDictionaries();
  await loadData();
});
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-head h1 {
  margin: 0;
  font-size: 28px;
  color: #101828;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 700;
  color: #4f7cff;
  letter-spacing: 0.08em;
}

.filter-panel {
  padding: 16px 16px 0;
  background: #fff;
  border: 1px solid #e7eaf0;
  border-radius: 8px;
}

.pager {
  display: flex;
  justify-content: flex-end;
}

.filter-panel {
  position: relative;
}
</style>
