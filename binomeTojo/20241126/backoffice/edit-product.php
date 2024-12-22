<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Product</title>
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
            max-width: 500px;
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
            background-color: #4CAF50;
            color: white;
            padding: 12px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            width: 100%;
            font-size: 16px;
        }

        button:hover {
            background-color: #45a049;
        }

    </style>
</head>
<body>
<h2>Edit Product</h2>

<?php
include('../inc/connexion.php');

if (isset($_GET['id'])) {
    $productId = $_GET['id'];

    // Récupérer le produit
    try {
        $connexion = connexion();
        $sql = "SELECT * FROM h_produits WHERE idProduit = :idProduit";
        $stmt = $connexion->prepare($sql);
        $stmt->bindParam(':idProduit', $productId);
        $stmt->execute();
        $produit = $stmt->fetch();

        if ($produit) {
            ?>

            <form action="edit-product.php?id=<?php echo $produit['idProduit']; ?>" method="POST" enctype="multipart/form-data">
                <label for="nom">Product Name:</label>
                <input type="text" name="nom" id="nom" value="<?php echo $produit['nom']; ?>" required><br><br>

                <label for="categorie">Category:</label>
                <select name="categorie" id="categorie" required>
                    <option value="1" <?php if ($produit['idCategorie'] == 1) echo 'selected'; ?>>Men's</option>
                    <option value="2" <?php if ($produit['idCategorie'] == 2) echo 'selected'; ?>>Women's</option>
                    <option value="3" <?php if ($produit['idCategorie'] == 3) echo 'selected'; ?>>Kid's</option>
                </select><br><br>

                <label for="prix">Price:</label>
                <input type="number" name="prix" id="prix" value="<?php echo $produit['prix']; ?>" required><br><br>

                <label for="etoiles">Stars:</label>
                <input type="number" name="etoiles" id="etoiles" value="<?php echo $produit['etoiles']; ?>" min="1" max="5" required><br><br>

                <label for="image[]">Product Images (min. 2 images):</label>
                <input type="file" name="image[]" accept="image/*" required multiple><br><br>

                <button type="submit" name="submit">Update Product</button>
            </form>

            <?php
        } else {
            echo "Product not found.";
        }
    } catch (PDOException $e) {
        echo "Error: " . $e->getMessage();
    }
}

if (isset($_POST['submit'])) {
    $nom = $_POST['nom'];
    $categorie = $_POST['categorie'];
    $prix = $_POST['prix'];
    $etoiles = $_POST['etoiles'];

    if (isset($_FILES['image']) && count($_FILES['image']['name']) >= 2) {
        try {
            // Mise à jour du produit
            $connexion = connexion();
            $sql = "UPDATE h_produits SET nom = :nom, prix = :prix, etoiles = :etoiles, idCategorie = :categorie WHERE idProduit = :idProduit";
            $stmt = $connexion->prepare($sql);
            $stmt->bindParam(':nom', $nom);
            $stmt->bindParam(':prix', $prix);
            $stmt->bindParam(':etoiles', $etoiles);
            $stmt->bindParam(':categorie', $categorie);
            $stmt->bindParam(':idProduit', $productId);
            $stmt->execute();

            $imagesPaths = [];
            foreach ($_FILES['image']['tmp_name'] as $key => $tmpName) {
                $imageName = $_FILES['image']['name'][$key];
                $generalImagePath = 'assets/images/' . basename($imageName);
                $backOfficeImagePath = 'backoffice/assets/images/' . basename($imageName);

                // Déplacer l'image vers le premier dossier (assets/images/)
                if (move_uploaded_file($tmpName, '../' . $generalImagePath)) {
                    // Copier l'image dans le deuxième dossier (backoffice/assets/images/)
                    copy('../' . $generalImagePath, '../' . $backOfficeImagePath);

                    $imagesPaths[] = $generalImagePath;
                }
            }

            // Ajouter les images à la base de données
            foreach ($imagesPaths as $imagePath) {
                $sql = "INSERT INTO h_image (chemin, idproduit) VALUES (:chemin, :idproduit)";
                $stmt = $connexion->prepare($sql);
                $stmt->bindParam(':chemin', $imagePath);
                $stmt->bindParam(':idproduit', $productId);
                $stmt->execute();
            }

            echo "Product updated successfully!";
        } catch (PDOException $e) {
            echo "Error: " . $e->getMessage();
        }
    } else {
        echo "Please upload at least 2 images.";
    }
}
?>

</body>
</html>
