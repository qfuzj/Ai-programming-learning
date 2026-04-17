<template>
  <div class="photo-gallery">
    <div class="main-photo" :style="{ backgroundImage: `url(${images[0]})` }"></div>
    <div class="side-photos">
      <div class="side-photo" :style="{ backgroundImage: `url(${images[1]})` }"></div>
      <div class="side-photo" :style="{ backgroundImage: `url(${images[2]})` }">
        <div class="photo-overlay">
          <el-icon><Picture /></el-icon>
          <span>80+</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Picture } from "@element-plus/icons-vue";

interface Props {
  images: string[];
}

defineProps<Props>();
</script>

<style scoped>
.photo-gallery {
  display: flex;
  gap: 4px;
  height: 440px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 40px;
}

.main-photo {
  flex: 2;
  background-size: cover;
  background-position: center;
}

.side-photos {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.side-photo {
  flex: 1;
  background-size: cover;
  background-position: center;
  position: relative;
}

.photo-overlay {
  position: absolute;
  right: 16px;
  bottom: 16px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  border-radius: 4px;
  padding: 6px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
}

@media (max-width: 768px) {
  .photo-gallery {
    flex-direction: column;
    height: auto;
  }

  .main-photo {
    height: 240px;
  }

  .side-photos {
    flex-direction: row;
    height: 120px;
  }
}
</style>
