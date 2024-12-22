<?php
function enleverAccents($str) {
    $transliterations = [
        'à' => 'a', 'á' => 'a', 'â' => 'a', 'ä' => 'a', 'ã' => 'a', 'å' => 'a', 'æ' => 'ae',
        'è' => 'e', 'é' => 'e', 'ê' => 'e', 'ë' => 'e', 'ì' => 'i', 'í' => 'i', 'î' => 'i', 'ï' => 'i',
        'ò' => 'o', 'ó' => 'o', 'ô' => 'o', 'ö' => 'o', 'õ' => 'o', 'ø' => 'o', 'œ' => 'oe',
        'ù' => 'u', 'ú' => 'u', 'û' => 'u', 'ü' => 'u', 'ý' => 'y', 'ÿ' => 'y',
        'ç' => 'c', 'ñ' => 'n', 'ß' => 'ss'
    ];
    $str = strtr($str, $transliterations);
    $str = str_replace('..', '', $str);
    return $str;
}

include('../inc/connexion.php');

if (isset($_POST['submit'])) {
    $nom = enleverAccents($_POST['nom']);
    $categorie = $_POST['categorie'];
    $prix = $_POST['prix'];
    $etoiles = $_POST['etoiles'];

    if (isset($_FILES['image']) && $_FILES['image']['error'] == 0) {
        $imageTmp = $_FILES['image']['tmp_name'];
        $imageName = $_FILES['image']['name'];
        $imageSize = $_FILES['image']['size'];

        $allowedExtensions = ['jpg', 'jpeg', 'png', 'gif'];
        $ext = pathinfo($imageName, PATHINFO_EXTENSION);
        if (!in_array(strtolower($ext), $allowedExtensions)) exit("L'image doit être au format JPG, JPEG, PNG ou GIF.");
        if ($imageSize > 50 * 1024 * 1024) exit("L'image est trop grande. La taille maximale est de 5 Mo.");

        $generalImagePath = 'assets/images/' . basename($imageName);
        $backOfficeImagePath = 'backoffice/assets/images/' . basename($imageName);

        if (move_uploaded_file($imageTmp, '../' . $generalImagePath)) {
            copy('../' . $generalImagePath, '../' . $backOfficeImagePath);

            try {
                $sql = "INSERT INTO h_produits (nom, image, prix, etoiles, idCategorie) 
                        VALUES (:nom, :image, :prix, :etoiles, :categorie)";
                $connexion = connexion();
                $stmt = $connexion->prepare($sql);
                $stmt->bindParam(':nom', $nom);
                $stmt->bindParam(':image', $generalImagePath);
                $stmt->bindParam(':prix', $prix);
                $stmt->bindParam(':etoiles', $etoiles);
                $stmt->bindParam(':categorie', $categorie);
                $stmt->execute();
                echo "Produit ajouté avec succès!";
            } catch (PDOException $e) {
                echo "Erreur : " . $e->getMessage();
            }
        } else {
            echo "Erreur lors du téléchargement de l'image.";
        }
    } else {
        echo "Veuillez sélectionner une image.";
    }
}
?>
