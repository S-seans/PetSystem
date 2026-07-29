<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <!-- 左侧系统信息 -->
      <el-col :sm="24" :lg="12" style="padding-left: 20px">
        <h2>动物救助站宠物领养管理框架</h2>
        <p>
          救助宠物，关爱动物，从我做起，让爱心改变它们的生活。
        </p>
        <p>
          <b>当前版本:</b> <span>v{{ version }}</span>
        </p>
        <el-button type="primary" size="medium" @click="goTarget('/adopt')">进入系统</el-button>
      </el-col>

      <!-- 右侧技术架构 -->
      <el-col :sm="24" :lg="12" style="padding-left: 50px">
        <el-row>
          <el-col :span="12">
            <h2>技术选型</h2>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <h4>后端技术</h4>
            <ul>
              <li>SpringBoot</li>
              <li>Spring Security</li>
              <li>JWT</li>
              <li>MyBatis</li>
              <li>Druid</li>
              <li>Fastjson</li>
            </ul>
          </el-col>
          <el-col :span="12">
            <h4>前端技术</h4>
            <ul>
              <li>Vue 3</li>
              <li>Vuex</li>
              <li>Element Plus</li>
              <li>Axios</li>
              <li>Sass</li>
              <li>Vue Router</li>
            </ul>
          </el-col>
        </el-row>
      </el-col>
    </el-row>

    <el-divider />

    <!-- 通知公告 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>📢 通知公告</span>
          </template>
          <div class="notice-content">
            <p><el-tag size="small" type="success">最新</el-tag> 欢迎爱心人士参加！</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 页脚 -->
    <el-divider />
    <div class="footer">
      <p>&copy; 2024 动物救助站管理系统 | 联系方式：400-123-4567</p>
    </div>
  </div>
</template>

<script setup name="Index">
import { ref } from 'vue'

const version = ref('3.9.0')

const stats = ref({
  waiting: 23,
  adopted: 156,
  volunteers: 45,
  todayVisits: 128
})

function goTarget(url) {
  window.open(url, '_blank')
}
</script>

<style scoped lang="scss">
.home {
  padding: 20px;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 14px;
  color: #676a6c;

  h2 {
    margin-top: 10px;
    font-size: 26px;
    font-weight: 400;
  }

  h4 {
    margin: 15px 0 10px;
    color: #333;
  }

  ul {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      padding: 5px 0;
      color: #666;
    }
  }

  .stat-item {
    text-align: center;

    .stat-number {
      font-size: 32px;
      font-weight: bold;
      color: #409EFF;
    }

    .stat-label {
      margin-top: 8px;
      font-size: 14px;
      color: #999;
    }
  }

  .notice-content {
    p {
      margin: 0;
      line-height: 1.8;
    }
  }

  .footer {
    text-align: center;
    padding: 20px 0;
    font-size: 13px;
    color: #999;
  }
}
</style>