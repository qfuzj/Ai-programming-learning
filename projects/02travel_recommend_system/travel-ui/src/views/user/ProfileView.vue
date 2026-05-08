<!--
  个人中心页 - 精致有机极简主义风格
  设计方向：有机极简 + 精致排版 + 微妙动效
  字体：本地系统字体栈
-->
<template>
  <div v-if="profile" class="profile-page">
    <!-- Hero Header -->
    <header class="hero-header">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="avatar-wrapper" @click="triggerAvatarUpload">
          <div class="avatar-ring">
            <img v-if="profile.avatar" :src="profile.avatar" class="avatar-img" />
            <span v-else class="avatar-letter">{{ avatarLetter }}</span>
          </div>
          <div class="avatar-edit-hint">更换头像</div>
        </div>
        <input
          ref="avatarInputRef"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleAvatarChange"
        />
        <div class="hero-info">
          <h1 class="hero-name">{{ profile.nickname || profile.username }}</h1>
          <p v-if="profile.signature" class="hero-signature">{{ profile.signature }}</p>
          <p class="hero-meta">
            <span class="meta-item">
              <span v-if="profile.gender === 1">男</span>
              <span v-else-if="profile.gender === 2">女</span>
              <span v-else>未知</span>
            </span>
            <span v-if="profile.birthday" class="meta-sep">|</span>
            <span v-if="profile.birthday" class="meta-item">{{ profile.birthday }}</span>
            <span v-if="profile.role" class="meta-sep">|</span>
            <span v-if="profile.role" class="meta-item role-tag">
              {{ profile.role === "ADMIN" ? "管理员" : "普通用户" }}
            </span>
          </p>
        </div>
        <div class="hero-stats">
          <div class="stat-item" style="animation-delay: 0.05s">
            <span class="stat-number">{{ stats.browseCount }}</span>
            <span class="stat-label">浏览次数</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item" style="animation-delay: 0.1s">
            <span class="stat-number">{{ stats.favoriteCount }}</span>
            <span class="stat-label">收藏景点</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item" style="animation-delay: 0.15s">
            <span class="stat-number">{{ stats.reviewCount }}</span>
            <span class="stat-label">发布点评</span>
          </div>
        </div>
        <button class="btn-edit-hero" @click="openEdit">
          <svg
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
          </svg>
          编辑资料
        </button>
      </div>
    </header>

    <!-- Main Layout: Left Sidebar + Right Content -->
    <div class="main-layout">
      <!-- Left Sidebar -->
      <aside class="sidebar">
        <!-- Preference Tags -->
        <section class="sidebar-card" style="animation-delay: 0.15s">
          <div class="card-header">
            <h3 class="card-title">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"
                />
                <line x1="7" y1="7" x2="7.01" y2="7" />
              </svg>
              偏好标签
            </h3>
            <button class="btn-icon" title="编辑标签" @click="openTagDialog">
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
            </button>
          </div>
          <div v-if="tagLoading" class="sidebar-loading">
            <div v-for="n in 3" :key="n" class="skeleton-line"></div>
          </div>
          <div v-else-if="myTagIds.length === 0" class="sidebar-empty">
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#ccc"
              stroke-width="1.5"
            >
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>点击编辑设置偏好</span>
          </div>
          <div v-else class="tag-groups">
            <div v-for="(names, cat) in myTagsByCategory" :key="cat" class="tag-group">
              <div class="tag-category">{{ cat }}</div>
              <div class="tag-list">
                <span v-for="name in names" :key="name" class="tag-chip">{{ name }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- Portrait Summary -->
        <section v-if="portrait" class="sidebar-card" style="animation-delay: 0.2s">
          <div class="card-header">
            <h3 class="card-title">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              旅行画像
            </h3>
          </div>
          <div class="portrait-body">
            <div v-if="portrait.travelStyle" class="portrait-row">
              <span class="portrait-icon">✦</span>
              <div>
                <span class="portrait-label">旅行风格</span>
                <span class="portrait-value">{{ portrait.travelStyle }}</span>
              </div>
            </div>
            <div v-if="portrait.budgetLevel" class="portrait-row">
              <span class="portrait-icon">◈</span>
              <div>
                <span class="portrait-label">预算水平</span>
                <span class="portrait-value">{{ portrait.budgetLevel }}</span>
              </div>
            </div>
            <div v-if="portrait.location" class="portrait-row">
              <span class="portrait-icon">◉</span>
              <div>
                <span class="portrait-label">常去地区</span>
                <span class="portrait-value">{{ portrait.location }}</span>
              </div>
            </div>
            <p v-if="portrait.summary" class="portrait-summary">{{ portrait.summary }}</p>
          </div>
        </section>
      </aside>

      <!-- Right Content Area -->
      <main class="content-area">
        <!-- Tabs -->
        <nav class="tab-nav">
          <button
            v-for="(tab, idx) in tabs"
            :key="tab.key"
            class="tab-btn"
            :class="{ active: activeTab === tab.key }"
            :style="{ animationDelay: `${0.3 + idx * 0.1}s` }"
            @click="switchTab(tab.key)"
          >
            {{ tab.label }}
            <span v-if="tab.count !== undefined" class="tab-count">{{ tab.count }}</span>
          </button>
        </nav>

        <!-- Favorites Tab -->
        <div v-if="activeTab === 'favorites'" class="tab-panel">
          <div v-if="favLoading" class="loading-grid">
            <div v-for="n in 3" :key="n" class="skeleton-card"></div>
          </div>
          <div v-else-if="favList.length === 0" class="empty-state">
            <svg
              width="48"
              height="48"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#ddd"
              stroke-width="1.5"
            >
              <path
                d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"
              />
            </svg>
            <p>暂无收藏</p>
          </div>
          <template v-else>
            <div class="content-list">
              <article
                v-for="(item, idx) in favList"
                :key="item.scenicId"
                class="content-card"
                :style="{ animationDelay: `${idx * 0.04}s` }"
                @click="router.push(`/scenic/${item.scenicId}`)"
              >
                <div class="card-img-wrapper">
                  <img :src="item.coverImage || ''" class="card-img" />
                  <div class="card-img-overlay"></div>
                </div>
                <div class="card-info">
                  <h4 class="card-title">{{ item.scenicName }}</h4>
                  <p class="card-meta">收藏于 {{ formatTime(item.favoriteTime) }}</p>
                </div>
                <button class="btn-remove-mini" @click.stop="removeFav(item.scenicId)">
                  <svg
                    width="14"
                    height="14"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </button>
              </article>
            </div>
            <div v-if="favTotalPages > 1" class="pagination">
              <button
                class="page-btn"
                :disabled="favPageNum <= 1"
                @click="changeFavPage(favPageNum - 1)"
              >
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <polyline points="15 18 9 12 15 6" />
                </svg>
                上一页
              </button>
              <span class="page-info">{{ favPageNum }} / {{ favTotalPages }}</span>
              <button
                class="page-btn"
                :disabled="favPageNum >= favTotalPages"
                @click="changeFavPage(favPageNum + 1)"
              >
                下一页
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <polyline points="9 18 15 12 9 6" />
                </svg>
              </button>
            </div>
          </template>
        </div>

        <!-- Reviews Tab -->
        <div v-if="activeTab === 'reviews'" class="tab-panel">
          <div v-if="reviewLoading" class="loading-grid">
            <div v-for="n in 3" :key="n" class="skeleton-card"></div>
          </div>
          <div v-else-if="reviewList.length === 0" class="empty-state">
            <svg
              width="48"
              height="48"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#ddd"
              stroke-width="1.5"
            >
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
            <p>暂无点评</p>
          </div>
          <template v-else>
            <div class="content-list">
              <article
                v-for="(item, idx) in reviewList"
                :key="item.id"
                class="content-card review-card"
                :style="{ animationDelay: `${idx * 0.04}s`, cursor: 'pointer' }"
                @click="openReviewDetail(item)"
              >
                <div class="card-info">
                  <div class="review-header">
                    <h4 class="card-title" @click.stop="router.push(`/scenic/${item.scenicId}`)">
                      {{ item.scenicName }}
                    </h4>
                    <span v-if="item.status === 0" class="status-badge pending">审核中</span>
                    <span v-else-if="item.status === 1" class="status-badge approved">已通过</span>
                    <span v-else-if="item.status === 2" class="status-badge rejected">已驳回</span>
                    <span v-else-if="item.status === 3" class="status-badge hidden">已隐藏</span>
                    <span class="score-badge" style="margin-left: auto">{{ item.score }}分</span>
                  </div>
                  <p class="review-content">{{ item.content }}</p>
                  <p class="card-meta">
                    {{ formatTime(item.createdAt) }}
                    <span v-if="item.images && item.images.length > 0" style="margin-left: 12px">
                      图片数：{{ item.images.length }}
                    </span>
                    <span v-if="item.likeCount && item.likeCount > 0" style="margin-left: 12px">
                      点赞数：{{ item.likeCount }}
                    </span>
                    <span v-if="item.replyCount && item.replyCount > 0" style="margin-left: 12px">
                      回复数：{{ item.replyCount }}
                    </span>
                  </p>
                  <p v-if="item.status === 2 && item.rejectReason" class="reject-reason">
                    驳回原因: {{ item.rejectReason }}
                  </p>
                </div>
                <button class="btn-remove-mini" @click.stop="removeReview(item.id)">
                  <svg
                    width="14"
                    height="14"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </button>
              </article>
            </div>
            <div v-if="reviewTotalPages > 1" class="pagination">
              <button
                class="page-btn"
                :disabled="reviewPageNum <= 1"
                @click="changeReviewPage(reviewPageNum - 1)"
              >
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <polyline points="15 18 9 12 15 6" />
                </svg>
                上一页
              </button>
              <span class="page-info">{{ reviewPageNum }} / {{ reviewTotalPages }}</span>
              <button
                class="page-btn"
                :disabled="reviewPageNum >= reviewTotalPages"
                @click="changeReviewPage(reviewPageNum + 1)"
              >
                下一页
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <polyline points="9 18 15 12 9 6" />
                </svg>
              </button>
            </div>
          </template>
        </div>

        <!-- History Tab -->
        <div v-if="activeTab === 'history'" class="tab-panel">
          <div v-if="histLoading" class="loading-grid">
            <div v-for="n in 3" :key="n" class="skeleton-card"></div>
          </div>
          <div v-else-if="histList.length === 0" class="empty-state">
            <svg
              width="48"
              height="48"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#ddd"
              stroke-width="1.5"
            >
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
            <p>暂无浏览记录</p>
          </div>
          <template v-else>
            <div class="content-list">
              <article
                v-for="(item, idx) in histList"
                :key="item.id"
                class="content-card"
                :style="{ animationDelay: `${idx * 0.04}s` }"
                @click="router.push(`/scenic/${item.scenicId}`)"
              >
                <div class="card-img-wrapper">
                  <img :src="item.coverImage || ''" class="card-img" />
                  <div class="card-img-overlay"></div>
                </div>
                <div class="card-info">
                  <h4 class="card-title">{{ item.scenicName }}</h4>
                  <p class="card-meta">浏览于 {{ formatTime(item.browseTime) }}</p>
                </div>
                <button class="btn-remove-mini" @click.stop="removeHist(item.id)">
                  <svg
                    width="14"
                    height="14"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </button>
              </article>
            </div>
            <div v-if="histTotalPages > 1" class="pagination">
              <button
                class="page-btn"
                :disabled="histPageNum <= 1"
                @click="changeHistPage(histPageNum - 1)"
              >
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <polyline points="15 18 9 12 15 6" />
                </svg>
                上一页
              </button>
              <span class="page-info">{{ histPageNum }} / {{ histTotalPages }}</span>
              <button
                class="page-btn"
                :disabled="histPageNum >= histTotalPages"
                @click="changeHistPage(histPageNum + 1)"
              >
                下一页
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <polyline points="9 18 15 12 9 6" />
                </svg>
              </button>
            </div>
          </template>
        </div>
      </main>
    </div>

    <!-- Edit Profile Modal -->
    <div v-if="showEdit" class="modal-mask" @click.self="showEdit = false">
      <div class="modal-dialog">
        <div class="modal-header">
          <h2 class="modal-title">编辑资料</h2>
          <button class="btn-modal-close" @click="showEdit = false">
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-field">
            <label class="field-label">昵称</label>
            <input v-model="editForm.nickname" class="field-input" placeholder="请输入昵称" />
          </div>
          <div class="form-field">
            <label class="field-label">个性签名</label>
            <input v-model="editForm.signature" class="field-input" placeholder="写下你的签名" />
          </div>
          <div class="form-field">
            <label class="field-label">性别</label>
            <div class="select-wrapper">
              <select v-model="editForm.gender" class="field-input field-select">
                <option :value="0">未知</option>
                <option :value="1">男</option>
                <option :value="2">女</option>
              </select>
              <svg
                class="select-arrow"
                width="12"
                height="12"
                viewBox="0 0 24 24"
                fill="none"
                stroke="#999"
                stroke-width="2"
              >
                <polyline points="6 9 12 15 18 9" />
              </svg>
            </div>
          </div>
          <div class="form-field">
            <label class="field-label">生日</label>
            <input v-model="editForm.birthday" class="field-input" type="date" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showEdit = false">取消</button>
          <button class="btn-primary" :disabled="saveLoading" @click="saveProfile">
            <span v-if="saveLoading" class="btn-spinner"></span>
            {{ saveLoading ? "保存中..." : "保存修改" }}
          </button>
        </div>
      </div>
    </div>

    <!-- Tag Edit Modal -->
    <div v-if="showTagDialog" class="modal-mask" @click.self="showTagDialog = false">
      <div class="modal-dialog">
        <div class="modal-header">
          <h2 class="modal-title">编辑偏好标签</h2>
          <button class="btn-modal-close" @click="showTagDialog = false">
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>
        <div class="modal-body tag-categories">
          <div v-for="(tags, cat) in tagsByCategory" :key="cat" class="tag-category-section">
            <div class="tag-category-title">{{ cat }}</div>
            <div class="tag-category-items">
              <label v-for="tag in tags" :key="tag.id" class="tag-checkbox">
                <input v-model="selectedTagIds" type="checkbox" :value="tag.id" />
                <span class="checkbox-custom"></span>
                <span class="tag-name">{{ tag.name }}</span>
              </label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showTagDialog = false">取消</button>
          <button class="btn-primary" :disabled="tagSaving" @click="saveTags">
            <span v-if="tagSaving" class="btn-spinner"></span>
            {{ tagSaving ? "保存中..." : "保存标签" }}
          </button>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="page-loading">
    <div class="loading-pulse"></div>
    <p>加载中...</p>
  </div>

  <!-- Review Detail Dialog -->
  <el-dialog
    v-model="reviewDetailVisible"
    title="点评详情"
    width="600px"
    custom-class="review-detail-modal"
  >
    <div v-if="currentReview" class="review-detail-content">
      <div class="review-detail-header">
        <h3>{{ currentReview.scenicName }}</h3>
        <span class="score-badge">{{ currentReview.score }}分</span>
      </div>

      <div class="review-detail-status">
        <span v-if="currentReview.status === 0" class="status-badge pending">审核中</span>
        <span v-else-if="currentReview.status === 1" class="status-badge approved">已通过</span>
        <span v-else-if="currentReview.status === 2" class="status-badge rejected">已驳回</span>
        <span v-else-if="currentReview.status === 3" class="status-badge hidden">已隐藏</span>
        <span class="meta-time">{{ formatTime(currentReview.createdAt) }}</span>
      </div>

      <div
        v-if="currentReview.status === 2 && currentReview.rejectReason"
        class="review-detail-reject"
      >
        驳回原因：{{ currentReview.rejectReason }}
      </div>

      <div class="review-detail-body">
        <p>{{ currentReview.content }}</p>
      </div>

      <div
        v-if="currentReview.images && currentReview.images.length > 0"
        class="review-detail-images"
      >
        <el-image
          v-for="(img, i) of currentReview.images"
          :key="i"
          :src="img"
          :preview-src-list="currentReview.images"
          :initial-index="Number(i)"
          fit="cover"
          class="detail-img"
        />
      </div>

      <div class="review-detail-footer">
        <span>点赞数: {{ currentReview.likeCount || 0 }}</span>
        <span>回复数: {{ currentReview.replyCount || 0 }}</span>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  getProfileInfo as getProfile,
  updateProfileInfo as updateProfile,
  getProfilePortrait,
  updatePreferenceTags,
  getMyPreferenceTags,
  type ProfileInfo,
  type ProfilePortraitSummary,
} from "@/api/profile";
import { useUserStore } from "@/store";
import { getFavoritesPage, removeFavorite } from "@/api/favorite";
import { getMyReviews, deleteMyReview } from "@/api/audit";
import { getBrowseHistoryPage as getBrowseHistory, deleteBrowseHistory } from "@/api/history";
import { getTags, type CommonTagItem } from "@/api/common";
import { getUploadToken, uploadCallback } from "@/api/file";

