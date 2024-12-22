<?php
include ('connexion.php');
function getCategories() {
    $pdo = connexion();
    $stmt = $pdo->prepare("SELECT * FROM h_categories");
    $stmt->execute();
    return $stmt->fetchAll(PDO::FETCH_ASSOC);
}
function formatCategorieNom($nom) {
    $correspondances = [
        "Men's" => "men",
        "Women's" => "women",
        "Kid's" => "kids",
    ];

    if (array_key_exists($nom, $correspondances)) {
        return $correspondances[$nom];
    }
    return $nom;
}

function getProduit($categories)
{
    $connexion = connexion();
    try {
        // Sélectionner les produits de la catégorie spécifiée et leur première image
        $sql = sprintf("SELECT p.*, i.chemin AS images
                        FROM h_produits p
                        LEFT JOIN h_image i ON p.idProduit = i.idproduit
                        WHERE p.idCategorie = %d
                        AND i.idimage = (SELECT MIN(idimage) FROM h_image WHERE idproduit = p.idProduit)", $categories);

        $stmt = $connexion->prepare($sql);
        $stmt->execute();
        $produits = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $produits;
    } catch (PDOException $e) {
        echo "Erreur : " . $e->getMessage();
        return [];
    }
}

function ficheProduit($produit)
{
    $connexion = connexion();
    try {
        $sql = sprintf("SELECT p.*, GROUP_CONCAT(i.chemin) AS images
                        FROM h_produits p
                        LEFT JOIN h_image i ON p.idProduit = i.idproduit
                        WHERE p.idProduit = %d", $produit);
        $stmt = $connexion->prepare($sql);
        $stmt->execute();
        $produits = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // Organiser les images dans un tableau pour chaque produit
        foreach ($produits as &$produit) {
            // Séparer les images en un tableau à partir de la chaîne
            if (!empty($produit['images'])) {
                $produit['images'] = explode(',', $produit['images']);
            } else {
                $produit['images'] = [];
            }
        }

        // Si aucun produit trouvé, retournez un tableau vide
        if (empty($produits)) {
            return [];
        }

        return $produits;
    } catch (PDOException $e) {
        echo "Erreur : " . $e->getMessage();
        return [];
    }
}


function listeProduit()
{
    $connexion = connexion();
    try {
        // Sélectionner les produits et leur première image
        $sql = "SELECT p.*, i.chemin AS images
                FROM h_produits p
                LEFT JOIN h_image i ON p.idProduit = i.idproduit
                WHERE i.idimage = (SELECT MIN(idimage) FROM h_image WHERE idproduit = p.idProduit)
                ";

        $stmt = $connexion->prepare($sql);
        $stmt->execute();
        $produits = $stmt->fetchAll(PDO::FETCH_ASSOC);
        return $produits;
    } catch (PDOException $e) {
        echo "Erreur : " . $e->getMessage();
        return [];
    }
}
function searchProduits($categories, $prixMin, $prixMax, $etoilesMin) {
    $pdo = connexion();
    try {
        // Sélectionner les produits et leur première image
        $sql = "SELECT p.*, i.chemin AS images
                FROM h_produits p
                LEFT JOIN h_image i ON p.idProduit = i.idproduit
                WHERE i.idimage = (SELECT MIN(idimage) FROM h_image WHERE idproduit = p.idProduit)
                AND p.prix BETWEEN :prixMin AND :prixMax
                AND p.etoiles >= :etoilesMin";

        if (!empty($categories)) {
            $sql .= " AND p.idCategorie = :categories";
        }

        $stmt = $pdo->prepare($sql);
        $stmt->bindParam(':prixMin', $prixMin, PDO::PARAM_INT);
        $stmt->bindParam(':prixMax', $prixMax, PDO::PARAM_INT);
        $stmt->bindParam(':etoilesMin', $etoilesMin, PDO::PARAM_INT);

        // Si une catégorie est spécifiée, lier ce paramètre
        if (!empty($categories)) {
            $stmt->bindParam(':categories', $categories, PDO::PARAM_INT);
        }

        $stmt->execute();
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    } catch (PDOException $e) {
        echo "Erreur : " . $e->getMessage();
        return [];
    }
}


function validerLogin($login, $motDePasse) {
    $pdo = connexion();
    $stmt = $pdo->prepare("SELECT * FROM h_admin WHERE login = :login AND mot_de_passe = :motDePasse");
    $stmt->bindParam(':login', $login);
    $stmt->bindParam(':motDePasse', $motDePasse);
    $stmt->execute();

    if ($stmt->rowCount() > 0) {
        return true;
    }
    return false;
}
function ajouterCategorie($nomCategorie) {
    $DBH = connexion();
    $stmt = $DBH->prepare("INSERT INTO h_categories (nomCategorie) VALUES (:nomCategorie)");
    $stmt->bindParam(':nomCategorie', $nomCategorie);
    $stmt->execute();
}
function obtenirCategories() {
    $DBH = connexion();
    $stmt = $DBH->query("SELECT * FROM h_categories");
    return $stmt->fetchAll(PDO::FETCH_ASSOC);
}
function modifierCategorie($idCategorie, $nomCategorie) {
    $DBH = connexion();
    $stmt = $DBH->prepare("UPDATE h_categories SET nomCategorie = :nomCategorie WHERE idCategorie = :idCategorie");
    $stmt->bindParam(':idCategorie', $idCategorie);
    $stmt->bindParam(':nomCategorie', $nomCategorie);
    $stmt->execute();
}
function supprimerCategorie($idCategorie) {
    $DBH = connexion();
    $stmt = $DBH->prepare("DELETE FROM h_categories WHERE idCategorie = :idCategorie");
    $stmt->bindParam(':idCategorie', $idCategorie);
    $stmt->execute();
}

