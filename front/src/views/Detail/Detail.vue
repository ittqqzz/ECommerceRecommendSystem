<template>
  <div class="container">
    <h1>商品详情</h1>
    <div class="info">
      <el-card>
        <img :src="info.imageUrl" />
        <h3>{{info.name}}</h3>
        <el-rate v-model="info.score" :colors="colors" :allow-half="true" @change="doRate(info.score, info.productId)"></el-rate>
        <p>平均评分：{{info.score | numFilter}}</p>
        <p>商品类别：{{info.categories}}</p>
        <p>商品标签：{{info.tags}}</p>
      </el-card>
    </div>

    <h1 title="基于物品的相似推荐(itemCF)">看过该商品的人还看了</h1>
    <div>
      <el-card v-for="item in itemcf" :key="item.productId * 10" class="card">
        <router-link :to="{path: '/detail', query: {productId: item.productId} }" class="a-name">
          <h5 class="name">{{item.name}}</h5>
        </router-link>
        <img :src="item.imageUrl" alt="商品图片" class="image" />
        <el-rate v-model="item.score" :colors="colors" :allow-half="true" @change="doRate(item.score, item.productId)"></el-rate>
      </el-card>
    </div>

    <h1 title="基于内容的相似推荐(contentCF)">喜欢该商品的人也喜欢</h1>
    <div>
      <el-card v-for="item in contentbased" :key="item.productId / 10" class="card">
        <router-link :to="{path: '/detail', query: {productId: item.productId} }" class="a-name">
          <h5 class="name">{{item.name}}</h5>
        </router-link>
        <img :src="item.imageUrl" alt="商品图片" class="image" />
        <el-rate v-model="item.score" :colors="colors" :allow-half="true" @change="doRate(item.score, item.productId)"></el-rate>
      </el-card>
    </div>
  </div>
</template>

<script src="./Detail.ts" lang="ts" />

<style scoped lang="stylus">
@import url('./Detail.stylus');
</style>
