const storeKey = "auto-parts-shop-v1";
const sample = {
  parts: [
    { id: crypto.randomUUID(), name: "机油滤芯", stock: 18, cost: 18, price: 35, models: "大众、丰田常用", supplier: "老王汽配 13800000000", supplierInfo: "当天发货，支持微信结算" },
    { id: crypto.randomUUID(), name: "前刹车片", stock: 4, cost: 95, price: 160, models: "朗逸、宝来、速腾", supplier: "城南制动件", supplierInfo: "可供原厂和副厂，急件可闪送" }
  ],
  sales: []
};

let state = load();

const $ = (selector) => document.querySelector(selector);
const money = (value) => Number(value || 0).toFixed(2).replace(/\.00$/, "");

function load() {
  const saved = localStorage.getItem(storeKey);
  return saved ? JSON.parse(saved) : sample;
}

function save() {
  localStorage.setItem(storeKey, JSON.stringify(state));
  render();
}

function render() {
  const query = $("#search").value.trim().toLowerCase();
  const filter = $("#stockFilter").value;
  const parts = state.parts.filter((part) => {
    const text = `${part.name} ${part.models} ${part.supplier} ${part.supplierInfo}`.toLowerCase();
    const matchesQuery = !query || text.includes(query);
    const matchesStock = filter === "all" || (filter === "low" && part.stock > 0 && part.stock <= 5) || (filter === "empty" && part.stock <= 0);
    return matchesQuery && matchesStock;
  });

  $("#partsCount").textContent = state.parts.length;
  $("#stockValue").textContent = money(state.parts.reduce((sum, part) => sum + Number(part.cost || 0) * Number(part.stock || 0), 0));
  $("#monthSales").textContent = money(monthSales());

  renderParts(parts);
  renderSales();
}

function renderParts(parts) {
  const list = $("#partsList");
  list.innerHTML = "";
  if (!parts.length) {
    list.innerHTML = '<div class="empty-state">没有匹配的库存记录。</div>';
    return;
  }

  for (const part of parts) {
    const node = $("#partTemplate").content.cloneNode(true);
    node.querySelector("h3").textContent = part.name;
    node.querySelector(".meta").textContent = `适用车型：${part.models || "未填写"} | 进货价 ${money(part.cost)} 元 | 售价 ${money(part.price)} 元`;
    node.querySelector(".supplier").textContent = `供货：${part.supplier || "未填写"} ${part.supplierInfo ? "｜" + part.supplierInfo : ""}`;
    const stock = node.querySelector(".stock");
    stock.textContent = `${part.stock} 件`;
    stock.className = `stock ${part.stock <= 0 ? "empty" : part.stock <= 5 ? "low" : ""}`;
    node.querySelector(".edit").addEventListener("click", () => fillPartForm(part));
    node.querySelector(".delete").addEventListener("click", () => deletePart(part.id));
    list.append(node);
  }
}

function renderSales() {
  const list = $("#salesList");
  list.innerHTML = "";
  const sales = [...state.sales].sort((a, b) => b.time.localeCompare(a.time));
  if (!sales.length) {
    list.innerHTML = '<div class="empty-state">还没有销售记录。</div>';
    return;
  }

  for (const sale of sales) {
    const item = document.createElement("article");
    item.className = "item";
    item.innerHTML = `
      <h3>${sale.partName} × ${sale.quantity}</h3>
      <p class="meta">${new Date(sale.time).toLocaleString()} | 单价 ${money(sale.price)} 元 | 合计 ${money(sale.quantity * sale.price)} 元</p>
      <p class="supplier">${sale.customer || "散客"} ${sale.note ? "｜" + sale.note : ""}</p>
    `;
    list.append(item);
  }
}

function monthSales() {
  const now = new Date();
  return state.sales.reduce((sum, sale) => {
    const date = new Date(sale.time);
    return date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth()
      ? sum + sale.quantity * sale.price
      : sum;
  }, 0);
}

function fillPartForm(part) {
  const form = $("#partForm");
  for (const key of ["name", "stock", "cost", "price", "models", "supplier", "supplierInfo"]) {
    form.elements[key].value = part[key] ?? "";
  }
  form.dataset.editing = part.id;
  form.scrollIntoView({ behavior: "smooth", block: "center" });
}

