<?php
session_start();
include ('../inc/fonction.php');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $login = $_POST['login'];
    $motDePasse = $_POST['mot_de_passe'];

    if (validerLogin($login, $motDePasse)) {
        $_SESSION['user'] = $login;
        header('Location: index1.php');
        exit();
    } else {
        $messageErreur = 'Nom d\'utilisateur ou mot de passe incorrect.';
    }
}
?>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connexion</title>
    <link rel="stylesheet" href="style.css">
</head>
<style>
    /* Global styles */
    body {
        font-family: Arial, sans-serif;
        background-color: #f4f4f9;
        margin: 0;
        padding: 0;
    }

    h1 {
        text-align: center;
        color: #333;
        margin-top: 50px;
    }

    /* Form container */
    form {
        max-width: 400px;
        margin: 0 auto;
        background-color: #ffffff;
        padding: 20px;
        border-radius: 8px;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        margin-top: 20px;
    }

    /* Labels and inputs */
    label {
        font-size: 1rem;
        color: #333;
        margin-bottom: 8px;
        display: block;
    }

    input[type="text"], input[type="password"] {
        width: 100%;
        padding: 10px;
        margin: 8px 0;
        border: 1px solid #ccc;
        border-radius: 4px;
        font-size: 1rem;
    }

    input[type="text"]:focus, input[type="password"]:focus {
        border-color: #007BFF;
        outline: none;
    }

    /* Error message */
    p {
        font-size: 0.9rem;
        color: red;
        margin-top: 10px;
    }

    /* Button styling */
    button {
        width: 100%;
        padding: 10px;
        background-color: #0e0e0e;
        color: #fff;
        border: none;
        border-radius: 4px;
        font-size: 1rem;
        cursor: pointer;
    }

    button:hover {
        background-color: #090909;
    }

    /* Responsive design */
    @media (max-width: 600px) {
        form {
            padding: 15px;
        }

        button {
            font-size: 0.9rem;
        }
    }

</style>
<body>

<h1>Page de Connexion</h1>

<form action="index.php" method="POST">
    <label for="login">Login:</label>
    <input type="text" name="login" id="login" required value="admin"><br>

    <label for="mot_de_passe">Mot de passe:</label>
    <input type="password" name="mot_de_passe" id="mot_de_passe" required value="adminpassword"><br>

    <?php if (isset($messageErreur)): ?>
        <p style="color:red;"><?= $messageErreur ?></p>
    <?php endif; ?>

    <button type="submit">Se connecter</button>
</form>

</body>
</html>
