<!-- 极简风格系统配置页 -->
<template>
  <div class="page">
    <h1 class="title">系统配置</h1>

    <div class="filter-bar">
      <input
        v-model="query.keyword"
        class="filter-input"
        placeholder="搜索配置键..."
        @keyup.enter="loadData"
      />
      <select v-model="query.configGroup" class="filter-select" @change="loadData">
        <option :value="undefined">全部分组</option>
        <option value="llm">LLM</option>
        <option value="system">系统</option>
        <option value="recommend">推荐</option>
      </select>
      <button class="btn-search" @click="loadData">查询</button>
      <button class="btn-reset" @click="resetQuery">重置</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="list.length === 0" class="empty">暂无配置数据</div>

    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>配置键</th>
            <th>值</th>
            <th>分组</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.configKey">
            <td>{{ item.configKey }}</td>
            <td class="mono">{{ item.configValue }}</td>
            <td>
              <span class="badge">{{ item.configGroup || "-" }}</span>
            </td>
            <td><span class="link" @click="editItem(item)">编辑</span></td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showDialog" class="modal-overlay" @click.self="closeDialog">
      <div class="modal">
        <h2 class="modal-title">编辑配置</h2>
        <div class="form-group">
          <label class="label">配置键</label>
          <input v-model="form.configKey" class="input" disabled />
        </div>
        <div class="form-group">
          <label class="label">值</label>
          <input v-model="form.configValue" class="input" placeholder="配置值" />
        </div>
        <div class="form-actions">
          <button class="btn-cancel" @click="closeDialog">取消</button>
          <button class="btn-submit" :disabled="saving" @click="save">
            {{ saving ? "保存中..." : "保存" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import {
  getSystemConfigList as getSystemConfigPage,
  updateSystemConfig,
} from "@/api/system-config";

const loading = ref(false);
const saving = ref(false);
const list = ref<any[]>([]);
const showDialog = ref(false);
const editingItem = ref<any>(null);

const query = reactive({ keyword: "", configGroup: undefined as string | undefined });

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getSystemConfigPage({
      pageNum: 1,
      pageSize: 50,
      keyword: query.keyword || undefined,
    });
    list.value = res.records || [];
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
}

function resetQuery(): void {
  query.keyword = "";
  query.configGroup = undefined;
  loadData();
}

const form = reactive({ configKey: "", configValue: "" });

function editItem(item: any): void {
  editingItem.value = item;
  form.configKey = item.configKey;
  form.configValue = item.configValue || "";
  showDialog.value = true;
}
async function save(): Promise<void> {
  saving.value = true;
  try {
    await updateSystemConfig(form.configKey, {
      configValue: form.configValue,
      configType: editingItem.value?.configType || "",
      configGroup: editingItem.value?.configGroup || "",
      isPublic: editingItem.value?.isPublic ?? 1,
    });
    closeDialog();
    loadData();
  } catch {
    alert("保存失败");
  } finally {
    saving.value = false;
  }
}
function closeDialog(): void {
  showDialog.value = false;
  editingItem.value = null;
}

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.page {
  max-width: 1200px;
  padding: 40px 24px;
  margin: 0 auto;
}
.title {
  margin: 0 0 32px 0;
  font-size: 28px;
  font-weight: 700;
  color: #000;
}
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}
.filter-input {
  flex: 1;
  min-width: 200px;
  padding: 10px 14px;
  font-size: 14px;
  color: #000;
  outline: none;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}
.filter-input:focus {
  border-color: #00e676;
}
.filter-select {
  padding: 10px 12px;
  font-size: 14px;
  color: #000;
  cursor: pointer;
  outline: none;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}
.btn-search {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 8px;
}
.btn-reset {
  padding: 10px 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}
.table-wrap {
  overflow: hidden;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
}
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th {
  padding: 14px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #666;
  text-align: left;
  background: #f9f9f9;
  border-bottom: 1px solid #f0f0f0;
}
.table td {
  padding: 14px 16px;
  font-size: 14px;
  color: #000;
  border-bottom: 1px solid #f0f0f0;
}
.table tr:hover {
  background: #f9fff9;
}
.mono {
  font-family: monospace;
  font-size: 13px;
}
.badge {
  padding: 4px 10px;
  font-size: 12px;
  color: #000;
  background: #f0f0f0;
  border-radius: 999px;
}
.link {
  font-size: 13px;
  color: #000;
  cursor: pointer;
}
.link:hover {
  color: #00c665;
}
.loading,
.empty {
  padding: 60px 20px;
  font-size: 14px;
  color: #999;
  text-align: center;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  background: rgba(0, 0, 0, 0.3);
}
.modal {
  width: 100%;
  max-width: 500px;
  padding: 32px;
  background: #fff;
  border-radius: 16px;
}
.modal-title {
  margin: 0 0 24px 0;
  font-size: 22px;
  font-weight: 700;
  color: #000;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
}
.label {
  font-size: 14px;
  font-weight: 500;
  color: #000;
}
.input {
  padding: 10px 14px;
  font-family: inherit;
  font-size: 14px;
  color: #000;
  outline: none;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}
.input:focus {
  border-color: #00e676;
}
.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 16px;
}
.btn-cancel {
  padding: 10px 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}
.btn-submit {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 8px;
}
</style>
