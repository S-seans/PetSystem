<template>
  <router-view />
  <AiChat v-if="route.meta.chat" />
</template>

<script setup>
import { useRoute } from 'vue-router'
import AiChat from '@/components/AiChat/ChatBubble'
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'

const route = useRoute()

onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(useSettingsStore().theme)
  })
})
</script>
