// src/main.ts
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router';

// 1. 导入所有需要的图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

// 全局样式：tokens 必须在 main.css 之前，保证变量先于使用注册
import './assets/tokens.css';
import './assets/main.css';

const app = createApp(App);

// 2. 注册所有 Element Plus 图标为全局组件 (推荐方式，简化模板代码)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  // 注册的组件名格式如 'Setting', 'User', 'Expand'
  app.component(key, component);
}

app.use(createPinia());
app.use(router);
app.use(ElementPlus);

app.mount('#app');