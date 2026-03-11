CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Login account',
    `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt encrypted password',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT 'Display name',
    `employed_company` VARCHAR(120) DEFAULT NULL COMMENT 'Current employed company',
    `role` ENUM('SUPER_ADMIN', 'ADMIN', 'EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE' COMMENT 'Role code',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0:disabled 1:enabled',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System users';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Role code',
    `label` VARCHAR(80) NOT NULL COMMENT 'Role label',
    `description` VARCHAR(255) DEFAULT NULL COMMENT 'Role description',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0:disabled 1:enabled',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System roles';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT NOT NULL COMMENT 'User id',
    `role_id` BIGINT NOT NULL COMMENT 'Role id',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_role_user` (`user_id`),
    KEY `idx_user_role_role` (`role_id`),
    CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
    CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User role mappings';

INSERT IGNORE INTO `sys_role` (`code`, `label`, `description`, `status`) VALUES
('SUPER_ADMIN', '超级管理员', '仅开发人员使用，拥有全量权限', 1),
('ADMIN', '管理员', '可管理员工账号和业务数据', 1),
('EMPLOYEE', '员工', '可使用工作日志和评价功能', 1);

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `sys_user` u
JOIN `sys_role` r ON r.code = u.role
LEFT JOIN `sys_user_role` ur ON ur.user_id = u.id AND ur.role_id = r.id
WHERE ur.id IS NULL;

CREATE TABLE IF NOT EXISTS `sys_company` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `name` VARCHAR(120) NOT NULL UNIQUE COMMENT 'Company name',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0:disabled 1:enabled',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Company catalog';

CREATE TABLE IF NOT EXISTS `sys_user_company` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT NOT NULL COMMENT 'User id',
    `company_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'Company id, 0 means none',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_company_user` (`user_id`),
    KEY `idx_user_company_company` (`company_id`),
    CONSTRAINT `fk_user_company_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User employed company mapping';

SET @sys_user_company_col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'employed_company'
);
SET @sys_user_company_col_sql = IF(
    @sys_user_company_col_exists = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `employed_company` VARCHAR(120) DEFAULT NULL COMMENT ''Current employed company'' AFTER `nickname`',
    'SELECT 1'
);
PREPARE sys_user_company_col_stmt FROM @sys_user_company_col_sql;
EXECUTE sys_user_company_col_stmt;
DEALLOCATE PREPARE sys_user_company_col_stmt;

CREATE TABLE IF NOT EXISTS `work_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT NOT NULL COMMENT 'User id',
    `work_date` DATE NOT NULL COMMENT 'Work date',
    `title` VARCHAR(120) NOT NULL COMMENT 'Log title',
    `content` LONGTEXT COMMENT 'Rich text content',
    `project_name` VARCHAR(120) DEFAULT NULL COMMENT 'Project name',
    `task_type` VARCHAR(50) DEFAULT NULL COMMENT 'Task type',
    `priority_level` VARCHAR(20) DEFAULT NULL COMMENT 'Priority level',
    `start_time` DATETIME DEFAULT NULL COMMENT 'Start time',
    `end_time` DATETIME DEFAULT NULL COMMENT 'End time',
    `work_hours` DECIMAL(5,2) DEFAULT 0.00 COMMENT 'Calculated work hours',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT 'Tag list',
    `attachment_urls` TEXT COMMENT 'Attachment urls',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_work_log_user_date` (`user_id`, `work_date`),
    CONSTRAINT `fk_work_log_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Daily work logs';

CREATE TABLE IF NOT EXISTS `company_review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT NOT NULL COMMENT 'User id',
    `company_name` VARCHAR(120) NOT NULL COMMENT 'Company name',
    `culture_score` TINYINT NOT NULL DEFAULT 0 COMMENT 'Culture score',
    `team_score` TINYINT NOT NULL DEFAULT 0 COMMENT 'Team atmosphere score',
    `growth_score` TINYINT NOT NULL DEFAULT 0 COMMENT 'Growth score',
    `salary_score` TINYINT NOT NULL DEFAULT 0 COMMENT 'Salary score',
    `balance_score` TINYINT NOT NULL DEFAULT 0 COMMENT 'Work balance score',
    `anonymous_mode` TINYINT NOT NULL DEFAULT 1 COMMENT '1 anonymous 0 public',
    `public_visible` TINYINT NOT NULL DEFAULT 0 COMMENT '1 public visible 0 hidden',
    `review_content` TEXT COMMENT 'Review content',
    `review_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING APPROVED REJECTED',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_company_review_company` (`company_name`),
    KEY `idx_company_review_user` (`user_id`),
    CONSTRAINT `fk_company_review_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Company reviews';

CREATE TABLE IF NOT EXISTS `review_audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `review_id` BIGINT NOT NULL COMMENT 'Review id',
    `operator_user_id` BIGINT NOT NULL COMMENT 'Operator user id',
    `operator_username` VARCHAR(50) NOT NULL COMMENT 'Operator username',
    `old_status` VARCHAR(20) NOT NULL COMMENT 'Old status',
    `new_status` VARCHAR(20) NOT NULL COMMENT 'New status',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT 'Audit remark',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_audit_review` (`review_id`),
    KEY `idx_audit_operator` (`operator_user_id`),
    CONSTRAINT `fk_audit_review` FOREIGN KEY (`review_id`) REFERENCES `company_review` (`id`),
    CONSTRAINT `fk_audit_operator` FOREIGN KEY (`operator_user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Review audit logs';

INSERT INTO `sys_company` (`name`)
SELECT DISTINCT t.company_name
FROM (
    SELECT TRIM(`employed_company`) AS company_name
    FROM `sys_user`
    WHERE `employed_company` IS NOT NULL AND TRIM(`employed_company`) <> ''
    UNION
    SELECT TRIM(`company_name`) AS company_name
    FROM `company_review`
    WHERE `company_name` IS NOT NULL AND TRIM(`company_name`) <> ''
) t
LEFT JOIN `sys_company` c ON c.`name` = t.company_name
WHERE t.company_name IS NOT NULL AND t.company_name <> '' AND c.`id` IS NULL;

INSERT INTO `sys_user_company` (`user_id`, `company_id`)
SELECT u.`id`, COALESCE(c.`id`, 0)
FROM `sys_user` u
LEFT JOIN `sys_company` c ON c.`name` = TRIM(u.`employed_company`)
LEFT JOIN `sys_user_company` uc ON uc.`user_id` = u.`id`
WHERE uc.`id` IS NULL;
