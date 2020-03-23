import { Component, Prop, Vue, Emit, Watch } from "vue-property-decorator";

@Component
export default class Header extends Vue {

    /*
        如何给子组件传值，在 vue 里面是通过 prop 解决的
        首先：
            子组件定义接收数据的变量，如下
        然后：
            在父组件内通过 :childMsg='xxx' 把数据传递进来    
    */
    @Prop({
        type: String,
        required: false,
        default: String
    })
    public childMsg!: string

    public searchInput: string = ''

    public username: string = ''

    public mounted() {
        this.$nextTick(() => {
            let user = localStorage.getItem('user')
            this.username = user || 'error'
        })
    }

    @Watch("childMsg")
    public updateUserData(oldVal: any, newVal: any) {
        console.log("侦测到 Header 里面的 childMsg 发生变化了，old：" + oldVal + " new：" + newVal)
        let user = localStorage.getItem('user')
        this.username = user || 'error'
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

        this.username = 'error' || 'error'
        // 通知赋组件，更改 fatherVar 的值，fatherVar值一改变其他的依赖组件会立刻变化
        this.propMsg()

        this.$router.push({ name: 'login' })
    }

    public msg: string = 'logout';
    @Emit('bindSend') send(msg: string) { };
    public propMsg() {
        this.send(this.msg)
    }
}