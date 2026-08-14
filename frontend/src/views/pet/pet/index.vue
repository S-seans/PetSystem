<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="宠物名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入宠物名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="品种" prop="breed">
        <el-input
          v-model="queryParams.breed"
          placeholder="请输入品种"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="年龄" prop="ageYear">
        <el-input
          v-model="queryParams.ageYear"
          placeholder="请输入年龄(年)"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="queryParams.gender" placeholder="请选择性别" clearable>
          <el-option label="公" value="1" />
          <el-option label="母" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="体重kg" prop="weight">
        <el-input
          v-model="queryParams.weight"
          placeholder="请输入体重kg"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="救助日期" prop="rescueDate">
        <el-date-picker clearable
          v-model="queryParams.rescueDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择救助日期">
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
          v-hasPermi="['pet:pet:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['pet:pet:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['pet:pet:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['pet:pet:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="petList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="宠物ID" align="center" prop="petId" />
      <el-table-column label="宠物名称" align="center" prop="name" />
      <el-table-column label="品种" align="center" prop="breed" />
      <el-table-column label="年龄" align="center" prop="age">
        <template #default="scope">
          <span>{{ formatPetAge(scope.row.age) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="性别" align="center" prop="gender">
        <template #default="scope">
          <span>{{ formatGender(scope.row.gender) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="体重kg" align="center" prop="weight" />
      <el-table-column label="状态" align="center" prop="status" >
      <template #default="scope">
        <el-tag
            :type="getStatusTagType(scope.row.status)"
            effect="light"
        >
          {{ scope.row.status }}
        </el-tag>
      </template>
      </el-table-column>
      <el-table-column label="照片" align="center" prop="imageUrl" width="100">
        <template #default="scope">
          <image-preview :src="scope.row.imageUrl" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="状况描述" align="center" prop="description" />
      <el-table-column label="救助日期" align="center" prop="rescueDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.rescueDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pet:pet:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pet:pet:remove']">删除</el-button>
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

    <!-- 添加或修改宠物信息对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="petRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入宠物名称" />
        </el-form-item>
        <el-form-item label="品种" prop="breed">
          <el-input v-model="form.breed" placeholder="请输入品种" />
        </el-form-item>
        <el-form-item label="年龄(年)" prop="ageYear">
          <el-input v-model="form.ageYear" placeholder="请输入年龄(年)" />
        </el-form-item>
        <el-form-item label="年龄(月)" prop="ageMonth">
          <el-input v-model="form.ageMonth" placeholder="请输入年龄(月)" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别">
            <el-option label="公" :value="PET_GENDER.MALE" />
            <el-option label="母" :value="PET_GENDER.FEMALE" />
          </el-select>
        </el-form-item>
        <el-form-item label="体重kg" prop="weight">
          <el-input v-model="form.weight" placeholder="请输入体重kg" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态":disabled="!!form.petId">
            <el-option :label="PET_STATUS.AVAILABLE" :value="PET_STATUS.AVAILABLE" />
            <el-option :label="PET_STATUS.ADOPTED" :value="PET_STATUS.ADOPTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="照片" prop="imageUrl">
          <image-upload v-model="form.imageUrl" :action="'/pet/pet/upload'" :file-size="2" />
        </el-form-item>
        <el-form-item label="状况描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="救助日期" prop="rescueDate">
          <el-date-picker clearable
            v-model="form.rescueDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择救助日期">
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

<script setup name="Pet">
import { listPet, getPet, delPet, addPet, updatePet } from "@/api/pet/pet"
import { formatPetAge, calcPetAgeMonths, splitPetAge } from "@/utils/petAge"
import { PET_STATUS, PET_GENDER, genderText } from "@/utils/business"

const { proxy } = getCurrentInstance()

const petList = ref([])
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
    name: null,
    breed: null,
    ageYear: null,
    gender: null,
    weight: null,
    status: null,
    imageUrl: null,
    description: null,
    rescueDate: null,
  },
  rules: {
    name: [
      { required: true, message: "宠物名称不能为空", trigger: "blur" }
    ],
    weight: [
      { pattern: /^\d+(\.\d{1,2})?$/, message: "体重必须为数字，且最多保留两位小数", trigger: "blur" }
    ],
    status: [
      { required: true, message: "状态不能为空", trigger: "change" }
    ],
    ageYear: [
      { validator: (rule, value, callback) => {
        const y = form.value.ageYear
        const m = form.value.ageMonth
        const hasY = y !== null && y !== undefined && y !== ''
        const hasM = m !== null && m !== undefined && m !== ''
        if (!hasY && !hasM) {
          callback(new Error("请填写年龄，年/月至少填一项"))
        } else {
          callback()
        }
      }, trigger: "blur" },
      { pattern: /^\d+$/, message: "年龄(年)必须为整数", trigger: "blur" },
      { validator: (rule, value, callback) => {
        if (value !== null && value !== undefined && value !== '' && (Number(value) < 0 || Number(value) > 100)) {
          callback(new Error("年龄(年)需在 0~100 之间"))
        } else {
          callback()
        }
      }, trigger: "blur" }
    ],
    ageMonth: [
      { pattern: /^\d+$/, message: "年龄(月)必须为整数", trigger: "blur" },
      { validator: (rule, value, callback) => {
        if (value !== null && value !== undefined && value !== '' && Number(value) > 11) {
          callback(new Error("年龄(月)需在 0~11 之间"))
        } else {
          callback()
        }
      }, trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 构建查询参数：年龄(年) 转换为月份区间 */
function buildQueryParams() {
  const params = { ...queryParams.value }
  const raw = queryParams.value.ageYear
  const hasY = raw !== null && raw !== undefined && raw !== ''
  if (hasY && Number.isFinite(Number(raw)) && Number(raw) >= 0) {
    const y = Number(raw)
    params.ageMin = y * 12
    params.ageMax = y * 12 + 11
  } else {
    params.ageMin = null
    params.ageMax = null
  }
  delete params.ageYear
  return params
}

/** 查询宠物信息列表 */
function getList() {
  loading.value = true
  listPet(buildQueryParams()).then(response => {
    petList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 格式化性别显示 */
function formatGender(gender) {
  return genderText(gender)
}

/** 获取状态标签类型 */
function getStatusTagType(status) {
  if (status === PET_STATUS.ADOPTED) {
    return 'success'
  } else if (status === PET_STATUS.AVAILABLE) {
    return 'primary'
  } else {
    return 'info'
  }
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    petId: null,
    name: null,
    breed: null,
    age: null,
    ageYear: null,
    ageMonth: null,
    gender: null,
    weight: null,
    status: PET_STATUS.AVAILABLE,
    imageUrl: null,
    description: null,
    rescueDate: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  }
  proxy.resetForm("petRef")
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
  ids.value = selection.map(item => item.petId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加宠物信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _petId = row.petId || ids.value
  getPet(_petId).then(response => {
    form.value = response.data
    const { years, months } = splitPetAge(form.value.age)
    form.value.ageYear = years
    form.value.ageMonth = months
    open.value = true
    title.value = "修改宠物信息"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["petRef"].validate(valid => {
    if (valid) {
      form.value.age = calcPetAgeMonths(form.value.ageYear, form.value.ageMonth)
      if (form.value.petId != null) {
        updatePet(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPet(form.value).then(response => {
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
  const _petIds = row.petId || ids.value
  proxy.$modal.confirm('是否确认删除宠物信息编号为"' + _petIds + '"的数据项？').then(function() {
    return delPet(_petIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('pet/pet/export', {
    ...buildQueryParams()
  }, `pet_${new Date().getTime()}.xlsx`)
}

getList()
</script>
