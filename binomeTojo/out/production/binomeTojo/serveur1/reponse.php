<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Réponse</title>
</head>
<body>
<h1>Résultat du formulaire</h1>

<?php

if (isset($_POST['nom'])) {
    var_dump($_POST);
    $nom = htmlspecialchars($_POST['nom']);
    echo "<p>Bienvenue, $nom !</p>";
} else {
    echo "<p>Aucun nom n'a été fourni.</p>";
}
?>

<a href="index.php">Retourner au formulaire</a>
</body>
</html>
