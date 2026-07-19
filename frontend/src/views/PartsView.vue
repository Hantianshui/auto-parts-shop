<template>
  <div>
    <header class="page-head">
      <div>
        <p class="eyebrow">Inventory</p>
        <h2>配件库存</h2>
      </div>
      <el-button v-if="canManage" type="primary" @click="openEditor()">新增配件</el-button>
    </header>

    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索名称、编码、车型" clearable @input="load" />
      <el-select v-model="stockStatus" @change="load">
        <el-option label="全部库存" value="" />
        <el-option label="低库存" value="low" />
        <el-option label="缺货" value="empty" />
      </el-select>
    </div>

    <el-table :data="parts" class="panel" row-key="id">
      <el-table-column prop="name" label="配件" min-width="150" />
      <el-table-column prop="code" label="编码" width="120" />
      <el-table-column prop="models" label="适用车型" min-width="180" />
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column prop="warningStock" label="预警" width="90" />
      <el-table-column prop="cost" label="进货价" width="100" />
      <el-table-column prop="price" label="售价" width="100" />
      <el-table-column v-if="canManage" label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openEditor(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="配件信息" width="620px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="适用车型"><el-input v-model="form.models" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="预警库存"><el-input-number v-model="form.warningStock" :min="0" /></el-form-item>
        <el-form-item label="进货价"><el-input-number v-model="form.cost" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="售价"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessageBox } from "element-plus";
import { http } from "../api/http";

defineProps({ canManage: Boolean });
const emit = defineEmits(["changed"]);
const parts = ref([]);
const keyword = ref("");
const stockStatus = ref("");
const dialog = ref(false);
const form = reactive({});

async function load() {
  parts.value = await http.get("/parts", { params: { keyword: keyword.value, stockStatus: stockStatus.value } });
}

function openEditor(row = {}) {
  Object.assign(form, { id: null, name: "", code: "", models: "", stock: 0, warningStock: 5, cost: 0, price: 0 }, row);
  dialog.value = true;
}

async function save() {
  await http.post("/parts", form);
  dialog.value = false;
  emit("changed");
  load();
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除 ${row.name}？`, "删除确认", { type: "warning" });
  await http.delete(`/parts/${row.id}`);
  emit("changed");
  load();
}

onMounted(load);
</script>
