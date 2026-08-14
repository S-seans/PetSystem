<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="宠物名称" prop="petName">
        <el-input
            v-model="queryParams.petName"
            placeholder="请输入宠物名称"
            clearable
            @keyup.enter="handleQuery"
            :maxlength="50"
            show-word-limit
        />
      </el-form-item>
      <el-form-item label="记录日期" prop="recordDate">
        <el-date-picker clearable
          v-model="queryParams.recordDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择记录日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="疫苗名称" prop="vaccineName">
        <el-input
          v-model="queryParams.vaccineName"
          placeholder="请输入疫苗名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否绝育" prop="isSterilized">
        <el-select v-model="queryParams.isSterilized" placeholder="请选择" clearable>
          <el-option label="是" value="1" />
          <el-option label="否" value="0" />
        </el-select>
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
          v-hasPermi="['health:health:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['health:health:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['health:health:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['health:health:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="healthList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="宠物ID" align="center" prop="petId" />
      <el-table-column label="宠物名称" align="center" prop="petName" />
      <el-table-column label="记录日期" align="center" prop="recordDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.recordDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="疫苗名称" align="center" prop="vaccineName" />
      <el-table-column label="是否绝育" align="center" prop="isSterilized">
        <template #default="scope">
          <span>{{ scope.row.isSterilized === 1 ? '是' : (scope.row.isSterilized === 0 ? '否' : '未知') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="健康状态" align="center" prop="healthStatus">
        <template #default="scope">
          <span>{{ getHealthStatusText(scope.row.healthStatus) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="详细描述" align="center" prop="description" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['health:health:edit']">修改</el-button>
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

    <!-- 添加或修改宠物健康记录对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="healthRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker clearable
            v-model="form.recordDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择记录日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="疫苗名称" prop="vaccineName">
          <el-input v-model="form.vaccineName" placeholder="请输入疫苗名称" />
        </el-form-item>
        <el-form-item label="是否绝育" prop="isSterilized">
          <el-select v-model="form.isSterilized" placeholder="请选择">
            <el-option label="是" value="1" />
            <el-option label="否" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="healthStatus">
          <el-select v-model="form.healthStatus" placeholder="请选择健康状态">
            <el-option label="健康" value="HEALTHY" />
            <el-option label="死亡" value="DEAD" />
            <el-option label="良好" value="GOOD" />
            <el-option label="生病" value="SICK" />
            <el-option label="康复中" value="RECOVERING" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
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

<script setup name="Health">
import { listHealth, getHealth, delHealth, addHealth, updateHealth } from "@/api/health/health"
import { healthStatusText } from "@/utils/business"

const { proxy } = getCurrentInstance()

const healthList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    petName: null,
    recordDate: null,
    vaccineName: null,
    isSterilized: null,
    healthStatus: null,
    description: null,
  },
  rules: {
    recordDate: [
      { required: true, message: "记录日期不能为空", trigger: "blur" }
    ],
    vaccineName: [
      { max: 100, message: "疫苗名称长度不能超过100个字符", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询宠物健康记录列表 */
function getList() {
  loading.value = true
  listHealth(queryParams.value).then(response => {
    healthList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    healthId: null,
    petId: null,
    recordDate: null,
    vaccineName: null,
    isSterilized: null,
    healthStatus: null,
    description: null,
    createBy: null,
    createTime: null
  }
  proxy.resetForm("healthRef")
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
  ids.value = selection.map(item => item.healthId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加宠物健康记录"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _healthId = row.healthId || ids.value
  getHealth(_healthId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改宠物健康记录"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["healthRef"].validate(valid => {
    if (valid) {
      if (form.value.healthId != null) {
        updateHealth(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addHealth(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _healthIds = row.healthId || ids.value
  proxy.$modal.confirm('是否确认删除宠物健康记录编号为"' + _healthIds + '"的数据项？').then(function() {
    return delHealth(_healthIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('health/health/export', {
    ...queryParams.value
  }, `health_${new Date().getTime()}.xlsx`)
}

/** 健康状态映射 */
function getHealthStatusText(status) {
  return healthStatusText(status)
}

getList()
</script>
<style scoped>
:deep(.el-select__input-wrapper.is-hidden) {
  position: static;
}
</style>
