<template>
  <LoginView v-if="!session.token" @login="handleLogin" />
  <main v-else class="shell">
    <aside class="sidebar">
      <div>
        <p class="eyebrow">Auto Parts</p>
        <h1>汽配库存销售</h1>
      </div>
      <el-menu :default-active="view" @select="view = $event">
        <el-menu-item index="dashboard">数据看板</el-menu-item>
        <el-menu-item index="parts">配件库存</el-menu-item>
        <el-menu-item index="sales">销售出库</el-menu-item>
        <el-menu-item index="base" v-if="canManage">分类供货商</el-menu-item>
      </el-menu>
      <div class="user-card">
        <strong>{{ session.user.nickname }}</strong>
        <span>{{ roleName }}</span>
        <el-button size="small" @click="logout">退出登录</el-button>
      </div>
    </aside>

    <section class="workspace">
      <DashboardView v-if="view === 'dashboard'" :key="refreshKey" />
      <PartsView v-if="view === 'parts'" :can-manage="canManage" @changed="refreshKey++" />
      <SalesView v-if="view === 'sales'" @changed="refreshKey++" />
      <BaseDataView v-if="view === 'base' && canManage" />
    </section>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import LoginView from "./views/LoginView.vue";
import DashboardView from "./views/DashboardView.vue";
import PartsView from "./views/PartsView.vue";
import SalesView from "./views/SalesView.vue";
import BaseDataView from "./views/BaseDataView.vue";

const savedUser = localStorage.getItem("auto-parts-user");
const session = reactive({
  token: localStorage.getItem("auto-parts-token"),
  user: savedUser ? JSON.parse(savedUser) : null
});
const view = ref("dashboard");
const refreshKey = ref(0);
const canManage = computed(() => ["ADMIN", "MANAGER"].includes(session.user?.role));
const roleName = computed(() => ({ ADMIN: "管理员", MANAGER: "店长", STAFF: "销售员" }[session.user?.role] || "未登录"));

function handleLogin(data) {
  session.token = data.token;
  session.user = data.user;
}

function logout() {
  localStorage.removeItem("auto-parts-token");
  localStorage.removeItem("auto-parts-user");
  session.token = "";
  session.user = null;
}
</script>
