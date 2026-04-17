<template>
  <div class="review-item">
    <div class="reviewer-info">
      <el-avatar :size="40">
        {{ displayName.charAt(0).toUpperCase() }}
      </el-avatar>
      <div class="reviewer-meta">
        <span class="reviewer-name">{{ displayName }}</span>
        <span v-if="review.createdAt" class="review-date">
          {{ new Date(review.createdAt).toLocaleDateString() }} 发布
        </span>
      </div>
    </div>
    <div class="review-rating">
      <div class="rating-dots">
        <span
          v-for="i in 5"
          :key="i"
          class="dot"
          :class="{ filled: i <= Math.round(reviewScore) }"
        ></span>
      </div>
      <span v-if="review.visitDate" class="visit-date">游玩时间：{{ review.visitDate }}</span>
    </div>
    <div class="review-content">{{ review.content }}</div>
    <div v-if="review.images && review.images.length > 0" class="review-images">
      <el-image
        v-for="(img, idx) in review.images"
        :key="idx"
        :src="img"
        :preview-src-list="review.images"
        class="review-img"
        fit="cover"
      />
    </div>

    <!-- Actions Bar -->
    <div class="review-actions">
      <div class="action-btn" :class="{ 'is-liked': review.isLiked }" @click="handleLike">
        <el-icon>
          <Discount v-if="!review.isLiked" />
          <SuccessFilled v-else />
        </el-icon>
        <span>{{ review.likeCount || 0 }} 赞</span>
      </div>
      <div class="action-btn" @click="toggleReplies">
        <el-icon>
          <ChatDotRound />
        </el-icon>
        <span>{{ review.replyCount || 0 }} 回复</span>
      </div>
    </div>

    <!-- Replies Section -->
    <div v-if="showReplies" class="replies-section">
      <div v-loading="repliesLoading" class="replies-list">
        <div v-if="repliesTotal > 0" class="reply-total">共 {{ repliesTotal }} 条回复</div>
        <div v-for="reply in replies" :key="reply.id" class="reply-item">
          <span class="reply-user">{{ reply.username }}:</span>
          <span class="reply-content">{{ reply.content }}</span>
          <div class="reply-time">{{ new Date(reply.createdAt).toLocaleString() }}</div>
        </div>
        <div v-if="replies.length === 0 && !repliesLoading" class="no-reply">
          暂无回复，快来抢沙发~
        </div>
        <!-- Simplified pagination for replies if needed, or maybe just load all/more -->
        <div v-if="repliesTotal > replies.length" class="reply-pagination">
          <el-button link type="primary" :loading="repliesLoading" @click="loadMoreReplies">
            查看更多回复
          </el-button>
        </div>
        <div v-else-if="repliesTotal > 0 && !repliesLoading" class="reply-pagination-end">
          已展示全部回复
        </div>
      </div>

      <div class="reply-input-box">
        <el-input
          v-model="replyText"
          placeholder="说点什么..."
          maxlength="200"
          show-word-limit
          size="small"
          class="reply-input"
          @keyup.enter.prevent="handleReply"
        >
          <template #append>
            <el-button
              type="primary"
              :loading="submittingReply"
              :disabled="!replyText.trim()"
              @click="handleReply"
            >
              发送
            </el-button>
          </template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { Discount, SuccessFilled, ChatDotRound } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { likeReview, replyReview, getReviewReplies } from "@/api/audit";
import type { ReviewItem, ReviewReplyItem } from "@/api/audit";

const props = defineProps<{
  review: ReviewItem;
}>();

const emit = defineEmits<{
  (e: "update-review", review: ReviewItem): void;
}>();

const showReplies = ref(false);
const repliesLoading = ref(false);
const replies = ref<ReviewReplyItem[]>([]);
const repliesTotal = ref(0);
const pageNum = ref(1);

const replyText = ref("");
const submittingReply = ref(false);

const displayName = computed(() =>
  props.review.isAnonymous === 1 ? "匿名用户" : props.review.username || "匿名用户"
);

const reviewScore = computed(() => props.review.score ?? 0);

