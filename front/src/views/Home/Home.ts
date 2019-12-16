import { Component, Prop, Vue } from "vue-property-decorator";

@Component
export default class Home extends Vue {
    // 实时推荐 
    // stream：StreamRecs
    public stream: any = []
    // 离线推荐
    // hot：RateMoreRecentlyProducts
    public hot: any = []
    // rate：RateMoreProducts
    public rate: any = []
    // offine：UserRecs
    public offline: any = []
    // 页面上商品最大展示数量
    public MAX_SHOW_NUM1: number = 5
    public MAX_SHOW_NUM2: number = 5
    public MAX_SHOW_NUM3: number = 5
    public MAX_SHOW_NUM4: number = 5

    public colors: any = ['#99A9BF', '#F7BA2A', '#FF9900']

    public created() {
        this.getRecommendData('/business/rest/product/stream', 0)
        this.getRecommendData('/business/rest/product/hot', 1)
        this.getRecommendData('/business/rest/product/rate', 2)
        this.getRecommendData('/business/rest/product/offline', 3)
    }

    public getRecommendData(url: string, index: number) {
        let name = localStorage.getItem('user')
        this.axios.get(url, {
            params: {
                username: name,
                num: this.MAX_SHOW_NUM1
            }
        }).then(
            (res) => {
                if (res.data.success) {
                    switch (index) {
                        case 0:
                            this.stream = res.data.products
                            break
                        case 1:
                            this.hot = res.data.products
                            break
                        case 2:
                            this.rate = res.data.products
                            break
                        case 3:
                            this.offline = res.data.products
                            break
                    }
                }
            }
        ).catch(
            (err) => {
                console.log('请求: ' + url + ' 的途中发生错误 ' + err)
            }
        )

    }

    public doMore(index: number) {
        switch (index) {
            case 1:
                this.MAX_SHOW_NUM1 = 19
                break
            case 2:
                this.MAX_SHOW_NUM2 = 19
                break
            case 3:
                this.MAX_SHOW_NUM3 = 19
                break
            case 4:
                this.MAX_SHOW_NUM4 = 19
                break
        }
    }

    public undoMore(index: number) {
        switch (index) {
            case 1:
                this.MAX_SHOW_NUM1 = 5
                break
            case 2:
                this.MAX_SHOW_NUM2 = 5
                break
            case 3:
                this.MAX_SHOW_NUM3 = 5
                break
            case 4:
                this.MAX_SHOW_NUM4 = 5
                break
        }
    }

    public async doRate(rate: number, productId: number) {
        // ?score=8&username=abc
        console.log('收到评分数据,productId: ' + productId + " rate: " + rate)
        let user = localStorage.getItem('user')
        let res = await this.axios.get('/business/rest/product/rate/' + productId, {
            params: {
                score: rate,
                username: user
            }
        })
        if (res.data.success == true) {
            this.$alert('评分成功', '提示', {
                confirmButtonText: '确定'
            });
        } else {
            this.$alert('评分失败', '提示', {
                confirmButtonText: '确定'
            });
        }
    }
}