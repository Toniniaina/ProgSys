Create Database hexashop;
       use hexashop;
CREATE TABLE h_categories(
                             idCategorie INT PRIMARY KEY AUTO_INCREMENT,
                             nomCategorie VARCHAR(255)
);

CREATE TABLE h_produits(
                           idProduit INT PRIMARY KEY AUTO_INCREMENT,
                           nom VARCHAR(200),
                           image VARCHAR(200),
                           prix INT,
                           etoiles INT,
                           idCategorie INT,
                           FOREIGN KEY (idCategorie) REFERENCES h_categories(idCategorie) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE h_admin(
                        idAdmin INT PRIMARY KEY AUTO_INCREMENT,
                        login VARCHAR(255) UNIQUE,
                        mot_de_passe VARCHAR(255)
);

CREATE TABLE h_image (
                         idimage INT AUTO_INCREMENT PRIMARY KEY,
                         chemin VARCHAR(200) NOT NULL,
                         idproduit INT NOT NULL,
                         FOREIGN KEY (idproduit) REFERENCES h_produits(idProduit) ON DELETE CASCADE
) ENGINE=InnoDB;