const router = useRouter();
const userStore = useUserStore();
const profile = ref<ProfileInfo | null>(null);
const portrait = ref<ProfilePortraitSummary | null>(null);
const activeTab = ref("favorites");
const showEdit = ref(false);
const showTagDialog = ref(false);
const saveLoading = ref(false);
const tagLoading = ref(false);
const tagSaving = ref(false);
const avatarInputRef = ref<HTMLInputElement | null>(null);

// 点评详情弹窗状态
const reviewDetailVisible = ref(false);
const currentReview = ref<any>(null);

function openReviewDetail(item: any) {
  currentReview.value = item;
  reviewDetailVisible.value = true;
}

const editForm = reactive({
  nickname: "",
  signature: "",
  gender: 0,
  birthday: "",
});

const allTags = ref<CommonTagItem[]>([]);
const selectedTagIds = ref<number[]>([]);
const myTagIds = ref<number[]>([]);

// 标签按分类分组
const tagsByCategory = computed(() => {
  const map: Record<string, CommonTagItem[]> = {};
  for (const tag of allTags.value) {
    const cat = tag.category || "其他";
    if (!map[cat]) map[cat] = [];
    map[cat].push(tag);
  }
  return map;
});

// 我的标签按分类分组
const myTagsByCategory = computed(() => {
  const map: Record<string, string[]> = {};
  for (const id of myTagIds.value) {
    const tag = allTags.value.find((t) => t.id === id);
    if (!tag) continue;
    const cat = tag.category || "其他";
    if (!map[cat]) map[cat] = [];
    map[cat].push(tag.name || `标签${id}`);
  }
  return map;
});

