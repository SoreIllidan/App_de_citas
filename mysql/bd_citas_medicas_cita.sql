CREATE DATABASE  IF NOT EXISTS `bd_citas_medicas` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `bd_citas_medicas`;
-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: bd_citas_medicas
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cita`
--

DROP TABLE IF EXISTS `cita`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cita` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_paciente` int(11) NOT NULL,
  `id_medico` int(11) NOT NULL,
  `fecha` date NOT NULL,
  `hora` time NOT NULL,
  `medico` varchar(100) DEFAULT NULL,
  `consultorio` varchar(100) DEFAULT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'programada',
  `confirmed_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `id_paciente` (`id_paciente`),
  KEY `id_medico` (`id_medico`),
  CONSTRAINT `cita_ibfk_1` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id`) ON DELETE CASCADE,
  CONSTRAINT `cita_ibfk_2` FOREIGN KEY (`id_medico`) REFERENCES `medico` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cita`
--

LOCK TABLES `cita` WRITE;
/*!40000 ALTER TABLE `cita` DISABLE KEYS */;
INSERT INTO `cita` VALUES (1,1,1,'2025-11-19','10:30:00',NULL,'Piso 3, Puerta 301','cancelada',NULL,'2025-10-25 09:44:51'),(2,1,2,'2025-11-26','18:00:00',NULL,'Piso 5 , Puerta 506','cancelada',NULL,'2025-10-25 09:45:21'),(3,1,1,'2025-11-12','08:30:00',NULL,'Piso 3 , Puerta 303','cancelada','2025-11-10 17:04:12','2025-10-25 09:45:51'),(4,1,3,'2025-11-05','11:00:00',NULL,'Piso 5,puerta 503','confirmada','2025-11-10 17:04:26','2025-10-25 12:53:39'),(5,1,1,'2025-11-11','09:30:00',NULL,'Piso 7 ,puerta 701','confirmada',NULL,'2025-10-25 12:55:18'),(6,1,1,'2025-11-12','10:00:00',NULL,'Consultorio 304,Piso 3','confirmada','2025-11-12 10:21:17','2025-11-10 17:12:10'),(7,1,3,'2025-11-27','09:30:00',NULL,'Piso 6 , consultorio 602','programada',NULL,'2025-11-12 13:16:36'),(8,1,2,'2025-11-13','16:00:00',NULL,'Piso 1,Consultorio 102','programada',NULL,'2025-11-12 14:17:36'),(9,1,2,'2025-11-13','14:00:00',NULL,'Piso 3,Consultorio 203','programada',NULL,'2025-11-12 14:21:56');
/*!40000 ALTER TABLE `cita` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-12 15:55:20
