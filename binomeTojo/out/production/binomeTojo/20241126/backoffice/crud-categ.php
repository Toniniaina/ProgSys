<?php
include ('../inc/fonction.php');

if (isset($_POST['ajouter'])) {
    ajouterCategorie($_POST['nomCategorie']);
}

if (isset($_POST['modifier'])) {
    modifierCategorie($_POST['idCategorie'], $_POST['nomCategorie']);
}

if (isset($_GET['supprimer'])) {
    supprimerCategorie($_GET['supprimer']);
}

$categories = obtenirCategories();
?>

    <style>

        h1 {
            text-align: center;
            color: white;
        }
        form {
            text-align: center;
            margin-bottom: 20px;
        }
        input[type="text"] {
            padding: 10px;
            margin: 5px;
            border: 1px solid #ccc;
            border-radius: 5px;
            width: 200px;
        }
        button {
            padding: 10px 20px;
            background-color: #333;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        button:hover {
            background-color: #555;
        }
        table {
            width: 80%;
            margin: 20px auto;
            border-collapse: collapse;
        }
        th, td {
            padding: 10px;
            text-align: left;
            border: 1px solid #ccc;
        }
        th {
            background-color: #333;
            color: white;
        }
        tr:nth-child(even) {
            background-color: #444;
        }
        a {
            color: #fff;
            text-decoration: none;
            background-color: red;
            padding: 5px 10px;
            border-radius: 3px;
        }
        a:hover {
            background-color: darkred;
        }
    </style>
</head>
<body>
<h1>Gestion des catégories</h1>

<form method="POST">
    <input type="text" name="nomCategorie" placeholder="Nom de la catégorie" required>
    <button type="submit" name="ajouter">Ajouter</button>
</form>

<table>
    <thead>
    <tr>
        <th>ID</th>
        <th>Nom de la catégorie</th>
        <th>Actions</th>
    </tr>
    </thead>
    <tbody>
    <?php foreach ($categories as $categorie) : ?>
        <tr>
            <td><?php echo $categorie['idCategorie']; ?></td>
            <td><?php echo $categorie['nomCategorie']; ?></td>
            <td>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="idCategorie" value="<?php echo $categorie['idCategorie']; ?>">
                    <input type="text" name="nomCategorie" value="<?php echo $categorie['nomCategorie']; ?>" required>
                    <button type="submit" name="modifier">Modifier</button>
                </form>
                <a href="?supprimer=<?php echo $categorie['idCategorie']; ?>" onclick="return confirm('Êtes-vous sûr ?')">Supprimer</a>
            </td>
        </tr>
    <?php endforeach; ?>
    </tbody>
</table>