// Stats
const stats = reactive({
  browseCount: 0,
  favoriteCount: 0,
  reviewCount: 0,
});

// Tabs with counts
const tabs = reactive([
  { key: "favorites", label: "收藏", count: undefined as number | undefined },
  { key: "reviews", label: "点评", count: undefined as number | undefined },
  { key: "history", label: "浏览历史", count: undefined as number | undefined },
]);

// Favorites
const favList = ref<any[]>([]);
const favLoading = ref(false);
const favPageNum = ref(1);
const favPageSize = ref(6);
const favTotal = ref(0);
const favTotalPages = computed(() => Math.max(1, Math.ceil(favTotal.value / favPageSize.value)));

// Reviews
const reviewList = ref<any[]>([]);
const reviewLoading = ref(false);
const reviewPageNum = ref(1);
const reviewPageSize = ref(6);
const reviewTotal = ref(0);
const reviewTotalPages = computed(() =>
  Math.max(1, Math.ceil(reviewTotal.value / reviewPageSize.value))
);

// History
const histList = ref<any[]>([]);
const histLoading = ref(false);
const histPageNum = ref(1);
const histPageSize = ref(6);
const histTotal = ref(0);
const histTotalPages = computed(() => Math.max(1, Math.ceil(histTotal.value / histPageSize.value)));

