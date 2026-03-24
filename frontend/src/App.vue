<script setup lang="ts">
import { defineAsyncComponent } from 'vue'

const AIFloatingChat = defineAsyncComponent(() => import('./components/AIFloatingChat.vue'))
</script>

<template>
  <div>
    <router-view v-slot="{ Component }">
      <transition name="page-rise" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
    <AIFloatingChat />
  </div>
</template>

<style>
.page-rise-enter-active,
.page-rise-leave-active {
  transition: opacity 0.28s ease, transform 0.28s ease;
}

.page-rise-enter-from,
.page-rise-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>
