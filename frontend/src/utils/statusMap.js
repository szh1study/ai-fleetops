// 统一状态映射工具：英文枚举 -> 中文展示 + Element Plus Tag 类型
// 注意：仅用于前端展示，不修改后端返回的原始英文值

const VEHICLE_STATUS_LABEL = {
  READY: '可运营',
  OPERATING: '运营中',
  IDLE: '空闲',
  CHARGING: '充电中',
  TO_CHARGE: '待充电',
  RETURNING: '返场中',
  MAINTENANCE: '维修中',
  OFFLINE: '离线'
}

const VEHICLE_STATUS_TYPE = {
  READY: 'success',
  OPERATING: 'primary',
  IDLE: 'info',
  CHARGING: 'warning',
  TO_CHARGE: 'warning',
  RETURNING: 'primary',
  MAINTENANCE: 'danger',
  OFFLINE: 'info'
}

const ORDER_STATUS_LABEL = {
  NONE: '无订单',
  ASSIGNED: '已派单',
  PICKING_UP: '接驾中',
  SERVING: '服务中'
}

const ORDER_STATUS_TYPE = {
  NONE: 'info',
  ASSIGNED: 'primary',
  PICKING_UP: 'primary',
  SERVING: 'warning'
}

const DEVICE_STATUS_LABEL = {
  NORMAL: '正常',
  WARNING: '警告',
  OFFLINE: '离线',
  ERROR: '故障'
}

const DEVICE_STATUS_TYPE = {
  NORMAL: 'success',
  WARNING: 'warning',
  OFFLINE: 'info',
  ERROR: 'danger'
}

const INCIDENT_TYPE_LABEL = {
  LOW_SOC: '低电量',
  CAMERA_OFFLINE: '摄像头离线',
  LIDAR_OFFLINE: '激光雷达离线',
  NETWORK_ERROR: '网络异常',
  SOFTWARE_ERROR: '软件异常',
  SUPPLY_SHORTAGE: '运力不足'
}

const SEVERITY_LABEL = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高',
  CRITICAL: '严重'
}

const SEVERITY_TYPE = {
  LOW: 'info',
  MEDIUM: 'primary',
  HIGH: 'warning',
  CRITICAL: 'danger'
}

const INCIDENT_STATUS_LABEL = {
  OPEN: '待处理',
  ANALYZING: '分析中',
  PENDING_APPROVAL: '待审批',
  PROCESSING: '处理中',
  RESOLVED: '已解决',
  REJECTED: '已拒绝'
}

const INCIDENT_STATUS_TYPE = {
  OPEN: 'danger',
  ANALYZING: 'warning',
  PENDING_APPROVAL: 'warning',
  PROCESSING: 'primary',
  RESOLVED: 'success',
  REJECTED: 'info'
}


const SUGGESTION_TYPE_LABEL = {
  CREATE_WORK_ORDER: '创建工单',
  UPDATE_INCIDENT_STATUS: '更新异常状态',
  UPDATE_VEHICLE_STATUS: '更新车辆状态'
}

const WORK_ORDER_STATUS_LABEL = {
  PENDING: '待处理',
  PROCESSING: '处理中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

const WORK_ORDER_STATUS_TYPE = {
  PENDING: 'warning',
  PROCESSING: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'info'
}

const WORK_ORDER_TYPE_LABEL = {
  CAMERA_INSPECTION: '摄像头检修',
  LIDAR_INSPECTION: '激光雷达检修',
  CHARGING: '充电',
  SOFTWARE_CHECK: '软件检查',
  GENERAL_INSPECTION: '常规检查'
}

// 运营区域名称中英映射（仅前端展示，不改数据库值）
const AREA_LABEL = {
  'Airport': '机场',
  'CBD': '中心商务区',
  'Railway Station': '火车站',
  'University Town': '大学城',
  'Residential Area': '居住区',
  'Charging Station': '充电站'
}

// 三类已知异常的中文说明（优先于数据库原始 description 展示）
const INCIDENT_DESCRIPTION = {
  LOW_SOC: '车辆电量低于建议运营阈值，建议暂停接收新订单并尽快前往充电站补能。',
  CAMERA_OFFLINE: '车辆前置摄像头离线，关键感知设备状态异常，完成检修前不建议继续运营。',
  SOFTWARE_ERROR: '车辆软件状态异常，建议保持维修状态并完成人工检查后再恢复运营。'
}

function pick(map, key) {
  return (key && map[key]) ? map[key] : key
}

export const vehicleStatusLabel = (s) => pick(VEHICLE_STATUS_LABEL, s)
export const vehicleStatusType = (s) => pick(VEHICLE_STATUS_TYPE, s) || 'info'

export const orderStatusLabel = (s) => pick(ORDER_STATUS_LABEL, s)
export const orderStatusType = (s) => pick(ORDER_STATUS_TYPE, s) || 'info'

export const deviceStatusLabel = (s) => pick(DEVICE_STATUS_LABEL, s)
export const deviceStatusType = (s) => pick(DEVICE_STATUS_TYPE, s) || 'info'

export const incidentTypeLabel = (s) => pick(INCIDENT_TYPE_LABEL, s)

export const severityLabel = (s) => pick(SEVERITY_LABEL, s)
export const severityType = (s) => pick(SEVERITY_TYPE, s) || 'info'

export const incidentStatusLabel = (s) => pick(INCIDENT_STATUS_LABEL, s)
export const incidentStatusType = (s) => pick(INCIDENT_STATUS_TYPE, s) || 'info'

export const suggestionTypeLabel = (s) => pick(SUGGESTION_TYPE_LABEL, s)

export const workOrderStatusLabel = (s) => pick(WORK_ORDER_STATUS_LABEL, s)
export const workOrderStatusType = (s) => pick(WORK_ORDER_STATUS_TYPE, s) || 'info'

export const workOrderTypeLabel = (s) => pick(WORK_ORDER_TYPE_LABEL, s)

// 运营区域：未命中映射时原样返回
export const areaLabel = (s) => pick(AREA_LABEL, s)

// 异常描述：已知类型返回中文说明，未知类型回退到数据库原始 description
export const incidentDescription = (type, fallback) => {
  if (type && INCIDENT_DESCRIPTION[type]) return INCIDENT_DESCRIPTION[type]
  return fallback || '-'
}
