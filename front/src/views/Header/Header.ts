import { Component, Prop, Vue } from "vue-property-decorator";

@Component
export default class Header extends Vue {

    public searchInput: string = ''

    public doSearch() {
        // 跳转到搜索页面
        this.$router.push(
            {
                path: '/search',
                query: {
                    searchInput: this.searchInput
                }
            }
        )
    }
}