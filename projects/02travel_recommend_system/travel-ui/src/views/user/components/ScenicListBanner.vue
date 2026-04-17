<template>
  <div v-if="!isSearchActive" class="hero-banner">
    <el-carousel height="380px" indicator-position="" :interval="5000">
      <el-carousel-item v-for="(bg, idx) in banners" :key="idx">
        <div class="banner-slide" :style="{ backgroundImage: `url('${bg}')` }">
          <div class="banner-overlay">
            <h1 class="banner-title">预订旅行者支持的景点玩乐</h1>
            <div class="banner-search-box">
              <el-icon class="search-icon"><Search /></el-icon>
              <input
                type="text"
                placeholder="按目的地搜索"
                :value="keyword"
                @input="onInput"
                @keyup.enter="emit('search')"
              />
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup lang="ts">
import { Search } from "@element-plus/icons-vue";

interface Props {
  isSearchActive: boolean;
  keyword: string;
  banners: string[];
}

defineProps<Props>();
const emit = defineEmits<{
  "update:keyword": [value: string];
  search: [];
}>();

function onInput(event: Event): void {
  const value = (event.target as HTMLInputElement).value;
  emit("update:keyword", value);
}
</script>

<style scoped>
.hero-banner {
  width: 100%;
  margin-bottom: 40px;
  background: #fff;
}

.banner-slide {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.banner-title {
  color: #fff;
  font-size: 42px;
  font-weight: 800;
  margin-bottom: 32px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.5);
  text-align: center;
}

.banner-search-box {
  background: #fff;
  border-radius: 999px;
  width: 600px;
  max-width: 90%;
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 28px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.search-icon {
  font-size: 22px;
  color: #333;
  margin-right: 16px;
}

.banner-search-box input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 18px;
  color: #333;
  background: transparent;
}

.banner-search-box input::placeholder {
  color: #888;
}
</style>
