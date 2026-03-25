from openai import OpenAI
import  os
os.environ['NO_PROXY'] = '127.0.0.1,localhost'
os.environ['http_proxy'] = ''
os.environ['https_proxy'] = ''

# 初始化客户端，指向 Ollama 的本地服务
client = OpenAI(
    base_url="http://localhost:11434/v1",  # Ollama API 地址
    api_key="ollama"  # Ollama 默认无需真实 API Key，填任意值即可
)

# 发送请求
response = client.chat.completions.create(
    model="qwen2.5-coder:7b",  # 指定模型
    messages=[
        {"role": "system", "content": "你是一个有帮助的助手。"},
        {"role": "user", "content": "你好，什么是大模型？"}
    ],
    temperature=0.7,  # 控制生成多样性
    max_tokens=512    # 最大生成 token 数
)

# 打印结果
print(response.choices[0].message.content)