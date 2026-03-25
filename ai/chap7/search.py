# my_advanced_search.py
from atexit import register
import os
from dotenv import load_dotenv
from typing import Optional, List, Dict, Any
from hello_agents import ToolRegistry
class MyAdvancedSearchTool:
  def __init__(self):
    self.name="advanced_search"
    self.description="一个高级搜索工具，可以搜索互联网信息"
    self.search_sources=[]
    self._setup_search_source()
  def _setup_search_source(self):
    if(os.getenv("TAVILY_API_KEY")):
      from tavily import TavilyClient
      self.tavily_client = TavilyClient(api_key=os.getenv("TAVILY_API_KEY"))
      # self.serpapi_client = serpapi.Client(api_key=os.getenv("SERPAPI_API_KEY"))
      self.search_sources.append("tavily")
      print("✅ Tavily搜索源已启用")
    if os.getenv("SERPAPI_API_KEY"):
            try:
                import serpapi
                self.search_sources.append("serpapi")
                print("✅ SerpApi搜索源已启用")
            except ImportError:
                print("⚠️ SerpApi库未安装")

    if self.search_sources:
            print(f"🔧 可用搜索源: {', '.join(self.search_sources)}")
    else:
            print("⚠️ 没有可用的搜索源，请配置API密钥")
  def search(self, query: str) -> str:
      if(not query.strip()):
        return "错误：搜索查询不能为空"
      if(not self.search_sources):
        return """❌ 没有可用的搜索源，请配置以下API密钥之一:

1. Tavily API: 设置环境变量 TAVILY_API_KEY
   获取地址: https://tavily.com/

2. SerpAPI: 设置环境变量 SERPAPI_API_KEY
   获取地址: https://serpapi.com/

配置后重新运行程序。"""
      for source in self.search_sources:
        try:
          if source == "tavily":
                    result = self._search_with_tavily(query)
                    if result and "未找到" not in result:
                        return f"📊 Tavily AI搜索结果:\n\n{result}"

          elif source == "serpapi":
                    result = self._search_with_serpapi(query)
                    if result and "未找到" not in result:
                        return f"🌐 SerpApi Google搜索结果:\n\n{result}"

        except Exception as e:
                print(f"⚠️ {source} 搜索失败: {e}")
                continue
  def _search_with_tavily(self, query: str) -> str:
      try:
          response = self.tavily_client.search(query=query,max_results=5)
          if response.get('answer'):
              result = f"💡 AI直接答案:{response['answer']}\n\n"
          else:
              result = ""

          result += "🔗 相关结果:\n"
          for i, item in enumerate(response.get('results', [])[:3], 1):
              result += f"[{i}] {item.get('title', '')}\n"
              result += f"    {item.get('content', '')[:150]}...\n\n"
          return result
      except Exception as e:
          return f"Tavily搜索失败: {e}"
  def _search_with_serpapi(self, query: str) -> str:
      import serpapi
      try:
          response = serpapi.GoogleSearch({"q":query,"num":5,"api_key":os.getenv("SERPAPI_API_KEY")}).get_dict()
          result = "🔗 Google搜索结果:\n"
          if "organic_results" in response:
            for (i, res) in enumerate(response["organic_results"][:3], 1):
                result += f"[{i}] {res.get('title', '')}\n"
                result += f"    {res.get('snippet', '')}\n\n"
          return result
      except Exception as e:
          return f"SerpApi搜索失败: {e}"
def create_advanced_search_registry():
    """创建包含高级搜索工具的注册表"""
    registry = ToolRegistry()

    # 创建搜索工具实例
    search_tool = MyAdvancedSearchTool()

    # 注册搜索工具的方法作为函数
    registry.register_function(
        name="advanced_search",
        description="高级搜索工具，整合Tavily和SerpAPI多个搜索源，提供更全面的搜索结果",
        func=search_tool.search
    )

    return registry
