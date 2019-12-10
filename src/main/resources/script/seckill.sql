/*
SQLyog Ultimate v12.3.1 (64 bit)
MySQL - 5.7.23-log : Database - seckilldb
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`seckilldb` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `seckilldb`;

/*Table structure for table `goods` */

DROP TABLE IF EXISTS `goods`;

CREATE TABLE `goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `goods_name` varchar(16) DEFAULT NULL COMMENT '商品名称',
  `goods_title` varchar(64) DEFAULT NULL COMMENT '商品标题',
  `goods_img` varchar(64) DEFAULT NULL COMMENT '商品图片',
  `goods_detail` longtext COMMENT '商品详情介绍',
  `goods_price` decimal(10,2) DEFAULT '0.00' COMMENT '商品单价',
  `goods_stock` int(11) DEFAULT '0' COMMENT '商品库存，-1表示没有限制',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/*Data for the table `goods` */

insert  into `goods`(`id`,`goods_name`,`goods_title`,`goods_img`,`goods_detail`,`goods_price`,`goods_stock`) values 
(1,'iphonex','Apple iphone x(A1865) 64G 银色 移动联通电信四G手机','/img/iphonex.png','Apple iphone x(A1865) 64G 银色 移动联通电信四G手机',8765.00,1000),
(2,'华为mate9','华为mate9 4G+128G 月光族 移动联通电信4G手机','/img/meta10.png','华为mate9 4G+128G 月光族 移动联通电信4G手机',4599.99,2000);

/*Table structure for table `order` */

DROP TABLE IF EXISTS `order`;

CREATE TABLE `order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `goods_id` bigint(20) DEFAULT NULL COMMENT '商品ID',
  `delivery_addr_id` bigint(20) DEFAULT NULL COMMENT '收货地址id',
  `goods_name` varchar(16) DEFAULT NULL COMMENT '冗余过来的商品名称',
  `goods_count` int(11) DEFAULT NULL COMMENT '商品数量',
  `goods_price` decimal(10,2) DEFAULT NULL COMMENT '商品单价',
  `order_channel` tinyint(4) DEFAULT '0' COMMENT '1-pc 2-android 3-ios',
  `status` tinyint(4) DEFAULT '0' COMMENT '订单状态，0新建未支付，1已支付，2已发货，3已收货，4已退款，5已完成',
  `create_date` datetime DEFAULT NULL COMMENT '订单创建时间',
  `pay_date` datetime DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2391 DEFAULT CHARSET=utf8;

/*Data for the table `order` */

insert  into `order`(`id`,`user_id`,`goods_id`,`delivery_addr_id`,`goods_name`,`goods_count`,`goods_price`,`order_channel`,`status`,`create_date`,`pay_date`) values 
(2390,15571387968,1,0,'iphonex',1,0.01,1,0,'2019-10-26 17:12:16',NULL);

/*Table structure for table `seckill_goods` */

DROP TABLE IF EXISTS `seckill_goods`;

CREATE TABLE `seckill_goods` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `goods_id` bigint(20) DEFAULT NULL,
  `seckill_price` decimal(10,2) DEFAULT '0.00',
  `stock_count` int(11) DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/*Data for the table `seckill_goods` */

insert  into `seckill_goods`(`id`,`goods_id`,`seckill_price`,`stock_count`,`start_date`,`end_date`) values 
(1,1,0.01,96,'2019-10-16 15:48:12','2019-10-27 14:10:53'),
(2,2,0.02,200,'2019-10-18 14:35:38','2019-10-20 14:35:43');

/*Table structure for table `seckill_order` */

DROP TABLE IF EXISTS `seckill_order`;

CREATE TABLE `seckill_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `goods_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `u_uid_gid` (`user_id`,`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2392 DEFAULT CHARSET=utf8;

/*Data for the table `seckill_order` */

insert  into `seckill_order`(`id`,`user_id`,`order_id`,`goods_id`) values 
(2391,15571387968,2390,1);

/*Table structure for table `seckill_user` */

DROP TABLE IF EXISTS `seckill_user`;

CREATE TABLE `seckill_user` (
  `id` bigint(20) NOT NULL COMMENT '用户ID，手机号码',
  `nickname` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL COMMENT 'md5(md5(明文+固定salt)+随机salt)',
  `salt` varchar(10) DEFAULT NULL,
  `head` varchar(128) DEFAULT NULL COMMENT '头像，云存储的id',
  `register_date` datetime DEFAULT NULL COMMENT '注册时间',
  `last_login_date` datetime DEFAULT NULL COMMENT '上次登录时间',
  `login_count` int(11) DEFAULT '0' COMMENT '登录次数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `seckill_user` */

insert  into `seckill_user`(`id`,`nickname`,`password`,`salt`,`head`,`register_date`,`last_login_date`,`login_count`) values 
(15571387968,'zhangsan','f99ef590c681e73ff91910979ccaf207','1a2b3c',NULL,NULL,NULL,0);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
