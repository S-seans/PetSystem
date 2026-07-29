<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="宠物ID" prop="petId">
        <el-input
            v-model="queryParams.petId"
            placeholder="请输入宠物ID"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="申请人ID" prop="userId" v-hasRole="['administrator']">
        <el-input
            v-model="queryParams.userId"
            placeholder="请输入申请人ID"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="审核时间" prop="reviewTime">
        <el-date-picker clearable
                        v-model="queryParams.reviewTime"
                        type="date"
                        value-format="YYYY-MM-DD"
                        placeholder="请选择审核时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="审核人" prop="reviewBy">
        <el-input
            v-model="queryParams.reviewBy"
            placeholder="请输入审核人"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['adoption:adoption:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['adoption:adoption:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['adoption:adoption:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['adoption:adoption:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="adoptionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="申请ID" align="center" prop="requestId" />
      <el-table-column label="宠物ID" align="center" prop="petId" />
      <el-table-column label="宠物名称" align="center" prop="petName" />
      <el-table-column label="申请人ID" align="center" prop="userId"  v-hasRole="['administrator']"/>
      <el-table-column label="领养理由" align="center" prop="reason" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <span :style="getStatusStyle(scope.row.status)">{{ getStatusText(scope.row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核备注" align="center" prop="reviewRemark" />
      <el-table-column label="审核时间" align="center" prop="reviewTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.reviewTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核人" align="center" prop="reviewBy" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['adoption:adoption:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['adoption:adoption:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
    />

    <!-- 添加或修改领养申请对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="adoptionRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="宠物ID" prop="petId">
          <el-input v-model="form.petId" placeholder="请输入宠物ID" :disabled="!!form.requestId"
                    @blur="checkPetAvailable" />
          <div v-if="petCheckResult" :class="['pet-check-result', petCheckResult.type]">
            {{ petCheckResult.message }}
          </div>
        </el-form-item>
        <el-form-item label="申请人ID" prop="userId" v-hasRole="['administrator']">
          <el-input v-model="form.userId" placeholder="请输入申请人ID" />
        </el-form-item>
        <el-form-item label="领养理由" prop="reason">
          <el-input v-model="form.reason" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态"  v-hasRole="['administrator']">
            <el-option label="待审核" value="pending" />
            <el-option label="通过" value="pass" />
            <el-option label="拒绝" value="out" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核备注" prop="reviewRemark">
          <el-input v-model="form.reviewRemark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="审核时间" prop="reviewTime">
          <el-date-picker clearable
                          v-model="form.reviewTime"
                          type="date"
                          value-format="YYYY-MM-DD"
                          placeholder="请选择审核时间">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="审核人" prop="reviewBy">
          <el-input v-model="form.reviewBy" placeholder="请输入审核人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Adoption">
import { listAdoption, getAdoption, delAdoption, addAdoption, updateAdoption } from "@/api/adoption/adoption"
import { getPet } from "@/api/pet/pet"
import { parseTime} from "@/utils/ruoyi.js";
import useUserStore from "@/store/modules/user.js";

const userStore = useUserStore()

const { proxy } = getCurrentInstance()
proxy.parseTime = parseTime
const adoptionList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const isAdmin = ref(false) // 是否是管理员
const petCheckResult = ref(null)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    petId: null,
    userId: null,
    reason: null,
    status: null,
    reviewRemark: null,
    reviewTime: null,
    reviewBy: null,
  },
  rules: {
    petId: [
      { required: true, message: "宠物ID不能为空", trigger: "blur" },
      { pattern: /^\d+$/, message: "宠物ID必须为数字", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

// 检查用户角色
function checkUserRole() {
  // 这里根据你的实际角色检查逻辑来实现
  const roles = userStore.roles || []
  isAdmin.value = roles.includes('admin') || roles.includes('administrator')
}

/** 查询领养申请列表 */
function getList() {
  loading.value = true
  if (!isAdmin.value) {
    queryParams.value.userId = null
  }
  listAdoption(queryParams.value).then(response => {
    adoptionList.value = response.rows
    total.value = response.total
    loading.value = false

    // 为每个申请获取宠物名称（如果后端没有返回）
    adoptionList.value.forEach(item => {
      if (item.petId && !item.petName) {
        getPetName(item.petId).then(petName => {
          item.petName = petName
        })
      }
    })
  }).catch(() => {
    loading.value = false
  })
}


/** 检查宠物是否可申请 */
function checkPetAvailable() {

  if (form.value.requestId) {
    petCheckResult.value = null
    return
  }

  if (!form.value.petId) {
    petCheckResult.value = null
    return
  }

  // 验证宠物ID格式
  if (!/^\d+$/.test(form.value.petId)) {
    petCheckResult.value = {
      type: 'error',
      message: '宠物ID必须为数字'
    }
    return
  }

  // 这里可以调用API检查宠物状态，但由于没有专门接口，我们会在提交时检查
  petCheckResult.value = {
    type: 'info',
    message: '将在提交时验证宠物状态'
  }
}

/** 获取宠物名称 */
async function getPetName(petId) {
  try {
    const response = await getPet(petId)
    if (response.data && response.data.name) {
      return response.data.name
    }
    return '未知'
  } catch (error) {
    console.error('获取宠物名称失败:', error)
    return '未知'
  }
}

/** 获取状态显示文本 */
function getStatusText(status) {
  const statusMap = {
    'pending': '待审核',
    'pass': '通过',
    'out': '拒绝'
  }
  return statusMap[status] || status
}

/** 获取状态显示样式 */
function getStatusStyle(status) {
  if (status === 'pass') {
    return { color: '#67C23A', fontWeight: 'bold' }
  } else if (status === 'out') {
    return { color: '#F56C6C', fontWeight: 'bold' }
  }
  return {}
}

/** 检查是否可以编辑 */
function canEdit(row) {
  // 管理员可以编辑所有，普通用户只能编辑自己的且状态为待审核的申请
  if (isAdmin.value) return true
  return row.status === 'pending'
}

/** 检查是否可以删除 */
function canDelete(row) {
  // 管理员可以删除所有，普通用户只能删除自己的且状态为待审核的申请
  if (isAdmin.value) return true
  return row.status === 'pending'
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    requestId: null,
    petId: null,
    userId: null,
    reason: null,
    status: null,
    reviewRemark: null,
    reviewTime: null,
    reviewBy: null,
    createTime: null
  }
  petCheckResult.value = null
  proxy.resetForm("adoptionRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.requestId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加领养申请"
  if (!isAdmin.value) {
    form.value.status = "pending"
  }
}

/** 修改按钮操作 */
async function handleUpdate(row) {
  reset()
  const _requestId = row.requestId || ids.value[0]
  try {
    const response = await getAdoption(_requestId)
    form.value = response.data

    // 获取宠物名称
    if (form.value.petId) {
      form.value.petName = await getPetName(form.value.petId)
    }

    // 自动设置审核时间和审核人（仅管理员且状态发生变化时）
    if (isAdmin.value) {
      // 设置审核时间为当前时间
      const now = new Date()
      form.value.reviewTime = proxy.parseTime(now, '{y}-{m}-{d} {h}:{i}:{s}')

      // 设置审核人为当前管理员昵称
      form.value.reviewBy = userStore.name || userStore.nickName || '管理员'
    }

    open.value = true
    title.value = "修改领养申请"
  } catch (error) {
    proxy.$modal.msgError("无权查看此申请记录")
  }
}

/** 格式化日期时间 */
function formatDateTime(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["adoptionRef"].validate(valid => {
    if (valid) {
      if (form.value.requestId != null) {
        updateAdoption(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        }).catch(error => {
          proxy.$modal.msgError(error.message || "修改失败")
        })
      } else {
        addAdoption(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功，将在24小时内完成审核")
          open.value = false
          getList()
        }).catch(error => {
          proxy.$modal.msgError(error.message || "新增失败")
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _requestIds = row.requestId || ids.value
  proxy.$modal.confirm('是否确认删除领养申请编号为"' + _requestIds + '"的数据项？').then(function() {
    return delAdoption(_requestIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch((error) => {
    proxy.$modal.msgError(error.message || "删除失败")
  })
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('adoption/adoption/export', {
    ...queryParams.value
  }, `adoption_${new Date().getTime()}.xlsx`)
}

// 初始化
onMounted(() => {
  checkUserRole()
  getList()
})
</script>

<style scoped>
.pet-check-result {
  margin-top: 5px;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.pet-check-result.info {
  background-color: #f4f4f5;
  color: #909399;
}

.pet-check-result.success {
  background-color: #f0f9eb;
  color: #67c23a;
}

.pet-check-result.error {
  background-color: #fef0f0;
  color: #f56c6c;
}

.pet-check-result.warning {
  background-color: #fdf6ec;
  color: #e6a23c;
}
</style>