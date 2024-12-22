<?php
include ('../inc/fonction.php');

$categories = isset($_GET['category']) ? $_GET['category'] : '';
$prixMin = isset($_GET['prixMin']) ? $_GET['prixMin'] : 0;
$prixMax = isset($_GET['prixMax']) ? $_GET['prixMax'] : 1000;
$etoilesMin = isset($_GET['etoilesMin']) ? $_GET['etoilesMin'] : 1;

$produits = searchProduits($categories, $prixMin, $prixMax, $etoilesMin);
?>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recherche Produits</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>

<h1>Recherche de Produits</h1>

<form action="recherche.php" method="GET">
    <label for="category">Catégorie:</label>
    <select name="category" id="category">
        <option value="">Toutes</option>
        <?php
        $categories = getCategories();
        foreach ($categories as $categorie) {
            echo "<option value='{$categorie['idCategorie']}'>{$categorie['nomCategorie']}</option>";
        }
        ?>
    </select><br>

    <label for="prixMin">Prix Min:</label>
    <input type="number" name="prixMin" id="prixMin" value="<?= $prixMin ?>"><br>

    <label for="prixMax">Prix Max:</label>
    <input type="number" name="prixMax" id="prixMax" value="<?= $prixMax ?>"><br>

    <label for="etoilesMin">Étoiles Min:</label>
    <input type="number" name="etoilesMin" id="etoilesMin" value="<?= $etoilesMin ?>" min="1" max="5"><br>

    <button type="submit">Rechercher</button>
</form>

<h2>Résultats de Recherche</h2>
<div id="loading" style="display:none;">Chargement...</div>

<?php if (!empty($produits)): ?>
    <ul>
        <?php foreach ($produits as $produit): ?>
            <li>
                <img src="<?= $produit['image'] ?>" alt="<?= $produit['nom'] ?>" width="100">
                <h3><?= $produit['nom'] ?></h3>
                <p>Prix: <?= $produit['prix'] ?>€</p>
                <p>Étoiles: <?= $produit['etoiles'] ?></p>
            </li>
        <?php endforeach; ?>
    </ul>
<?php else: ?>
    <p>Aucun produit trouvé.</p>
<?php endif; ?>

</body>
</html>
