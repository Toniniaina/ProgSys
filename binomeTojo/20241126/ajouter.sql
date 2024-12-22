INSERT INTO h_categories (nomCategorie)
VALUES ('Men''s');

INSERT INTO h_categories (nomCategorie)
VALUES ('Women''s');

INSERT INTO h_categories (nomCategorie)
VALUES ('Kid''s');

INSERT INTO h_produits (nom, image, prix, etoiles, idCategorie)
VALUES
    ('T-shirt Women 01', 'assets/images/women-01.jpg', 50, 4, 2),  -- ID 2 pour Men’s
    ('T-shirt Women 02', 'assets/images/women-02.jpg', 60, 5, 2),
    ('T-shirt Women 03', 'assets/images/women-03.jpg', 55, 4, 2),
    ('T-shirt Women 04', 'assets/images/women-01.jpg', 50, 4, 2);  -- ID 2 pour Men’s

-- Insertion des h_produits pour la catégorie 'Men''s' (ID 1)
INSERT INTO h_produits (nom, image, prix, etoiles, idCategorie)
VALUES
    ('T-shirt Men 01', 'assets/images/men-01.jpg', 80, 4, 1),  -- ID 1 pour Women’s
    ('T-shirt Men 02', 'assets/images/men-02.jpg', 90, 5, 1),
    ('T-shirt Men 03', 'assets/images/men-03.jpg', 85, 4, 1),
    ('T-shirt Men 04', 'assets/images/men-01.jpg', 80, 4, 1);  -- ID 1 pour Women’s

-- Insertion des produits pour la catégorie 'Kid''s' (ID 3)
INSERT INTO h_produits (nom, image, prix, etoiles, idCategorie)
VALUES
    ('T-shirt Kid 01', 'assets/images/kid-01.jpg', 30, 3, 3),
    ('T-shirt Kid 02', 'assets/images/kid-02.jpg', 35, 4, 3),
    ('T-shirt Kid 03', 'assets/images/kid-03.jpg', 40, 5, 3),
    ('T-shirt Kid 04', 'assets/images/kid-01.jpg', 30, 3, 3);  -- ID 3 pour Kid’s



INSERT INTO h_admin (login, mot_de_passe)
VALUES ('admin', 'adminpassword');
