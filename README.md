# wechatter

#### 介绍
基于 wechatbot-webhook 的微信机器人，支持 GPT 问答、热搜、天气预报

#### 软件架构

基于Springboot3.3


#### 安装教程

1. 使用docker-compose模式启动wechatbot-webhook，修改wechatbot-webhook配置文件
    1. `ACCEPT_RECVD_MSG_MYSELF=true`
    2. `RECVD_MSG_API=http://[wechatter ip]:[wechatter port]/wechatter/receive_msg`
    3. `LOGIN_API_TOKEN=[your token]`
2. 修改参照[example.yml](src%2Fmain%2Fresources%2Fexample.yml) 创建并配置application.yml
3. 直接运行`wechatter`jar（需要java17环境）或者使用[docker-compose](docker-compose.yml)运行

#### 使用说明

1. 直接聊天会触发ChatGPT
2. 使用`/[cmd]`会触发命令
   - [x] 天气查询
   - [x] 微博热搜
   - [x] 随机数
   - [ ] b站热搜
   - [ ] TODO

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request
