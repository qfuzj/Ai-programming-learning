<!-- 极简风格标签管理页 -->
<template>
  <div class="page">
    <div class="header">
      <h1 class="title">标签管理</h1>
      <button class="btn-new" @click="showDialog = true">新增标签</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="list.length === 0" class="empty">暂无标签数据</div>

    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>名称</th>
            <th>分类</th>
            <th>作用域</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.name }}</td>
            <td>{{ item.category || "-" }}</td>
            <td>
              <span class="badge">{{ scopeText(item.scope) }}</span>
            </td>
            <td>
              <span class="link" @click="editItem(item)">编辑</span>
              <span class="link danger" @click="deleteItem(item.id)">删除</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showDialog" class="modal-overlay" @click.self="closeDialog">
      <div class="modal">
        <h2 class="modal-title">{{ editing ? "编辑标签" : "新增标签" }}</h2>
        <div class="form-group">
          <label class="label">名称</label>
          <input v-model="form.name" class="input" placeholder="标签名称" />
        </div>
        <div class="form-group">
          <label class="label">分类</label>
          <input v-model="form.category" class="input" placeholder="分类" />
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
  getAdminTagPage as getTagPage,
  createAdminTag as createTag,
  updateAdminTag as updateTag,
  deleteAdminTag as deleteTag,
} from "@/api/common";

const loading = ref(false);
const saving = ref(false);
const list = ref<any[]>([]);
const showDialog = ref(false);
const editing = ref<any>(null);

const form = reactive({ name: "", category: "" });

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getTagPage({ pageNum: 1, pageSize: 100 });
    list.value = res.records || [];
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
}

function editItem(item: any): void {
  editing.value = item;
  form.name = item.name;
  form.category = item.category || "";
  showDialog.value = true;
}
async function save(): Promise<void> {
  if (!form.name.trim()) {
    alert("请输入名称");
    return;
  }
  saving.value = true;
  try {
    if (editing.value) {
      await updateTag(editing.value.id, { name: form.name, category: form.category });
    } else {
      await createTag({ name: form.name, category: form.category });
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
    await deleteTag(id);
    loadData();
  } catch {
    alert("删除失败");
  }
}
function closeDialog(): void {
  showDialog.value = false;
  editing.value = null;
  form.name = "";
  form.category = "";
}
function scopeText(s: string): string {
  return { tag_scope_scenic: "景点", tag_scope_travel: "行程" }[s] || s || "-";
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
.badge {
  padding: 4px 10px;
  font-size: 12px;
  color: #000;
  background: #f0f0f0;
  border-radius: 999px;
}
.link {
  margin-right: 12px;
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
