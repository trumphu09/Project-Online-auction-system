CREATE DATABASE  IF NOT EXISTS `online_auction` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `online_auction`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: online_auction
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `artworks`
--

DROP TABLE IF EXISTS `artworks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `artworks` (
  `item_id` int NOT NULL,
  `artist` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `creation_year` int DEFAULT NULL,
  `material` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  CONSTRAINT `fk_art_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `auctions`
--

DROP TABLE IF EXISTS `auctions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auctions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `item_id` int NOT NULL,
  `seller_id` int NOT NULL,
  `current_max_price` double DEFAULT '0',
  `highest_bidder_id` int DEFAULT NULL,
  `end_time` datetime NOT NULL,
  `status` enum('OPEN','RUNNING','FINISHED','PAID','CANCELLED') COLLATE utf8mb4_unicode_ci DEFAULT 'OPEN',
  `has_extended` tinyint(1) DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `price_step` double DEFAULT '1000',
  PRIMARY KEY (`id`),
  KEY `fk_auctions_item` (`item_id`),
  KEY `fk_auctions_seller` (`seller_id`),
  KEY `fk_auctions_highest_bidder` (`highest_bidder_id`),
  CONSTRAINT `fk_auctions_highest_bidder` FOREIGN KEY (`highest_bidder_id`) REFERENCES `bidders` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_auctions_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_auctions_seller` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `auto_bids`
--

DROP TABLE IF EXISTS `auto_bids`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auto_bids` (
  `id` int NOT NULL AUTO_INCREMENT,
  `auction_id` int NOT NULL,
  `user_id` int NOT NULL,
  `max_amount` decimal(15,2) NOT NULL,
  `price_step` decimal(15,2) DEFAULT '50000.00',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_auto_bid` (`auction_id`,`user_id`),
  KEY `fk_autobid_user` (`user_id`),
  CONSTRAINT `fk_autobid_auction` FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_autobid_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bidders`
--

DROP TABLE IF EXISTS `bidders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bidders` (
  `user_id` int NOT NULL,
  `account_balance` double DEFAULT '0',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_bidders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bids`
--

DROP TABLE IF EXISTS `bids`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bids` (
  `id` int NOT NULL AUTO_INCREMENT,
  `auction_id` int NOT NULL,
  `bidder_id` int NOT NULL,
  `bid_amount` double NOT NULL,
  `bid_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_bids_auction` (`auction_id`),
  KEY `fk_bids_bidder` (`bidder_id`),
  CONSTRAINT `fk_bids_auction` FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_bids_bidder` FOREIGN KEY (`bidder_id`) REFERENCES `bidders` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=324 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `electronics`
--

DROP TABLE IF EXISTS `electronics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `electronics` (
  `item_id` int NOT NULL,
  `warranty_months` int DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  CONSTRAINT `fk_elec_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `items`
--

DROP TABLE IF EXISTS `items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `seller_id` int NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `starting_price` double NOT NULL,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `image_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_items_seller` (`seller_id`),
  CONSTRAINT `fk_items_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `auction_id` int NOT NULL,
  `from_bidder_id` int NOT NULL,
  `to_seller_id` int NOT NULL,
  `amount` double NOT NULL,
  `status` enum('PENDING','COMPLETED','FAILED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `payment_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_payment_auction` (`auction_id`),
  KEY `fk_payment_bidder` (`from_bidder_id`),
  KEY `fk_payment_seller` (`to_seller_id`),
  CONSTRAINT `fk_payment_auction` FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_payment_bidder` FOREIGN KEY (`from_bidder_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_payment_seller` FOREIGN KEY (`to_seller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `seller_ratings`
--

DROP TABLE IF EXISTS `seller_ratings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seller_ratings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `auction_id` int NOT NULL,
  `bidder_id` int NOT NULL,
  `seller_id` int NOT NULL,
  `rating` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_auction_bidder` (`auction_id`,`bidder_id`),
  KEY `fk_sr_bidder` (`bidder_id`),
  KEY `fk_sr_seller` (`seller_id`),
  CONSTRAINT `fk_sr_auction` FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sr_bidder` FOREIGN KEY (`bidder_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sr_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sellers`
--

DROP TABLE IF EXISTS `sellers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sellers` (
  `user_id` int NOT NULL,
  `total_rating` double DEFAULT '5',
  `sale_count` int DEFAULT '0',
  `account_balance` double DEFAULT '0',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_sellers_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('BIDDER','SELLER','ADMIN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `isActive` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vehicles`
--

DROP TABLE IF EXISTS `vehicles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicles` (
  `item_id` int NOT NULL,
  `brand` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mileage` int DEFAULT NULL,
  `condition_state` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  CONSTRAINT `fk_vehicle_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `watchlist`
--

DROP TABLE IF EXISTS `watchlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `watchlist` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `item_id` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_item` (`user_id`,`item_id`),
  KEY `fk_watchlist_item` (`item_id`),
  CONSTRAINT `fk_watchlist_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_watchlist_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-22 11:18:43