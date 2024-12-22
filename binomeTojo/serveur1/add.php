<?php
// Connexion à la base de données
$conn = mysqli_connect("localhost", "root", "", "mon_projet");
if ($conn) {
    echo "rrrrrrrrrrrrrr";
}

if (!$conn) {
    die("Erreur de connexion : " . mysqli_connect_error());
}

// Traitement du formulaire
if (isset($_POST['nom']) && isset($_POST['prix'])) {
    $nom = $_POST['nom'];
    $prix = $_POST['prix'] ;

    // Validation simple
    if (!empty($nom) && is_numeric($prix) && $prix > 0) {
        $nom = mysqli_real_escape_string($conn, $nom);
        $prix = (float)$prix;

        $query = "INSERT INTO produits (nom, prix) VALUES ('$nom', $prix)";
        if (mysqli_query($conn, $query)) {
            header("Location: index.php");
            exit;
        } else {
            $erreur = "Erreur lors de l'ajout : " . mysqli_error($conn);
        }
    } else {
        $erreur = "Veuillez fournir un nom valide et un prix positif.";
    }
}
?>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Ajouter un produit</title>
</head>
<body>
<h1>Ajouter un produit</h1>
<?php if (isset($erreur)): ?>
    <p style="color: red;"><?= htmlspecialchars($erreur) ?></p>
<?php endif; ?>
<form method="post">
    <label for="nom">Nom du produit :</label>
    <input type="text" id="nom" name="nom" required><br><br>

    <label for="prix">Prix :</label>
    <input type="number" step="0.01" id="prix" name="prix" required><br><br>

    <button type="submit">Ajouter</button>
</form>

<a href="index.php">Retour à la liste</a>
</body>
</html>

<?php
// Fermeture de la connexion
mysqli_close($conn);
?>
