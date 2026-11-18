-- 商家智能运营系统数据库初始化脚本
-- 数据库: merchant_ops

CREATE DATABASE IF NOT EXISTS merchant_ops DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE merchant_ops;

-- ----------------------------
-- 员工表
-- ----------------------------
CREATE TABLE employee (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(32)  NOT NULL COMMENT '用户名',
    name        VARCHAR(32)  NOT NULL COMMENT '姓名',
    password    VARCHAR(64)  NOT NULL COMMENT '密码',
    phone       VARCHAR(11)  NOT NULL COMMENT '手机号',
    sex         VARCHAR(2)   NOT NULL COMMENT '性别',
    id_number   VARCHAR(18)  NOT NULL COMMENT '身份证号',
    status      INT          NOT NULL DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    create_time DATETIME     NOT NULL COMMENT '创建时间',
    update_time DATETIME     NOT NULL COMMENT '更新时间',
    create_user BIGINT       NOT NULL COMMENT '创建人',
    update_user BIGINT       NOT NULL COMMENT '修改人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- ----------------------------
-- 分类表
-- ----------------------------
CREATE TABLE category (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    type        INT          NOT NULL COMMENT '类型 1:菜品分类 2:套餐分类',
    name        VARCHAR(32)  NOT NULL COMMENT '分类名称',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '顺序',
    status      INT          NOT NULL DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    create_time DATETIME     NOT NULL COMMENT '创建时间',
    update_time DATETIME     NOT NULL COMMENT '更新时间',
    create_user BIGINT       NOT NULL COMMENT '创建人',
    update_user BIGINT       NOT NULL COMMENT '修改人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- ----------------------------
-- 菜品表
-- ----------------------------
CREATE TABLE dish (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(32)   NOT NULL COMMENT '菜品名称',
    category_id BIGINT        NOT NULL COMMENT '菜品分类id',
    price       DECIMAL(10,2) NOT NULL COMMENT '菜品价格',
    image       VARCHAR(255)  NOT NULL COMMENT '图片',
    description VARCHAR(255)           DEFAULT NULL COMMENT '描述信息',
    status      INT           NOT NULL DEFAULT 1 COMMENT '0:停售 1:起售',
    create_time DATETIME      NOT NULL COMMENT '创建时间',
    update_time DATETIME      NOT NULL COMMENT '更新时间',
    create_user BIGINT        NOT NULL COMMENT '创建人',
    update_user BIGINT        NOT NULL COMMENT '修改人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- ----------------------------
-- 菜品口味表
-- ----------------------------
CREATE TABLE dish_flavor (
    id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    dish_id BIGINT       NOT NULL COMMENT '菜品id',
    name    VARCHAR(32)  NOT NULL COMMENT '口味名称',
    value   VARCHAR(255)          DEFAULT NULL COMMENT '口味数据list',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品口味表';

-- ----------------------------
-- 套餐表
-- ----------------------------
CREATE TABLE setmeal (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    category_id BIGINT        NOT NULL COMMENT '分类id',
    name        VARCHAR(32)   NOT NULL COMMENT '套餐名称',
    price       DECIMAL(10,2) NOT NULL COMMENT '套餐价格',
    status      INT           NOT NULL DEFAULT 1 COMMENT '状态 0:停用 1:启用',
    description VARCHAR(255)           DEFAULT NULL COMMENT '描述信息',
    image       VARCHAR(255)  NOT NULL COMMENT '图片',
    create_time DATETIME      NOT NULL COMMENT '创建时间',
    update_time DATETIME      NOT NULL COMMENT '更新时间',
    create_user BIGINT        NOT NULL COMMENT '创建人',
    update_user BIGINT        NOT NULL COMMENT '修改人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';

-- ----------------------------
-- 套餐菜品关系表
-- ----------------------------
CREATE TABLE setmeal_dish (
    id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    setmeal_id BIGINT        NOT NULL COMMENT '套餐id',
    dish_id    BIGINT        NOT NULL COMMENT '菜品id',
    name       VARCHAR(32)            DEFAULT NULL COMMENT '菜品名称（冗余）',
    price      DECIMAL(10,2)          DEFAULT NULL COMMENT '菜品原价',
    copies     INT           NOT NULL DEFAULT 1 COMMENT '份数',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐菜品关系表';

-- ----------------------------
-- 用户表
-- ----------------------------
CREATE TABLE user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    openid      VARCHAR(45)           DEFAULT NULL COMMENT '微信openid',
    name        VARCHAR(32)           DEFAULT NULL COMMENT '姓名',
    phone       VARCHAR(11)           DEFAULT NULL COMMENT '手机号',
    sex         VARCHAR(2)            DEFAULT NULL COMMENT '性别',
    id_number   VARCHAR(18)           DEFAULT NULL COMMENT '身份证号',
    avatar      VARCHAR(500)          DEFAULT NULL COMMENT '头像',
    create_time DATETIME     NOT NULL COMMENT '注册时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 地址簿表
-- ----------------------------
CREATE TABLE address_book (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        BIGINT       NOT NULL COMMENT '用户id',
    consignee      VARCHAR(50)  NOT NULL COMMENT '收货人',
    phone          VARCHAR(11)  NOT NULL COMMENT '手机号',
    sex            VARCHAR(2)            DEFAULT NULL COMMENT '性别',
    province_code  VARCHAR(12)           DEFAULT NULL COMMENT '省级区划编号',
    province_name  VARCHAR(32)           DEFAULT NULL COMMENT '省级名称',
    city_code      VARCHAR(12)           DEFAULT NULL COMMENT '市级区划编号',
    city_name      VARCHAR(32)           DEFAULT NULL COMMENT '市级名称',
    district_code  VARCHAR(12)           DEFAULT NULL COMMENT '区级区划编号',
    district_name  VARCHAR(32)           DEFAULT NULL COMMENT '区级名称',
    detail         VARCHAR(200) NOT NULL COMMENT '详细地址',
    label          VARCHAR(100)          DEFAULT NULL COMMENT '标签',
    is_default     INT          NOT NULL DEFAULT 0 COMMENT '是否默认 0:否 1:是',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址簿表';

-- ----------------------------
-- 订单表
-- ----------------------------
CREATE TABLE orders (
    id                       BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    number                   VARCHAR(32)   NOT NULL COMMENT '订单号',
    status                   INT           NOT NULL COMMENT '订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消',
    user_id                  BIGINT        NOT NULL COMMENT '下单用户id',
    address_book_id          BIGINT        NOT NULL COMMENT '地址id',
    order_time               DATETIME      NOT NULL COMMENT '下单时间',
    checkout_time            DATETIME               DEFAULT NULL COMMENT '结账时间',
    pay_method               INT                    DEFAULT NULL COMMENT '支付方式 1微信 2支付宝',
    pay_status               INT           NOT NULL DEFAULT 0 COMMENT '支付状态 0未支付 1已支付 2退款',
    amount                   DECIMAL(10,2) NOT NULL COMMENT '实收金额',
    remark                   VARCHAR(100)           DEFAULT NULL COMMENT '备注',
    phone                    VARCHAR(11)            DEFAULT NULL COMMENT '手机号',
    address                  VARCHAR(255)           DEFAULT NULL COMMENT '地址',
    consignee                VARCHAR(50)            DEFAULT NULL COMMENT '收货人',
    cancel_reason            VARCHAR(255)           DEFAULT NULL COMMENT '订单取消原因',
    rejection_reason         VARCHAR(255)           DEFAULT NULL COMMENT '订单拒绝原因',
    cancel_time              DATETIME               DEFAULT NULL COMMENT '订单取消时间',
    estimated_delivery_time  DATETIME               DEFAULT NULL COMMENT '预计送达时间',
    delivery_status          INT                    DEFAULT NULL COMMENT '配送状态 1立即送出 0选择具体时间',
    delivery_time            DATETIME               DEFAULT NULL COMMENT '送达时间',
    pack_amount              INT           NOT NULL DEFAULT 0 COMMENT '打包费',
    tableware_number         INT           NOT NULL DEFAULT 0 COMMENT '餐具数量',
    tableware_status         INT                    DEFAULT NULL COMMENT '餐具数量状态 1按餐量提供 0选择具体数量',
    PRIMARY KEY (id),
    UNIQUE KEY uk_number (number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ----------------------------
-- 订单明细表
-- ----------------------------
CREATE TABLE order_detail (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(32)   NOT NULL COMMENT '名称',
    order_id    BIGINT        NOT NULL COMMENT '订单id',
    dish_id     BIGINT                 DEFAULT NULL COMMENT '菜品id',
    setmeal_id  BIGINT                 DEFAULT NULL COMMENT '套餐id',
    dish_flavor VARCHAR(50)            DEFAULT NULL COMMENT '口味',
    number      INT           NOT NULL COMMENT '数量',
    amount      DECIMAL(10,2) NOT NULL COMMENT '金额',
    image       VARCHAR(255)           DEFAULT NULL COMMENT '图片',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ----------------------------
-- 购物车表
-- ----------------------------
CREATE TABLE shopping_cart (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(32)   NOT NULL COMMENT '名称',
    user_id     BIGINT        NOT NULL COMMENT '用户id',
    dish_id     BIGINT                 DEFAULT NULL COMMENT '菜品id',
    setmeal_id  BIGINT                 DEFAULT NULL COMMENT '套餐id',
    dish_flavor VARCHAR(50)            DEFAULT NULL COMMENT '口味',
    number      INT           NOT NULL DEFAULT 1 COMMENT '数量',
    amount      DECIMAL(10,2) NOT NULL COMMENT '金额',
    image       VARCHAR(255)           DEFAULT NULL COMMENT '图片',
    create_time DATETIME               DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ----------------------------
-- 开发环境管理员账号 (用户名: admin  密码: Merchant@123；请在生产环境立即修改)
-- ----------------------------
INSERT INTO employee (id, username, name, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)
VALUES (1, 'admin', '管理员', '3e8a4c5153d31e5014839c3bee71cc78', '13800138000', '1', '110101199001011234', 1, NOW(), NOW(), 1, 1);