const avatarLetter = computed(() => {
  const name = profile.value?.nickname || profile.value?.username || "U";
  return name.charAt(0);
});

// Load counts on page load
async function loadCounts(): Promise<void> {
  try {
    const [favRes, reviewRes, histRes] = await Promise.allSettled([
      getFavoritesPage({ pageNum: 1, pageSize: 1 }),
      getMyReviews({ pageNum: 1, pageSize: 1 }),
      getBrowseHistory({ pageNum: 1, pageSize: 1 }),
    ]);
    if (favRes.status === "fulfilled") {
      stats.favoriteCount = favRes.value.total || 0;
      tabs[0].count = favRes.value.total || 0;
    }
    if (reviewRes.status === "fulfilled") {
      stats.reviewCount = reviewRes.value.total || 0;
      tabs[1].count = reviewRes.value.total || 0;
    }
    if (histRes.status === "fulfilled") {
      stats.browseCount = histRes.value.total || 0;
      tabs[2].count = histRes.value.total || 0;
    }
  } catch {
    // Ignore
  }
}

async function loadProfile(): Promise<void> {
  try {
    const p = await getProfile();
    profile.value = p;
    editForm.nickname = p.nickname || "";
    editForm.signature = p.signature || "";
    editForm.gender = p.gender || 0;
  } catch {
    alert("加载个人资料失败");
  }
}