function getErrorMessage(error: unknown, fallback: string): string {
  if (
    typeof error === "object" &&
    error &&
    "message" in error &&
    typeof error.message === "string"
  ) {
    return error.message;
  }
  return fallback;
}

function mergeRepliesWithDedupe(
  current: ReviewReplyItem[],
  incoming: ReviewReplyItem[]
): ReviewReplyItem[] {
  const merged = new Map<number, ReviewReplyItem>();
  for (const item of current) {
    merged.set(item.id, item);
  }
  for (const item of incoming) {
    merged.set(item.id, item);
  }
  return [...merged.values()].sort(
    (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  );
}

const handleLike = async () => {
  try {
    await likeReview(props.review.id);
    const newLiked = !props.review.isLiked;
    const countChange = newLiked ? 1 : -1;
    emit("update-review", {
      ...props.review,
      isLiked: newLiked,
      likeCount: (props.review.likeCount || 0) + countChange,
    });
    ElMessage.success(newLiked ? "点赞成功" : "已取消点赞");
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, "操作失败"));
  }
};

const toggleReplies = () => {
  showReplies.value = !showReplies.value;
  if (showReplies.value && replies.value.length === 0) {
    pageNum.value = 1;
    replies.value = [];
    fetchReplies();
  }
};

const fetchReplies = async () => {
  if (repliesLoading.value) return;
  repliesLoading.value = true;
  try {
    const res = await getReviewReplies(props.review.id, { pageNum: pageNum.value, pageSize: 5 });
    const nextRecords = res.records || [];
    if (pageNum.value === 1) {
      replies.value = mergeRepliesWithDedupe([], nextRecords);
    } else {
      replies.value = mergeRepliesWithDedupe(replies.value, nextRecords);
    }
    repliesTotal.value = res.total || 0;
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, "获取回复失败"));
  } finally {
    repliesLoading.value = false;
  }
};

const loadMoreReplies = () => {
  pageNum.value++;
  fetchReplies();
};

const handleReply = async () => {
  if (!replyText.value.trim() || submittingReply.value) return;
  submittingReply.value = true;
  try {
    await replyReview(props.review.id, replyText.value);
    ElMessage.success("回复成功");
    replyText.value = "";

    // Refresh replies and update count
    pageNum.value = 1;
    await fetchReplies();
    emit("update-review", {
      ...props.review,
      replyCount: repliesTotal.value,
    });
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, "回复失败"));
  } finally {
    submittingReply.value = false;
  }
};
</script>

<style scoped>
.review-item {
  padding: 24px 0;
  border-bottom: 1px solid #f0f0f0;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.reviewer-meta {
  display: flex;
  flex-direction: column;
}

.reviewer-name {
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.review-date {
  font-size: 13px;
  color: #888;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.rating-dots {
  display: flex;
  gap: 4px;
}

.rating-dots .dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #e0e0e0;
}

.rating-dots .dot.filled {
  background: #ff5252;
}

.visit-date {
  font-size: 13px;
  color: #666;
}

.review-content {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 16px;
  white-space: pre-wrap;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.review-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  cursor: pointer;
}

.review-actions {
  display: flex;
  gap: 24px;
  margin-top: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 0.3s;
}

.action-btn:hover {
  color: #1a1a1a;
}

.action-btn.is-liked {
  color: #ff5252;
}

.replies-section {
  margin-top: 16px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.replies-list {
  margin-bottom: 12px;
}

.reply-total {
  margin-bottom: 8px;
  font-size: 12px;
  color: #6b7280;
}

.reply-item {
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 8px;
  border-bottom: 1px dashed #ebebeb;
  padding-bottom: 8px;
}

.reply-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.reply-user {
  font-weight: bold;
  color: #333;
}

.reply-content {
  color: #555;
  word-break: break-all;
}

.reply-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.no-reply {
  font-size: 13px;
  color: #999;
  text-align: center;
  padding: 8px 0;
}

.reply-pagination {
  text-align: center;
  margin-top: 8px;
}

.reply-pagination-end {
  text-align: center;
  margin-top: 8px;
  color: #9ca3af;
  font-size: 12px;
}

.reply-input-box {
  margin-top: 12px;
}
</style>