function deletePart(id) {
  if (!confirm("确定删除这个零件吗？销售记录会保留。")) return;
  state.parts = state.parts.filter((part) => part.id !== id);
  save();
}

$("#partForm").addEventListener("submit", (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const data = Object.fromEntries(new FormData(form));
  const part = {
    id: form.dataset.editing || crypto.randomUUID(),
    name: data.name.trim(),
    stock: Number(data.stock || 0),
    cost: Number(data.cost || 0),
    price: Number(data.price || 0),
    models: data.models.trim(),
    supplier: data.supplier.trim(),
    supplierInfo: data.supplierInfo.trim()
  };

  const index = state.parts.findIndex((item) => item.id === part.id);
  if (index >= 0) state.parts[index] = part;
  else state.parts.push(part);
  form.reset();
  delete form.dataset.editing;
  save();
});

$("#saleForm").addEventListener("submit", (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const data = Object.fromEntries(new FormData(form));
  const sale = {
    id: crypto.randomUUID(),
    partName: data.partName.trim(),
    quantity: Number(data.quantity || 1),
    price: Number(data.price || 0),
    customer: data.customer.trim(),
    note: data.note.trim(),
    time: new Date().toISOString()
  };

  let part = state.parts.find((item) => item.name === sale.partName);
  if (!part) {
    part = { id: crypto.randomUUID(), name: sale.partName, stock: 0, cost: 0, price: sale.price, models: "", supplier: "", supplierInfo: "" };
    state.parts.push(part);
  }
  part.stock = Number(part.stock || 0) - sale.quantity;
  if (!part.price) part.price = sale.price;
  state.sales.push(sale);
  form.reset();
  form.elements.quantity.value = 1;
  save();
});

$("#search").addEventListener("input", render);
$("#stockFilter").addEventListener("change", render);

$("#exportBtn").addEventListener("click", () => {
  const blob = new Blob([JSON.stringify(state, null, 2)], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `汽配店数据-${new Date().toISOString().slice(0, 10)}.json`;
  link.click();
  URL.revokeObjectURL(url);
});

$("#importInput").addEventListener("change", async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  state = JSON.parse(await file.text());
  save();
  event.target.value = "";
});

const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
if (!SpeechRecognition) {
  $("#voiceBtn").disabled = true;
  $("#voiceStatus").textContent = "当前浏览器不支持语音识别，可以使用 Chrome 或 Edge 打开本项目。";
} else {
  const recognizer = new SpeechRecognition();
  recognizer.lang = "zh-CN";
  recognizer.continuous = false;
  recognizer.interimResults = false;

  $("#voiceBtn").addEventListener("click", () => {
    $("#voiceBtn").classList.add("listening");
    $("#voiceStatus").textContent = "正在听，请说销售内容...";
    recognizer.start();
  });

  recognizer.addEventListener("result", (event) => {
    const text = event.results[0][0].transcript;
    $("#voiceStatus").textContent = `识别到：${text}`;
    applyVoiceSale(text);
  });

  recognizer.addEventListener("end", () => $("#voiceBtn").classList.remove("listening"));
  recognizer.addEventListener("error", () => {
    $("#voiceStatus").textContent = "语音识别没有成功，请检查麦克风权限后再试。";
  });
}

function applyVoiceSale(text) {
  const form = $("#saleForm");
  const quantity = Number((text.match(/(\d+)\s*(个|件|套|只|条)?/) || [])[1] || 1);
  const price = Number((text.match(/单价\s*(\d+(\.\d+)?)/) || text.match(/(\d+(\.\d+)?)\s*元/) || [])[1] || "");
  const customer = (text.match(/客户\s*([\u4e00-\u9fa5A-Za-z0-9-]+)/) || text.match(/给\s*([\u4e00-\u9fa5A-Za-z0-9-]+)/) || [])[1] || "";
  let partName = text
    .replace(/卖出|销售|出库|客户.+|给.+|单价\s*\d+(\.\d+)?|\d+\s*(个|件|套|只|条)?|\d+(\.\d+)?\s*元/g, "")
    .trim();

  if (!partName) {
    const known = state.parts.find((part) => text.includes(part.name));
    partName = known?.name || "";
  }

  form.elements.partName.value = partName;
  form.elements.quantity.value = quantity;
  if (price) form.elements.price.value = price;
  form.elements.customer.value = customer;
  form.elements.note.value = text;
}

render();
