import { Component, Prop, Vue } from "vue-property-decorator";

@Component
export default class Login extends Vue {

  public isRegisterStatus: boolean = false

  public form: any = {
    'username': '',
    'password': ''
  }

  public async created() {
    // 检测浏览器是否缓存了用户登录信息，没有就登陆，否则就进入首页
    // dubug 的时候手动打开开发者工具的 Applicatuion 清空 localStorage
    let user = localStorage.getItem('user')
    if (user == null) {
      console.log('没有找到已登陆用户')
    } else {
      console.log('发现已登陆用户：' + user)
      // 直接前往首页
      this.$router.push({name: 'home'})
    }
  }

  public async doLogin() {
    console.dir(this.form)
    // 发送登录请求
    let res = await this.axios.get('/rest/users/login', {
      params: {
        username: this.form.username,
        password: this.form.password
      }
    })
    if (res.data.success) {
      await this.$alert('登录成功', '提示', {
        confirmButtonText: '确定'
      });
  
      // 将登陆信息缓存在浏览器里面
      localStorage.setItem('user', this.form.username)
  
      // 前往首页
      this.$router.push({name: 'home'})

    } else {
      this.$alert('登陆失败', '提示', {
        confirmButtonText: '确定'
      });
      this.form = {}
    }   
  }

  public async doRegister() {
    console.dir(this.form)
    // 向后端发送注册请求
    let res = await this.axios.get('/rest/users/register', {
      params: {
        username: this.form.username,
        password: this.form.password
      }
    })
    if (res.data.success) {
      await this.$alert('注册成功', '提示', {
        confirmButtonText: '确定'
      });
      // 成功后跳回到上一级登录页面
      this.goRegister()
    } else {
      this.$alert('注册失败', '提示', {
        confirmButtonText: '确定'
      });
    }    
  }

  public goRegister(): void {
    this.form = {
      'username': '',
      'password': ''
    }
    this.isRegisterStatus = !this.isRegisterStatus
  }
}