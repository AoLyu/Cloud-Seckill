/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.174.12
 Source Server Type    : MySQL
 Source Server Version : 80028
 Source Host           : 192.168.174.12:3306
 Source Schema         : miaosha

 Target Server Type    : MySQL
 Target Server Version : 80028
 File Encoding         : 65001

 Date: 22/02/2022 17:03:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for item
-- ----------------------------
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `price` double(10, 0) NOT NULL DEFAULT 0,
  `description` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `sales` int NOT NULL DEFAULT 0,
  `img_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item
-- ----------------------------
INSERT INTO `item` VALUES (6, 'iphone13', 800, '最好用的苹果手机', 6360, 'https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3974550569,4161544558&fm=27&gp=0.jpg');
INSERT INTO `item` VALUES (7, 'iphone12', 600, '第二好用的苹果手机', 88, 'http://img5.imgtn.bdimg.com/it/u=2067197169,357050228&fm=26&gp=0.jpg');

-- ----------------------------
-- Table structure for item_stock
-- ----------------------------
DROP TABLE IF EXISTS `item_stock`;
CREATE TABLE `item_stock`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `stock` int NOT NULL DEFAULT 0,
  `item_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `item_id_index`(`item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_stock
-- ----------------------------
INSERT INTO `item_stock` VALUES (6, 200, 6);
INSERT INTO `item_stock` VALUES (7, 200, 7);

-- ----------------------------
-- Table structure for order_info
-- ----------------------------
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info`  (
  `id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `user_id` int NOT NULL DEFAULT 0,
  `item_id` int NOT NULL DEFAULT 0,
  `item_price` double NOT NULL DEFAULT 0,
  `amount` int NOT NULL DEFAULT 0,
  `order_price` double NOT NULL DEFAULT 0,
  `promo_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_id_index`(`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_info
-- ----------------------------

-- ----------------------------
-- Table structure for promo
-- ----------------------------
DROP TABLE IF EXISTS `promo`;
CREATE TABLE `promo`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `promo_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `start_date` datetime(0) NOT NULL DEFAULT '0000-00-00 00:00:00',
  `item_id` int NOT NULL DEFAULT 0,
  `promo_item_price` double NOT NULL DEFAULT 0,
  `end_date` datetime(0) NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of promo
-- ----------------------------
INSERT INTO `promo` VALUES (1, 'iphone4抢购活动', '2022-02-20 14:04:30', 6, 100, '2022-02-23 19:00:00');

-- ----------------------------
-- Table structure for sequence_info
-- ----------------------------
DROP TABLE IF EXISTS `sequence_info`;
CREATE TABLE `sequence_info`  (
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `current_value` int NOT NULL DEFAULT 0,
  `step` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sequence_info
-- ----------------------------
INSERT INTO `sequence_info` VALUES ('order_info', 811, 1);

-- ----------------------------
-- Table structure for stock_log
-- ----------------------------
DROP TABLE IF EXISTS `stock_log`;
CREATE TABLE `stock_log`  (
  `stock_log_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `item_id` int NOT NULL DEFAULT 0,
  `amount` int NOT NULL DEFAULT 0,
  `status` int NOT NULL DEFAULT 0 COMMENT '//1表示初始状态，2表示下单扣减库存成功，3表示下单回滚',
  PRIMARY KEY (`stock_log_id`) USING BTREE,
  UNIQUE INDEX `stock_log_id_index`(`stock_log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of stock_log
-- ----------------------------

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `gender` tinyint NOT NULL DEFAULT 0 COMMENT '//1代表男性，2代表女性',
  `age` int NOT NULL DEFAULT 0,
  `telphone` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `register_mode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '//byphone,bywechat,byalipay',
  `third_party_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `telphone_unique_index`(`telphone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_info
-- ----------------------------
INSERT INTO `user_info` VALUES (1, '第一个用户', 1, 30, '13521234859', 'byphone', '');
INSERT INTO `user_info` VALUES (15, 'teambition', 1, 20, '1312345678', 'byphone', '');
INSERT INTO `user_info` VALUES (20, '82030', 1, 1, '11111122', 'byphone', '');
INSERT INTO `user_info` VALUES (21, 'hzl', 1, 31, '13671573214', 'byphone', '');
INSERT INTO `user_info` VALUES (22, 'testuser', 1, 20, '13562514273', 'byphone', '');
INSERT INTO `user_info` VALUES (23, 'Bruce', 1, 22, '13411222111', 'byphone', '');
INSERT INTO `user_info` VALUES (24, 'Bruce2', 1, 27, '13411222112', 'byphone', '');

-- ----------------------------
-- Table structure for user_password
-- ----------------------------
DROP TABLE IF EXISTS `user_password`;
CREATE TABLE `user_password`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `encrpt_password` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `user_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_password
-- ----------------------------
INSERT INTO `user_password` VALUES (1, 'ddlsjfjfjfjlf', 1);
INSERT INTO `user_password` VALUES (9, '4QrcOUm6Wau+VuBX8g+IPg==', 15);
INSERT INTO `user_password` VALUES (11, 'xMpCOKC5I4INzFCab3WEmw==', 20);
INSERT INTO `user_password` VALUES (12, '4QrcOUm6Wau+VuBX8g+IPg==', 21);
INSERT INTO `user_password` VALUES (13, '4QrcOUm6Wau+VuBX8g+IPg==', 22);
INSERT INTO `user_password` VALUES (14, 'jdz/OoD0GJyhydTZAsPJCQ==', 23);
INSERT INTO `user_password` VALUES (15, 'hylmli6Jf2MQ3Hc4XdDMMg==', 24);

SET FOREIGN_KEY_CHECKS = 1;
