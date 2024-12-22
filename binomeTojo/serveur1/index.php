<?php
// Connexion à la base de données
$conn=mysqli_connect("localhost","root","","mon_projet");
if(!$conn){die("Erreur de connexion : ".mysqli_connect_error());}

// Récupération des produits
$result=mysqli_query($conn,"SELECT * FROM produits");
?>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Liste des produits</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<h1>Liste des produits</h1>
<table>
    <thead>
    <tr>
        <th>ID</th>
        <th>Nom</th>
        <th>Prix (€)</th>
    </tr>
    </thead>
    <tbody>
    <?php while($row=mysqli_fetch_assoc($result)): ?>
        <tr>
            <td><?=htmlspecialchars($row['id'])?></td>
            <td><?=htmlspecialchars($row['nom'])?></td>
            <td><?=htmlspecialchars($row['prix'])?></td>
        </tr>
    <?php endwhile; ?>
    </tbody>
</table>

<a href="add.php">Ajouter un produit</a>
</body>
</html>

<?php
// Fermeture de la connexion
mysqli_close($conn);
?>
