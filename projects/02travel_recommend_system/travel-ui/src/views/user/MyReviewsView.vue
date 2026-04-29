<!-- 极简风格我的点评页 -->
<template>
  <div class="page">
    <div class="header">
      <h1 class="title">我的点评</h1>
      <button v-if="!isWriting" class="btn-write" @click="startWrite">写点评</button>
      <button v-else class="btn-back" @click="isWriting = false">返回列表</button>
    </div>

    <!-- 写点评表单 -->
    <div v-if="isWriting" class="review-form">
      <div class="form-group">
        <label class="label">选择景点</label>
        <select v-model="writeForm.scenicId" class="select">
          <option :value="undefined">请选择景点</option>
          <option v-for="s in scenicOptions" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>
      <div class="form-group">
        <label class="label">评分</label>
        <select v-model="writeForm.score" class="select">
          <option :value="5">5分 - 很棒</option>
          <option :value="4">4分 - 不错</option>
          <option :value="3">3分 - 还行</option>
          <option :value="2">2分 - 较差</option>
          <option :value="1">1分 - 很差</option>
        </select>
      </div>
      <div class="form-group">
        <label class="label">点评内容</label>
        <textarea
          v-model="writeForm.content"
          class="textarea"
          placeholder="分享您的旅行体验..."
          rows="5"
        ></textarea>
      </div>
      <div class="form-actions">
        <button class="btn-submit" :disabled="submitLoading" @click="submitReview">提交点评</button>
      </div>
    </div>

    <!-- 点评列表 -->
    <div v-else>
      <div v-if="loading" class="skeleton-list">
        <div v-for="n in 4" :key="n" class="skeleton-item">
          <div class="skeleton-avatar"></div>
          <div class="skeleton-lines">
            <div class="skeleton-line"></div>
            <div class="skeleton-line short"></div>
          </div>
        </div>
      </div>

      <div v-else-if="list.length === 0" class="empty">暂无点评记录</div>

      <div v-else class="list">
        <div v-for="item in list" :key="item.id" class="list-item">
          <div class="item-header">
            <div class="item-info">
              <span class="scenic-name" @click="router.push(`/scenic/${item.scenicId}`)">
                {{ item.scenicName }}
              </span>
              <span class="score">{{ item.score }}分</span>
            </div>
            <span class="time">{{ item.createdAt }}</span>
          </div>
          <p class="content">{{ item.content }}</p>
          <div class="item-actions">
            <span v-if="item.likeCount" class="likes">{{ item.likeCount }}人觉得有用</span>
            <button class="btn-delete" @click="deleteReview(item.id)">删除</button>
          </div>
        </div>
      </div>

      <div v-if="total > 0" class="pagination">
        <button class="page-btn" :disabled="pageNum <= 1" @click="changePage(pageNum - 1)">
          上一页
        </button>
        <span class="page-info">{{ pageNum }} / {{ Math.ceil(total / pageSize) }}</span>
        <button
          class="page-btn"
          :disabled="pageNum >= Math.ceil(total / pageSize)"
          @click="changePage(pageNum + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getMyReviews, deleteMyReview, submitReview as createReview } from "@/api/audit";
import { getScenicPage as getScenicList } from "@/api/scenic";
import type { ReviewItem } from "@/api/audit";

const router = useRouter();
const loading = ref(false);
const list = ref<ReviewItem[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const isWriting = ref(false);
const submitLoading = ref(false);
const scenicOptions = ref<{ id: number; name: string }[]>([]);

const writeForm = reactive({
  scenicId: undefined as number | undefined,
  score: 5,
  content: "",
});

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getMyReviews({ pageNum: pageNum.value, pageSize: pageSize.value });
    list.value = res.records || [];
    total.value = res.total || 0;
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
}

async function loadScenicOptions(): Promise<void> {
  try {
    const res = await getScenicList({ pageNum: 1, pageSize: 100 });
    scenicOptions.value = (res.records || []).map((s) => ({ id: s.id, name: s.name }));
  } catch {
    // ignore
  }
}

function startWrite(): void {
  isWriting.value = true;
  if (scenicOptions.value.length === 0) {
    loadScenicOptions();
  }
}

async function submitReview(): Promise<void> {
  if (!writeForm.scenicId) {
    alert("请选择景点");
    return;
  }
  if (!writeForm.content.trim()) {
    alert("请输入点评内容");
    return;
  }
  submitLoading.value = true;
  try {
    await createReview({
      scenicId: writeForm.scenicId,
      score: writeForm.score,
      content: writeForm.content,
    });
    isWriting.value = false;
    writeForm.scenicId = undefined;
    writeForm.content = "";
    pageNum.value = 1;
    loadData();
  } catch {
    alert("提交失败");
  } finally {
    submitLoading.value = false;
  }
}

async function deleteReview(id: number): Promise<void> {
  if (!confirm("确定删除该点评？")) return;
  try {
    await deleteMyReview(id);
    loadData();
  } catch {
    alert("删除失败");
  }
}

function changePage(page: number): void {
  pageNum.value = page;
  loadData();
}

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.page {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 24px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #000000;
  margin: 0;
}

.btn-write {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  background: #00e676;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-write:hover {
  background: #00c665;
}

.btn-back {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #666666;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-back:hover {
  border-color: #000000;
  color: #000000;
}

/* Form */
.review-form {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  margin-bottom: 8px;
}

.select {
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  color: #000000;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  outline: none;
  cursor: pointer;
}

.select:focus {
  border-color: #00e676;
}

.textarea {
  width: 100%;
  padding: 12px;
  font-size: 14px;
  color: #000000;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  outline: none;
  resize: vertical;
  font-family: inherit;
}

.textarea:focus {
  border-color: #00e676;
}

.form-actions {
  display: flex;
  gap: 12px;
}

.btn-submit {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  background: #00e676;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-submit:hover:not(:disabled) {
  background: #00c665;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* List */
.list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.list-item {
  padding: 20px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  transition: all 0.2s;
}

.list-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.scenic-name {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
  cursor: pointer;
}

.scenic-name:hover {
  color: #00c665;
}

.score {
  font-size: 14px;
  font-weight: 600;
  color: #000000;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 4px;
}

.time {
  font-size: 13px;
  color: #999999;
}

.content {
  font-size: 14px;
  color: #333333;
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.item-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.likes {
  font-size: 13px;
  color: #999999;
}

.btn-delete {
  padding: 6px 14px;
  font-size: 13px;
  color: #999999;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-delete:hover {
  color: #ff5252;
  border-color: #ff5252;
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 40px;
}

.page-btn {
  padding: 8px 16px;
  font-size: 14px;
  color: #000000;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #00e676;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #999999;
}

/* Empty */
.empty {
  padding: 80px 20px;
  text-align: center;
  color: #999999;
  font-size: 15px;
}

/* Skeleton */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-item {
  display: flex;
  gap: 16px;
  padding: 20px;
}

.skeleton-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(90deg, #f5f5f5 25%, #eeeeee 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  flex-shrink: 0;
}

.skeleton-lines {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.skeleton-line {
  height: 16px;
  background: linear-gradient(90deg, #f5f5f5 25%, #eeeeee 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}

.skeleton-line.short {
  width: 60%;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
