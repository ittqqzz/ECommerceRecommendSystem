<template>
  <div class="container">
    <h1>实时推荐</h1>
    <a @click="doMore(1)" class="more" href="#" v-if="MAX_SHOW_NUM1 == 5">查看更多</a>
    <a @click="undoMore(1)" class="more" href="#" v-if="MAX_SHOW_NUM1 == 19">收起更多</a>
    <el-card v-for="item in stream.slice(0, MAX_SHOW_NUM1)" :key="item.productId + 1" class="card">
      <router-link :to="{path: '/detail', query: {productId: item.productId} }" class="a-name">
        <h5 class="name">{{item.name}}</h5>
      </router-link>
      <img :src="item.imageUrl" alt="商品图片" class="image" />
      <el-rate v-model="item.score" :colors="colors" :allow-half="true" @change="doRate(item.score, item.productId)"></el-rate>
    </el-card>

    <h1>离线推荐</h1>
    <a @click="doMore(2)" class="more" href="#" v-if="MAX_SHOW_NUM2 == 5">查看更多</a>
    <a @click="undoMore(2)" class="more" href="#" v-if="MAX_SHOW_NUM2 == 19">收起更多</a>
    <el-card v-for="item in offline.slice(0, MAX_SHOW_NUM2)" :key="item.productId - 1" class="card">
      <router-link :to="{path: '/detail', query: {productId: item.productId} }" class="a-name">
        <h5 class="name">{{item.name}}</h5>
      </router-link>
      <img :src="item.imageUrl" alt="商品图片" class="image" />
      <el-rate v-model="item.score" :colors="colors" :allow-half="true" @change="doRate(item.score, item.productId)"></el-rate>
    </el-card>

    <h1>热门推荐</h1>
    <a @click="doMore(3)" class="more" href="#" v-if="MAX_SHOW_NUM3 == 5">查看更多</a>
    <a @click="undoMore(3)" class="more" href="#" v-if="MAX_SHOW_NUM3 == 19">收起更多</a>
    <el-card v-for="item in hot.slice(0, MAX_SHOW_NUM3)" :key="item.productId * 10" class="card">
      <router-link :to="{path: '/detail', query: {productId: item.productId} }" class="a-name">
        <h5 class="name">{{item.name}}</h5>
      </router-link>
      <img :src="item.imageUrl" alt="商品图片" class="image" />
      <el-rate v-model="item.score" :colors="colors" :allow-half="true" @change="doRate(item.score, item.productId)"></el-rate>
    </el-card>

    <h1>评分最多</h1>
    <a @click="doMore(4)" class="more" href="#" v-if="MAX_SHOW_NUM4 == 5">查看更多</a>
    <a @click="undoMore(4)" class="more" href="#" v-if="MAX_SHOW_NUM4 == 19">收起更多</a>
    <el-card v-for="item in rate.slice(0, MAX_SHOW_NUM4)" :key="item.productId / 10" class="card">
      <router-link :to="{path: '/detail', query: {productId: item.productId} }" class="a-name">
        <h5 class="name">{{item.name}}</h5>
      </router-link>
      <img :src="item.imageUrl" alt="商品图片" class="image" />
      <el-rate v-model="item.score" :colors="colors" :allow-half="true" @change="doRate(item.score, item.productId)"></el-rate>
    </el-card>
  </div>
</template>

<script src="./Home.ts" lang="ts" />

<style scoped lang="stylus">
@import url('./Home.stylus');
</style>