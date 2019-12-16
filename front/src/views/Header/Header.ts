import { Component, Prop, Vue } from "vue-property-decorator";

@Component
export default class Header extends Vue {

    public searchInput: string = ''

    public username: string = ''

    public mounted() {
        let user = localStorage.getItem('user')
        this.username = user || '获取用户名失败'
        console.log(this.username)
    }

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

    public goHome() {
        this.$router.push({ name: 'home' })
    }

    public showUserInfo() {
        console.log('123456')
    }

    public async doLogout() {
        localStorage.removeItem('user')
        await this.$alert('退出成功', '提示', {
            confirmButtonText: '确定'
        });
        this.$router.push({ name: 'login' })
    }
}