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
      </template>

      <el-alert
        v-else-if="historyMode"
        title="以下为该异常的历史建议，可直接确认执行或忽略。"
        type="info"
        :closable="false"
        show-icon
      />

      <div class="analysis-section">
        <div class="section-title-row">
          <div class="section-title">建议操作</div>
          <el-button
            v-if="historyMode"
            size="small"
            link
            type="primary"
            :loading="reanalyzing"
            @click="reanalyze"
          >
            重新分析
          </el-button>
        </div>
        <div v-if="suggestions.length" class="action-cards">
          <div
            v-for="(action, index) in suggestions"
            :key="action.suggestionId || `${action.type}-${index}`"
            class="action-card"
          >
            <div class="action-card-head">
              <span class="action-card-title">{{ actionTitle(action) }}</span>
              <el-tag :type="actionStateType(action)" size="small" effect="plain">{{ actionStateLabel(action) }}</el-tag>
            </div>
            <div class="action-card-reason">理由：{{ action.reason || '-' }}</div>
            <div v-if="action.type === 'CREATE_WORK_ORDER'" class="action-card-payload">
              工单描述：{{ action.payload?.description || '-' }}
            </div>
            <div class="action-card-btns">
              <el-button
                size="small"
                type="primary"
                :disabled="isHandled(action)"
                @click="openConfirm(action)"
              >
                确认执行
              </el-button>
              <el-button size="small" :disabled="isHandled(action)" @click="handleReject(action)">
                忽略
              </el-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-text">暂无</div>
      </div>

      <el-alert
        title="AI 建议需人工确认后才会执行，确认前系统不会修改任何数据。"
        type="info"
        :closable="false"
        show-icon
      />
    </el-dialog>

    <el-dialog v-model="confirmDialogVisible" title="确认执行 AI 建议" width="520px" append-to-body>
      <template v-if="pendingAction">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="动作">{{ actionTitle(pendingAction) }}</el-descriptions-item>
          <el-descriptions-item label="理由">{{ pendingAction.reason || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="pendingAction.type === 'CREATE_WORK_ORDER'" class="confirm-field">
          <div class="confirm-label">工单描述</div>
          <el-input v-model="editableDescription" type="textarea" :rows="3" placeholder="工单描述" />
        </div>
        <div v-else class="confirm-field">
          <div class="confirm-label">目标状态</div>
          <div class="confirm-value">{{ targetStatusLabel(pendingAction) }}</div>
        </div>

        <div class="confirm-field">
          <div class="confirm-label">操作人（可选）</div>
          <el-input v-model="confirmedBy" placeholder="留空表示不记录" />
        </div>
      </template>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="confirming" @click="submitConfirm">确认执行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { analyzeIncident, listIncidents } from '../api/incident'
import { confirmSuggestion, rejectSuggestion, listSuggestions } from '../api/suggestion'
import {
  incidentTypeLabel, severityLabel, severityType,
  incidentStatusLabel, incidentStatusType,
  incidentDescription,
  suggestionTypeLabel, workOrderTypeLabel, vehicleStatusLabel
} from '../utils/statusMap'

const incidents = ref([])
const loading = ref(false)
const filterSeverity = ref('')
const filterStatus = ref('')
const analyzingId = ref(null)
const reanalyzing = ref(false)
const aiDialogVisible = ref(false)
const aiResponse = ref(null)
const suggestions = ref([])
const historyMode = ref(false)
const currentIncident = ref(null)

const confirmDialogVisible = ref(false)
const pendingAction = ref(null)
const confirmedBy = ref('')
const editableDescription = ref('')
const confirming = ref(false)

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

// 统一两种数据来源的卡片结构：新分析返回的 AiSuggestedAction 与历史 AiSuggestion。
function normalizeSuggestion(s) {
  return {
    suggestionId: s.suggestionId ?? s.id,
    type: s.type ?? s.suggestionType,
    payload: s.payload || {},
    reason: s.reason || '',
    status: s.status || 'PENDING'
  }
}

function actionTitle(action) {
  if (action.type === 'CREATE_WORK_ORDER') {
    return `创建工单：${workOrderTypeLabel(action.payload?.workOrderType)}`
  }
  if (action.type === 'UPDATE_INCIDENT_STATUS') {
    return `更新异常状态：${incidentStatusLabel(action.payload?.status)}`
  }
  if (action.type === 'UPDATE_VEHICLE_STATUS') {
    return `更新车辆状态：${vehicleStatusLabel(action.payload?.status)}`
  }
  return suggestionTypeLabel(action.type)
}

function targetStatusLabel(action) {
  if (action.type === 'UPDATE_INCIDENT_STATUS') {
    return incidentStatusLabel(action.payload?.status)
  }
  if (action.type === 'UPDATE_VEHICLE_STATUS') {
    return vehicleStatusLabel(action.payload?.status)
  }
  return '-'
}

function actionStateLabel(action) {
  const map = { PENDING: '待确认', EXECUTED: '已执行', REJECTED: '已忽略' }
  return map[action.status] || action.status || '待确认'
}

function actionStateType(action) {
  const map = { PENDING: 'warning', EXECUTED: 'success', REJECTED: 'info' }
  return map[action.status] || 'info'
}

function isHandled(action) {
  return ['EXECUTED', 'REJECTED'].includes(action.status)
}

function openConfirm(action) {
  pendingAction.value = action
  confirmedBy.value = ''
  editableDescription.value = action.payload?.description || ''
  confirmDialogVisible.value = true
}

async function submitConfirm() {
  if (!pendingAction.value?.suggestionId) return
  confirming.value = true
  try {
    await confirmSuggestion(
      pendingAction.value.suggestionId,
      confirmedBy.value.trim() || null,
      editableDescription.value
    )
    pendingAction.value.status = 'EXECUTED'
    confirmDialogVisible.value = false
    ElMessage.success(`已执行：${actionTitle(pendingAction.value)}`)
    await loadIncidents()
  } catch (e) {
    // 错误提示由 request 拦截器统一处理
  } finally {
    confirming.value = false
  }
}

async function handleReject(action) {
  if (!action.suggestionId) return
  try {
    await rejectSuggestion(action.suggestionId)
    action.status = 'REJECTED'
    ElMessage.success('已忽略该建议')
  } catch (e) {
    // 错误提示由 request 拦截器统一处理
  }
}

async function handleAiAnalyze(row) {
  currentIncident.value = row
  analyzingId.value = row.id
  try {
    await openSuggestions(row)
  } catch (e) {
    // 错误提示由 request 拦截器统一处理
  } finally {
    analyzingId.value = null
  }
}

// 先查历史：有 PENDING 就直接展示（以服务端状态为准，不再调模型），否则重新分析。
async function openSuggestions(row) {
  const historyRes = await listSuggestions(row.id)
  const history = historyRes.data || []
  if (history.some(s => s.status === 'PENDING')) {
    showHistory(history)
    return
  }
  await runAnalysis(row.id)
}

function showHistory(history) {
  aiResponse.value = null
  suggestions.value = history.map(normalizeSuggestion)
  historyMode.value = true
  aiDialogVisible.value = true
}

async function runAnalysis(incidentId) {
  const res = await analyzeIncident(incidentId)
  const data = res.data
  if (!data?.aiAvailable || !data.analysis) {
    ElMessage.warning(data?.message || 'AI Incident Copilot 当前不可用')
    return
  }
  aiResponse.value = data
  suggestions.value = (data.analysis.suggestedActions || []).map(normalizeSuggestion)
  historyMode.value = false
  aiDialogVisible.value = true
}

async function reanalyze() {
  if (!currentIncident.value) return
  reanalyzing.value = true
  try {
    await runAnalysis(currentIncident.value.id)
  } catch (e) {
    // 错误提示由 request 拦截器统一处理
  } finally {
    reanalyzing.value = false
  }
}

async function loadIncidents() {
  loading.value = true
  try {
    const res = await listIncidents()
    incidents.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(loadIncidents)
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
.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.section-title-row .section-title {
  margin-bottom: 0;
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
.action-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.action-card {
  padding: 12px 14px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fafafa;
}
.action-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.action-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.action-card-reason {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
.action-card-payload {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}
.action-card-btns {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}
.confirm-field {
  margin-top: 16px;
}
.confirm-label {
  margin-bottom: 6px;
  font-size: 13px;
  color: #606266;
}
.confirm-value {
  font-size: 14px;
  color: #303133;
}
.empty-text {
  color: #909399;
}
</style>
