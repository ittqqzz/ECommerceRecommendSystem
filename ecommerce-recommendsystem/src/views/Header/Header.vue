<template>
  <div class="header">
    <img src="@/assets/logo.png" />
    <div class="title">电商推荐系统</div>
    <div class="search-container">
      <div class="search">
        <el-input type="text" v-model="searchInput" :clearable="true" placeholder="搜索商品" />
      </div>
      <div class="search-button">
        <el-button type="info" plain @click="doSearch">搜索</el-button>
      </div>
    </div>
  </div>
</template>

<script src="./Header.ts" lang="ts" />

<style scoped lang="stylus">
@import url('./Header.stylus');
</style>