async function loadPortrait(): Promise<void> {
  try {
    portrait.value = await getProfilePortrait();
  } catch {
    // Ignore
  }
}

async function loadMyTags(): Promise<void> {
  tagLoading.value = true;
  try {
    const [tags, myTags] = await Promise.all([getTags(), getMyPreferenceTags()]);
    allTags.value = tags;
    myTagIds.value = myTags.map((t: any) => Number(t.id ?? t));
  } catch {
    // Ignore
  } finally {
    tagLoading.value = false;
  }
}

// Favorites
async function loadFav(page = favPageNum.value): Promise<void> {
  favLoading.value = true;
  try {
    favPageNum.value = page;
    const res = await getFavoritesPage({ pageNum: page, pageSize: favPageSize.value });
    favList.value = res.records || [];
    favTotal.value = res.total || 0;
    if (activeTab.value === "favorites") {
      tabs[0].count = res.total || 0;
      stats.favoriteCount = res.total || 0;
    }
  } finally {
    favLoading.value = false;
  }
}
function changeFavPage(page: number) {
  if (page < 1 || page > favTotalPages.value) return;
  loadFav(page);
}
async function removeFav(scenicId: number): Promise<void> {
  if (!confirm("确定取消收藏？")) return;
  try {
    await removeFavorite(scenicId);
    if (favList.value.length === 1 && favPageNum.value > 1) {
      loadFav(favPageNum.value - 1);
    } else {
      loadFav(favPageNum.value);
    }
  } catch {
    alert("操作失败");
  }
}

// Reviews
async function loadReviews(page = reviewPageNum.value): Promise<void> {
  reviewLoading.value = true;
  try {
    reviewPageNum.value = page;
    const res = await getMyReviews({ pageNum: page, pageSize: reviewPageSize.value });
    reviewList.value = res.records || [];
    reviewTotal.value = res.total || 0;
    if (activeTab.value === "reviews") {
      tabs[1].count = res.total || 0;
      stats.reviewCount = res.total || 0;
    }
  } finally {
    reviewLoading.value = false;
  }
}
function changeReviewPage(page: number) {
  if (page < 1 || page > reviewTotalPages.value) return;
  loadReviews(page);
}
async function removeReview(id: number): Promise<void> {
  if (!confirm("确定删除点评？")) return;
  try {
    await deleteMyReview(id);
    if (reviewList.value.length === 1 && reviewPageNum.value > 1) {
      loadReviews(reviewPageNum.value - 1);
    } else {
      loadReviews(reviewPageNum.value);
    }
  } catch {
    alert("删除失败");
  }
}

// History
async function loadHist(page = histPageNum.value): Promise<void> {
  histLoading.value = true;
  try {
    histPageNum.value = page;
    const res = await getBrowseHistory({ pageNum: page, pageSize: histPageSize.value });
    histList.value = res.records || [];
    histTotal.value = res.total || 0;
    if (activeTab.value === "history") {
      tabs[2].count = res.total || 0;
      stats.browseCount = res.total || 0;
    }
  } finally {
    histLoading.value = false;
  }
}
function changeHistPage(page: number) {
  if (page < 1 || page > histTotalPages.value) return;
  loadHist(page);
}
async function removeHist(id: number): Promise<void> {
  if (!confirm("确定删除该记录？")) return;
  try {
    await deleteBrowseHistory(id);
    if (histList.value.length === 1 && histPageNum.value > 1) {
      loadHist(histPageNum.value - 1);
    } else {
      loadHist(histPageNum.value);
    }
  } catch {
    alert("删除失败");
  }
}

function switchTab(key: string): void {
  activeTab.value = key;
  if (key === "favorites" && favList.value.length === 0) loadFav();
  if (key === "reviews" && reviewList.value.length === 0) loadReviews();
  if (key === "history" && histList.value.length === 0) loadHist();
}

function openEdit(): void {
  editForm.nickname = profile.value?.nickname || "";
  editForm.signature = profile.value?.signature || "";
  editForm.gender = profile.value?.gender || 0;
  editForm.birthday = profile.value?.birthday || "";
  showEdit.value = true;
}

async function saveProfile(): Promise<void> {
  saveLoading.value = true;
  try {
    await updateProfile({
      nickname: editForm.nickname || undefined,
      signature: editForm.signature || undefined,
      gender: editForm.gender || undefined,
      birthday: editForm.birthday || undefined,
    });
    showEdit.value = false;
    await loadProfile();
    // 同步更新 userStore 中的头像，使右上角立即生效
    await userStore.fetchProfile();
  } catch {
    alert("保存失败");
  } finally {
    saveLoading.value = false;
  }
}

