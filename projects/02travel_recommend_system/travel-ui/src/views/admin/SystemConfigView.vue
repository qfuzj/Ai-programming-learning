<template>
  <div class="page-container">
    <el-card v-loading="loading" class="page-card">
      <template #header>
        <div class="card-header">系统配置</div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="配置键">
          <el-input v-model="query.keyword" clearable placeholder="如 llm.model" />
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
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadConfigs"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑配置" width="560px">
      <el-form ref="editFormRef" :model="editing" :rules="editRules" label-position="top">
        <el-form-item label="配置键">
          <el-input v-model="editing.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
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
import { onMounted, reactive, ref } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import {
  getSystemConfigByKey,
  getSystemConfigList,
  updateSystemConfig,
  type SystemConfigItem,
  type SystemConfigQuery,
  type SystemConfigUpdatePayload,
} from "@/api/system-config";

const loading = ref(false);
const saving = ref(false);
const configList = ref<SystemConfigItem[]>([]);
const total = ref(0);
const editDialogVisible = ref(false);
const editFormRef = ref<FormInstance>();

const query = reactive<SystemConfigQuery>({
  keyword: "",
  configGroup: "",
  pageNum: 1,
  pageSize: 10,
});

const editing = reactive<SystemConfigUpdatePayload & { configKey: string }>({
  configKey: "",
  configValue: "",
  configType: "string",
  configGroup: "default",
  description: "",
  isPublic: 0,
});

const editRules: FormRules<typeof editing> = {
  configValue: [{ required: true, message: "请输入配置值", trigger: "blur" }],
};

async function loadConfigs(): Promise<void> {
  loading.value = true;
  try {
    const page = await getSystemConfigList({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword?.trim() || undefined,
      configGroup: query.configGroup?.trim() || undefined,
    });
    configList.value = page.records;
    total.value = page.total;
  } catch {
    ElMessage.error("系统配置加载失败");
  } finally {
    loading.value = false;
  }
}

function onSearch(): void {
  query.pageNum = 1;
  void loadConfigs();
}

function onReset(): void {
  query.keyword = "";
  query.configGroup = "";
  query.pageNum = 1;
  query.pageSize = 10;
  void loadConfigs();
}

function onSizeChange(): void {
  query.pageNum = 1;
  void loadConfigs();
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
    editFormRef.value?.clearValidate();
  } catch {
    ElMessage.error("配置详情加载失败");
  }
}

async function onSave(): Promise<void> {
  if (!editing.configKey) {
    return;
  }
  const valid = await editFormRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }
  saving.value = true;
  try {
    const payload: SystemConfigUpdatePayload = {
      configValue: editing.configValue,
      configType: editing.configType,
      configGroup: editing.configGroup,
      description: editing.description,
      isPublic: editing.isPublic,
    };
    await updateSystemConfig(editing.configKey, payload);
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

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.page-container {
  padding: 0px;
}
</style>
