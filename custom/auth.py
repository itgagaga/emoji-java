# -*- coding: utf-8 -*-
"""
Admin认证模块
提供安全的登录验证和会话管理功能
"""

import json
import hashlib
import time
import secrets
from pathlib import Path
from typing import Optional, Dict, Tuple
from fastapi import HTTPException, Request, Response
from fastapi.responses import JSONResponse


class AuthManager:
    """认证管理器"""

    def __init__(self, config_path: str = "custom/auth_config.json"):
        self.config_path = Path(config_path)
        self.config = self._load_config()
        self.active_sessions: Dict[str, Dict] = {}
        self.login_attempts: Dict[str, int] = {}
        self.locked_ips: Dict[str, float] = {}

    def _load_config(self) -> dict:
        """加载认证配置"""
        if self.config_path.exists():
            with open(self.config_path, 'r', encoding='utf-8') as f:
                return json.load(f)

        # 从 .credentials 文件读取默认凭据
        credentials_file = Path(".credentials")
        default_username = "admin"
        default_password = "admin123"

        if credentials_file.exists():
            try:
                with open(credentials_file, 'r', encoding='utf-8') as f:
                    for line in f:
                        line = line.strip()
                        if line.startswith("ADMIN_USERNAME="):
                            default_username = line.split("=", 1)[1]
                        elif line.startswith("ADMIN_PASSWORD="):
                            default_password = line.split("=", 1)[1]
            except Exception as e:
                print(f"⚠️ 读取 .credentials 文件失败: {e}，使用默认值")

        default_config = {
            "username": default_username,
            "password": self._hash_password(default_password),
            "session_timeout": 3600,
            "allowed_ips": [],
            "enable_rate_limit": True,
            "max_login_attempts": 5,
            "lockout_duration": 900
        }

        with open(self.config_path, 'w', encoding='utf-8') as f:
            json.dump(default_config, f, indent=2)

        return default_config

    def _save_config(self):
        """保存配置到文件"""
        with open(self.config_path, 'w', encoding='utf-8') as f:
            json.dump(self.config, f, indent=2)

    @staticmethod
    def _hash_password(password: str) -> str:
        """密码哈希（使用SHA256+盐值）"""
        salt = secrets.token_hex(16)
        hash_value = hashlib.sha256((password + salt).encode()).hexdigest()
        return f"{salt}${hash_value}"

    @staticmethod
    def _verify_password(password: str, stored_hash: str) -> bool:
        """验证密码"""
        try:
            salt, hash_value = stored_hash.split('$')
            new_hash = hashlib.sha256((password + salt).encode()).hexdigest()
            return new_hash == hash_value
        except:
            return False

    def _check_rate_limit(self, ip_address: str) -> bool:
        """检查IP是否被限制"""
        if not self.config.get("enable_rate_limit", True):
            return True

        if ip_address in self.locked_ips:
            lock_time = self.locked_ips[ip_address]
            lockout_duration = self.config.get("lockout_duration", 900)
            
            if time.time() - lock_time < lockout_duration:
                remaining = int(lockout_duration - (time.time() - lock_time))
                raise HTTPException(
                    status_code=429,
                    detail=f"IP已被锁定，请{remaining}秒后重试"
                )
            else:
                del self.locked_ips[ip_address]
                if ip_address in self.login_attempts:
                    del self.login_attempts[ip_address]

        return True

    def _record_failed_attempt(self, ip_address: str):
        """记录失败尝试"""
        max_attempts = self.config.get("max_login_attempts", 5)
        
        if ip_address not in self.login_attempts:
            self.login_attempts[ip_address] = 0
        
        self.login_attempts[ip_address] += 1
        
        if self.login_attempts[ip_address] >= max_attempts:
            self.locked_ips[ip_address] = time.time()

    def _clear_login_attempts(self, ip_address: str):
        """清除失败尝试记录"""
        if ip_address in self.login_attempts:
            del self.login_attempts[ip_address]

    def login(self, username: str, password: str, request: Request) -> Dict:
        """
        用户登录
        
        返回：
        {
            "success": bool,
            "token": str,
            "expires_in": int,
            "message": str
        }
        """
        ip_address = request.client.host if request.client else "unknown"
        
        try:
            self._check_rate_limit(ip_address)
        except HTTPException as e:
            return {
                "success": False,
                "token": None,
                "expires_in": 0,
                "message": e.detail
            }

        # 验证用户名和密码
        if username != self.config.get("username", "admin"):
            self._record_failed_attempt(ip_address)
            return {
                "success": False,
                "token": None,
                "expires_in": 0,
                "message": "用户名或密码错误"
            }

        stored_password = self.config.get("password", "")
        
        if not self._verify_password(password, stored_password):
            self._record_failed_attempt(ip_address)
            return {
                "success": False,
                "token": None,
                "expires_in": 0,
                "message": "用户名或密码错误"
            }

        # 检查IP白名单（如果启用）
        allowed_ips = self.config.get("allowed_ips", [])
        if allowed_ips and ip_address not in allowed_ips:
            return {
                "success": False,
                "token": None,
                "expires_in": 0,
                "message": "您的IP不在允许列表中"
            }

        # 生成Token
        token = secrets.token_urlsafe(32)
        session_timeout = self.config.get("session_timeout", 3600)
        
        session_data = {
            "token": token,
            "username": username,
            "ip_address": ip_address,
            "created_at": time.time(),
            "expires_at": time.time() + session_timeout,
            "user_agent": request.headers.get("user-agent", "")[:200]
        }
        
        self.active_sessions[token] = session_data
        self._clear_login_attempts(ip_address)

        return {
            "success": True,
            "token": token,
            "expires_in": session_timeout,
            "message": "登录成功"
        }

    def verify_token(self, token: str, request: Request) -> bool:
        """
        验证Token有效性
        
        返回：bool (True表示有效)
        """
        if not token or token not in self.active_sessions:
            return False

        session = self.active_sessions[token]
        
        # 检查过期时间
        if time.time() > session["expires_at"]:
            del self.active_sessions[token]
            return False

        # 检查IP绑定（可选增强安全性）
        current_ip = request.client.host if request.client else ""
        if session["ip_address"] != current_ip:
            return False

        # 更新最后访问时间（滑动过期）
        session_timeout = self.config.get("session_timeout", 3600)
        session["expires_at"] = time.time() + session_timeout

        return True

    def logout(self, token: str) -> bool:
        """登出（销毁Session）"""
        if token in self.active_sessions:
            del self.active_sessions[token]
            return True
        return False

    def get_token_from_request(self, request: Request) -> Optional[str]:
        """从请求中提取Token"""
        # 方法1：从Authorization头获取
        auth_header = request.headers.get("authorization", "")
        if auth_header.startswith("Bearer "):
            return auth_header[7:]

        # 方法2：从Cookie获取
        token_cookie = request.cookies.get("auth_token")
        if token_cookie:
            return token_cookie

        # 方法3：从查询参数获取（不推荐，但兼容性考虑）
        token_param = request.query_params.get("token")
        if token_param:
            return token_param

        return None

    def require_auth(self, request: Request) -> Tuple[bool, Optional[str]]:
        """
        验证请求是否有权限访问Admin资源
        
        返回：(is_authenticated, error_message)
        """
        token = self.get_token_from_request(request)
        
        if not token:
            return (False, "未提供认证Token")

        if not self.verify_token(token, request):
            return (False, "Token无效或已过期")

        return (True, None)

    def change_password(self, old_password: str, new_password: str) -> Dict:
        """修改密码"""
        stored_password = self.config.get("password", "")

        if not self._verify_password(old_password, stored_password):
            return {
                "success": False,
                "message": "原密码错误"
            }

        if len(new_password) < 6:
            return {
                "success": False,
                "message": "新密码长度不能少于6位"
            }

        self.config["password"] = self._hash_password(new_password)
        self._save_config()

        # 使所有现有Session失效
        self.active_sessions.clear()

        return {
            "success": True,
            "message": "密码已更新，所有Session已失效"
        }

    def update_config(self, new_config: dict) -> Dict:
        """更新配置"""
        for key, value in new_config.items():
            if key in self.config:
                self.config[key] = value
        
        self._save_config()
        
        return {
            "success": True,
            "message": "配置已更新",
            "config": {k: v for k, v in self.config.items() if k != "password"}
        }

    def cleanup_expired_sessions(self):
        """清理过期的Session"""
        current_time = time.time()
        expired_tokens = [
            token for token, session in self.active_sessions.items()
            if current_time > session["expires_at"]
        ]
        
        for token in expired_tokens:
            del self.active_sessions[token]
        
        return len(expired_tokens)

    def get_session_info(self, token: str) -> Optional[Dict]:
        """获取Session信息（用于调试）"""
        if token in self.active_sessions:
            session = self.active_sessions[token].copy()
            # 不返回完整Token
            session["token_preview"] = token[:8] + "..."
            del session["token"]
            return session
        return None


# 全局认证管理器实例
auth_manager = AuthManager()


def init_auth_system():
    """初始化认证系统（在应用启动时调用）"""
    global auth_manager
    auth_manager = AuthManager()
    print(f"✅ 认证系统已初始化")
    print(f"   用户名: {auth_manager.config['username']}")
    print(f"   会话超时: {auth_manager.config['session_timeout']}秒")


if __name__ == "__main__":
    print("=" * 60)
    print("🔐 Admin认证模块测试")
    print("=" * 60)
    
    auth = AuthManager()
    
    print("\n📋 当前配置:")
    print(f"   用户名: {auth.config['username']}")
    print(f"   会话超时: {auth.config['session_timeout']}秒")
    print(f"   最大尝试次数: {auth.config['max_login_attempts']}")
    print(f"   锁定时长: {auth.config['lockout_duration']}秒")
    
    print("\n💡 使用方法:")
    print("1. 在 custom/auth_config.json 中配置用户名和密码")
    print("2. 访问 /admin/login 进行登录")
    print("3. 登录后获得Token用于后续API调用")
    print("\n⚠️ 安全提示:")
    print("- 请立即修改默认密码！")
    print("- 生产环境建议启用HTTPS")
    print("- 定期更换密码")
