# Document de synthèse — Projet ÉCOLE

**Couche persistance en Java & MySQL** · DEV9 CREA, année 2026-2027 · Encadrant : Taha RIDENE · Auteur : Matis WOSTRY

---

## Sommaire

1. [Sujet](#1-sujet)
2. [Architecture](#2-architecture)
3. [Base de données](#3-base-de-données)
4. [Diagramme de classes](#4-diagramme-de-classes)
5. [Couverture de la spécification fonctionnelle](#5-couverture-de-la-spécification-fonctionnelle)
6. [Codes retour et wrappers](#6-codes-retour-et-wrappers)
7. [Blindage des données](#7-blindage-des-données)
8. [Tests unitaires](#8-tests-unitaires)
9. [Installation et lancement](#9-installation-et-lancement)

---

## 1. Sujet

Créer en Java une application de **gestion d'école** : élèves, chambres, livres, UV et inscriptions.

- Les données sont stockées dans une base **MySQL**, créée à partir d'un script SQL.
- L'application permet de consulter, ajouter, modifier et supprimer ces données.
- Son bon fonctionnement est vérifié par des **tests unitaires**.

---

## 2. Architecture

Ce projet constitue la couche **Model** d'une architecture MVC2 / 3-tiers. Les vues (JSP) et le contrôleur (Servlet) feront l'objet d'un projet « interface » séparé, qui appellera les DAO de ce projet.

```mermaid
flowchart LR
    V["Vues (JSP)<br/><i>projet interface, à venir</i>"]
    C["Contrôleur (Servlet)<br/><i>projet interface, à venir</i>"]
    subgraph M["Model : ce projet"]
        direction TB
        W["Wrappers<br/>objet + code réponse"]
        D["DAO<br/>une classe par table"]
        B["Beans<br/>une classe par table"]
        U["DBAction (connexion JDBC)<br/>Validation (blindage)"]
    end
    DB[("MySQL<br/>ecole / ecole_test")]

    V <--> C
    C <-->|"appels DAO / wrappers"| D
    D --> W
    D --> B
    D --> U
    U <-->|"JDBC"| DB
```

### Organisation du code

| Package | Contenu |
|---|---|
| `com.crea.jee.beans` | `Eleve`, `Chambre`, `Livre`, `Uv`, `Inscrit` : une classe par table |
| `com.crea.jee.dao` | `EleveDao`, `ChambreDao`, `LivreDao`, `UvDao`, `InscritDao` : accès aux données |
| `com.crea.jee.wrappers` | `Wrapper` et `EleveWrapper`, `ChambreWrapper`, `LivreWrapper`, `UvWrapper` : objet + code réponse |
| `com.crea.jee.utils` | `DBAction` (connexion, fournie par l'encadrant), `Validation` (contrôle des données) |
| `com.crea.jee.junit` | Tests unitaires JUnit 5 : un fichier par DAO, `ValidationTest`, et un fichier par onglet de l'interface |
| `com.crea.jee.test` | Tests manuels (`main`) qui affichent les résultats dans la console |
| `com.crea.jee.ihm` | Application de démonstration (Swing) : une fenêtre à onglets qui utilise les DAO |

Toutes les requêtes passent par des `PreparedStatement` : les valeurs saisies ne sont jamais concaténées dans le SQL, ce qui protège contre l'injection SQL.

---

## 3. Base de données

Modèle relationnel de la base `ecole` (script [`data/ecole.sql`](../data/ecole.sql), qui crée et remplit la base). La base de test `ecole_test` ([`data/ecole_test.sql`](../data/ecole_test.sql)) a exactement le même schéma, sans données.

```mermaid
erDiagram
    ELEVE {
        varchar num PK "numéro de l'élève"
        int no FK "chambre occupée, NULL si aucune"
        varchar nom
        tinyint age
        varchar adresse
    }
    CHAMBRE {
        int no PK "numéro de la chambre"
        varchar num FK "occupant, NULL si libre"
        float prix
    }
    LIVRE {
        varchar cote PK
        varchar num FK "emprunteur, NULL si disponible"
        varchar titre
        datetime datepret "date du prêt, NULL si disponible"
    }
    UV {
        varchar code PK
        tinyint nbh "nombre d'heures"
        varchar coord "coordinateur"
    }
    INSCRIT {
        varchar code PK, FK
        varchar num PK, FK
        float note
    }

    CHAMBRE }o--o| ELEVE : "occupée par (chambre.num)"
    ELEVE }o--o| CHAMBRE : "loge dans (eleve.no)"
    LIVRE }o--o| ELEVE : "emprunté par (livre.num)"
    INSCRIT }o--|| ELEVE : "concerne (inscrit.num)"
    INSCRIT }o--|| UV : "porte sur (inscrit.code)"
```

### Points à noter sur les contraintes

- **Clés étrangères `ON UPDATE CASCADE`** : renommer un élève (`updateNumEleve`) met automatiquement à jour `chambre.num`, `livre.num` et `inscrit.num`.
- **Aucune clé étrangère n'a de `ON DELETE`** : MySQL refuse de supprimer une ligne encore référencée. Les suppressions concernées sont donc gérées dans les DAO, par une **transaction** (tout passe ou rien ne passe) :

| Méthode | Ce que fait la transaction avant la suppression |
|---|---|
| `EleveDao.deleteEleveByNum` | libère sa chambre (`num = NULL`), rend ses livres (`num` et `datepret = NULL`), supprime ses inscriptions |
| `ChambreDao.deleteChambreByNo` | détache les élèves rattachés (`eleve.no = NULL`) |
| `UvDao.deleteUvByCode` | supprime les inscriptions à cette UV |

- **`eleve.no` et `chambre.num` décrivent le même lien dans les deux sens.** `ChambreDao.updateOccupantChambre` les met à jour ensemble, dans une transaction : l'ancien occupant de la chambre est détaché, l'ancienne chambre du nouvel occupant est libérée (un élève n'occupe qu'une chambre), puis la chambre et l'élève sont reliés. Si la chambre ou l'élève n'existe pas, rien n'est modifié.

---

## 4. Diagramme de classes

Les getters et setters des beans ne sont pas représentés. Les méthodes soulignées sont statiques.

### Beans et wrappers

```mermaid
classDiagram
    direction LR

    class Eleve {
        -String num
        -int no
        -String nom
        -int age
        -String adresse
        +affiche() void
    }
    class Chambre {
        -int no
        -String num
        -float prix
        +affiche() void
    }
    class Livre {
        -String cote
        -String num
        -String titre
        -Timestamp datepret
        +affiche() void
    }
    class Uv {
        -String code
        -int nbh
        -String coord
        +affiche() void
    }
    class Inscrit {
        -String code
        -String num
        -float note
        +affiche() void
    }

    class Wrapper {
        <<abstract>>
        +int TROUVE$
        +int NON_TROUVE$
        +int ERREUR_BASE$
        +int DONNEES_INVALIDES$
        -int codeResponse
        +getCodeResponse() int
    }
    class EleveWrapper {
        -Eleve eleve
        +getEleve() Eleve
    }
    class ChambreWrapper {
        -Chambre chambre
        +getChambre() Chambre
    }
    class LivreWrapper {
        -Livre livre
        +getLivre() Livre
    }
    class UvWrapper {
        -Uv uv
        +getUv() Uv
    }

    Wrapper <|-- EleveWrapper
    Wrapper <|-- ChambreWrapper
    Wrapper <|-- LivreWrapper
    Wrapper <|-- UvWrapper
    EleveWrapper o-- "0..1" Eleve
    ChambreWrapper o-- "0..1" Chambre
    LivreWrapper o-- "0..1" Livre
    UvWrapper o-- "0..1" Uv
```

### DAO et utilitaires

```mermaid
classDiagram
    direction LR

    class EleveDao {
        +getEleveByNum(String num)$ EleveWrapper
        +getElevesByNom(String nom)$ List~Eleve~
        +getEleveByNo(int no)$ EleveWrapper
        +deleteEleveByNum(String num)$ int
        +updateAdresseEleve(String num, String adresse)$ int
        +updateNumEleve(String ancienNum, String nouveauNum)$ int
        +addEleve(Eleve nouvelEleve)$ int
        +getElevesByAge(int age)$ List~Eleve~
        +getAllEleves()$ List~Eleve~
    }
    class ChambreDao {
        +getChambreByNo(int no)$ ChambreWrapper
        +getChambreByOccupant(String num)$ ChambreWrapper
        +deleteChambreByNo(int no)$ int
        +updateOccupantChambre(int no, String num)$ int
        +updatePrixChambre(int no, float prix)$ int
        +addChambre(Chambre nouvelleChambre)$ int
        +getChambresPrixSuperieur(float prix)$ List~Chambre~
        +getAllChambres()$ List~Chambre~
        +getChambresNonOccupees()$ List~Chambre~
    }
    class LivreDao {
        +getLivreByCote(String cote)$ LivreWrapper
        +getLivresEmpruntesByEleve(String num)$ List~Livre~
        +deleteLivreByCote(String cote)$ int
        +updateEmprunteurLivre(String cote, String num)$ int
        +updateTitreLivre(String cote, String titre)$ int
        +addLivre(Livre nouveauLivre)$ int
        +getLivresDisponibles()$ List~Livre~
        +getAllLivres()$ List~Livre~
    }
    class UvDao {
        +getUvByCode(String code)$ UvWrapper
        +deleteUvByCode(String code)$ int
        +updateNbhUv(String code, int nbh)$ int
        +updateCoordUv(String code, String coord)$ int
        +getAllUvs()$ List~Uv~
        +getUvsNbhSuperieur(int valeur)$ List~Uv~
        +addUv(Uv nouvelleUv)$ int
    }
    class InscritDao {
        +deleteInscription(String code, String num)$ int
        +getAllInscriptions()$ List~Inscrit~
        +updateNoteInscrit(String code, String num, float note)$ int
        +addInscription(Inscrit nouvelleInscription)$ int
    }

    class DBAction {
        +DBConnexion()$ Exception
        +DBClose()$ int
        +getCon()$ Connection
    }
    class Validation {
        +estRenseigne(String valeur)$ boolean
        +longueurValide(String valeur, int longueurMax)$ boolean
        +estValide(String valeur, int longueurMax)$ boolean
        +estPositif(int valeur)$ boolean
        +estPositif(float valeur)$ boolean
        +estDansPlageTinyint(int valeur)$ boolean
    }

    EleveDao ..> DBAction
    ChambreDao ..> DBAction
    LivreDao ..> DBAction
    UvDao ..> DBAction
    InscritDao ..> DBAction
    EleveDao ..> Validation
    ChambreDao ..> Validation
    LivreDao ..> Validation
    UvDao ..> Validation
```

Chaque DAO crée les beans de sa table (méthode privée `mapResultSet`) et, pour la lecture d'un objet unique, le wrapper correspondant.

---

## 5. Couverture de la spécification fonctionnelle

Toutes les fonctionnalités de la spécification sont implémentées, y compris les parties optionnelles (UV et Inscrit). Deux ajouts **en plus de la spécification**, marqués *(en plus)*, complètent les UV et les inscriptions pour l'application de démonstration.

### Élève (indispensable)

| Spécification | Méthode |
|---|---|
| Récupérer un élève par son numéro | `EleveDao.getEleveByNum` |
| Récupérer un élève par son nom | `EleveDao.getElevesByNom` (liste : plusieurs élèves peuvent porter le même nom) |
| Récupérer un élève par son numéro de chambre | `EleveDao.getEleveByNo` |
| Supprimer un élève par son numéro | `EleveDao.deleteEleveByNum` |
| Mettre à jour l'adresse d'un élève | `EleveDao.updateAdresseEleve` |
| Mettre à jour le numéro d'un élève | `EleveDao.updateNumEleve` |
| Ajouter un nouvel élève | `EleveDao.addEleve` |
| Récupérer les élèves ayant le même âge | `EleveDao.getElevesByAge` |
| Récupérer la liste de tous les élèves | `EleveDao.getAllEleves` |

### Chambre (indispensable)

| Spécification | Méthode |
|---|---|
| Récupérer une chambre par son numéro | `ChambreDao.getChambreByNo` |
| Récupérer une chambre par son occupant | `ChambreDao.getChambreByOccupant` |
| Supprimer une chambre par son numéro | `ChambreDao.deleteChambreByNo` |
| Mettre à jour l'occupant d'une chambre | `ChambreDao.updateOccupantChambre` |
| Mettre à jour le prix d'une chambre | `ChambreDao.updatePrixChambre` |
| Ajouter une nouvelle chambre | `ChambreDao.addChambre` |
| Récupérer les chambres au-dessus d'un prix | `ChambreDao.getChambresPrixSuperieur` |
| Récupérer la liste de toutes les chambres | `ChambreDao.getAllChambres` |
| Récupérer la liste des chambres non occupées | `ChambreDao.getChambresNonOccupees` |

### Livre (indispensable)

| Spécification | Méthode |
|---|---|
| Récupérer un livre par sa cote | `LivreDao.getLivreByCote` |
| Récupérer les livres prêtés à un élève | `LivreDao.getLivresEmpruntesByEleve` |
| Supprimer un livre par sa cote | `LivreDao.deleteLivreByCote` |
| Mettre à jour l'emprunteur d'un livre | `LivreDao.updateEmprunteurLivre` (renseigne aussi la date de prêt, ou la remet à NULL au retour) |
| Mettre à jour le titre d'un livre | `LivreDao.updateTitreLivre` |
| Ajouter un nouveau livre | `LivreDao.addLivre` |
| Récupérer les livres disponibles | `LivreDao.getLivresDisponibles` |
| Récupérer la liste de tous les livres | `LivreDao.getAllLivres` |

### UV (optionnel)

| Spécification | Méthode |
|---|---|
| Récupérer une UV par son code | `UvDao.getUvByCode` |
| Supprimer une UV par son code | `UvDao.deleteUvByCode` |
| Mettre à jour le nombre d'heures d'une UV | `UvDao.updateNbhUv` |
| Mettre à jour le coordinateur d'une UV | `UvDao.updateCoordUv` |
| Récupérer la liste de toutes les UV | `UvDao.getAllUvs` |
| Récupérer les UV au-dessus d'un nombre d'heures | `UvDao.getUvsNbhSuperieur` |
| *(en plus)* Ajouter une UV | `UvDao.addUv` |

### Inscrit (optionnel)

| Spécification | Méthode |
|---|---|
| Supprimer l'inscription d'un élève à une UV | `InscritDao.deleteInscription` |
| Récupérer toutes les inscriptions | `InscritDao.getAllInscriptions` |
| Mettre à jour la note d'un élève à une UV | `InscritDao.updateNoteInscrit` |
| *(en plus)* Inscrire un élève à une UV | `InscritDao.addInscription` (l'UV et l'élève doivent exister, sinon code `-1`) |

---

## 6. Codes retour et wrappers

### Le problème

Une méthode qui renvoie directement un `Eleve` ne peut exprimer que deux situations : l'élève, ou `null`. Or `null` peut vouloir dire « élève inexistant » comme « la base ne répond pas ». Le contrôleur ne peut donc pas savoir quoi afficher à l'utilisateur.

### La solution : les wrappers

Les méthodes qui lisent **un seul objet** renvoient un **wrapper**, qui enveloppe l'objet avec un **code réponse** :

| Code | Constante | Signification | Objet contenu |
|---|---|---|---|
| `1` | `Wrapper.TROUVE` | l'objet a été trouvé | l'objet |
| `0` | `Wrapper.NON_TROUVE` | aucun objet ne correspond | `null` |
| `-1` | `Wrapper.ERREUR_BASE` | erreur côté serveur (connexion impossible ou requête en échec) | `null` |
| `-3` | `Wrapper.DONNEES_INVALIDES` | paramètre invalide (vide, numéro à 0…), refusé sans interroger la base | `null` |

### Écritures (ajout, mise à jour, suppression)

Elles renvoient un `int` :

| Valeur | Signification |
|---|---|
| `1` (ou plus) | nombre de lignes modifiées |
| `0` | aucune ligne concernée (objet non trouvé) |
| `-1` | erreur côté base (connexion impossible ou erreur SQL) |
| `-2` | clé déjà existante (ajout ou renommage vers une clé déjà prise) |
| `-3` | données invalides, refusées avant tout accès à la base |

Le cours associe `-1`, `-2` et `-3` à des erreurs « 501 », « 505 » et « 404 ». Ce projet leur donne un sens précis lié aux cas réellement rencontrés, en gardant les mêmes valeurs. Le code `-2` n'existe pas en lecture, puisqu'une lecture ne peut pas créer de doublon.

### Lectures de listes

Elles renvoient une **liste vide** si rien ne correspond ou en cas d'erreur côté base (jamais `null`).

Dans tous les cas, une base injoignable ne fait jamais planter l'application : chaque méthode vérifie la connexion avant d'envoyer sa requête.

---

## 7. Blindage des données

La classe `Validation` contrôle les données **avant** tout accès à la base. Une donnée refusée renvoie le code `-3` sans ouvrir de connexion.

| Contrôle | Règle |
|---|---|
| `estRenseigne` | chaîne non nulle et non vide (les espaces seuls sont refusés) |
| `longueurValide` | longueur inférieure ou égale à la taille de la colonne `varchar` |
| `estValide` | `estRenseigne` et `longueurValide` à la fois |
| `estPositif` | nombre strictement positif (entier ou décimal) |
| `estDansPlageTinyint` | valeur comprise entre -128 et 127 (colonnes `tinyint`) |

Règles appliquées par les DAO :

| Donnée | Règle |
|---|---|
| Numéro d'élève, cote, code d'UV | renseigné, 100 caractères maximum |
| Nom d'élève | renseigné, 50 caractères maximum |
| Adresse | 200 caractères maximum (renseignée lors d'une mise à jour) |
| Âge, nombre d'heures | strictement positif, au plus 127 (`tinyint`) |
| Numéro et prix de chambre | strictement positifs |
| Titre de livre | renseigné, 100 caractères maximum |
| Coordinateur d'UV | 255 caractères maximum |
| Inscription | code d'UV et numéro d'élève renseignés, note numérique |

---

## 8. Tests unitaires

### Une base de test séparée

Les tests vident et remplissent les tables à chaque exécution. Ils tournent donc sur une base dédiée, pour que les vraies données ne soient jamais modifiées et que chaque test parte toujours d'une base connue :

| Base | Usage | Sélection |
|---|---|---|
| `ecole` | données de démonstration, utilisée par l'application | par défaut |
| `ecole_test` | même schéma, vide, réservée aux tests | `-Ddb.name=ecole_test` au lancement de la JVM |

Par sécurité, les tests **refusent de démarrer** si `-Ddb.name=ecole_test` n'est pas passé, pour ne jamais vider la vraie base par erreur.

### Une pile de test indépendante des données

Chaque test suit le schéma `@BeforeEach` → test → `@AfterEach` (équivalent JUnit 5 de `@Before` / `@After`) :

- **avant** : les tables sont vidées, et le test insère lui-même les données dont il a besoin ;
- **après** : les tables sont de nouveau vidées, pour ne rien laisser derrière soi.

Les tests ne dépendent donc ni du contenu initial de la base, ni de leur ordre d'exécution.

### Contenu

Chaque test porte une description qui commence par **[OK]** (cas valide, l'opération doit réussir) ou **[ERREUR]** (cas invalide : le DAO doit refuser ou ne rien trouver).

| Classe de test | Tests | Couverture |
|---|---|---|
| `EleveDaoTest` | 22 | toutes les méthodes, suppression en cascade, codes du wrapper |
| `ChambreDaoTest` | 25 | toutes les méthodes, synchronisation chambre ↔ élève, détachement de l'élève à la suppression, codes du wrapper |
| `LivreDaoTest` | 17 | toutes les méthodes, date de prêt à l'emprunt et au retour, codes du wrapper |
| `UvDaoTest` | 17 | toutes les méthodes (dont l'ajout), suppression des inscriptions liées, codes du wrapper |
| `InscritDaoTest` | 11 | toutes les méthodes (dont l'ajout), élève ou UV inexistant |
| `ValidationTest` | 12 | chaque contrôle, sans base de données |
| **Sous-total couche persistance** | **104** | |
| `OngletElevesTest` | 9 | ajout, doublon, saisies invalides, recherche, suppression annulée puis confirmée |
| `OngletChambresTest` | 13 | ajout, recherche, attribuer et libérer, modifier le prix, filtres, suppression |
| `OngletLivresTest` | 10 | ajout, recherche, prêt et retour, modification du titre, filtres, suppression |
| `OngletUvTest` | 7 | ajout, recherche, modification des heures et du coordinateur, filtre, suppression |
| `OngletInscriptionsTest` | 7 | inscription, doublon, élève ou UV inexistant, modification de la note, suppression |
| `FenetreEcoleTest` | 5 | onglets présents, suppressions et attributions de chambre répercutées d'un onglet à l'autre |
| **Sous-total interface** | **51** | |
| **Total** | **155** | **155 réussis** |

### Tests de l'interface

Les tests `Onglet…Test` et `FenetreEcoleTest` ouvrent la vraie fenêtre (hors de l'écran) et agissent comme un utilisateur : ils remplissent les champs, déclenchent les boutons, répondent « Oui » ou « Non » aux confirmations, puis vérifient le message de la barre d'état, le contenu du tableau et la ligne sélectionnée.

- Les onglets et leurs actions ne sont pas publics : les tests y accèdent par réflexion (classe `OutilsIhm`), sans rien modifier dans le code de l'interface.
- Ils ont besoin d'un écran : sur une machine sans affichage (serveur d'intégration continue), ils sont automatiquement ignorés au lieu d'échouer.
- Ils sont plus lents que les tests des DAO : la suite complète prend un peu plus d'une minute.

---

## 9. Installation et lancement

### Prérequis

- JDK 11 ou plus récent (développé et testé avec OpenJDK 25)
- Docker Desktop (pour MySQL) ou un MySQL local sur le port 8889

Les bibliothèques sont fournies dans [`lib/`](../lib) : le driver `mysql-connector-j` et `junit-platform-console-standalone`.

### Démarrer la base

```bash
docker compose up -d
```

Ce fichier [`docker-compose.yml`](../docker-compose.yml) démarre :
- **MySQL 8.4** sur le port **8889**, avec les bases `ecole` et `ecole_test` créées automatiquement à partir des scripts SQL ;
- **phpMyAdmin** sur http://localhost:8091 (utilisateur `root`, sans mot de passe).

### Ouvrir le projet

- **Eclipse** : *File > Import > General > Existing Projects into Workspace*, puis choisir ce dossier. Les fichiers `.project` et `.classpath` déclarent déjà les bibliothèques de `lib/`, et `.settings/` impose l'encodage UTF-8.
- **IntelliJ IDEA** : ouvrir le dossier, le fichier `GestionEcole.iml` déclare déjà les bibliothèques de `lib/`.

### Lancer les tests

- **IntelliJ / Eclipse** : clic droit sur le package `com.crea.jee.junit` puis *Run*, en ajoutant `-Ddb.name=ecole_test` dans les *VM options*.
- **Ligne de commande** (Git Bash sous Windows ; sous Linux ou macOS, remplacer `;` par `:` dans le classpath) :

```bash
javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name "*.java")
```

```bash
java -Ddb.name=ecole_test -cp "lib/*;out" org.junit.platform.console.ConsoleLauncher execute --select-package=com.crea.jee.junit --details=tree
```

### Lancer l'application de démonstration

L'application se lance **uniquement depuis la classe `com.crea.jee.ihm.FenetreEcole`** : clic droit sur `FenetreEcole.java`, puis *Run 'FenetreEcole.main()'*. C'est la seule classe du package à contenir un `main` ; les classes `Onglet…` sont des parties de la fenêtre et ne se lancent pas seules.

Une fenêtre s'ouvre, avec un onglet par table : **Élèves, Chambres, Livres, UV, Inscriptions**. Chaque onglet permet de rechercher, filtrer, ajouter, modifier et supprimer, et affiche dans sa barre d'état le message et le code retour du DAO.

Elle utilise la base `ecole` par défaut ; ajouter `-Ddb.name=ecole_test` dans les *VM options* pour travailler sur la base de test.

Cette application en Swing (inclus dans le JDK, rien à installer) sert à montrer la couche persistance en action. Ce n'est pas le front MVC2 du cours (Servlet + JSP), qui fera l'objet d'un projet séparé.
