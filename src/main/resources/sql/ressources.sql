-- Create table Categorie
CREATE TABLE IF NOT EXISTS `projet3a5`.`categorie` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `nom` VARCHAR(255) NOT NULL,
  `description` LONGTEXT,
  `date_creation` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Create table Ressource
CREATE TABLE IF NOT EXISTS `projet3a5`.`ressource` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `titre` VARCHAR(255) NOT NULL,
  `description` LONGTEXT NOT NULL,
  `type` VARCHAR(255) NOT NULL,
  `lien` VARCHAR(255) NOT NULL,
  `image` VARCHAR(255),
  `date_publication` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `nb_vues` INT DEFAULT 0,
  `categorie_id` INT,
  FOREIGN KEY (`categorie_id`) REFERENCES `projet3a5`.`categorie`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Insert some default categories
INSERT INTO `projet3a5`.`categorie` (`nom`, `description`) VALUES 
('Videos', 'Ressources video educatives'),
('Articles', 'Articles a lire'),
('Podcasts', 'Audio et podcasts');
