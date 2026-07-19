<template>
  <section class="login-page">
    <div class="login-panel">
      <p class="eyebrow">Auto Parts Admin</p>
      <h1>汽配库存销售管理系统</h1>
      <el-form :model="form" label-position="top" @submit.prevent="login">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="admin / manager / staff" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="默认 123456" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading">登录</el-button>
      </el-form>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from "vue";
import { http } from "../api/http";

const emit = defineEmits(["login"]);
const form = reactive({ username: "admin", password: "123456" });
const loading = ref(false);

async function login() {
  loading.value = true;
  try {
    const data = await http.post("/auth/login", form);
    localStorage.setItem("auto-parts-token", data.token);
    localStorage.setItem("auto-parts-user", JSON.stringify(data.user));
    emit("login", data);
  } finally {
    loading.value = false;
  }
}
</script>
