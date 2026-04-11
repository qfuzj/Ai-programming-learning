<!-- 系统配置页：查询 + 编辑配置。 -->
<template>
  <div class="page-container">
    <el-card v-loading="loading" class="page-card">
      <template #header>系统配置</template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="配置分组">
          <el-input v-model="query.configGroup" clearable placeholder="如 llm / recommend" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="configList">
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

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadConfigs"
          @size-change="onSizeChange"
        />
      </div>
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
              <el-input v-model="editing.configType" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配置分组">
              <el-input v-model="editing.configGroup" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="说明">
          <el-input v-model="editing.description" />
        </el-form-item>
        <el-form-item label="前端可见">
          <el-switch v-model="editing.isPublic" :active-value="1" :inactive-value="0" />
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
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  getSystemConfigDetail,
  getSystemConfigPage,
  updateSystemConfig,
  type SystemConfigItem,
} from "@/api/admin-config";

const loading = ref(false);
const saving = ref(false);
const total = ref(0);
const configList = ref<SystemConfigItem[]>([]);
const editDialogVisible = ref(false);

const query = reactive({
  pageNum: 1,
  pageSize: 10,
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

async function loadConfigs(): Promise<void> {
  loading.value = true;
  try {
    const page = await getSystemConfigPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      configGroup: query.configGroup || undefined,
    });
    configList.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
}

function onSearch(): void {
  query.pageNum = 1;
  void loadConfigs();
}

function onSizeChange(): void {
  query.pageNum = 1;
  void loadConfigs();
}

async function onEdit(configKey: string): Promise<void> {
  const detail = await getSystemConfigDetail(configKey);
  editing.configKey = detail.configKey;
  editing.configValue = detail.configValue;
  editing.configType = detail.configType;
  editing.configGroup = detail.configGroup;
  editing.description = detail.description ?? "";
  editing.isPublic = detail.isPublic;
  editDialogVisible.value = true;
}

async function onSave(): Promise<void> {
  if (!editing.configKey) return;
  saving.value = true;
  try {
    await updateSystemConfig(editing.configKey, {
      configValue: editing.configValue,
      configType: editing.configType,
      configGroup: editing.configGroup,
      description: editing.description,
      isPublic: editing.isPublic,
    });
    ElMessage.success("配置已更新");
    editDialogVisible.value = false;
    await loadConfigs();
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadConfigs();
});
</script>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>