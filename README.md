# 一個簡易的 AI聊天 Line機器人
# 使用LineBotAPI 串接 ChatGPT API




# 使用方式:
* 註冊OpenAI帳號
因為是付費服務須購買使用量
取得 *OpenAI token


* 註冊 LINE Developers帳號
並建立一個Bot
Basic settings 頁面 取得 *Channel secret
Messaging API settings頁面 取得 *Channel access token
Messaging API settings頁面 設定 Webhook URL
因 LineAPI 只接受https 本地執行可使用 ngrok
Ex: https://*********/callback

* 將 OpenAI token、Channel secret、Channel access token
填入設定檔 application.yml

* 開始運行

![1172073](https://github.com/max7789632/LineRobot-OpenAi/assets/73981687/4530e0b7-7c90-43e6-9df3-76d110ff864e)
