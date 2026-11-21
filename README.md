# S-Pay 演示商城

一个可以在本机从选购走到订单结算的 Spring MVC 练习。商品沿用原项目中的机械键盘，单价 1.68 元。应用提供本地模拟支付，金额由服务端商品目录确定。微信登录和支付宝商户收款尚未接入。

## 运行

需要 JDK 17 和 Maven 3.8+。在仓库根目录执行：

```sh
mvn clean verify
java -jar s-pay-mall-web/target/s-pay-mall-app.jar
```

打开 <http://127.0.0.1:8092/>，创建订单，选择「模拟付款」，刷新页面仍能看到「已付款」。再创建一单并选择「取消订单」。H2 将订单写到启动目录的 `data/shop.mv.db`。浏览器会话用于隔离订单。正常关闭服务时，会话保存到 `data/sessions/`，同一浏览器可在会话有效期内继续查看当前订单。

## 接口

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | `/api/product` | 查询商品、编号和定价 |
| POST | `/api/orders` | JSON 传入 `productId` 和 UUID 格式的 `requestId` |
| GET | `/api/orders/{id}` | 查询当前会话的订单 |
| POST | `/api/orders/{id}/demo-pay` | 将待付款订单标为已付款 |
| POST | `/api/orders/{id}/cancel` | 取消待付款订单 |

同一会话重复提交相同请求编号会返回同一订单。付款或取消可安全重试，终态冲突返回 HTTP 409，跨会话查询返回 404。使用 curl 调用时需保存并传回 JSESSIONID。

## 代码入口

`web` 处理 HTTP 和浏览器会话，`service` 决定下单和状态转换规则，`dao` 用带条件的 SQL 更新状态，`domain` 定义商品和订单。商品页面位于 `docs/dev-ops/nginx/html/index.html`，Maven 将它打包进 JAR。旧版 Nginx 登录页、支付宝表单和 MySQL 运维示例保留作历史参考。构建测试使用独立的内存数据库。
