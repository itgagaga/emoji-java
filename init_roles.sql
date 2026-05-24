INSERT IGNORE INTO roles (id, name, description, permissions, max_mappings, max_overrides) VALUES 
(1, '超级管理员', '拥有所有权限', 'all', 99999, 99999),
(2, '普通用户', '可自定义映射和管理个人数据', 'basic', 200, 100),
(3, 'VIP用户', '更多配额和高级功能', 'all', 1000, 500);