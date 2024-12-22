<?php
// Définir le type de contenu pour la réponse
header("Content-Type: text/html; charset=UTF-8");

// Traitement des données GET
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    echo "<h1>Réponse GET</h1>";
    if (!empty($_GET['name']) && !empty($_GET['age'])) {
        echo "<p>Nom : " . htmlspecialchars($_GET['name']) . "</p>";
        echo "<p>Âge : " . htmlspecialchars($_GET['age']) . "</p>";
    } else {
        echo "<p>Aucune donnée GET reçue.</p>";
    }
}

// Traitement des données POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    echo "<h1>Réponse POST</h1>";
    if (!empty($_POST['name']) && !empty($_POST['age'])) {
        echo "<p>Nom : " . htmlspecialchars($_POST['name']) . "</p>";
        echo "<p>Âge : " . htmlspecialchars($_POST['age']) . "</p>";
    } else {
        echo "<p>Aucune donnée POST reçue.</p>";
    }
}
?>
