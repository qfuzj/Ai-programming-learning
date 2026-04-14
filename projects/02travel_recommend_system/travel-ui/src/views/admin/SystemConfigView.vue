<template>
  <div class="page-container">
    <el-card v-loading="loading" class="page-card">
      <template #header>
        <div class="card-header">系统配置</div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="配置键">
          <el-input v-model="query.configKey" clearable placeholder="如 llm.model" />
        </el-form-item>
        <el-form-item label="配置分组">
          <el-input v-model="query.configGroup" clearable placeholder="如 llm / recommend" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="filteredConfigList">
        <el-table-column prop="configKey" label="配置键" min-width="180" show-overflow-tooltip />
        <el-table-column prop="configValue" label="配置值" min-width="220" show-overflow-tooltip />
        <el-table-column prop="configType" label="类型" width="100" />
        <el-table-column prop="configGroup" label="分组" width="120" />
        <el-table-column label="前端可见" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isPublic === 1 ? 'success' : 'info'">
              {{ scope.row.isPublic === 1 ? "是" : "否" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="primary" text @click="onEdit(scope.row.configKey)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑配置" width="560px">
      <el-form label-position="top">
        <el-form-item label="配置键">
          <el-input v-model="editing.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="editing.configValue" type="textarea" :rows="4" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="配置类型">
              <el-input v-model="editing.configType" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配置分组">
              <el-input v-model="editing.configGroup" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="说明">
          <el-input v-model="editing.description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button :loading="saving" type="primary" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  getSystemConfigByKey,
  getSystemConfigList,
  updateSystemConfig,
  type SystemConfigItem,
} from "@/api/system-config";

const loading = ref(false);
const saving = ref(false);
const configList = ref<SystemConfigItem[]>([]);
const editDialogVisible = ref(false);

const query = reactive({
  configKey: "",
  configGroup: "",
});

const editing = reactive({
  configKey: "",
  configValue: "",
  configType: "string",
  configGroup: "default",
  description: "",
  isPublic: 0,
});

const filteredConfigList = computed(() => {
  const key = query.configKey.trim().toLowerCase();
  const group = query.configGroup.trim().toLowerCase();
  return configList.value.filter((item) => {
    const itemKey = (item.configKey ?? "").toLowerCase();
    const itemGroup = (item.configGroup ?? "").toLowerCase();
    const matchKey = !key || itemKey.includes(key);
    const matchGroup = !group || itemGroup.includes(group);
    return matchKey && matchGroup;
  });
});

async function loadConfigs(): Promise<void> {
  loading.value = true;
  try {
    configList.value = await getSystemConfigList();
  } catch {
    ElMessage.error("系统配置加载失败");
  } finally {
    loading.value = false;
  }
}

function onSearch(): void {
  // 本地过滤，无需远程分页查询。
}

function onReset(): void {
  query.configKey = "";
  query.configGroup = "";
}

async function onEdit(configKey: string): Promise<void> {
  try {
    const detail = await getSystemConfigByKey(configKey);
    editing.configKey = detail.configKey ?? "";
    editing.configValue = detail.configValue ?? "";
    editing.configType = detail.configType ?? "string";
    editing.configGroup = detail.configGroup ?? "default";
    editing.description = detail.description ?? "";
    editing.isPublic = detail.isPublic ?? 0;
    editDialogVisible.value = true;
  } catch {
    ElMessage.error("配置详情加载失败");
  }
}

async function onSave(): Promise<void> {
  if (!editing.configKey) {
    return;
  }
  saving.value = true;
  try {
    await updateSystemConfig(editing.configKey, {
      configValue: editing.configValue,
      description: editing.description,
    });
    ElMessage.success("配置已更新");
    editDialogVisible.value = false;
    await loadConfigs();
  } catch {
    ElMessage.error("配置更新失败");
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadConfigs();
});
</script>

<style scoped>
.card-header {
  font-size: 16px;
  font-weight: 700;
}

.filter-form {
  margin-bottom: 12px;
}

.page-container {
  padding: 0px;
}
</style>
