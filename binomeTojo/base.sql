CREATE TABLE produits (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          nom VARCHAR(100) NOT NULL,
                          prix DECIMAL(10, 2) NOT NULL
);
INSERT INTO produits (nom, prix) VALUES
                                     ('Produit 1', 10.99),
                                     ('Produit 2', 5.49),
                                     ('Produit 3', 20.00),
                                     ('Produit 4', 15.75),
                                     ('Produit 5', 8.30);
