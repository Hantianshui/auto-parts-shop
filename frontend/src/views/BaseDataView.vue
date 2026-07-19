<template>
  <div>
    <header class="page-head">
      <div>
        <p class="eyebrow">Base Data</p>
        <h2>分类供货商</h2>
      </div>
    </header>
    <div class="two-col">
      <section class="panel">
        <h3>分类</h3>
        <el-form :model="category" inline>
          <el-input v-model="category.name" placeholder="分类名称" />
          <el-button type="primary" @click="saveCategory">保存</el-button>
        </el-form>
        <el-table :data="categories">
          <el-table-column prop="name" label="名称" />
        </el-table>
      </section>
      <section class="panel">
        <h3>供货商</h3>
        <el-form :model="supplier" label-width="70px">
          <el-form-item label="名称"><el-input v-model="supplier.name" /></el-form-item>
          <el-form-item label="电话"><el-input v-model="supplier.phone" /></el-form-item>
          <el-form-item label="地址"><el-input v-model="supplier.address" /></el-form-item>
          <el-button type="primary" @click="saveSupplier">保存</el-button>
        </el-form>
        <el-table :data="suppliers">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="phone" label="电话" />
        </el-table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { http } from "../api/http";

const categories = ref([]);
const suppliers = ref([]);
const category = reactive({ name: "", sortOrder: 0 });
const supplier = reactive({ name: "", phone: "", address: "", note: "" });

async function load() {
  categories.value = await http.get("/categories");
  suppliers.value = await http.get("/suppliers");
}

async function saveCategory() {
  await http.post("/categories", category);
  Object.assign(category, { name: "", sortOrder: 0 });
  load();
}

async function saveSupplier() {
  await http.post("/suppliers", supplier);
  Object.assign(supplier, { name: "", phone: "", address: "", note: "" });
  load();
}

onMounted(load);
</script>
