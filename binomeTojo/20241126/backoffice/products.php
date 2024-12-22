<?php
include("../inc/fonction.php");
$categories = isset($_GET['category']) ? $_GET['category'] : '';
$prixMin = isset($_GET['prixMin']) ? $_GET['prixMin'] : 0;
$prixMax = isset($_GET['prixMax']) ? $_GET['prixMax'] : 1000;
$etoilesMin = isset($_GET['etoilesMin']) ? $_GET['etoilesMin'] : 1;

$produits = searchProduits($categories, $prixMin, $prixMax, $etoilesMin);

// Suppression d'un produit
if (isset($_GET['id'])) {
    $productId = intval($_GET['id']);
    try {
        $connexion = connexion();
        $sql = "DELETE FROM h_produits WHERE idProduit = :idProduit";
        $stmt = $connexion->prepare($sql);
        $stmt->bindParam(':idProduit', $productId, PDO::PARAM_INT);

        if ($stmt->execute()) {
            header("Location: products.php?message=Produit supprimé avec succès.");
        } else {
            header("Location: products.php?error=Erreur lors de la suppression.");
        }
    } catch (PDOException $e) {
        echo "Erreur : " . $e->getMessage();
    }
}
?>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hexashop - Liste des Produits</title>
    <link rel="stylesheet" href="assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="assets/css/font-awesome.css">
    <link rel="stylesheet" href="assets/css/templatemo-hexashop.css">
    <link rel="stylesheet" href="assets/css/owl-carousel.css">
    <link rel="stylesheet" href="assets/css/lightbox.css">
    <style>

        form .form-control {
            width: 200px; /* Limite la largeur des champs à 200px */
            height: 40px; /* Hauteur uniforme pour tous les champs */
            border-radius: 5px; /* Coins arrondis pour un look moderne */
            font-size: 14px; /* Taille de texte uniforme */
            padding: 0 10px; /* Un peu d'espace à l'intérieur des champs */
        }

        form button {
            height: 40px; /* Assurez-vous que le bouton est de la même hauteur que les champs */
            border-radius: 5px; /* Coins arrondis pour le bouton */
            font-size: 14px; /* Taille uniforme */
            display: flex;
            align-items: center;
            justify-content: center;
            padding-left: 15px;
            padding-right: 15px;
            gap: 5px; /* Espace entre le texte et l'icône si ajoutée */
        }

        form button:hover {
            opacity: 0.8; /* Ajout d'un effet au survol */
        }

        .black-btn {
            background-color: #000;
            color: #fff;
            border: none;
            padding: 5px 10px;
            border-radius: 5px;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 5px; /* Espace entre l'icône et le texte */
            cursor: pointer;
        }

        .black-btn i {
            font-size: 14px;
        }

        .black-btn:hover {
            opacity: 0.8;
        }
    </style>
</head>
<body>

<header class="header-area header-sticky">
    <div class="container">
        <div class="row">
            <div class="col-12">
                <nav class="main-nav">
                    <a href="index.php" class="logo">
                        <img src="assets/images/logo.png" alt="Hexashop Logo">
                    </a>
                    <ul class="nav">
                        <li class="scroll-to-section"><a href="../index.php" class="active">Accueil</a></li>
                        <li class="scroll-to-section"><a href="index.php">Hommes</a></li>
                        <li class="scroll-to-section"><a href="index.php">Femmes</a></li>
                        <li class="scroll-to-section"><a href="index.php">Enfants</a></li>
                        <li class="submenu">
                            <a href="javascript:;">Pages</a>
                            <ul>
                                <li><a href="about.html">À propos de nous</a></li>
                                <li><a href="products.html">Produits</a></li>
                                <li><a href="single-product.php">Produit unique</a></li>
                                <li><a href="contact.html">Contact</a></li>
                            </ul>
                        </li>
                        <li class="submenu">
                            <a href="javascript:;">Caractéristiques</a>
                            <ul>
                                <li><a href="#">Page de caractéristiques 1</a></li>
                                <li><a href="#">Page de caractéristiques 2</a></li>
                                <li><a href="#">Page de caractéristiques 3</a></li>
                                <li><a rel="nofollow" href="https://templatemo.com/page/4" target="_blank">Page de modèle 4</a></li>
                            </ul>
                        </li>
                        <li class="scroll-to-section"><a href="index.php">Explorer</a></li>
                    </ul>
                    <a class='menu-trigger'>
                        <span>Menu</span>
                    </a>
                </nav>
            </div>
        </div>
    </div>
