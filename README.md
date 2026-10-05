![CI](https://github.com/<OWNER>/<REPO>/actions/workflows/ci.yml/badge.svg)

# Distress Signal

Résolution d'[Advent of Code 2022, jour 13](https://adventofcode.com/2022/day/13) : comparaison de paquets imbriqués (entiers et listes) pour déterminer leur ordre. Partie 1 : somme des indices des paires déjà dans le bon ordre. Partie 2 : tri de tous les paquets avec deux diviseurs ajoutés (`[[2]]` et `[[6]]`), puis produit de leurs positions finales.

## Prérequis

Java 21 uniquement. Le Maven Wrapper (`mvnw` / `mvnw.cmd`) télécharge et utilise la version de Maven requise, aucune installation de Maven n'est nécessaire.

## Commandes

```bash
# Lancer les tests
./mvnw test

# Formater le code (Google Java Format via Spotless)
./mvnw spotless:apply

# Vérifier le formatage, la couverture et exécuter la suite complète
./mvnw verify

# Lancer le programme sur un fichier d'entrée
java -jar target/distress-signal-1.0.0.jar chemin/vers/input.txt
```

## Architecture

Architecture hexagonale légère : le domaine ne dépend de rien, les adaptateurs dépendent du domaine via les ports.

```
                        ┌─────────────────────┐
  (driving adapter)     │         cli          │
                        │  Main (entry point)  │
                        └──────────┬───────────┘
                                   │ appelle
                        ┌──────────▼───────────┐
                        │     application       │
                        │  use cases + ports     │
                        └──────────┬───────────┘
                                   │ dépend de
                        ┌──────────▼───────────┐
                        │        domain         │
                        │ Packet, PacketPair     │
                        │  (aucune dépendance)   │
                        └──────────▲───────────┘
                                   │ implémente le port
                        ┌──────────┴───────────┐
  (driven adapter)      │    infrastructure     │
                        │ PacketParser, lecture  │
                        │      de fichier        │
                        └───────────────────────┘
```

- `domain` : les types métier (`Packet` scellé, `IntegerPacket`, `ListPacket`, `PacketPair`) et leur logique de comparaison. Aucune dépendance vers les autres packages.
- `application` : les ports (interfaces) dont le domaine ou les use cases ont besoin, et les use cases eux-mêmes (partie 1 : somme des indices des paires ordonnées ; partie 2 : clé de décodage). Dépend uniquement du domaine.
- `infrastructure` : les adaptateurs pilotés (driven adapters) — `PacketParser` (parseur descente récursive) et la lecture du fichier d'entrée. Implémente les ports définis par `application`.
- `cli` : l'adaptateur pilotant (driving adapter) et la racine de composition (`Main`), qui assemble les adaptateurs et les use cases.

Règle de dépendance : les flèches pointent toujours vers le centre (`domain`). `domain` ne connaît ni `application`, ni `infrastructure`, ni `cli`. `infrastructure` ne connaît que les ports exposés par `application`, jamais l'inverse.

## Décisions de conception

- **Types scellés et `Comparable`** : `Packet` modélise la variance entier/liste via une hiérarchie scellée (`IntegerPacket`, `ListPacket`), ce qui force à traiter tous les cas à la compilation plutôt que de retomber sur une structure générique (`Object`, `Map`) typée dynamiquement.
- **Parseur maison à descente récursive** : plutôt qu'un `eval` (risque d'exécution de code arbitraire) ou une librairie JSON (dépendance externe pour un format trivial et pour garder le contrôle fin sur les messages d'erreur et les positions), le parseur est écrit à la main.
- **Emplacement du parseur dans `infrastructure`** : le parseur transforme une représentation texte externe (le fichier d'entrée) en objets du domaine ; c'est la responsabilité d'un adaptateur piloté, pas du domaine.
- **Paquets identiques** : deux paquets strictement identiques sont considérés comme étant dans le bon ordre (comparaison `<= 0`), hypothèse assumée en l'absence de cas de ce type dans l'énoncé original.
- **Gestion des erreurs** : les erreurs de parsing lèvent une exception métier dédiée (`InvalidPacketException`) avec un message qui localise le problème (position dans la chaîne, caractère attendu vs. trouvé) plutôt qu'une exception générique du JDK.

## Stratégie de tests

- **`domain`** : tests unitaires de la relation d'ordre (`compareTo`) pour `IntegerPacket`, `ListPacket` et `PacketPair`, avec des fixtures partagées (`PacketFixtures`) pour construire des paquets sans répéter la verbosité des constructeurs.
- **`infrastructure`** : tests du parseur couvrant les paquets valides (listes vides, imbrication, entiers multi-chiffres) et les paquets invalides (position de l'erreur, cause conservée pour les dépassements d'entier), avec des tests paramétrés (`@ParameterizedTest`, `@CsvSource`, `@ValueSource`) pour couvrir plusieurs entrées sans dupliquer le test.
- Chaque test suit le pattern **AAA** (Arrange, Act, Assert), explicité par des commentaires dans les cas les plus longs.

## Outils qualité

- **CI** (`.github/workflows/ci.yml`) : déclenchée sur `push` et `pull_request`, exécute `./mvnw -B verify` sur `ubuntu-latest` avec Temurin 21 et cache Maven, puis publie le rapport JaCoCo en artifact.
- **Spotless** (`google-java-format`) : vérifié automatiquement à la phase `verify`. Pour appliquer le formatage localement : `./mvnw spotless:apply`.
- **JaCoCo** : rapport de couverture généré à la phase `verify` (`target/site/jacoco/index.html`), avec une règle de couverture minimale de 80 % des lignes sur les packages `domain`, `application` et `infrastructure`.

Pour exécuter l'ensemble des vérifications : `./mvnw verify`.
