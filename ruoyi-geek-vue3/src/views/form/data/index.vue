<template>
  <div class="app-container">
    <el-card shadow="never" body-class="search-card">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
        <el-form-item label="关联的表单ID" prop="formId">
          <el-input v-model="queryParams.formId" placeholder="请输入关联的表单ID" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="表单版本" prop="formVersion">
          <el-input v-model="queryParams.formVersion" placeholder="请输入表单版本" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="表单名称" prop="formName">
          <el-input v-model="queryParams.formName" placeholder="请输入表单名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="用户账号" prop="userName">
          <el-input v-model="queryParams.userName" placeholder="请输入用户账号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="mt10">
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['form:data:add']">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate"
            v-hasPermi="['form:data:edit']">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete"
            v-hasPermi="['form:data:remove']">删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="Download" @click="handleExport"
            v-hasPermi="['form:data:export']">导出</el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="表单版本" align="center" prop="formVersion" />
        <el-table-column label="数据状态" align="center" prop="status" />
        <el-table-column label="备注" align="center" prop="remark" />
        <el-table-column label="表单名称" align="center" prop="formName" />
        <el-table-column label="用户账号" align="center" prop="createBy" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
              v-hasPermi="['form:data:edit']">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
              v-hasPermi="['form:data:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 添加或修改单数据对话框 -->
    <el-dialog :title="title" v-model="open" width="80%" :max-width="'900px'" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="选择表单" prop="formId" v-if="!form.dataId">
          <el-select v-model="form.formId" placeholder="请选择表单" @change="onFormChange">
            <el-option
              v-for="template in templateList"
              :key="template.formId"
              :label="template.formName"
              :value="template.formId"
            >
              <div>
                <span>{{ template.formName }}</span>
                <span class="el-tag el-tag--success el-tag--small ml-2">{{ template.formVersion }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="表单版本" prop="formVersion">
          <el-input v-model="form.formVersion" placeholder="请输入表单版本" readonly />
        </el-form-item>
        <el-form-item label="数据状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择数据状态">
            <el-option label="草稿" value="draft" />
            <el-option label="已提交" value="submitted" />
            <el-option label="已审批" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="表单内容" v-if="form.formId" class="form-content-wrapper">
          <div class="form-render-container">
            <v-form-render ref="vFormRef" :form-json="formSchema" :key="form.formId" />
          </div>
        </el-form-item>
        <el-form-item v-else>
          <div class="text-center text-gray-400 py-8">请先选择表单</div>
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

<script setup name="Data">
import { listData, getData, delData, addData, updateData } from "@/api/form/data";
import { listTemplate } from "@/api/form/template";
import { ref, reactive, onMounted } from "vue";

const { proxy } = getCurrentInstance();

// 表单模板列表
const templateList = ref([]);

// 获取表单模板列表
function getTemplateList() {
  listTemplate({}).then(response => {
    templateList.value = response.rows;
  });
}

// 组件挂载时获取表单模板列表
onMounted(() => {
  getTemplateList();
});

const dataList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    formId: null,
    formVersion: null,
    dataContent: null,
    status: null,
    formName: null,
    userName: null
  },
  rules: {
    formId: [
      { required: true, message: "关联的表单ID不能为空", trigger: "blur" }
    ],
    formVersion: [
      { required: true, message: "表单版本不能为空", trigger: "blur" }
    ],
    dataContent: [
      { required: true, message: "表单数据内容不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "数据状态不能为空", trigger: "change" }
    ],
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询单数据列表 */
function getList() {
  loading.value = true;
  listData(queryParams.value).then(response => {
    dataList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    dataId: null,
    formId: null,
    formVersion: null,
    dataContent: null,
    status: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null,
    delFlag: null
  };
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.dataId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加单数据";
}
const vFormRef = ref();
const formRef = ref();
const formSchema = ref({});
/** 表单选择变化处理 */
function onFormChange(formId) {
  // 获取选中表单的详细信息
  const selectedTemplate = templateList.value.find(t => t.formId === formId);
  if (selectedTemplate) {
    form.value.formVersion = selectedTemplate.formVersion;
    formSchema.value = JSON.parse(selectedTemplate.formSchema || "{}");
  }
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _dataId = row.dataId || ids.value
  getData(_dataId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改单数据";
    // 设置表单模板信息
    const selectedTemplate = templateList.value.find(t => t.formId === form.value.formId);
    if (selectedTemplate) {
      formSchema.value = JSON.parse(selectedTemplate.formSchema || "{}");
    } else if (form.value.formSchema) {
      formSchema.value = JSON.parse(form.value.formSchema);
    }
    nextTick(() => {
      if (vFormRef.value && form.value.dataContent) {
        vFormRef.value.setFormData(JSON.parse(form.value.dataContent));
      }
    })
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
          // 确保表单数据内容存在
          if (!form.value.dataId) {
            // 新增操作 - dataId本就应该是null
            if (vFormRef.value) {
              vFormRef.value.getFormData().then(formData => {
                form.value.dataContent = JSON.stringify(formData);
                addData(form.value).then(response => {
                  proxy.$modal.msgSuccess("新增成功");
                  open.value = false;
                  getList();
                });
              });
            } else {
              // 如果没有vFormRef但有表单ID，使用空对象作为默认数据
              if (form.value.formId) {
                form.value.dataContent = JSON.stringify({});
                addData(form.value).then(response => {
                  proxy.$modal.msgSuccess("新增成功");
                  open.value = false;
                  getList();
                });
              } else {
                proxy.$modal.msgError("请先选择表单");
              }
            }
          } else {
            // 修改操作
            if (vFormRef.value) {
              vFormRef.value.getFormData().then(formData => {
                form.value.dataContent = JSON.stringify(formData);
                updateData(form.value).then(response => {
                  proxy.$modal.msgSuccess("修改成功");
                  open.value = false;
                  getList();
                });
              });
            } else {
              updateData(form.value).then(response => {
                proxy.$modal.msgSuccess("修改成功");
                open.value = false;
                getList();
              });
            }
          }
        }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _dataIds = row.dataId || ids.value;
  proxy.$modal.confirm('是否确认删除单数据编号为"' + _dataIds + '"的数据项？').then(function () {
    return delData(_dataIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => { });
}



/** 导出按钮操作 */
function handleExport() {
  proxy.download('form/data/export', {
    ...queryParams.value
  }, `data_${new Date().getTime()}.xlsx`)
}

getList();
</script>

<style scoped>
/* 表单渲染容器样式优化 */
.form-content-wrapper {
  margin-bottom: 20px;
}

.form-render-container {
  width: 100%;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

/* 调整v-form-render组件样式 */
:deep(.v-form-render) {
  width: 100%;
  background-color: white;
  padding: 25px;
  border-radius: 4px;
}

/* 增加表单项目之间的垂直间距 */
:deep(.v-form-render .el-form-item) {
  margin-bottom: 20px;
}

/* 增加标签与输入框之间的间距 */
:deep(.v-form-render .el-form-item__label) {
  margin-right: 15px;
}

/* 优化表单内输入框等元素的样式 */
:deep(.v-form-render .el-input__wrapper) {
  min-width: 250px;
}

:deep(.v-form-render .el-select) {
  min-width: 250px;
  margin-right: 10px;
}

:deep(.v-form-render .el-input) {
  min-width: 250px;
  margin-right: 10px;
}

:deep(.v-form-render .el-textarea) {
  min-width: 250px;
}

/* 增加复选框、单选框组内的间距 */
:deep(.v-form-render .el-checkbox-group) {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

:deep(.v-form-render .el-radio-group) {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

:deep(.v-form-render .el-checkbox) {
  margin-right: 10px;
}

:deep(.v-form-render .el-radio) {
  margin-right: 10px;
}

/* 确保弹窗内容区域有适当的内边距 */
:deep(.el-dialog__body) {
  padding: 30px;
  overflow-y: auto;
}

/* 调整主要表单的项目间距 */
:deep(.el-form .el-form-item) {
  margin-bottom: 20px;
}
</style>
