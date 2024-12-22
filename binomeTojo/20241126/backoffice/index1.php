<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload Product</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        h2 {
            text-align: center;
            color: #333;
        }

        form {
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 450px; /* Réduit la largeur du formulaire */
        }

        label {
            display: block;
            margin-bottom: 8px;
            color: #555;
        }

        input[type="text"],
        input[type="number"],
        select,
        input[type="file"] {
            width: 100%;
            padding: 10px;
            margin-bottom: 15px;
            border: 1px solid #ccc;
            border-radius: 4px;
            box-sizing: border-box;
        }

        input[type="number"] {
            -moz-appearance: textfield;
        }

        button {
            background-color: black;
            color: white;
            padding: 12px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            width: 100%;
            font-size: 16px;
        }

        button:hover {
            background-color: #333;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .modify-btn {
            background-color: #333;
            color: white;
            font-size: 16px;
            width: 100%;
        }

        .modify-btn:hover {
            background-color: #555;
        }

    </style>
</head>
<body>
<form action="products.php" method="GET" style="margin-top: 20px;">
    <button type="submit" class="modify-btn">Modify Product</button>
</form>
<form action="crud-categ.php" method="GET" style="margin-top: 20px;">
    <button type="submit" class="modify-btn">CRUD categories</button>
</form>

<form action="" method="POST" enctype="multipart/form-data">
    <h2>Upload Product</h2>
    <div class="form-group">
        <label for="nom">Product Name:</label>
        <input type="text" name="nom" id="nom" required><br><br>
    </div>

    <div class="form-group">
        <label for="categorie">Category:</label>
        <select name="categorie" id="categorie" required>
            <option value="1">Men's</option>
            <option value="2">Women's</option>
            <option value="3">Kid's</option>
        </select><br><br>
    </div>

    <div class="form-group">
        <label for="prix">Price:</label>
        <input type="number" name="prix" id="prix" required><br><br>
    </div>

    <div class="form-group">
        <label for="etoiles">Stars:</label>
        <input type="number" name="etoiles" id="etoiles" min="1" max="5" required><br><br>
    </div>

    <div class="form-group">
        <label for="image[]">Product Images (min. 2 images):</label>
        <input type="file" name="image[]" accept="image/*" required multiple><br><br>
    </div>

    <button type="submit" name="submit">Upload</button>
</form>

<?php
include('../inc/connexion.php');

if (isset($_POST['submit'])) {
    $nom = $_POST['nom'];
    $categorie = $_POST['categorie'];
    $prix = $_POST['prix'];
    $etoiles = $_POST['etoiles'];

    // Vérifier si des fichiers ont été envoyés
    if (isset($_FILES['image']) && count($_FILES['image']['name']) >= 2) {
        try {
            // Connexion à la base de données
            $connexion = connexion();
            $sql = "INSERT INTO h_produits (nom, prix, etoiles, idCategorie) VALUES (:nom, :prix, :etoiles, :categorie)";
            $stmt = $connexion->prepare($sql);
            $stmt->bindParam(':nom', $nom);
            $stmt->bindParam(':prix', $prix);
            $stmt->bindParam(':etoiles', $etoiles);
            $stmt->bindParam(':categorie', $categorie);
            $stmt->execute();
            $productId = $connexion->lastInsertId();

            // Tableau pour stocker les chemins des images
            $imagesPaths = [];
            foreach ($_FILES['image']['tmp_name'] as $key => $tmpName) {
                $imageName = $_FILES['image']['name'][$key];
                $generalImagePath = 'assets/images/' . basename($imageName);
                $backOfficeImagePath = 'backoffice/assets/images/' . basename($imageName);

                // Déplacer l'image dans le dossier principal
                if (move_uploaded_file($tmpName, '../' . $generalImagePath)) {
                    // Copier l'image dans le dossier backoffice
                    if (copy('../' . $generalImagePath, '../' . $backOfficeImagePath)) {
                        // Ajouter le chemin du dossier principal à l'array
                        $imagesPaths[] = $generalImagePath;
                    } else {
                        echo "Échec du téléchargement de l'image dans le second dossier.";
                    }
                } else {
                    echo "Échec du téléchargement de l'image dans le premier dossier.";
                }
            }

            // Insertion des images dans la base de données
            foreach ($imagesPaths as $imagePath) {
                $sql = "INSERT INTO h_image (chemin, idproduit) VALUES (:chemin, :idproduit)";
                $stmt = $connexion->prepare($sql);
                $stmt->bindParam(':chemin', $imagePath);
                $stmt->bindParam(':idproduit', $productId);
                $stmt->execute();
            }

            echo "Produit ajouté avec succès!";
        } catch (PDOException $e) {
            echo "Erreur : " . $e->getMessage();
        }
    } else {
        echo "Veuillez télécharger au moins 2 images.";
    }
}
?>

</body>
</html>
