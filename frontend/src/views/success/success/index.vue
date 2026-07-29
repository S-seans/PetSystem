<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="申请ID" prop="requestId">
        <el-input
          v-model="queryParams.requestId"
          placeholder="请输入申请ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="宠物ID" prop="petId">
        <el-input
          v-model="queryParams.petId"
          placeholder="请输入宠物ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="领养人ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入领养人ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="领养日期" prop="adoptTime">
        <el-date-picker clearable
          v-model="queryParams.adoptTime"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择领养日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="回访日期" prop="followUpDate">
        <el-date-picker clearable
          v-model="queryParams.followUpDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择回访日期">
        </el-date-picker>
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
          v-hasPermi="['success:success:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['success:success:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['success:success:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['success:success:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="successList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="记录ID" align="center" prop="successId" />
      <el-table-column label="申请ID" align="center" prop="requestId" />
      <el-table-column label="宠物ID" align="center" prop="petId" />
      <el-table-column label="领养人ID" align="center" prop="userId" />
      <el-table-column label="领养日期" align="center" prop="adoptTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.adoptTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="回访日期" align="center" prop="followUpDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.followUpDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['success:success:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['success:success:remove']">删除</el-button>
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

    <!-- 添加或修改领养成功记录对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="successRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="申请ID" prop="requestId">
          <el-input v-model="form.requestId" placeholder="请输入申请ID" />
        </el-form-item>
        <el-form-item label="宠物ID" prop="petId">
          <el-input v-model="form.petId" placeholder="请输入宠物ID" />
        </el-form-item>
        <el-form-item label="领养人ID" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入领养人ID" />
        </el-form-item>
        <el-form-item label="领养日期" prop="adoptTime">
          <el-date-picker clearable
            v-model="form.adoptTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择领养日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="回访日期" prop="followUpDate">
          <el-date-picker clearable
            v-model="form.followUpDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择回访日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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

<script setup name="Success">
import { listSuccess, getSuccess, delSuccess, addSuccess, updateSuccess } from "@/api/success/success"

const { proxy } = getCurrentInstance()

const successList = ref([])
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
    requestId: null,
    petId: null,
    userId: null,
    adoptTime: null,
    followUpDate: null,
    status: null,
  },
  rules: {
    requestId: [
      { required: true, message: "申请ID不能为空", trigger: "blur" }
    ],
    petId: [
      { required: true, message: "宠物ID不能为空", trigger: "blur" }
    ],
    userId: [
      { required: true, message: "领养人ID不能为空", trigger: "blur" }
    ],
    adoptTime: [
      { required: true, message: "领养日期不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询领养成功记录列表 */
function getList() {
  loading.value = true
  listSuccess(queryParams.value).then(response => {
    successList.value = response.rows
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
    successId: null,
    requestId: null,
    petId: null,
    userId: null,
    adoptTime: null,
    followUpDate: null,
    status: null,
    remark: null,
    createTime: null
  }
  proxy.resetForm("successRef")
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
  ids.value = selection.map(item => item.successId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加领养成功记录"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _successId = row.successId || ids.value
  getSuccess(_successId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改领养成功记录"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["successRef"].validate(valid => {
    if (valid) {
      if (form.value.successId != null) {
        updateSuccess(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addSuccess(form.value).then(response => {
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
  const _successIds = row.successId || ids.value
  proxy.$modal.confirm('是否确认删除领养成功记录编号为"' + _successIds + '"的数据项？').then(function() {
    return delSuccess(_successIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('success/success/export', {
    ...queryParams.value
  }, `success_${new Date().getTime()}.xlsx`)
}

getList()
</script>
