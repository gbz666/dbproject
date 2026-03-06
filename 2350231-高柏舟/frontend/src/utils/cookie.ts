/**
 * Cookie 工具函数
 * 用于持久化存储登录状态
 */

/**
 * 设置 cookie
 * @param name cookie 名称
 * @param value cookie 值
 * @param days 过期天数（默认 30 天）
 */
export function setCookie(
  name: string,
  value: string,
  days: number = 30
): void {
  const expires = new Date();
  expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
  document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/;SameSite=Lax`;
}

/**
 * 获取 cookie
 * @param name cookie 名称
 * @returns cookie 值，不存在返回 null
 */
export function getCookie(name: string): string | null {
  const nameEQ = name + "=";
  const ca = document.cookie.split(";");
  for (let i = 0; i < ca.length; i++) {
    let c = ca[i];
    if (c) {
      while (c.charAt(0) === " ") c = c.substring(1, c.length);
      if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
  }
  return null;
}

/**
 * 删除 cookie
 * @param name cookie 名称
 */
export function removeCookie(name: string): void {
  document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;`;
}
