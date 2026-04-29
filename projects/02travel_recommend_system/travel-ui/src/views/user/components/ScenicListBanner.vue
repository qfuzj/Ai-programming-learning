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
  position: relative;
  width: 100%;
  height: 100%;
  background-position: center;
  background-size: cover;
}

.banner-overlay {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.35);
}

.banner-title {
  margin-bottom: 32px;
  font-size: 42px;
  font-weight: 800;
  color: #fff;
  text-align: center;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.5);
}

.banner-search-box {
  display: flex;
  align-items: center;
  width: 600px;
  max-width: 90%;
  height: 60px;
  padding: 0 28px;
  background: #fff;
  border-radius: 999px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.search-icon {
  margin-right: 16px;
  font-size: 22px;
  color: #333;
}

.banner-search-box input {
  flex: 1;
  font-size: 18px;
  color: #333;
  outline: none;
  background: transparent;
  border: none;
}

.banner-search-box input::placeholder {
  color: #888;
}
</style>
