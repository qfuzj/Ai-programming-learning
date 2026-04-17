<template>
  <div v-if="detail" class="page-container">
    <ScenicDetailHeader
      :detail="detail"
      @scroll-reviews="scrollToReviews"
      @toggle-favorite="toggleFavorite"
      @write-review="goWriteReview"
    />

    <ScenicDetailGallery :images="galleryImages" />

    <div class="content-grid">
      <div class="left-column">
        <ScenicDetailInfoSection
          :detail="detail"
          :intro-expanded="introExpanded"
          @toggle-intro="introExpanded = !introExpanded"
        />

        <ScenicSimilarSection
          :title="`${detail.name} 的相似推荐`"
          :items="relatedExperiences"
          @select="goToSimilar"
        />

        <ScenicReviewsSection
          section-id="reviews-section"
          :loading="reviewsLoading"
          :reviews="reviews"
          :total="reviewsTotal"
          :page-num="reviewQuery.pageNum"
          :page-size="reviewQuery.pageSize"
          @write-review="goWriteReview"
          @update:page-num="
            (v) => {
              reviewQuery.pageNum = v;
              loadReviews();
            }
          "
          @update-review="({ index, review }) => updateReview(index, review)"
        />
      </div>

      <div class="right-column">
        <ScenicSidePanel :ticket-price="detail.ticketPrice" :open-time="detail.openTime" />
      </div>
    </div>
  </div>
  <el-empty v-else-if="!loading" description="暂无景点数据" />
</template>

<script setup lang="ts">
import { computed, watch } from "vue";
import { useRoute } from "vue-router";
import ScenicDetailHeader from "./components/ScenicDetailHeader.vue";
import ScenicDetailGallery from "./components/ScenicDetailGallery.vue";
import ScenicDetailInfoSection from "./components/ScenicDetailInfoSection.vue";
import ScenicSimilarSection from "./components/ScenicSimilarSection.vue";
import ScenicReviewsSection from "./components/ScenicReviewsSection.vue";
import ScenicSidePanel from "./components/ScenicSidePanel.vue";
import { useScenicDetail } from "@/composables/useScenicDetail";

const route = useRoute();
const scenicId = computed(() => Number(route.params.id));

const {
  loading,
  detail,
  introExpanded,
  reviewsLoading,
  reviews,
  reviewsTotal,
  reviewQuery,
  relatedExperiences,
  galleryImages,
  loadReviews,
  loadDetail,
  toggleFavorite,
  goWriteReview,
  goToSimilar,
  scrollToReviews,
  updateReview,
} = useScenicDetail();

watch(
  scenicId,
  (id) => {
    void loadDetail(id);
  },
  { immediate: true }
);
</script>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.content-grid {
  display: flex;
  gap: 40px;
}

.left-column {
  flex: 2;
  min-width: 0;
}

.right-column {
  flex: 1;
  min-width: 0;
}

@media (max-width: 992px) {
  .content-grid {
    flex-direction: column;
  }
}
</style>
