<template>
  <div id="app">
    <Header></Header>
    <keep-alive>
      <router-view />
    </keep-alive>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import { Header } from '@/views'

@Component({
  components: {
    Header
  }
})
export default class App extends Vue {

}
</script>

<style lang="stylus">
body {
  margin: 0px 0px 0px 0px;
}
</style>
