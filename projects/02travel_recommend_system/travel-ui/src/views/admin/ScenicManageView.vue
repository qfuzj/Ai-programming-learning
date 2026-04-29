<!-- 极简风格景点管理页 -->
<template>
  <div class="page">
    <div class="header">
      <h1 class="title">景点管理</h1>
      <button class="btn-new" @click="showCreate = true">新增景点</button>
    </div>

    <div class="filter-bar">
      <input
        v-model="query.keyword"
        class="filter-input"
        placeholder="搜索景点..."
        @keyup.enter="loadData"
      />
      <select v-model="query.status" class="filter-select" @change="loadData">
        <option :value="undefined">全部状态</option>
        <option :value="1">上架</option>
        <option :value="0">下架</option>
      </select>
      <button class="btn-search" @click="loadData">查询</button>
      <button class="btn-reset" @click="resetQuery">重置</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="list.length === 0" class="empty">暂无景点数据</div>

    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>景点名称</th>
            <th>地区</th>
            <th>等级</th>
            <th>评分</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.name }}</td>
            <td>{{ item.regionName || "-" }}</td>
            <td>{{ item.level || "-" }}</td>
            <td>{{ item.score ? item.score.toFixed(1) : "-" }}</td>
            <td>
              <span class="badge" :class="item.status === 1 ? 'on' : 'off'">
                {{ item.status === 1 ? "上架" : "下架" }}
              </span>
            </td>
            <td>
              <span class="link" @click="editItem(item)">编辑</span>
              <span class="link danger" @click="deleteItem(item.id)">删除</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showCreate || editingItem" class="modal-overlay" @click.self="closeDialog">
      <div class="modal">
        <h2 class="modal-title">{{ editingItem ? "编辑景点" : "新增景点" }}</h2>
        <div class="form-group">
          <label class="label">名称</label>
          <input v-model="form.name" class="input" placeholder="景点名称" />
        </div>
        <div class="form-group">
          <label class="label">地区</label>
          <select v-model="form.regionId" class="input">
            <option :value="undefined">请选择</option>
            <option v-for="r in regionList" :key="r.id" :value="r.id">{{ r.name }}</option>
          </select>
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
  getAdminScenicPage as getScenicList,
  createAdminScenic as createScenic,
  updateAdminScenic as updateScenic,
  deleteAdminScenic as deleteScenic,
} from "@/api/scenic";
import { getRegionTree } from "@/api/common";

const loading = ref(false);
const saving = ref(false);
const list = ref<any[]>([]);
const regionList = ref<any[]>([]);
const showCreate = ref(false);
const editingItem = ref<any>(null);

const query = reactive({
  keyword: "",
  status: undefined as number | undefined,
});

const form = reactive({
  name: "",
  regionId: undefined as number | undefined,
});

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getScenicList({
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

async function loadRegions(): Promise<void> {
  try {
    regionList.value = await getRegionTree();
  } catch {
    /* empty */
  }
}

function resetQuery(): void {
  query.keyword = "";
  query.status = undefined;
  loadData();
}

function editItem(item: any): void {
  editingItem.value = item;
  form.name = item.name;
  form.regionId = item.regionId;
  showCreate.value = true;
}

async function save(): Promise<void> {
  if (!form.name.trim()) {
    alert("请输入名称");
    return;
  }
  saving.value = true;
  try {
    if (editingItem.value) {
      await updateScenic(editingItem.value.id, { name: form.name, regionId: form.regionId });
    } else {
      await createScenic({ name: form.name, regionId: form.regionId } as any);
    }
    closeDialog();
    loadData();
  } catch {
    alert("保存失败");
  } finally {
    saving.value = false;
  }
}

async function deleteItem(id: number): Promise<void> {
  if (!confirm("确定删除？")) return;
  try {
    await deleteScenic(id);
    loadData();
  } catch {
    alert("删除失败");
  }
}

function closeDialog(): void {
  showCreate.value = false;
  editingItem.value = null;
  form.name = "";
  form.regionId = undefined;
}

onMounted(() => {
  loadData();
  loadRegions();
});
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}
.title {
  font-size: 28px;
  font-weight: 700;
  color: #000;
  margin: 0 0 32px 0;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.btn-new {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: #000;
  background: #00e676;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.btn-new:hover {
  background: #00c665;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.filter-input {
  flex: 1;
  min-width: 200px;
  padding: 10px 14px;
  font-size: 14px;
  color: #000;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  outline: none;
}
.filter-input:focus {
  border-color: #00e676;
}
.filter-select {
  padding: 10px 12px;
  font-size: 14px;
  color: #000;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  outline: none;
  cursor: pointer;
}
.btn-search {
  padding: 10px 20px;
  font-size: 14px;
  color: #000;
  background: #00e676;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.btn-reset {
  padding: 10px 20px;
  font-size: 14px;
  color: #666;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
}

.table-wrap {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
}
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th {
  text-align: left;
  padding: 14px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #666;
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
.badge {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 999px;
}
.badge.on {
  background: #e8f5e9;
  color: #000;
}
.badge.off {
  background: #f5f5f5;
  color: #999;
}
.link {
  font-size: 13px;
  color: #000;
  cursor: pointer;
  margin-right: 12px;
}
.link:hover {
  color: #00c665;
}
.link.danger {
  color: #ff5252;
}
.link.danger:hover {
  color: #ff1744;
}

.loading,
.empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: grid;
  place-items: center;
  z-index: 1000;
}
.modal {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  width: 100%;
  max-width: 500px;
}
.modal-title {
  font-size: 22px;
  font-weight: 700;
  color: #000;
  margin: 0 0 24px 0;
}
.form-group {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.label {
  font-size: 14px;
  font-weight: 500;
  color: #000;
}
.input {
  padding: 10px 14px;
  font-size: 14px;
  color: #000;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  outline: none;
  font-family: inherit;
}
.input:focus {
  border-color: #00e676;
}
.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  justify-content: flex-end;
}
.btn-cancel {
  padding: 10px 20px;
  font-size: 14px;
  color: #666;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
}
.btn-submit {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #000;
  background: #00e676;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
