<?php
// Affichage de l'heure et de la date actuelles
echo "<h1>Test PHP sur ton serveur</h1>";
echo "<p>La date et l'heure actuelles sont : " . date("Y-m-d H:i:s") . "</p>";

// Affichage d'une variable pour tester l'exécution
$nom = "Ton nom";
echo "<p>Bonjour, " . htmlspecialchars($nom) . " !</p>";

// Test d'un calcul simple
$nombre1 = 5;
$nombre2 = 7;
$addition = $nombre1 + $nombre2;
echo "<p>Le résultat de $nombre1 + $nombre2 est : $addition</p>";

// Test d'un tableau PHP
$tableau = array("pomme", "banane", "cerise");
echo "<p>Liste des fruits : ";
foreach ($tableau as $fruit) {
    echo htmlspecialchars($fruit) . " ";
}
echo "</p>";

// Test de l'affichage d'une variable superglobale
echo "<p>Voici la variable globale \$_SERVER['SERVER_NAME'] : " . $_SERVER['SERVER_NAME'] . "</p>";
?>
