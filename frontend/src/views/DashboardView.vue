<template>
  <div>
    <header class="page-head">
      <div>
        <p class="eyebrow">Dashboard</p>
        <h2>数据看板</h2>
      </div>
      <el-button @click="load">刷新</el-button>
    </header>

    <div class="metrics">
      <article><span>配件种类</span><strong>{{ data.partCount || 0 }}</strong></article>
      <article><span>低库存</span><strong>{{ data.lowStockCount || 0 }}</strong></article>
      <article><span>库存金额</span><strong>{{ money(data.stockValue) }}</strong></article>
      <article><span>本月销售</span><strong>{{ money(data.monthSales) }}</strong></article>
    </div>
    <section class="panel">
      <div ref="chartEl" class="chart"></div>
    </section>
  </div>
</template>

<script setup>
import * as echarts from "echarts";
import { nextTick, onMounted, ref } from "vue";
import { http } from "../api/http";

const data = ref({});
const chartEl = ref();
let chart;
const money = (value) => `¥${Number(value || 0).toFixed(2)}`;

async function load() {
  data.value = await http.get("/dashboard");
  await nextTick();
  if (!chart) chart = echarts.init(chartEl.value);
  const entries = Object.entries(data.value.salesByPart || {});
  chart.setOption({
    tooltip: {},
    xAxis: { type: "category", data: entries.map(([name]) => name) },
    yAxis: { type: "value" },
    series: [{ name: "销售额", type: "bar", data: entries.map(([, value]) => value), itemStyle: { color: "#2f6f5e" } }]
  });
}

onMounted(load);
</script>
