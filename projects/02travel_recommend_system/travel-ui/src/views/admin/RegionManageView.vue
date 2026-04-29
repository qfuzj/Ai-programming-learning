<!-- 极简风格地区管理页 -->
<template>
  <div class="page">
    <div class="header">
      <h1 class="title">地区管理</h1>
      <button class="btn-new" @click="showCreate = true">新增地区</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="regionTree.length === 0" class="empty">暂无地区数据</div>

    <div v-else class="tree">
      <div v-for="r in regionTree" :key="r.id" class="tree-item">
        <div class="tree-row">
          <span class="name">{{ r.name }}</span>
          <span class="level">{{ levelText(r.level) }}</span>
          <span class="actions">
            <span class="link" @click="editItem(r)">编辑</span>
            <span class="link danger" @click="deleteItem(r.id)">删除</span>
          </span>
        </div>
        <div v-if="r.children && r.children.length > 0" class="tree-children">
          <div v-for="c in r.children" :key="c.id" class="tree-row child">
            <span class="name">{{ c.name }}</span>
            <span class="level">{{ levelText(c.level) }}</span>
            <span class="actions">
              <span class="link" @click="editItem(c)">编辑</span>
              <span class="link danger" @click="deleteItem(c.id)">删除</span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showCreate || editing" class="modal-overlay" @click.self="closeDialog">
      <div class="modal">
        <h2 class="modal-title">{{ editing ? "编辑地区" : "新增地区" }}</h2>
        <div class="form-group">
          <label class="label">名称</label>
          <input v-model="form.name" class="input" placeholder="地区名称" />
        </div>
        <div class="form-group">
          <label class="label">层级</label>
          <select v-model="form.level" class="input">
            <option :value="1">省</option>
            <option :value="2">市</option>
            <option :value="3">区/县</option>
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
  getRegionTree,
  createAdminRegion as createRegion,
  updateAdminRegion as updateRegion,
  deleteAdminRegion as deleteRegion,
} from "@/api/common";

const loading = ref(false);
const saving = ref(false);
const regionTree = ref<any[]>([]);
const showCreate = ref(false);
const editing = ref<any>(null);

const form = reactive({ name: "", level: 1 });
function levelText(level: number): string {
  if (level === 1) return "省";
  if (level === 2) return "市";
  if (level === 3) return "区/县";
  return "未知";
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    regionTree.value = await getRegionTree();
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
}

function editItem(item: any): void {
  editing.value = item;
  form.name = item.name;
  form.level = item.level;
  showCreate.value = true;
}
async function save(): Promise<void> {
  if (!form.name.trim()) {
    alert("请输入名称");
    return;
  }
  saving.value = true;
  try {
    if (editing.value) {
      await updateRegion(editing.value.id, {
        parentId: editing.value.parentId || 0,
        name: form.name,
        level: form.level,
      });
    } else {
      await createRegion({ parentId: 0, name: form.name, level: form.level });
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
    await deleteRegion(id);
    loadData();
  } catch {
    alert("删除失败");
  }
}
function closeDialog(): void {
  showCreate.value = false;
  editing.value = null;
  form.name = "";
  form.level = 1;
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
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 8px;
}
.btn-new:hover {
  background: #00c665;
}
.tree {
  padding: 16px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
}
.tree-item {
  border-bottom: 1px solid #f0f0f0;
}
.tree-item:last-child {
  border-bottom: none;
}
.tree-row {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 12px;
}
.tree-row.child {
  padding-left: 32px;
}
.name {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: #000;
}
.level {
  padding: 4px 10px;
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  border-radius: 999px;
}
.actions {
  display: flex;
  gap: 12px;
}
.link {
  font-size: 13px;
  color: #000;
  cursor: pointer;
}
.link:hover {
  color: #00c665;
}
.link.danger {
  color: #ff5252;
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