// Avatar upload
function triggerAvatarUpload(): void {
  avatarInputRef.value?.click();
}
async function handleAvatarChange(e: Event): Promise<void> {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;
  try {
    const tokenRes = await getUploadToken({ fileName: file.name, bizType: "avatar" });
    await fetch(tokenRes.uploadUrl, {
      method: "PUT",
      body: file,
      headers: { "Content-Type": file.type },
    });
    const fileId = await uploadCallback({
      bucketName: tokenRes.bucketName,
      objectKey: tokenRes.objectKey,
      originalName: file.name,
      bizType: tokenRes.bizType || "avatar",
      bizId: tokenRes.bizId,
    });
    await updateProfile({ avatar: String(fileId) });
    await loadProfile();
    // 同步更新 userStore 中的头像，使右上角立即生效
    await userStore.fetchProfile();
  } catch {
    alert("头像上传失败");
  } finally {
    if (avatarInputRef.value) avatarInputRef.value.value = "";
  }
}

// Tag dialog
async function openTagDialog(): Promise<void> {
  try {
    const [tags, myTags] = await Promise.all([getTags(), getMyPreferenceTags()]);
    allTags.value = tags;
    selectedTagIds.value = myTags.map((t: any) => Number(t.id ?? t));
    showTagDialog.value = true;
  } catch {
    alert("加载标签失败");
  }
}
async function saveTags(): Promise<void> {
  tagSaving.value = true;
  try {
    await updatePreferenceTags(selectedTagIds.value);
    myTagIds.value = [...selectedTagIds.value];
    showTagDialog.value = false;
    portrait.value = await getProfilePortrait();
  } catch {
    alert("保存失败");
  } finally {
    tagSaving.value = false;
  }
}

// Time formatting
function formatTime(time?: string): string {
  if (!time) return "";
  return time.replace("T", " ").slice(0, 19);
}

onMounted(async () => {
  await Promise.all([loadProfile(), loadPortrait(), loadMyTags(), loadCounts(), loadFav()]);
});
</script>

<style scoped>
/* ========== Fonts & Base ========== */
.profile-page {
  max-width: 1200px;
  padding: 0 24px 60px;
  margin: 0 auto;
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB",
    "Microsoft YaHei", sans-serif;
}

/* ========== Hero Header ========== */
.hero-header {
  position: relative;
  padding: 48px 40px 40px;
  margin: 24px 0 40px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 20px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(
    135deg,
    rgba(0, 230, 118, 0.08) 0%,
    rgba(0, 230, 118, 0.03) 50%,
    transparent 100%
  );
}

.hero-content {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
  cursor: pointer;
}

