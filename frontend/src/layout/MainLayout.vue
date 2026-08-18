<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo-block">
        <div class="logo-title">AI FleetOps</div>
        <div class="logo-subtitle">Robotaxi 智能车队运营系统</div>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        class="sidebar-menu"
        background-color="#1f2d3d"
        text-color="#c0ccda"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>运营总览</span>
        </el-menu-item>
        <el-menu-item index="/fleet">
          <el-icon><Van /></el-icon>
          <span>车队管理</span>
        </el-menu-item>
        <el-menu-item index="/incidents">
          <el-icon><Warning /></el-icon>
          <span>异常中心</span>
        </el-menu-item>
        <el-menu-item index="/work-orders">
          <el-icon><Tickets /></el-icon>
          <span>工单中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">{{ $route.meta.title || '运营总览' }}</div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { Odometer, Van, Warning, Tickets } from '@element-plus/icons-vue'
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.sidebar {
  background-color: #1f2d3d;
  overflow-x: hidden;
}
.logo-block {
  padding: 20px 16px;
  border-bottom: 1px solid #2c3e50;
}
.logo-title {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.logo-subtitle {
  color: #8492a6;
  font-size: 12px;
  margin-top: 4px;
}
.sidebar-menu {
  border-right: none;
}
.header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  padding: 0 24px;
  height: 56px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.main-content {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>
