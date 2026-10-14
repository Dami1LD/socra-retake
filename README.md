# Distress Signal

Résolution d'[Advent of Code 2022, jour 13](https://adventofcode.com/2022/day/13) : comparaison de paquets imbriqués (entiers et listes) pour déterminer leur ordre.

- **Partie 1** : somme des indices (à partir de 1) des paires déjà dans le bon ordre — `SumOfOrderedIndicesUseCase`.
- **Partie 2** : tous les paquets de l'entrée (les deux de chaque paire) sont mis dans une seule liste avec deux paquets diviseurs, `[[2]]` et `[[6]]`. La liste est triée, puis on multiplie les positions (à partir de 1) des deux diviseurs — `DecoderKeyUseCase`.

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

Le jar est produit par `./mvnw package` (ou `verify`). Le programme affiche deux lignes : le résultat de la partie 1, puis celui de la partie 2. Code de sortie `0` en cas de succès, `1` en cas d'erreur (argument manquant, fichier illisible, paquet invalide).

## Architecture

Architecture hexagonale légère : le domaine ne dépend de rien, les adaptateurs dépendent du domaine via les ports.

```
                        ┌─────────────────────┐
  (driving adapter)     │         cli         │
                        │  Main (entry point) │
                        └──────────┬──────────┘
                                   │ appelle
                        ┌──────────▼───────────┐
                        │     application      │
                        │  use cases + ports   │
                        └──────────┬───────────┘
                                   │ dépend de
                        ┌──────────▼───────────┐
                        │        domain        │
                        │ Packet, PacketPair   │
                        │  (aucune dépendance) │
                        └──────────▲───────────┘
                                   │ implémente le port
                        ┌──────────┴────────────┐
  (driven adapter)      │    infrastructure     │
                        │ PacketParser, lecture │
                        │      de fichier       │
                        └───────────────────────┘
```

- `domain` : les types métier (interface `Packet`, `IntegerPacket`, `ListPacket`, `PacketPair`) et leur logique de comparaison. Aucune dépendance vers les autres packages.
- `application` : le port `PacketPairSourcePort` (source des paires de paquets) et les deux use cases : `SumOfOrderedIndicesUseCase` (partie 1) et `DecoderKeyUseCase` (partie 2), qui partagent le même port. Dépend uniquement du domaine.
- `infrastructure` : les adaptateurs pilotés (driven adapters) — `PacketParser` (parseur à descente récursive, profondeur limitée) et `FilePacketPairSourceAdapter` (lecture du fichier, découpage en paires séparées par une ligne vide, deux lignes par paire). Implémente le port défini par `application`.
- `cli` : l'adaptateur pilotant (driving adapter) et la racine de composition (`Main`), qui assemble l'adaptateur et les deux use cases.

Règle de dépendance : les flèches pointent toujours vers le centre (`domain`). `domain` ne connaît ni `application`, ni `infrastructure`, ni `cli`. `infrastructure` ne connaît que les ports exposés par `application`, jamais l'inverse.

## Décisions de conception

- **Hiérarchie typée et `compareTo`** : `Packet` modélise la variance entier/liste via deux records (`IntegerPacket`, `ListPacket`) qui implémentent la règle de comparaison de l'énoncé (un entier comparé à une liste est d'abord converti en liste d'un élément), plutôt qu'une structure générique (`Object`, `Map`) typée dynamiquement. `Packet` est une interface scellée et `Comparable<Packet>` : le `switch` par pattern est vérifié exhaustif par le compilateur, et la partie 2 trie avec l'ordre naturel.
- **Parseur maison à descente récursive** : plutôt qu'un `eval` (risque d'exécution de code arbitraire) ou une librairie JSON (dépendance externe pour un format trivial et pour garder le contrôle fin sur les messages d'erreur et les positions), le parseur est écrit à la main.
- **Emplacement du parseur dans `infrastructure`** : le parseur transforme une représentation texte externe (le fichier d'entrée) en objets du domaine ; c'est la responsabilité d'un adaptateur piloté, pas du domaine.
- **Paquets identiques** : deux paquets strictement identiques sont considérés comme étant dans le bon ordre (comparaison `<= 0`), hypothèse assumée en l'absence de cas de ce type dans l'énoncé original.
- **Partie 2 : un seul port, deux use cases** : `DecoderKeyUseCase` réutilise `PacketPairSourcePort` et aplatit les paires, sans changer le domaine ni l'infrastructure ; le format d'entrée reste le même qu'en partie 1.
- **Diviseurs repérés par identité** (`==`) et non par égalité : si l'entrée contient déjà `[[2]]` ou `[[6]]`, la position du diviseur ajouté reste correcte. `List.sort` étant stable, un diviseur se place après les paquets égaux de l'entrée.
- **Gestion des erreurs** : les erreurs de parsing lèvent une exception métier dédiée (`InvalidPacketException`) avec un message qui localise le problème (position dans la chaîne, caractère attendu vs. trouvé) plutôt qu'une exception générique du JDK.

## Stratégie de tests

- **`domain`** : tests unitaires de la relation d'ordre (`compareTo`) pour `IntegerPacket`, `ListPacket` et `PacketPair`, avec des fixtures partagées (`PacketFixtures`) pour construire des paquets sans répéter la verbosité des constructeurs.
- **`infrastructure`** : tests du parseur couvrant les paquets valides (listes vides, imbrication, entiers multi-chiffres) et les paquets invalides (position de l'erreur, cause conservée pour les dépassements d'entier), avec des tests paramétrés (`@ParameterizedTest`, `@CsvSource`, `@ValueSource`) pour couvrir plusieurs entrées sans dupliquer le test.
- **`application`** : tests des deux use cases avec un port stub (lambda) — indices à partir de 1 pour la partie 1 ; positions des diviseurs, liste vide et paquets égaux aux diviseurs pour la partie 2.
- **`cli`** : `MainTest` vérifie la sortie (deux lignes), les codes de sortie et les messages d'erreur (argument manquant, fichier absent, paquet invalide).
- Chaque test suit le pattern **AAA** (Arrange, Act, Assert), explicité par des commentaires dans les cas les plus longs.

## Outils qualité

- **CI** (`.github/workflows/ci.yml`) : déclenchée sur `push` et `pull_request`, exécute `./mvnw -B verify` sur `ubuntu-latest` avec Temurin 21 et cache Maven, puis publie le rapport JaCoCo en artifact.
- **Spotless** (`google-java-format`) : vérifié automatiquement à la phase `verify`. Pour appliquer le formatage localement : `./mvnw spotless:apply`.
- **JaCoCo** : rapport de couverture généré à la phase `verify` (`target/site/jacoco/index.html`), avec une règle de couverture minimale de 80 % des lignes sur les packages `domain`, `application` et `infrastructure`.

Pour exécuter l'ensemble des vérifications : `./mvnw verify`.
