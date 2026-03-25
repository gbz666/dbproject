# my_llm.py
from dotenv import load_dotenv
import os
from typing import Optional
from openai import OpenAI
from hello_agents import HelloAgentsLLM
class MyLLM(HelloAgentsLLM):
  """
  一个自定义的LLM客户端，通过继承增加了对ModelScope的支持。
  """
  def __init__(
    self,
    model: Optional[str] = None,
    api_key: Optional[str] = None,
    base_url: Optional[str] = None,
    provider: Optional[str] = "auto",
    **kwargs
  ):
    load_dotenv()
    if provider=="modelscope":
      print("使用默认配置")
      self.api_key = api_key or os.getenv("LLM_API_KEY")
      self.base_url = base_url or os.getenv("LLM_BASE_URL")
    # 验证凭证是否存在
      if not self.api_key:
        raise ValueError("ModelScope API key not found. Please set MODELSCOPE_API_KEY environment variable.")
      # 设置默认模型和其他参数
      self.model = model or os.getenv("LLM_MODEL_ID") or "Qwen/Qwen2.5-VL-72B-Instruct"
      self.temperature = kwargs.get('temperature', 0.7)
      self.max_tokens = kwargs.get('max_tokens')
      self.timeout = kwargs.get('timeout', 60)
      self._client = OpenAI(api_key=self.api_key, base_url=self.base_url,
timeout=self.timeout)
    # 使用获取的参数创建OpenAI客户端实例
    else:
      print("使用自定义配置")
      self.api_key = api_key
      self.base_url = base_url
      self.timeout = kwargs.get('timeout', 60)
      self._client = OpenAI(api_key=self.api_key, base_url=self.base_url, timeout=self.timeout)
    # return self._client
