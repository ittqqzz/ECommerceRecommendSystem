<template>
  <div class="container">
    <h1 title="实时推荐">猜你喜欢</h1>
    <span v-if="stream.length == 0">
      <p>你当前的账号是首次使用，且无评论操作，暂无实时推荐数据可查询</p>
      <p>你需要使用一段时间后才能获得实时推荐数据</p>
      <p>你也可以使用系统默认账号访问，以体验实时推荐数据</p>
    </span>
    <span v-if="stream.length != 0">
      <a @click="doMore(1)" class="more" href="#" v-if="MAX_SHOW_NUM1 == 5">查看更多</a>
      <a @click="undoMore(1)" class="more" href="#" v-if="MAX_SHOW_NUM1 == 19">收起更多</a>
      <el-card
        v-for="item in stream.slice(0, MAX_SHOW_NUM1)"
        :key="item.productId + 1"
        class="card"
      >
        <router-link :to="{path: '/detail', query: {productId: item.productId} }" class="a-name">
          <h5 class="name">{{item.name}}</h5>
        </router-link>
        <img :src="item.imageUrl" alt="商品图片" class="image" />
        <el-rate
          v-model="item.score"
          :colors="colors"
          :allow-half="true"
          @change="doRate(item.score, item.productId)"
        ></el-rate>
      </el-card>
    </span>

    <h1 title="离线推荐">看了又看</h1>
    <span v-if="offline.length == 0">
      <p>你当前的账号是首次使用，暂无离线个性化离线推荐数据可查询</p>
      <p>你需要使用一段时间后才能获得个性化离线推荐数据</p>
      <p>你也可以使用系统默认账号访问，以体验个性化离线推荐数据</p>
    </span>
    <span v-if="offline.length != 0">
      <a @click="doMore(2)" class="more" href="#" v-if="MAX_SHOW_NUM2 == 5">查看更多</a>
      <a @click="undoMore(2)" class="more" href="#" v-if="MAX_SHOW_NUM2 == 19">收起更多</a>
      <el-card
        v-for="item in offline.slice(0, MAX_SHOW_NUM2)"
        :key="item.productId - 1"
        class="card"
      >
        <router-link :to="{path: '/detail', query: {productId: item.productId} }" class="a-name">
          <h5 class="name">{{item.name}}</h5>
        </router-link>
        <img :src="item.imageUrl" alt="商品图片" class="image" />
        <el-rate
          v-model="item.score"
          :colors="colors"
          :allow-half="true"
          @change="doRate(item.score, item.productId)"
        ></el-rate>
      </el-card>
    </span>

    <h1 title="热门商品推荐">大家都在看</h1>
    <a @click="doMore(3)" class="more" href="#" v-if="MAX_SHOW_NUM3 == 5">查看更多</a>
    <a @click="undoMore(3)" class="more" href="#" v-if="MAX_SHOW_NUM3 == 19">收起更多</a>
    <el-card v-for="item in hot.slice(0, MAX_SHOW_NUM3)" :key="item.productId * 10" class="card">
      <router-link :to="{path: '/detail', query: {productId: item.productId} }" class="a-name">
        <h5 class="name">{{item.name}}</h5>
      </router-link>
      <img :src="item.imageUrl" alt="商品图片" class="image" />
      <el-rate
        v-model="item.score"
        :colors="colors"
        :allow-half="true"
        @change="doRate(item.score, item.productId)"
      ></el-rate>
    </el-card>

    <h1 title="好评商品推荐">大家都在买</h1>
    <a @click="doMore(4)" class="more" href="#" v-if="MAX_SHOW_NUM4 == 5">查看更多</a>
    <a @click="undoMore(4)" class="more" href="#" v-if="MAX_SHOW_NUM4 == 19">收起更多</a>
    <el-card v-for="item in rate.slice(0, MAX_SHOW_NUM4)" :key="item.productId / 10" class="card">
      <router-link :to="{path: '/detail', query: {productId: item.productId} }" class="a-name">
        <h5 class="name">{{item.name}}</h5>
      </router-link>
      <img :src="item.imageUrl" alt="商品图片" class="image" />
      <el-rate
        v-model="item.score"
        :colors="colors"
        :allow-half="true"
        @change="doRate(item.score, item.productId)"
      ></el-rate>
    </el-card>
  </div>
</template>

<script src="./Home.ts" lang="ts" />

<style scoped lang="stylus">
@import url('./Home.stylus');
</style>