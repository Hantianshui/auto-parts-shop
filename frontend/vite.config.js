import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  plugins: [vue()],
  server: {
    allowedHosts: [
      "photosynthetically-semichaotic-lezlie.ngrok-free.dev"
    ],
    proxy: {
      "/api": "http://localhost:8080",
      "/uploads": "http://localhost:8080"
    }
  }
});

