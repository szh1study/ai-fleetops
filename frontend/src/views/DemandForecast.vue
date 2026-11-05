<template>
  <div class="forecast-page">
    <el-card shadow="never">
      <template #header><span class="card-title">预测条件</span></template>
      <el-form inline>
        <el-form-item label="日期">
          <el-date-picker
            v-model="form.targetDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="form.timeRange" style="width: 160px">
            <el-option v-for="t in TIME_RANGES" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="区域">
          <el-select v-model="form.areas" multiple style="width: 320px" placeholder="选择区域">
            <el-option v-for="a in AREAS" :key="a" :label="areaLabel(a)" :value="a" />
          </el-select>
        </el-form-item>
        <el-form-item label="外部因素">
          <el-select
            v-model="form.externalFactors"
            multiple
            filterable
            allow-create
            default-first-option
            style="width: 260px"
            placeholder="如：演唱会、下雨"
          >
            <el-option v-for="f in FACTOR_PRESETS" :key="f" :label="f" :value="f" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="generating" @click="handleGenerate">生成预测</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="result-card">
      <template #header><span class="card-title">预测结果</span></template>
      <div v-if="forecasts.length" class="forecast-grid">
        <div v-for="f in forecasts" :key="f.id ?? f.area" class="forecast-card">
          <div class="forecast-head">
            <span class="forecast-area">{{ areaLabel(f.area) }}</span>
            <el-tag size="small" type="primary">{{ f.predictedOrders }} 单</el-tag>
          </div>
          <div class="forecast-meta">
            当前可调度：{{ currentIdle(f.area) }} 辆
            <template v-if="f.weatherSummary"> · 因素：{{ f.weatherSummary }}</template>
          </div>
          <div class="forecast-reason">{{ f.predictionReason || '-' }}</div>
        </div>
      </div>
      <el-empty v-else description="填写条件后点击「生成预测」" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { generateForecast } from '../api/demandForecast'
import { areaLabel } from '../utils/statusMap'

const TIME_RANGES = ['08:00-12:00', '12:00-17:00', '17:00-21:00', '20:00-24:00']
const AREAS = ['海淀区', '朝阳区', '西城区']
const FACTOR_PRESETS = ['演唱会', '展会', '体育赛事', '下雨', '降雪', '节假日', '工作日']

const form = ref({
  targetDate: formatToday(),
  timeRange: '20:00-24:00',
  areas: ['海淀区'],
  externalFactors: []
})
const generating = ref(false)
const response = ref(null)

const forecasts = computed(() => response.value?.forecasts || [])

function formatToday() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

function currentIdle(area) {
  return response.value?.currentIdleVehicles?.[area] ?? 0
}

async function handleGenerate() {
  if (!form.value.areas.length) {
    ElMessage.warning('请至少选择一个区域')
    return
  }
  generating.value = true
  try {
    const res = await generateForecast({
      targetDate: form.value.targetDate,
      timeRange: form.value.timeRange,
      areas: form.value.areas,
      externalFactors: form.value.externalFactors
    })
    response.value = res.data
    ElMessage.success('预测完成')
  } catch (e) {
    // 错误提示由 request 拦截器统一处理
  } finally {
    generating.value = false
  }
}
</script>

<style scoped>
.forecast-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.result-card {
  min-height: 200px;
}
.forecast-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}
.forecast-card {
  padding: 14px 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fafafa;
}
.forecast-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.forecast-area {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.forecast-meta {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}
.forecast-reason {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
</style>
