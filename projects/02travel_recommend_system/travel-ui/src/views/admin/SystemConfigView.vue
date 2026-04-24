<template>
  <div class="page-container">
    <el-card v-loading="loading" class="page-card">
      <template #header>
        <div class="card-header">系统配置</div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="配置键">
          <el-input
            v-model="query.keyword"
            clearable
            placeholder="如 llm.model"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="配置分组">
          <el-select v-model="query.configGroup" clearable placeholder="全部" style="width: 140px">
            <el-option
              v-for="item in configGroupOptions"
              :key="item.code"
              :label="item.desc"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item style="margin-left: auto">
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="configList">
        <el-table-column prop="configKey" label="配置键" min-width="180" show-overflow-tooltip />
        <el-table-column prop="configValue" label="配置值" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="scope">
            {{ findDictDesc(configTypeOptions, scope.row.configType, scope.row.configType) }}
          </template>
        </el-table-column>
        <el-table-column label="分组" width="120">
          <template #default="scope">
            {{ findDictDesc(configGroupOptions, scope.row.configGroup, scope.row.configGroup) }}
          </template>
        </el-table-column>
        <el-table-column label="前端可见" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isPublic === 1 ? 'success' : 'info'">
              {{ findDictDesc(yesNoOptions, scope.row.isPublic, "-") }}
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

    <el-dialog
      v-model="editDialogVisible"
      title="编辑配置"
      width="620px"
      class="config-form-dialog"
    >
      <div class="dialog-body-scroll">
        <el-form ref="editFormRef" :model="editing" :rules="editRules" label-position="top">
          <div class="form-section">
            <div class="form-section-title">配置信息</div>
            <div class="form-section-content">
              <el-form-item label="配置键">
                <el-input v-model="editing.configKey" disabled />
              </el-form-item>
              <el-form-item label="配置值" prop="configValue">
                <el-input v-model="editing.configValue" type="textarea" :rows="3" />
              </el-form-item>
            </div>
          </div>

          <div class="form-section">
            <div class="form-section-title">元数据</div>
            <div class="form-section-content">
              <div class="form-row-grid">
                <el-form-item label="配置类型">
                  <el-input v-model="editing.configType" disabled />
                </el-form-item>
                <el-form-item label="配置分组">
                  <el-input v-model="editing.configGroup" disabled />
                </el-form-item>
              </div>
              <el-form-item label="说明">
                <el-input v-model="editing.description" />
              </el-form-item>
            </div>
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button :loading="saving" type="primary" @click="onSave">保存</el-button>
        </div>
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
import { getConfigGroupDict, getConfigTypeDict, getYesNoFlagDict } from "@/api/dict";
import { findDictDesc, useDictOptions } from "@/composables/useDictOptions";

const { options: configGroupOptions } = useDictOptions("config-group", getConfigGroupDict);
const { options: configTypeOptions } = useDictOptions("config-type", getConfigTypeDict);
const { options: yesNoOptions } = useDictOptions("yes-no-flag", getYesNoFlagDict);

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
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 6px;
  margin-bottom: 12px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 0;
}

.filter-form :deep(.el-form-item__label) {
  padding-right: 6px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.page-container {
  padding: 0px;
}

.config-form-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-right: 0;
}

.config-form-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 500;
}

.config-form-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.config-form-dialog .dialog-body-scroll {
  max-height: calc(85vh - 120px);
  overflow-y: auto;
  padding: 20px;
}

.config-form-dialog .form-section {
  margin-bottom: 24px;
}

.config-form-dialog .form-section:last-child {
  margin-bottom: 0;
}

.config-form-dialog .form-section-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.config-form-dialog :deep(.el-form-item__label) {
  font-size: 13px;
  color: #606266;
  line-height: 20px;
  padding-bottom: 4px;
}

.config-form-dialog :deep(.el-form-item) {
  margin-bottom: 16px;
}

.config-form-dialog :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.config-form-dialog .form-row-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.config-form-dialog .form-row-grid :deep(.el-form-item) {
  margin-bottom: 16px;
}

.config-form-dialog :deep(.el-input__wrapper),
.config-form-dialog :deep(.el-textarea__inner),
.config-form-dialog :deep(.el-input-number .el-input__wrapper) {
  border-radius: 4px;
}

.config-form-dialog :deep(.el-input__wrapper) {
  border-color: #dcdfe6;
}

.config-form-dialog :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.config-form-dialog .dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
}

.config-form-dialog :deep(.el-dialog__footer) {
  padding: 0;
  border-top: none;
}
</style>
