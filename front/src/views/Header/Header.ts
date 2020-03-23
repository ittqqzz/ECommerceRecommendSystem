import { Component, Prop, Vue, Emit } from "vue-property-decorator";

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

    public updated() {
        console.log('head组件中 childMsg ：' + this.childMsg)
        if (this.childMsg == 'updateUserData') {
            let user = localStorage.getItem('user')
            this.username = user || 'error'
        }
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
        
        this.propMsg()

        this.$router.push({ name: 'login' })
    }

    public msg: string = 'logout';
    @Emit('bindSend') send(msg: string) { }; 
    public propMsg() {
        this.send(this.msg)
    }
}