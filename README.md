# Projet ÉCOLE : couche persistance Java & MySQL

Couche **Model** (persistance) d'une application de gestion d'école : beans, DAO JDBC, wrappers et tests unitaires JUnit, sur une base MySQL.

DEV9 CREA, année 2026-2027 · Encadrant : Taha RIDENE

**Document de synthèse complet (sujet, diagramme de BD, diagramme de classes, tests) : [`docs/`](docs/README.md)**

## Démarrage rapide

1. Démarrer MySQL (bases `ecole` et `ecole_test`) et phpMyAdmin :

   ```bash
   docker compose up -d
   ```

2. Ouvrir le projet :
   - **Eclipse** : *File > Import > General > Existing Projects into Workspace*, puis choisir ce dossier ;
   - **IntelliJ IDEA** : ouvrir directement ce dossier.

   Dans les deux cas, les bibliothèques de `lib/` sont déjà déclarées.

3. Lancer les tests : clic droit sur le package `com.crea.jee.junit` puis *Run*, avec `-Ddb.name=ecole_test` dans les *VM options*.

## Choix volontaires

- **Le projet contient à la fois la configuration Eclipse et IntelliJ.** Le projet a été développé sous IntelliJ IDEA (`.idea/`, `GestionEcole.iml`), mais le sujet demande un projet Eclipse. Les fichiers `.project`, `.classpath` et `.settings/` sont donc fournis exprès, pour qu'il s'importe sous Eclipse sans aucune configuration. Ils imposent aussi l'encodage UTF-8, sans lequel Eclipse sous Windows afficherait mal les accents des sources.
- **Une base injoignable ne fait jamais planter l'application.** Comme le prévoit le cours (« erreur côté serveur »), les DAO renvoient `-1` pour une écriture, une liste vide pour une lecture de liste, et un wrapper avec le code `-1` pour la lecture d'un objet seul. Le détail des codes est dans le [document de synthèse](docs/README.md#6-codes-retour-et-wrappers).

## Contenu

| Dossier / fichier | Rôle |
|---|---|
| `src/com/crea/jee/beans` | Une classe par table |
| `src/com/crea/jee/dao` | Accès aux données (spécification fonctionnelle) |
| `src/com/crea/jee/wrappers` | Objet renvoyé + code réponse |
| `src/com/crea/jee/utils` | Connexion à la base, contrôle des données |
| `src/com/crea/jee/junit` | Tests unitaires JUnit 5 |
| `src/com/crea/jee/test` | Tests manuels (affichage console) |
| `docker-compose.yml` | MySQL 8.4 et phpMyAdmin |
| `lib/` | Driver MySQL et JUnit |
| `data/` | Scripts SQL de création et de remplissage des bases (`ecole.sql`, `ecole_test.sql`) |
| `docs/` | Document de synthèse |
