<template>
  <div class="fleet-page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="searchCode"
          placeholder="按车辆编号搜索"
          clearable
          style="width: 240px"
        />
        <el-select v-model="filterStatus" placeholder="车辆状态" clearable style="width: 180px">
          <el-option v-for="s in statusOptions" :key="s" :label="vehicleStatusLabel(s)" :value="s" />
        </el-select>
        <span class="count-text">共 {{ filteredVehicles.length }} 辆</span>
      </div>

      <el-table :data="filteredVehicles" stripe border style="width: 100%" v-loading="loading">
        <el-table-column prop="vehicleCode" label="车辆编号" width="120" fixed />
        <el-table-column prop="vehicleModel" label="车型" width="140" />
        <el-table-column label="SOC" width="160">
          <template #default="{ row }">
            <el-progress
              :percentage="row.soc || 0"
              :color="socColor(row.soc)"
              :stroke-width="14"
              :text-inside="true"
            />
          </template>
        </el-table-column>
        <el-table-column label="当前区域" width="120">
          <template #default="{ row }">{{ areaLabel(row.currentArea) }}</template>
        </el-table-column>
        <el-table-column label="车辆状态" width="130">
          <template #default="{ row }">
            <el-tag :type="vehicleStatusType(row.vehicleStatus)" size="small">{{ vehicleStatusLabel(row.vehicleStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="orderStatusType(row.orderStatus)" size="small" effect="plain">{{ orderStatusLabel(row.orderStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="摄像头" width="100">
          <template #default="{ row }">
            <el-tag :type="deviceStatusType(row.cameraStatus)" size="small" effect="plain">{{ deviceStatusLabel(row.cameraStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="LiDAR" width="100">
          <template #default="{ row }">
            <el-tag :type="deviceStatusType(row.lidarStatus)" size="small" effect="plain">{{ deviceStatusLabel(row.lidarStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="网络" width="100">
          <template #default="{ row }">
            <el-tag :type="deviceStatusType(row.networkStatus)" size="small" effect="plain">{{ deviceStatusLabel(row.networkStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="软件" width="100">
          <template #default="{ row }">
            <el-tag :type="deviceStatusType(row.softwareStatus)" size="small" effect="plain">{{ deviceStatusLabel(row.softwareStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" min-width="170">
          <template #default="{ row }">
            {{ formatTime(row.updatedAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listVehicles } from '../api/vehicle'
import {
  vehicleStatusLabel, vehicleStatusType,
  orderStatusLabel, orderStatusType,
  deviceStatusLabel, deviceStatusType,
  areaLabel
} from '../utils/statusMap'

const vehicles = ref([])
const loading = ref(false)
const searchCode = ref('')
const filterStatus = ref('')

const statusOptions = computed(() => {
  const set = new Set(vehicles.value.map(v => v.vehicleStatus).filter(Boolean))
  return Array.from(set)
})

const filteredVehicles = computed(() => {
  return vehicles.value.filter(v => {
    const matchCode = !searchCode.value || (v.vehicleCode || '').toLowerCase().includes(searchCode.value.toLowerCase())
    const matchStatus = !filterStatus.value || v.vehicleStatus === filterStatus.value
    return matchCode && matchStatus
  })
})

function socColor(soc) {
  if (soc == null) return '#909399'
  if (soc < 20) return '#f56c6c'
  if (soc < 50) return '#e6a23c'
  return '#67c23a'
}
function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await listVehicles()
    vehicles.value = res.data || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.fleet-page {
  width: 100%;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.count-text {
  margin-left: auto;
  font-size: 13px;
  color: #909399;
}
</style>
