<template>
  <div>
    <header class="page-head">
      <div>
        <p class="eyebrow">Sales</p>
        <h2>销售出库</h2>
      </div>
    </header>

    <section class="panel form-panel">
      <el-form :model="form" label-width="90px">
        <el-form-item label="配件">
          <el-select v-model="form.partId" filterable placeholder="选择配件" @change="fillPrice">
            <el-option v-for="part in parts" :key="part.id" :label="`${part.name}（库存 ${part.stock}）`" :value="part.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量"><el-input-number v-model="form.quantity" :min="1" /></el-form-item>
        <el-form-item label="单价"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="客户"><el-input v-model="form.customer" placeholder="客户、车牌或电话" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.note" /></el-form-item>
        <el-button type="primary" @click="save">记录销售并扣库存</el-button>
      </el-form>
    </section>

    <el-table :data="sales" class="panel">
      <el-table-column prop="partName" label="配件" min-width="150" />
      <el-table-column prop="quantity" label="数量" width="90" />
      <el-table-column prop="price" label="单价" width="100" />
      <el-table-column prop="totalAmount" label="合计" width="110" />
      <el-table-column prop="customer" label="客户" min-width="140" />
      <el-table-column prop="createdAt" label="时间" min-width="180" />
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { http } from "../api/http";

const emit = defineEmits(["changed"]);
const parts = ref([]);
const sales = ref([]);
const form = reactive({ partId: null, quantity: 1, price: 0, customer: "", note: "" });

async function load() {
  parts.value = await http.get("/parts");
  sales.value = await http.get("/sales");
}

function fillPrice(partId) {
  const part = parts.value.find((item) => item.id === partId);
  form.price = part?.price || 0;
}

async function save() {
  await http.post("/sales", form);
  Object.assign(form, { partId: null, quantity: 1, price: 0, customer: "", note: "" });
  emit("changed");
  load();
}

onMounted(load);
</script>