</header>

<div class="page-heading" id="top">
    <div class="container">
        <div class="row">
            <div class="col-lg-12">
                <div class="inner-content">
                    <h2>Vérifiez nos produits</h2>
                    <span>Conception HTML CSS créative et magnifique par TemplateMo</span>
                </div>
            </div>
        </div>
    </div>
</div>

<h5>Recherche de Produits</h5>

<!-- Formulaire de recherche stylisé -->
<form action="products.php" method="GET" class="form-inline justify-content-center mb-4">
    <div class="form-group mr-2">
        <label for="category" class="sr-only">Catégorie:</label>
        <select name="category" id="category" class="form-control">
            <option value="">Toutes</option>
            <?php
            $categories = getCategories();
            foreach ($categories as $categorie) {
                echo "<option value='{$categorie['idCategorie']}'>{$categorie['nomCategorie']}</option>";
            }
            ?>
        </select>
    </div>

    <div class="form-group mr-2">
        <label for="prixMin" class="sr-only">Prix Min:</label>
        <input type="number" name="prixMin" id="prixMin" value="<?= $prixMin ?>" class="form-control" placeholder="Prix Min">
    </div>

    <div class="form-group mr-2">
        <label for="prixMax" class="sr-only">Prix Max:</label>
        <input type="number" name="prixMax" id="prixMax" value="<?= $prixMax ?>" class="form-control" placeholder="Prix Max">
    </div>

    <div class="form-group mr-2">
        <label for="etoilesMin" class="sr-only">Étoiles Min:</label>
        <input type="number" name="etoilesMin" id="etoilesMin" value="<?= $etoilesMin ?>" min="1" max="5" class="form-control" placeholder="Étoiles Min">
    </div>

    <button type="submit" class="btn btn-dark">Rechercher</button>
</form>


<h5>Résultats de Recherche</h5>

<section class="section" id="products">
    <div class="container">
        <div class="row">
            <div class="col-lg-12">
                <div class="section-heading">
                    <h2>Nos derniers produits</h2>
                    <span>Découvrez tous nos produits.</span>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-12">
                <table class="table table-striped table-bordered" style="text-align: center;">
                    <thead class="thead-dark">
                    <tr>
                        <th>Image</th>
                        <th>Nom</th>
                        <th>Prix</th>
                        <th>Étoiles</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <?php if (!empty($produits)): ?>
                        <?php foreach ($produits as $produit): ?>
                            <tr style="height: 120px;">
                                <td>
                                    <img src="<?= $produit['images']; ?>" alt="<?= $produit['nom']; ?>" style="width: 80px; height: 80px; object-fit: cover; border-radius: 5px;">
                                </td>
                                <td><?= htmlspecialchars($produit['nom']); ?></td>
                                <td><?= htmlspecialchars($produit['prix']); ?>€</td>
                                <td>
                                    <ul class="stars" style="list-style: none; padding: 0; margin: 0; display: flex; justify-content: center;">
                                        <?php for ($i = 0; $i < 5; $i++): ?>
                                            <li style="margin-right: 3px;">
                                                <i class="fa <?= $i < $produit['etoiles'] ? 'fa-star' : 'fa-star-o'; ?>" style="color: gold;"></i>
                                            </li>
                                        <?php endfor; ?>
                                    </ul>
                                </td>
                                <td>
                                    <a href="edit-product.php?id=<?= $produit['idProduit']; ?>" class="btn black-btn btn-sm">
                                        <i class="fa fa-pencil"></i> Éditer
                                    </a>
                                    <button onclick="confirmDelete(<?= $produit['idProduit']; ?>)" class="btn black-btn btn-sm">
                                        <i class="fa fa-trash"></i> Supprimer
                                    </button>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                    <?php else: ?>
                        <tr>
                            <td colspan="5">Aucun produit trouvé pour cette recherche.</td>
                        </tr>
                    <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</section>

