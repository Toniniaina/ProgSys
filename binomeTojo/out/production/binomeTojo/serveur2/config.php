<?php
$host = 'localhost';
$user = 'root';
$password = '';
$dbname = 'mon_projet';

// Connexion à la base de données
$conn = mysqli_connect($host, $user, $password, $dbname);

if (!$conn) {
    die("Erreur de connexion : " . mysqli_connect_error());
}
?>
