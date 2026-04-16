<template>
  <div class="my-reviews-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h2 v-if="isWriting">评价体验</h2>
          <h2 v-else>我的点评</h2>
          <el-button v-if="isWriting" @click="cancelWrite">返回点评列表</el-button>
        </div>
      </template>

      <!-- 写点评表单区 -->
      <div v-if="isWriting" class="write-review-section">
        <el-form
          ref="reviewFormRef"
          :model="reviewForm"
          :rules="rules"
          label-width="120px"
          label-position="top"
        >
          <el-form-item label="整体评价" prop="score">
            <el-rate v-model="reviewForm.score" allow-half show-score></el-rate>
          </el-form-item>

          <el-form-item label="您的体验和感受" prop="content">
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="5"
              placeholder="分享您的游玩体验，为其他旅行者提供参考（至少15个字）"
            ></el-input>
          </el-form-item>

          <el-form-item label="出游时间">
            <el-date-picker
              v-model="reviewForm.visitDate"
              type="date"
              placeholder="选择出游日期"
              value-format="YYYY-MM-DD"
            ></el-date-picker>
          </el-form-item>

          <el-form-item label="出游类型">
            <el-select v-model="reviewForm.travelType" placeholder="请选择">
              <el-option label="个人游" value="个人游"></el-option>
              <el-option label="情侣游" value="情侣游"></el-option>
              <el-option label="家庭游" value="家庭游"></el-option>
              <el-option label="朋友出游" value="朋友出游"></el-option>
              <el-option label="商务出行" value="商务出行"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="isAnonymousBool">匿名评价</el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="submitLoading"
              class="submit-btn"
              size="large"
              @click="submit"
            >
              提交点评
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 我的点评列表区 -->
      <div v-else v-loading="listLoading" class="reviews-list-section">
        <template v-if="reviews.length > 0">
          <div v-for="item in reviews" :key="item.id" class="review-item">
            <div class="review-header">
              <div class="scenic-info">
                <h4 @click="goScenic(item.scenicId)">{{ item.scenicName || "未知景点" }}</h4>
                <el-rate :model-value="item.score" disabled text-color="#ff9900" />
              </div>
              <div class="review-meta">
                <span class="status-tag" :class="getStatusClass(item.status)">
                  {{ getStatusText(item.status) }}
                </span>
                <span class="time">{{ formatTime(item.createdAt) }}</span>
              </div>
            </div>
            <div class="review-content">
              {{ item.content }}
            </div>
            <div class="review-actions">
              <el-button type="danger" link @click="handleDelete(item.id)">删除点评</el-button>
            </div>
          </div>

          <div v-if="total > 0" class="pagination-wrapper">
            <el-pagination
              v-model:current-page="currentPage"
              background
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              @current-change="handlePageChange"
            />
          </div>
        </template>
        <el-empty v-else description="暂无点评记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { submitReview, getMyReviews, deleteMyReview, type ReviewItem } from "@/api/audit";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";

const route = useRoute();
const router = useRouter();

// 状态控制
const isWriting = computed(() => route.query.action === "write" && route.query.scenicId);
const targetScenicId = computed(() => Number(route.query.scenicId) || 0);

// 表单相关
const reviewFormRef = ref<FormInstance>();
const submitLoading = ref(false);
const isAnonymousBool = ref(false);

const reviewForm = reactive({
  scenicId: 0,
  score: 5,
  content: "",
  visitDate: "",
  travelType: "",
});

const rules = reactive<FormRules>({
  score: [{ required: true, message: "请打分", trigger: "change" }],
  content: [
    { required: true, message: "请填写评价内容", trigger: "blur" },
    { min: 15, message: "评价内容不能少于15个字", trigger: "blur" },
  ],
});

// 列表相关
const reviews = ref<ReviewItem[]>([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const listLoading = ref(false);

watch(
  () => route.query,
  () => {
    if (isWriting.value) {
      reviewForm.scenicId = targetScenicId.value;
      reviewForm.content = "";
      reviewForm.score = 5;
    } else {
      fetchMyReviews();
    }
  },
  { deep: true }
);

onMounted(() => {
  if (isWriting.value) {
    reviewForm.scenicId = targetScenicId.value;
  } else {
    fetchMyReviews();
  }
});

function cancelWrite() {
  router.replace({ path: "/my-reviews" });
}

function goScenic(id: number | undefined) {
  if (id) {
    router.push(`/scenic/${id}`);
  }
}

async function submit() {
  if (!reviewFormRef.value) return;
  await reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        await submitReview({
          scenicId: reviewForm.scenicId,
          score: reviewForm.score,
          content: reviewForm.content,
          visitDate: reviewForm.visitDate || undefined,
          travelType: reviewForm.travelType || undefined,
          isAnonymous: isAnonymousBool.value ? 1 : 0,
        });

        ElMessage.success("点评提交成功，请等待平台审核");
        cancelWrite(); // 提交成功后返回列表
      } catch (error) {
        console.error(error);
      } finally {
        submitLoading.value = false;
      }
    }
  });
}

async function fetchMyReviews() {
  listLoading.value = true;
  try {
    const res = await getMyReviews({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    });
    reviews.value = res.records;
    total.value = res.total;
  } catch (error) {
    console.error("Failed to fetch reviews", error);
  } finally {
    listLoading.value = false;
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm("确定要删除这条点评吗？删除后不可恢复", "提示", {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消",
    });
    await deleteMyReview(id);
    ElMessage.success("删除成功");
    if (reviews.value.length === 1 && currentPage.value > 1) {
      currentPage.value -= 1;
    }
    fetchMyReviews();
  } catch (error) {
    if (error !== "cancel") {
      console.error(error);
    }
  }
}

function handlePageChange(page: number) {
  currentPage.value = page;
  fetchMyReviews();
}

function formatTime(timeStr?: string) {
  if (!timeStr) return "";
  return timeStr.replace("T", " ").substring(0, 16);
}

function getStatusText(status: number | undefined) {
  const map: Record<number, string> = {
    0: "待审核",
    1: "已通过",
    2: "未通过",
    3: "已隐藏",
  };
  return status !== undefined ? map[status] || "未知状态" : "未知状态";
}

function getStatusClass(status: number | undefined) {
  const map: Record<number, string> = {
    0: "status-pending",
    1: "status-approved",
    2: "status-rejected",
    3: "status-hidden",
  };
  return status !== undefined ? map[status] || "" : "";
}
</script>

<style scoped>
.my-reviews-container {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 20px;
}

.box-card {
  border-radius: 12px;
  border: 1px solid #eaeaea;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #1a1a1a;
}

.write-review-section {
  padding: 20px;
}

.review-item {
  padding: 24px 0;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.scenic-info h4 {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  color: #1a1a1a;
}

.scenic-info h4:hover {
  text-decoration: underline;
  color: #000;
}

.review-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
}

.status-pending {
  background-color: #fff8e6;
  color: #f59e0b;
}
.status-approved {
  background-color: #ecfdf5;
  color: #10b981;
}
.status-rejected {
  background-color: #fef2f2;
  color: #ef4444;
}
.status-hidden {
  background-color: #f3f4f6;
  color: #6b7280;
}

.time {
  font-size: 13px;
  color: #6b7280;
}

.review-content {
  font-size: 15px;
  color: #374151;
  line-height: 1.6;
  white-space: pre-wrap;
  margin-bottom: 16px;
}

.review-actions {
  display: flex;
  justify-content: flex-end;
}

.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
}

.submit-btn {
  width: 200px;
  font-weight: bold;
}
</style>
