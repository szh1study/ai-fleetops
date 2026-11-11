<template>
  <div class="review-page">
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <span class="card-title">复盘日期</span>
        <el-date-picker
          v-model="date"
          type="date"
          value-format="YYYY-MM-DD"
          style="width: 180px"
          @change="loadAll"
        />
      </div>
    </el-card>

    <!-- 预测偏差 -->
    <el-card shadow="never">
      <template #header><span class="card-title">预测偏差</span></template>
      <div v-if="forecastReview.length" class="chart-box">
        <div ref="forecastChartRef" class="chart"></div>
      </div>
      <el-table :data="forecastReview" size="small" border>
        <el-table-column label="区域" min-width="120">
          <template #default="{ row }">{{ areaLabel(row.area) }}</template>
        </el-table-column>
        <el-table-column prop="timeRange" label="时段" min-width="130" />
        <el-table-column prop="predictedOrders" label="预测订单" width="100" />
        <el-table-column prop="actualOrders" label="实际订单" width="100" />
        <el-table-column label="偏差率" width="120">
          <template #default="{ row }">
            <span v-if="row.deviation == null">-</span>
            <span v-else :class="devClass(row.deviation)">{{ fmtDev(row.deviation) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!forecastReview.length" description="该日期暂无预测记录" />
    </el-card>

    <!-- 调度效果 -->
    <el-card shadow="never">
      <template #header><span class="card-title">调度效果</span></template>
      <el-table :data="dispatchReview" size="small" border>
        <el-table-column label="调度" min-width="210">
          <template #default="{ row }">
            {{ areaLabel(row.fromArea) }} → {{ areaLabel(row.toArea) }} · {{ row.vehicleCount }} 辆
          </template>
        </el-table-column>
        <el-table-column prop="timeRange" label="时段" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="suggestionStatusType(row.status)">
              {{ suggestionStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="predictedOrders" label="预测订单" width="100" />
        <el-table-column prop="actualOrders" label="实际订单" width="100" />
        <el-table-column label="需求满足" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.demandMet == null" size="small" type="info">-</el-tag>
            <el-tag v-else size="small" :type="row.demandMet ? 'success' : 'warning'">
              {{ row.demandMet ? '已满足' : '未满足' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!dispatchReview.length" description="该日期暂无调度建议" />
    </el-card>

    <!-- 异常时效 -->
    <el-card shadow="never">
      <template #header><span class="card-title">异常处理时效</span></template>
      <div class="stat-row">
        <div class="stat-item">
          <div class="stat-value">{{ incidentEfficiency?.resolvedCount ?? 0 }}</div>
          <div class="stat-label">当日解决异常</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ fmtMinutes(incidentEfficiency?.avgResolutionMinutes) }}</div>
          <div class="stat-label">平均解决耗时</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ fmtMinutes(incidentEfficiency?.maxResolutionMinutes) }}</div>
          <div class="stat-label">最长解决耗时</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ fmtMinutes(incidentEfficiency?.avgWorkOrderCompletionMinutes) }}</div>
          <div class="stat-label">平均工单完成耗时</div>
        </div>
      </div>
      <div class="group-tables">
        <div class="group-table">
          <div class="group-title">按类型</div>
          <el-table :data="incidentEfficiency?.byType || []" size="small" border>
            <el-table-column prop="key" label="类型" />
            <el-table-column prop="count" label="数量" width="90" />
            <el-table-column label="平均耗时" width="110">
              <template #default="{ row }">{{ fmtMinutes(row.avgMinutes) }}</template>
            </el-table-column>
          </el-table>
        </div>
        <div class="group-table">
          <div class="group-title">按严重度</div>
          <el-table :data="incidentEfficiency?.bySeverity || []" size="small" border>
            <el-table-column label="严重度">
              <template #default="{ row }">{{ severityLabel(row.key) }}</template>
            </el-table-column>
            <el-table-column prop="count" label="数量" width="90" />
            <el-table-column label="平均耗时" width="110">
              <template #default="{ row }">{{ fmtMinutes(row.avgMinutes) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>

    <!-- AI 采纳率 -->
    <el-card shadow="never">
      <template #header><span class="card-title">AI 建议采纳率（累计）</span></template>
      <div class="adoption-top">
        <div v-if="aiAdoption" class="chart-box adoption-chart">
          <div ref="adoptionChartRef" class="chart"></div>
        </div>
        <div class="adoption-side">
          <div class="stat-item">
            <div class="stat-value">{{ aiAdoption?.executed ?? 0 }}</div>
            <div class="stat-label">已执行</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ aiAdoption?.rejected ?? 0 }}</div>
            <div class="stat-label">已拒绝</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ aiAdoption?.pending ?? 0 }}</div>
            <div class="stat-label">待处理</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">
              {{ aiAdoption?.adoptionRate == null ? '-' : aiAdoption.adoptionRate + '%' }}
            </div>
            <div class="stat-label">采纳率</div>
          </div>
        </div>
      </div>
      <el-table :data="aiAdoption?.byType || []" size="small" border>
        <el-table-column label="建议类型">
          <template #default="{ row }">{{ suggestionTypeLabel(row.suggestionType) }}</template>
        </el-table-column>
        <el-table-column prop="executed" label="已执行" width="90" />
        <el-table-column prop="rejected" label="已拒绝" width="90" />
        <el-table-column prop="pending" label="待处理" width="90" />
        <el-table-column label="采纳率" width="110">
          <template #default="{ row }">{{ row.adoptionRate == null ? '-' : row.adoptionRate + '%' }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  listForecastReview,
  listDispatchReview,
  getIncidentEfficiency,
  getAiAdoption
} from '../api/operationReview'
import { areaLabel, severityLabel, suggestionTypeLabel } from '../utils/statusMap'

