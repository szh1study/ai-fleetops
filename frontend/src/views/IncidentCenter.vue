<template>
  <div class="incident-page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="filterSeverity" placeholder="严重程度" clearable style="width: 160px">
          <el-option v-for="s in ['LOW','MEDIUM','HIGH','CRITICAL']" :key="s" :label="severityLabel(s)" :value="s" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 200px">
          <el-option v-for="s in ['OPEN','ANALYZING','PENDING_APPROVAL','PROCESSING','RESOLVED','REJECTED']" :key="s" :label="incidentStatusLabel(s)" :value="s" />
        </el-select>
        <span class="count-text">共 {{ filteredIncidents.length }} 条</span>
      </div>

      <el-table :data="filteredIncidents" stripe border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="异常ID" width="80" />
        <el-table-column prop="vehicleId" label="车辆ID" width="100" />
        <el-table-column label="异常类型" min-width="160">
          <template #default="{ row }">{{ incidentTypeLabel(row.incidentType) }}</template>
        </el-table-column>
        <el-table-column label="严重程度" width="110">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severity)" size="small">{{ severityLabel(row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">{{ incidentDescription(row.incidentType, row.description) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="incidentStatusType(row.status)" size="small" effect="plain">{{ incidentStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="解决时间" width="170">
          <template #default="{ row }">{{ formatTime(row.resolvedAt) }}</template>
        </el-table-column>
        <el-table-column label="AI Copilot" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :loading="analyzingId === row.id"
              @click="handleAiAnalyze(row)"
            >
              AI 分析
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="aiDialogVisible" title="AI Incident Copilot" width="720px">
      <template v-if="aiResponse?.analysis">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="Incident ID">{{ aiResponse.incidentId }}</el-descriptions-item>
          <el-descriptions-item label="Provider">{{ aiResponse.provider || '-' }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag :type="severityType(aiResponse.analysis.riskLevel)">
              {{ severityLabel(aiResponse.analysis.riskLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="置信度">
            {{ formatConfidence(aiResponse.analysis.confidence) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="analysis-section">
          <div class="section-title">异常摘要</div>
          <div class="section-content">{{ aiResponse.analysis.summary || '-' }}</div>
        </div>

        <div class="analysis-section">
          <div class="section-title">可能原因</div>
          <ul v-if="aiResponse.analysis.possibleCauses?.length" class="analysis-list">
            <li v-for="(item, index) in aiResponse.analysis.possibleCauses" :key="`cause-${index}`">{{ item }}</li>
          </ul>
          <div v-else class="empty-text">暂无</div>
        </div>

        <div class="analysis-section">
          <div class="section-title">处置建议</div>
          <ul v-if="aiResponse.analysis.recommendations?.length" class="analysis-list">
            <li v-for="(item, index) in aiResponse.analysis.recommendations" :key="`recommendation-${index}`">{{ item }}</li>
          </ul>
          <div v-else class="empty-text">暂无</div>
        </div>

        <div class="analysis-section">
          <div class="section-title">建议动作</div>
          <div v-if="aiResponse.analysis.suggestedActions?.length" class="action-tags">
            <el-tag
              v-for="action in aiResponse.analysis.suggestedActions"
              :key="action"
              effect="plain"
            >
              {{ suggestedActionLabel(action) }}
            </el-tag>
          </div>
          <div v-else class="empty-text">暂无</div>
        </div>

        <el-alert
          title="当前版本仅提供 AI 建议，不会自动修改车辆、异常或工单状态。"
          type="info"
          :closable="false"
          show-icon
        />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { analyzeIncident, listIncidents } from '../api/incident'
import {
  incidentTypeLabel, severityLabel, severityType,
  incidentStatusLabel, incidentStatusType,
  incidentDescription, suggestedActionLabel
} from '../utils/statusMap'

const incidents = ref([])
const loading = ref(false)
const filterSeverity = ref('')
const filterStatus = ref('')
const analyzingId = ref(null)
const aiDialogVisible = ref(false)
const aiResponse = ref(null)

const filteredIncidents = computed(() => {
  return incidents.value.filter(i => {
    const matchSev = !filterSeverity.value || i.severity === filterSeverity.value
    const matchStatus = !filterStatus.value || i.status === filterStatus.value
    return matchSev && matchStatus
  })
})

function formatTime(t) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

function formatConfidence(value) {
  if (value === null || value === undefined) return '-'
  return `${Math.round(Number(value) * 100)}%`
}

async function handleAiAnalyze(row) {
  analyzingId.value = row.id
  try {
    const res = await analyzeIncident(row.id)
    const data = res.data

    if (!data?.aiAvailable || !data.analysis) {
      ElMessage.warning(data?.message || 'AI Incident Copilot 当前不可用')
      return
    }

    aiResponse.value = data
    aiDialogVisible.value = true
  } finally {
    analyzingId.value = null
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await listIncidents()
    incidents.value = res.data || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.incident-page { width: 100%; }
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
.analysis-section {
  margin-top: 18px;
}
.section-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.section-content,
.analysis-list {
  line-height: 1.7;
  color: #606266;
}
.analysis-list {
  margin: 0;
  padding-left: 22px;
}
.action-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 18px;
}
.empty-text {
  color: #909399;
}
</style>
