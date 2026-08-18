<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="4" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">车辆状态分布</span></template>
          <div ref="statusChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">车辆电量分布</span></template>
          <div ref="socChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 列表区 -->
    <el-row :gutter="16" class="list-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">近期异常</span></template>
          <el-table :data="recentIncidents" size="small" stripe max-height="320">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="vehicleId" label="车辆" width="80" />
            <el-table-column label="类型" min-width="140">
              <template #default="{ row }">{{ incidentTypeLabel(row.incidentType) }}</template>
            </el-table-column>
            <el-table-column label="严重程度" width="100">
              <template #default="{ row }">
                <el-tag :type="severityType(row.severity)" size="small">{{ severityLabel(row.severity) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="130">
              <template #default="{ row }">
                <el-tag :type="incidentStatusType(row.status)" size="small" effect="plain">{{ incidentStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">近期工单</span></template>
          <el-table :data="recentWorkOrders" size="small" stripe max-height="320">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="workOrderCode" label="工单编号" min-width="160" />
            <el-table-column prop="vehicleId" label="车辆" width="80" />
            <el-table-column label="类型" min-width="150">
              <template #default="{ row }">{{ workOrderTypeLabel(row.type) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="workOrderStatusType(row.status)" size="small" effect="plain">{{ workOrderStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { listVehicles, listReadyVehicles, listAbnormalVehicles } from '../api/vehicle'
import { listIncidents } from '../api/incident'
import { listWorkOrders } from '../api/workOrder'
import {
  incidentTypeLabel, severityLabel, severityType,
  incidentStatusLabel, incidentStatusType,
  workOrderTypeLabel, workOrderStatusLabel, workOrderStatusType,
  vehicleStatusLabel
} from '../utils/statusMap'

const statusChartRef = ref(null)
const socChartRef = ref(null)
let statusChart = null
let socChart = null

const statCards = reactive([
  { label: '车辆总数', value: '-', color: '#303133' },
  { label: '可运营', value: '-', color: '#67c23a' },
  { label: '运营中', value: '-', color: '#409eff' },
  { label: '充电中', value: '-', color: '#e6a23c' },
  { label: '异常车辆', value: '-', color: '#f56c6c' }
])

const recentIncidents = ref([])
const recentWorkOrders = ref([])

function renderStatusChart(vehicles) {
  const countMap = {}
  vehicles.forEach(v => {
    const s = v.vehicleStatus || 'UNKNOWN'
    countMap[s] = (countMap[s] || 0) + 1
  })
  const data = Object.keys(countMap).map(k => ({ name: vehicleStatusLabel(k), value: countMap[k] }))
  statusChart = echarts.init(statusChartRef.value)
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      data,
      label: { formatter: '{b}: {c}' }
    }],
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9c27b0']
  })
}

function renderSocChart(vehicles) {
  const buckets = { '0-20%': 0, '20-40%': 0, '40-60%': 0, '60-80%': 0, '80-100%': 0 }
  vehicles.forEach(v => {
    const soc = v.soc || 0
    if (soc < 20) buckets['0-20%']++
    else if (soc < 40) buckets['20-40%']++
    else if (soc < 60) buckets['40-60%']++
    else if (soc < 80) buckets['60-80%']++
    else buckets['80-100%']++
  })
  socChart = echarts.init(socChartRef.value)
  socChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Object.keys(buckets) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      data: Object.values(buckets),
      itemStyle: { color: '#409eff' },
      barWidth: '50%'
    }],
    grid: { left: 40, right: 20, top: 20, bottom: 40 }
  })
}

function handleResize() {
  statusChart && statusChart.resize()
  socChart && socChart.resize()
}

onMounted(async () => {
  try {
    const [vRes, readyRes, abnRes, incRes, woRes] = await Promise.all([
      listVehicles(),
      listReadyVehicles(),
      listAbnormalVehicles(),
      listIncidents(),
      listWorkOrders()
    ])
    const vehicles = vRes.data || []
    const ready = readyRes.data || []
    const abnormal = abnRes.data || []
    const incidents = incRes.data || []
    const workOrders = woRes.data || []

    statCards[0].value = vehicles.length
    statCards[1].value = ready.length
    statCards[2].value = vehicles.filter(v => v.vehicleStatus === 'OPERATING').length
    statCards[3].value = vehicles.filter(v => v.vehicleStatus === 'CHARGING').length
    statCards[4].value = abnormal.length

    recentIncidents.value = incidents.slice(0, 8)
    recentWorkOrders.value = workOrders.slice(0, 8)

    await nextTick()
    renderStatusChart(vehicles)
    renderSocChart(vehicles)
    window.addEventListener('resize', handleResize)
  } catch (e) {
    console.error('Dashboard load failed', e)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  statusChart && statusChart.dispose()
  socChart && socChart.dispose()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.stat-row { margin-bottom: 0; }
.stat-card {
  text-align: center;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
}
.chart-row { margin-bottom: 0; }
.chart-box {
  height: 280px;
}
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.list-row { margin-bottom: 0; }
</style>
