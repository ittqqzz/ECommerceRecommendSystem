<template>
  <div class="header">
    <div class="site" v-on:click="goHome" title="回到首页">
      <img src="@/assets/logo.png" class="site-img" />
      <div class="title">Amazon ERS</div>
    </div>
    <div class="search-container">
      <div class="search">
        <el-input type="text" v-model="searchInput" :clearable="true" placeholder="搜索商品" />
      </div>
      <div class="search-button">
        <el-button type="info" plain @click="doSearch">搜索</el-button>
      </div>
    </div>
    <div class="user" v-on:click="showUserInfo" v-if="username != 'error'">
      <el-popover placement="bottom" width="200" trigger="hover">
        <img src="@/assets/user.png" class="site-img" slot="reference" />
        <p>用户名：{{username}}</p>
        <p>系统状态：在线</p>
        <el-button v-on:click="doLogout">退出登陆</el-button>
      </el-popover>
    </div>
  </div>
</template>

<script src="./Header.ts" lang="ts" />

<style scoped lang="stylus">
@import url('./Header.stylus');
</style>
