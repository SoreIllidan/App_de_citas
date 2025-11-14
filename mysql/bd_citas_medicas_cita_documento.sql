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
-- Table structure for table `cita_documento`
--

DROP TABLE IF EXISTS `cita_documento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cita_documento` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_cita` int(11) NOT NULL,
  `id_paciente` int(11) NOT NULL,
  `id_medico` int(11) NOT NULL,
  `tipo` varchar(20) NOT NULL,
  `titulo` varchar(150) DEFAULT NULL,
  `descripcion` text DEFAULT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(255) NOT NULL,
  `file_size` int(11) DEFAULT NULL,
  `mime_type` varchar(100) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_cd_paciente` (`id_paciente`),
  KEY `idx_cd_medico` (`id_medico`),
  KEY `idx_cd_tipo` (`tipo`),
  KEY `idx_cd_cita` (`id_cita`),
  CONSTRAINT `fk_cd_cita` FOREIGN KEY (`id_cita`) REFERENCES `cita` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cita_documento`
--

LOCK TABLES `cita_documento` WRITE;
/*!40000 ALTER TABLE `cita_documento` DISABLE KEYS */;
INSERT INTO `cita_documento` VALUES (1,3,1,1,'descanso','Descanso Médico','Documento de la cita del 2025-11-12 08:30','doc_descanso_20251111_014608_1cffc6.pdf','uploads/documentos/3/doc_descanso_20251111_014608_1cffc6.pdf',314409,'application/pdf','2025-11-10 19:46:08'),(2,5,1,1,'descanso','Descanso Médico','Documento de la cita del 2025-11-11 09:30','doc_descanso_20251111_014619_5b43cc.pdf','uploads/documentos/5/doc_descanso_20251111_014619_5b43cc.pdf',314409,'application/pdf','2025-11-10 19:46:19'),(3,2,1,2,'descanso','Descanso Médico','Documento de la cita del 2025-11-26 18:00','doc_descanso_20251112_202347_323242.pdf','uploads/documentos/2/doc_descanso_20251112_202347_323242.pdf',314409,'application/pdf','2025-11-12 14:23:47'),(4,5,1,1,'receta','Receta Médica','Documento de la cita del 2025-11-11 09:30','doc_receta_20251112_204032_93d211.pdf','uploads/documentos/5/doc_receta_20251112_204032_93d211.pdf',314409,'application/pdf','2025-11-12 14:40:32');
/*!40000 ALTER TABLE `cita_documento` ENABLE KEYS */;
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
