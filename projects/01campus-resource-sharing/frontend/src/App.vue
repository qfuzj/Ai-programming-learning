<template>
  <n-config-provider>
    <n-dialog-provider>
      <n-message-provider>
        <router-view />
      </n-message-provider>
    </n-dialog-provider>
  </n-config-provider>
</template>

<script setup>
import { onMounted } from 'vue'
import { NConfigProvider, NDialogProvider, NMessageProvider } from 'naive-ui'
import { ping } from './api/common'

onMounted(async () => {
  try {
    await ping()
  } catch (error) {
    console.warn('Backend ping failed:', error)
  }
})
</script>