<!-- ***** Products Area Ends ***** -->

<!-- ***** Footer Start ***** -->
<footer>
    <div class="container">
        <div class="row">
            <div class="col-lg-3">
                <div class="first-item">
                    <div class="logo">
                        <img src="assets/images/white-logo.png" alt="hexashop ecommerce templatemo">
                    </div>
                    <ul>
                        <li><a href="#">16501 Collins Ave, Sunny Isles Beach, FL 33160, United States</a></li>
                        <li><a href="#">hexashop@company.com</a></li>
                        <li><a href="#">010-020-0340</a></li>
                    </ul>
                </div>
            </div>
            <div class="col-lg-3">
                <h4>Shopping &amp; Categories</h4>
                <ul>
                    <li><a href="#">Men’s Shopping</a></li>
                    <li><a href="#">Women’s Shopping</a></li>
                    <li><a href="#">Kid's Shopping</a></li>
                </ul>
            </div>
            <div class="col-lg-3">
                <h4>Useful Links</h4>
                <ul>
                    <li><a href="#">Homepage</a></li>
                    <li><a href="#">About Us</a></li>
                    <li><a href="#">Help</a></li>
                    <li><a href="#">Contact Us</a></li>
                </ul>
            </div>
            <div class="col-lg-3">
                <h4>Help &amp; Information</h4>
                <ul>
                    <li><a href="#">Help</a></li>
                    <li><a href="#">FAQ's</a></li>
                    <li><a href="#">Shipping</a></li>
                    <li><a href="#">Tracking ID</a></li>
                </ul>
            </div>
            <div class="col-lg-12">
                <div class="under-footer">
                    <p>Copyright © 2022 HexaShop Co., Ltd. All Rights Reserved.

                        <br>Design: <a href="https://templatemo.com" target="_parent" title="free css templates">TemplateMo</a></p>
                    <ul>
                        <li><a href="#"><i class="fa fa-facebook"></i></a></li>
                        <li><a href="#"><i class="fa fa-twitter"></i></a></li>
                        <li><a href="#"><i class="fa fa-linkedin"></i></a></li>
                        <li><a href="#"><i class="fa fa-behance"></i></a></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</footer>


<!-- jQuery -->
<script src="assets/js/jquery-2.1.0.min.js"></script>

<!-- Bootstrap -->
<script src="assets/js/popper.js"></script>
<script src="assets/js/bootstrap.min.js"></script>

<!-- Plugins -->
<script src="assets/js/owl-carousel.js"></script>
<script src="assets/js/accordions.js"></script>
<script src="assets/js/datepicker.js"></script>
<script src="assets/js/scrollreveal.min.js"></script>
<script src="assets/js/waypoints.min.js"></script>
<script src="assets/js/jquery.counterup.min.js"></script>
<script src="assets/js/imgfix.min.js"></script>
<script src="assets/js/slick.js"></script>
<script src="assets/js/lightbox.js"></script>
<script src="assets/js/isotope.js"></script>

<!-- Global Init -->
<script src="assets/js/custom.js"></script>
<script>
    function confirmDelete(productId) {
        if (confirm("Êtes-vous sûr de vouloir supprimer ce produit ? Cette action est irréversible.")) {
            // Redirige vers le script PHP pour supprimer le produit
            window.location.href = `products.php?id=${productId}`;
        }
    }
</script>

<script>

    $(function() {
        var selectedClass = "";
        $("p").click(function(){
            selectedClass = $(this).attr("data-rel");
            $("#portfolio").fadeTo(50, 0.1);
            $("#portfolio div").not("."+selectedClass).fadeOut();
            setTimeout(function() {
                $("."+selectedClass).fadeIn();
                $("#portfolio").fadeTo(50, 1);
            }, 500);

        });
    });

</script>

</body>

</html>
