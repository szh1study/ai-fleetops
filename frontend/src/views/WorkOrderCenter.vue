<template>
  <div class="work-order-page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 200px">
          <el-option v-for="s in ['PENDING','PROCESSING','COMPLETED','CANCELLED']" :key="s" :label="workOrderStatusLabel(s)" :value="s" />
        </el-select>
        <span class="count-text">共 {{ filteredWorkOrders.length }} 条</span>
      </div>

      <el-table :data="filteredWorkOrders" stripe border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="工单ID" width="80" />
        <el-table-column prop="workOrderCode" label="工单编号" min-width="180" />
        <el-table-column prop="vehicleId" label="车辆ID" width="100" />
        <el-table-column prop="incidentId" label="异常ID" width="100" />
        <el-table-column label="工单类型" min-width="150">
          <template #default="{ row }">{{ workOrderTypeLabel(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="workOrderStatusType(row.status)" size="small" effect="plain">{{ workOrderStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="完成时间" width="170">
          <template #default="{ row }">{{ formatTime(row.completedAt) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listWorkOrders } from '../api/workOrder'
import { workOrderStatusLabel, workOrderStatusType, workOrderTypeLabel } from '../utils/statusMap'

const workOrders = ref([])
const loading = ref(false)
const filterStatus = ref('')

const filteredWorkOrders = computed(() => {
  return workOrders.value.filter(w => !filterStatus.value || w.status === filterStatus.value)
})

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await listWorkOrders()
    workOrders.value = res.data || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.work-order-page { width: 100%; }
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