const date = ref(formatToday())
const forecastReview = ref([])
const dispatchReview = ref([])
const incidentEfficiency = ref(null)
const aiAdoption = ref(null)

const forecastChartRef = ref(null)
const adoptionChartRef = ref(null)
let forecastChart = null
let adoptionChart = null

function formatToday() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

async function loadAll() {
  const d = date.value
  try {
    const [fr, dr, ie, aa] = await Promise.all([
      listForecastReview(d),
      listDispatchReview(d),
      getIncidentEfficiency(d),
      getAiAdoption()
    ])
    forecastReview.value = fr.data || []
    dispatchReview.value = dr.data || []
    incidentEfficiency.value = ie.data
    aiAdoption.value = aa.data
  } catch (e) {
    // 错误提示由 request 拦截器统一处理
  }
  await nextTick()
  renderForecastChart()
  renderAdoptionChart()
}

function renderForecastChart() {
  if (!forecastChartRef.value) {
    if (forecastChart) { forecastChart.dispose(); forecastChart = null }
    return
  }
  if (!forecastChart) forecastChart = echarts.init(forecastChartRef.value)
  const rows = forecastReview.value
  forecastChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['预测订单', '实际订单'] },
    grid: { left: 50, right: 20, top: 40, bottom: 70 },
    xAxis: {
      type: 'category',
      data: rows.map((r) => `${areaLabel(r.area)} ${r.timeRange}`),
      axisLabel: { rotate: 30 }
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '预测订单', type: 'bar', data: rows.map((r) => r.predictedOrders) },
      { name: '实际订单', type: 'bar', data: rows.map((r) => r.actualOrders) }
    ]
  }, true)
}

function renderAdoptionChart() {
  if (!adoptionChartRef.value) {
    if (adoptionChart) { adoptionChart.dispose(); adoptionChart = null }
    return
  }
  if (!adoptionChart) adoptionChart = echarts.init(adoptionChartRef.value)
  const a = aiAdoption.value
  adoptionChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        data: [
          { name: '已执行', value: a?.executed ?? 0 },
          { name: '已拒绝', value: a?.rejected ?? 0 },
          { name: '待处理', value: a?.pending ?? 0 }
        ]
      }
    ]
  }, true)
}

function handleResize() {
  forecastChart?.resize()
  adoptionChart?.resize()
}

function fmtDev(v) {
  const n = Number(v)
  const sign = n > 0 ? '+' : ''
  return `${sign}${n.toFixed(2)}%`
}

function devClass(v) {
  const abs = Math.abs(Number(v))
  if (abs >= 30) return 'dev-high'
  if (abs >= 10) return 'dev-mid'
  return 'dev-low'
}

function fmtMinutes(v) {
  if (v == null) return '-'
  if (v < 60) return `${v} 分钟`
  const h = Math.floor(v / 60)
  const m = v % 60
  return m ? `${h} 时 ${m} 分` : `${h} 小时`
}

function suggestionStatusLabel(s) {
  return { PENDING: '待确认', EXECUTED: '已执行', REJECTED: '已忽略' }[s] || s
}

function suggestionStatusType(s) {
  return { PENDING: 'warning', EXECUTED: 'success', REJECTED: 'info' }[s] || 'info'
}

onMounted(() => {
  loadAll()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  forecastChart?.dispose()
  adoptionChart?.dispose()
})
</script>

<style scoped>
.review-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.filter-card .filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.chart-box {
  margin-bottom: 16px;
}
.chart {
  width: 100%;
  height: 320px;
}
.stat-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
}
.stat-item {
  flex: 1 1 150px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fafafa;
  text-align: center;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}
.stat-label {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}
.group-tables {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.group-title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}
.adoption-top {
  display: flex;
  gap: 24px;
  align-items: center;
  margin-bottom: 16px;
}
.adoption-chart {
  flex: 0 0 380px;
  margin-bottom: 0;
}
.adoption-side {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  flex: 1;
}
.dev-high {
  color: #f56c6c;
  font-weight: 600;
}
.dev-mid {
  color: #e6a23c;
}
.dev-low {
  color: #67c23a;
}
@media (max-width: 900px) {
  .group-tables {
    grid-template-columns: 1fr;
  }
  .adoption-top {
    flex-direction: column;
  }
  .adoption-chart {
    flex: 0 0 auto;
    width: 100%;
  }
}
</style>