.avatar-ring {
  display: grid;
  place-items: center;
  width: 80px;
  height: 80px;
  overflow: hidden;
  background: linear-gradient(135deg, #00e676 0%, #00c665 100%);
  border-radius: 50%;
  box-shadow: 0 4px 15px rgba(0, 230, 118, 0.3);
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
}

.avatar-wrapper:hover .avatar-ring {
  box-shadow: 0 6px 20px rgba(0, 230, 118, 0.4);
  transform: scale(1.05);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-letter {
  font-size: 30px;
  font-weight: 700;
  color: #ffffff;
}

.avatar-edit-hint {
  position: absolute;
  bottom: -4px;
  left: 50%;
  padding: 2px 8px;
  font-size: 11px;
  color: #999;
  white-space: nowrap;
  pointer-events: none;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  opacity: 0;
  transform: translateX(-50%);
  transition: opacity 0.2s;
}

.avatar-wrapper:hover .avatar-edit-hint {
  opacity: 1;
}

.hero-info {
  flex: 1;
  min-width: 200px;
}

.hero-name {
  margin: 0 0 6px 0;
  font-size: 26px;
  font-weight: 700;
  line-height: 1.3;
  color: #000000;
}

.hero-signature {
  margin: 0 0 8px 0;
  font-size: 14px;
  line-height: 1.5;
  color: #666666;
}

.hero-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  margin: 0;
  font-size: 14px;
  color: #999999;
}

.meta-tag {
  display: inline-block;
  padding: 2px 10px;
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  border-radius: 12px;
}

.meta-dot {
  color: #ddd;
}

.meta-text {
  color: #999;
}

.hero-stats {
  display: flex;
  gap: 24px;
  align-items: center;
  margin-left: auto;
  animation: fadeInUp 0.3s ease both;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
  animation: fadeInUp 0.3s ease both;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
  color: #000000;
}

.stat-label {
  font-size: 12px;
  color: #999999;
  white-space: nowrap;
}

.stat-divider {
  width: 1px;
  height: 36px;
  background: #f0f0f0;
}

.btn-edit-hero {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 10px 20px;
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  cursor: pointer;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  transition: all 0.25s ease;
}

.btn-edit-hero:hover {
  background: #f0faf4;
  border-color: #00e676;
}

/* ========== Main Layout ========== */
.main-layout {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

/* ========== Sidebar ========== */
.sidebar {
  position: sticky;
  top: 24px;
  display: flex;
  flex-shrink: 0;
  flex-direction: column;
  gap: 16px;
  width: 280px;
}

.sidebar-card {
  padding: 24px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
  transition: border-color 0.25s ease;
  animation: fadeInUp 0.3s ease both;
}

.sidebar-card:hover {
  border-color: #e0e0e0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-title {
  display: flex;
  gap: 8px;
  align-items: center;
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #000000;
}

.card-title svg {
  color: #00e676;
}

.btn-icon {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  color: #999;
  cursor: pointer;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.btn-icon:hover {
  color: #00c665;
  background: #f0faf4;
  border-color: #00e676;
}

.sidebar-loading {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-line {
  height: 28px;
  background: linear-gradient(90deg, #f5f5f5 25%, #eeeeee 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  border-radius: 6px;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.sidebar-empty {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  padding: 12px 0;
  font-size: 13px;
  color: #ccc;
  text-align: center;
}

.tag-groups {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tag-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tag-category {
  font-size: 11px;
  font-weight: 500;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-chip {
  padding: 5px 14px;
  font-size: 13px;
  color: #000000;
  background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 100%);
  border-radius: 20px;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.tag-chip:hover {
  box-shadow: 0 2px 8px rgba(0, 230, 118, 0.15);
  transform: translateY(-1px);
}

/* Tag dialog categories */
.tag-categories {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 300px;
  padding: 4px 0;
  overflow-y: auto;
}

.tag-category-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tag-category-title {
  padding-bottom: 6px;
  font-size: 12px;
  font-weight: 500;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #f0f0f0;
}

.tag-category-items {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

/* Portrait */
.portrait-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.portrait-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.portrait-icon {
  flex-shrink: 0;
  margin-top: 3px;
  font-size: 12px;
  color: #00e676;
}

.portrait-label {
  display: block;
  font-size: 11px;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.portrait-value {
  display: block;
  margin-top: 2px;
  font-size: 14px;
  font-weight: 500;
  color: #000;
}

.portrait-summary {
  padding-top: 12px;
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: #666;
  border-top: 1px solid #f5f5f5;
}

/* ========== Content Area ========== */
.content-area {
  flex: 1;
  min-width: 0;
}

/* Tabs */
.tab-nav {
  display: flex;
  gap: 8px;
  margin-bottom: 28px;
  border-bottom: 2px solid #f5f5f5;
}

.tab-btn {
  position: relative;
  bottom: -2px;
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 12px 20px;
  font-family: inherit;
  font-size: 15px;
  font-weight: 500;
  color: #999;
  cursor: pointer;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  transition: all 0.25s ease;
  animation: fadeInUp 0.3s ease both;
}

.tab-btn:hover {
  color: #000;
}

.tab-btn.active {
  font-weight: 600;
  color: #000;
  border-bottom-color: #00e676;
}

.tab-count {
  padding: 1px 8px;
  font-size: 12px;
  font-weight: 400;
  color: #999;
  background: #f5f5f5;
  border-radius: 10px;
}

.tab-btn.active .tab-count {
  color: #00c665;
  background: #e8f5e9;
}

/* Tab Panel */
.tab-panel {
  min-height: 300px;
}

/* Loading State */
.loading-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-card {
  height: 88px;
  background: linear-gradient(90deg, #f8f8f8 25%, #f0f0f0 50%, #f8f8f8 75%);
  background-size: 200% 100%;
  border-radius: 12px;
  animation: shimmer 1.5s infinite;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #ccc;
}

.empty-state p {
  margin: 16px 0 0;
  font-size: 14px;
  color: #999;
}

/* Content List */
.content-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-card {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 14px;
  transition: all 0.3s ease;
  animation: fadeInUp 0.3s ease both;
}

.content-card:hover {
  border-color: #00e676;
  box-shadow: 0 4px 12px rgba(0, 230, 118, 0.08);
  transform: translateY(-2px);
}

.card-img-wrapper {
  position: relative;
  flex-shrink: 0;
  width: 88px;
  height: 64px;
  overflow: hidden;
  border-radius: 10px;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.content-card:hover .card-img {
  transform: scale(1.08);
}

.card-img-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(180deg, transparent 50%, rgba(0, 0, 0, 0.03) 100%);
}

.card-info {
  flex: 1;
  min-width: 0;
}

.review-card .card-info {
  display: flex;
  flex-direction: column;
}

.review-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 6px;
}

.card-title {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 15px;
  font-weight: 600;
  color: #000000;
  white-space: nowrap;
  cursor: pointer;
}

.card-title:hover {
  color: #00c665;
}

.score-badge {
  flex-shrink: 0;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 600;
  color: #ffffff;
  background: linear-gradient(135deg, #00e676 0%, #00c665 100%);
  border-radius: 12px;
}

.review-content {
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  line-height: 1.5;
  color: #666666;
  white-space: nowrap;
}

.card-meta {
  margin: 0;
  font-size: 12px;
  color: #999999;
}

.btn-remove-mini {
  display: grid;
  flex-shrink: 0;
  place-items: center;
  width: 32px;
  height: 32px;
  color: #999;
  cursor: pointer;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.btn-remove-mini:hover {
  color: #ff5252;
  background: #fff5f5;
  border-color: #ff5252;
}

/* Pagination */
.pagination {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: center;
  padding: 16px 0;
  margin-top: 28px;
}

.page-btn {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 8px 18px;
  font-family: inherit;
  font-size: 13px;
  color: #000;
  cursor: pointer;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  background: #f0faf4;
  border-color: #00e676;
}

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.page-info {
  min-width: 60px;
  font-size: 13px;
  color: #999;
  text-align: center;
}

/* ========== Modal ========== */
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  background: rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-dialog {
  width: 100%;
  max-width: 480px;
  padding: 28px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  animation: scaleIn 0.3s ease;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.modal-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #000;
}

.hero-signature {
  margin: 0 0 8px 0;
  font-size: 14px;
  line-height: 1.5;
  color: #666666;
}

.meta-item {
  display: inline-block;
  padding: 2px 10px;
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  border-radius: 12px;
}

.meta-sep {
  margin: 0 4px;
  color: #ddd;
}

.role-tag {
  color: #00c665;
  background: #e8f5e9;
}

.btn-modal-close {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  color: #999;
  cursor: pointer;
  background: #fafafa;
  border: none;
  border-radius: 10px;
  transition: all 0.2s ease;
}

.btn-modal-close:hover {
  color: #000;
  background: #f0f0f0;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 13px;
  font-weight: 500;
  color: #000;
}

.field-input {
  padding: 10px 14px;
  font-family: inherit;
  font-size: 14px;
  color: #000;
  outline: none;
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  transition:
    border-color 0.2s ease,
    background 0.2s ease;
}

.field-input:focus {
  background: #fff;
  border-color: #00e676;
}

.select-wrapper {
  position: relative;
}

.field-select {
  width: 100%;
  appearance: none;
  cursor: pointer;
}

.select-arrow {
  position: absolute;
  top: 50%;
  right: 14px;
  pointer-events: none;
  transform: translateY(-50%);
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.btn-cancel {
  padding: 10px 24px;
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: #f0f0f0;
}

.btn-primary {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 10px 24px;
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
  color: #000;
  cursor: pointer;
  background: linear-gradient(135deg, #00e676 0%, #00d66b 100%);
  border: none;
  border-radius: 10px;
  transition: all 0.25s ease;
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #00d66b 0%, #00c665 100%);
  box-shadow: 0 4px 12px rgba(0, 230, 118, 0.3);
}

.btn-primary:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(0, 0, 0, 0.3);
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Tag Grid in Modal */
.tag-checkbox {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 6px 12px;
  font-size: 14px;
  color: #000;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s ease;
}

.tag-checkbox:hover {
  background: #f5f5f5;
}

.tag-checkbox input[type="checkbox"] {
  display: none;
}

.checkbox-custom {
  display: grid;
  flex-shrink: 0;
  place-items: center;
  width: 18px;
  height: 18px;
  border: 2px solid #e0e0e0;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.tag-checkbox input:checked + .checkbox-custom {
  background: #00e676;
  border-color: #00e676;
}

.tag-checkbox input:checked + .checkbox-custom::after {
  width: 6px;
  height: 10px;
  content: "";
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg) translate(-1px, -1px);
}

.tag-name {
  user-select: none;
}

/* ========== Page Loading ========== */
.page-loading {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.loading-pulse {
  width: 40px;
  height: 40px;
  background: #00e676;
  border-radius: 50%;
  animation: pulse 1.2s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.5;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.page-loading p {
  font-size: 14px;
  color: #999;
}

/* ========== Fade In Up Animation ========== */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== Responsive ========== */
@media (max-width: 900px) {
  .main-layout {
    flex-direction: column;
  }
  .sidebar {
    position: static;
    width: 100%;
  }
  .hero-header {
    padding: 32px 24px 28px;
  }
  .hero-content {
    flex-direction: column;
    align-items: flex-start;
  }
  .hero-stats {
    justify-content: space-around;
    width: 100%;
    margin-left: 0;
  }
  .hero-name {
    font-size: 24px;
  }
}

@media (max-width: 600px) {
  .profile-page {
    padding: 0 16px 40px;
  }
  .hero-header {
    margin: 16px 0 28px;
    border-radius: 16px;
  }
  .hero-stats {
    gap: 16px;
  }
  .stat-number {
    font-size: 22px;
  }
  .tab-btn {
    padding: 10px 14px;
    font-size: 14px;
  }
}

.status-badge {
  flex-shrink: 0;
  padding: 2px 6px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 4px;
}
.status-badge.pending {
  color: #e6a23c;
  background: #fdf6ec;
  border: 1px solid #f5dab1;
}
.status-badge.approved {
  color: #67c23a;
  background: #f0f9eb;
  border: 1px solid #c2e7b0;
}
.status-badge.rejected {
  color: #f56c6c;
  background: #fef0f0;
  border: 1px solid #fbc4c4;
}
.status-badge.hidden {
  color: #909399;
  background: #f4f4f5;
  border: 1px solid #d3d4d6;
}

.reject-reason {
  padding: 6px 10px;
  margin-top: 8px;
  font-size: 12px;
  color: #f56c6c;
  background: #fef0f0;
  border-radius: 4px;
}

.review-detail-modal .el-dialog__body {
  padding-top: 10px;
}
.review-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 1px solid #eee;
}
.review-detail-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}
.review-detail-status {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}
.meta-time {
  font-size: 13px;
  color: #999;
}
.review-detail-reject {
  padding: 10px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #f56c6c;
  background: #fef0f0;
  border-radius: 6px;
}
.review-detail-body {
  margin-bottom: 20px;
  font-size: 15px;
  line-height: 1.6;
  color: #444;
  white-space: pre-wrap;
}
.review-detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}
.detail-img {
  width: 100px;
  height: 100px;
  cursor: pointer;
  border: 1px solid #eee;
  border-radius: 6px;
}
.review-detail-footer {
  display: flex;
  gap: 20px;
  padding-top: 16px;
  font-size: 14px;
  color: #666;
  border-top: 1px solid #eee;
}
</style>
