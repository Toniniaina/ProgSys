<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Modify Product</title>
    <style>

        @keyframes fadeIn {
            0% {
                opacity: 0;
                transform: translateY(-20px);
            }
            100% {
                opacity: 1;
                transform: translateY(0);
            }
        }
        .product-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr); /* 4 colonnes */
            gap: 20px; /* Espacement entre les produits */
            animation: fadeIn 1s ease-out;
        }

        .product-card {
            border: 1px solid #ddd;
            padding: 20px;
            text-align: center;
            border-radius: 8px;
            background-color: #f9f9f9;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;
        }

        .product-card:hover {
            transform: scale(1.05);
        }

        .product-card img {
            max-width: 100%;
            height: auto;
            border-radius: 8px;
        }

        .product-card h3 {
            font-size: 18px;
            color: #333;
        }

        .product-card p {
            color: #666;
        }

        /* Style des liens transformés en boutons */
        .action-btn {
            display: inline-block;
            padding: 10px 15px;
            border-radius: 4px;
            text-decoration: none;
            font-size: 16px;
            text-align: center;
            width: 100%;
            margin-top: 10px;
        }

        .blue-btn {
            background-color: #007BFF;
            color: white;
        }

        .blue-btn:hover {
            background-color: #0056b3;
        }

        .red-btn {
            background-color: #dc3545;
            color: white;
        }

        .red-btn:hover {
            background-color: #c82333;
        }

    </style>
</head>
<body>
<h2>Modify Product</h2>
<?php
include('../inc/connexion.php');
try {
    $connexion = connexion();
    $sql = "SELECT * FROM h_produits";
    $stmt = $connexion->prepare($sql);
    $stmt->execute();
    $produits = $stmt->fetchAll();

    if ($produits) {
        echo "<div class='product-grid'>";

        foreach ($produits as $index => $produit) {
            $buttonClass = ($index % 2 == 0) ? 'blue-btn' : 'red-btn';
            echo "<div class='product-card'>
                    <img src='../" . $produit['image'] . "' alt='" . $produit['nom'] . "'>
                    <h3>" . $produit['nom'] . "</h3>
                    <p>Price: $" . $produit['prix'] . "</p>
                    <p>Stars: " . $produit['etoiles'] . "</p>
                    <p>Category: " . $produit['idCategorie'] . "</p>
                    <a href='edit-product.php?id=" . $produit['idProduit'] . "' class='action-btn $buttonClass'>Edit</a>
                  </div>";
        }

        echo "</div>";
    } else {
        echo "No products found.";
    }
} catch (PDOException $e) {
    echo "Error: " . $e->getMessage();
}
?>
</body>
</html>
