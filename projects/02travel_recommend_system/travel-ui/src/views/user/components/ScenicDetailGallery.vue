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
  margin-bottom: 40px;
  overflow: hidden;
  border-radius: 8px;
}

.main-photo {
  flex: 2;
  background-position: center;
  background-size: cover;
}

.side-photos {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 4px;
}

.side-photo {
  position: relative;
  flex: 1;
  background-position: center;
  background-size: cover;
}

.photo-overlay {
  position: absolute;
  right: 16px;
  bottom: 16px;
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 6px 12px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: rgba(0, 0, 0, 0.7);
  border-radius: 4px;
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
