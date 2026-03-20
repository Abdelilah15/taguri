# TAGURI — Jeu Mobile de Mots Amazighs 🎮

> **Plateforme mobile de jeux de mots pour la préservation et l'apprentissage de la langue et culture amazighe.**

---

## 📖 Vision du Projet

**TAGURI** est une application mobile innovante de jeux de mots en cours de développement sous Android Studio, conçue comme un vecteur culturel et un outil de revitalisation numérique de la **langue et culture amazighe**. Le projet positionne le divertissement comme un levier stratégique pour la **préservation, l'apprentissage et la promotion de l'amazigh** auprès des générations numériques.

---

## 🎯 Objectifs

1. **Enrichissement du vocabulaire amazigh numérisé** — base de données lexicale exhaustive en tamazight
2. **Apprentissage ludique** — gamification scientifique de l'acquisition lexicale
3. **Valorisation culturelle** — intégration de traditions, festivités, symboles et histoire amazighs

---

## 🛠️ Stack Technique

| Composant     | Technologie                         |
|---------------|-------------------------------------|
| Frontend      | Android (Kotlin) + Jetpack Compose  |
| Backend       | Firebase (Auth, Firestore)          |
| Architecture  | MVVM + Game Engine Data-Driven      |
| Stockage      | JSON Local (`assets/levels.json`) + Firebase Cloud |
| Langues       | Tamazight (Tifinagh & Latin)        |

---

## 🏗️ Architecture du Projet

```
app/src/main/
├── java/com/tamazightgames/taguri/
│   ├── MainActivity.kt               # Point d'entrée, navigation écrans
│   ├── FirestoreHelper.kt            # CRUD profils utilisateurs Firestore
│   ├── data/
│   │   ├── model/
│   │   │   ├── Chapter.kt            # Métadonnées d'un chapitre
│   │   │   ├── Puzzle.kt             # Classe abstraite de puzzle
│   │   │   └── WordSearchPuzzle.kt   # Modèle de grille mots mêlés
│   │   └── repository/
│   │       └── PuzzleRepository.kt   # Chargement puzzles depuis levels.json
│   ├── domain/
│   │   └── engine/
│   │       └── WordSearchEngine.kt   # Logique de validation mots mêlés
│   └── ui/
│       ├── screens/
│       │   ├── WelcomeScreen.kt      # Accueil — OAuth (Google/Facebook/Email)
│       │   ├── LoginScreen.kt        # Connexion / Inscription Email
│       │   ├── VerificationScreen.kt # Vérification adresse email
│       │   ├── GameScreen.kt         # Écran principal — chapitres & score
│       │   └── PlayScreen.kt         # Écran de jeu — puzzle actif
│       ├── viewmodel/
│       │   └── PlayViewModel.kt      # État du puzzle courant (StateFlow)
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           └── Type.kt
└── assets/
    └── levels.json                   # Contenu des puzzles (chapitres & niveaux)
```

---

## 🎮 Types de Puzzles

### 1. Mots Mêlés (Word Search)
- Grille de lettres contenant des mots amazighs cachés horizontalement, verticalement ou diagonalement
- Le joueur sélectionne une lettre de départ et glisse pour tracer le mot
- **Score:** 5 points par mot trouvé

### 2. Mots Croisés (Crossword)
- Grille prédéfinie avec indices (images / descriptions en amazigh)
- Joueur recompose les mots à partir de lettres mélangées
- **Score:** 10 points par puzzle complété

### 3. Puzzles de Citation (Quote Puzzle)
- Image illustrant une phrase ou citation amazighe
- Joueur reconstitue la citation en glissant sur les lettres adjacentes dans une grille
- **Score:** 15 points par citation

---

## 📈 Système de Progression

### Score et Niveaux

```
Seuil(k) = 10 + (k × 2)
où k = index du chapitre (chapitre 1 → k = 0)
```

| Chapitre | Points requis |
|----------|--------------|
| 1        | 10 pts       |
| 2        | 12 pts       |
| 3        | 14 pts       |
| N        | 10 + (N−1)×2 pts |

### Chapitres Thématiques
Chaque chapitre (8–18 puzzles) explore une thématique :
- **Culturelles Amazighes** : Traditions, symboles, histoire, festivités (Yennayer, Amazigh Day…)
- **Éducatives Universelles** : Corps humain, animaux, nature, sciences, vie quotidienne

---

## 🔒 Authentification

Intégration Firebase multi-canal :

| Méthode           | Status     |
|-------------------|-----------|
| Email / Mot de passe | ✅ Implémenté |
| Google OAuth      | ✅ Implémenté |
| Facebook OAuth    | ✅ Implémenté |
| Vérification email | ✅ Implémenté |

---

## 🗄️ Structure Firestore — Collection `users`

```json
{
  "email":          "joueur@example.com",
  "pseudo":         "NomJoueur",
  "score":          0,
  "niveau":         1,
  "mots":           0,
  "chapitreActuel": 1,
  "puzzleActuel":   1,
  "dateCreation":   1700000000000
}
```

---

## 🚀 Feuille de Route

### Phase 1 — MVP Fondations *(en cours)*
- [x] Authentification multi-canaux (Email, Google, Facebook)
- [ ] Puzzle de type Mots Mêlés
- [ ] Puzzle de type Mots Croisés
- [ ] Puzzle de type Citation (Quote Puzzle)
- [ ] Système de progression scores / niveaux
- [ ] Stockage données Firebase (profil utilisateur)
- [ ] Polish UI/UX et tests

### Phase 2 — Gamification
- [ ] Système de badges thématiques amazighs
- [ ] Leaderboards globaux et sociaux
- [ ] Défis quotidiens amazighs
- [ ] Événements saisonniers (Yennayer, Amazigh Day…)
- [ ] Streak (série de jours consécutifs)
- [ ] Système de partage et lien de parrainage

### Phase 3 — Communauté
- [ ] Forum communauté
- [ ] Contributions de puzzles par les utilisateurs
- [ ] Intégration Wikitionnaire amazigh

### Phase 4 — Expansion
- [ ] Nouveaux types de puzzles (anagrammes, associations…)
- [ ] Support dialectal étendu (tarifit, tachelhit…)
- [ ] Extensions iOS et web

---

## ⚙️ Installation & Configuration

### Prérequis
- Android Studio Hedgehog (2023.1.1) ou supérieur
- JDK 17+
- Compte Firebase (projet configuré)

### Étapes

1. **Cloner le dépôt**
   ```bash
   git clone https://github.com/Abdelilah15/taguri.git
   cd taguri
   ```

2. **Configurer Firebase**
   - Créer un projet dans [Firebase Console](https://console.firebase.google.com)
   - Activer Authentication (Email/Password, Google, Facebook)
   - Activer Firestore Database
   - Télécharger `google-services.json` et le placer dans `app/`
   - ⚠️ **Ne jamais committer `google-services.json` dans Git** (déjà exclu par `.gitignore`)

3. **Configurer Google Sign-In**
   - Dans `app/src/main/res/values/strings.xml`, remplacer `YOUR_WEB_CLIENT_ID_HERE` par votre **Web Client ID** (disponible dans Firebase Console → Project Settings → Your apps → OAuth 2.0)

4. **Configurer Facebook Login**
   - Ajouter dans `AndroidManifest.xml` votre `facebook_app_id` et `fb_login_protocol_scheme`
   - Voir [la documentation officielle Facebook SDK](https://developers.facebook.com/docs/android/)

5. **Ouvrir dans Android Studio**
   - `File → Open` → sélectionner le dossier du projet
   - Laisser Gradle synchroniser les dépendances

6. **Lancer l'application**
   - Brancher un appareil Android (API 26+) ou démarrer un émulateur
   - Cliquer sur **Run ▶**

---

## 🔐 Sécurité

| Fichier sensible         | Status Git   | Action requise |
|--------------------------|--------------|----------------|
| `google-services.json`   | ✅ Exclu (`.gitignore`) | Partager en privé avec l'équipe |
| `local.properties`       | ✅ Exclu | Généré automatiquement par Android Studio |
| `*.jks` / `*.keystore`   | ✅ Exclu | Stocker dans un coffre-fort sécurisé |
| `my_web_client_id`       | ⚠️ Placeholder dans `strings.xml` | Remplacer par la valeur réelle avant build |

> **Important :** Ne jamais committer de clés API, tokens OAuth, ou fichiers de configuration Firebase dans le dépôt public. Utiliser des variables d'environnement ou des fichiers exclus du versionnage.

---

## 📁 Format des Données — `assets/levels.json`

```json
{
  "chapters": [
    {
      "id": 1,
      "title": "La Culture Amazigh",
      "description": "Découvrez les mots fondamentaux.",
      "puzzles": [
        {
          "type": "WordSearch",
          "id": 1,
          "level": 1,
          "rewardPoints": 5,
          "gridSize": 4,
          "grid": [
            ["I", "Z", "M", "N"],
            ["A", "F", "G", "A"],
            ["F", "U", "S", "A"],
            ["T", "I", "R", "R"]
          ],
          "wordsToFind": ["IZM", "FUS", "AFGAN", "TIRRA"]
        }
      ]
    }
  ]
}
```

---

## 🤝 Contribution

Les contributions sont les bienvenues ! Pour contribuer :

1. Fork le dépôt
2. Créer une branche : `git checkout -b feat/votre-fonctionnalite`
3. Committer avec un message descriptif : `git commit -m "feat(puzzle): add crossword engine"`
4. Pousser : `git push origin feat/votre-fonctionnalite`
5. Ouvrir une Pull Request

### Convention de commits
```
feat(scope): description       # Nouvelle fonctionnalité
fix(scope): description        # Correction de bug
style(scope): description      # Changements UI/UX
docs(scope): description       # Documentation
refactor(scope): description   # Refactorisation
test(scope): description       # Tests
```

---

## 📜 Licence

Ce projet est open-source. Voir [LICENSE](LICENSE) pour plus d'informations.

---

## 🌍 Impact Culturel

TAGURI contribue activement à la **revitalisation numérique de la langue amazighe** en :
- Rendant l'apprentissage du tamazight ludique et accessible
- Générant un corpus lexical numérisé et standardisé
- Collaborant avec les ressources linguistiques publiques (Wikitionnaire amazigh)
- Représentant équitablement la diversité dialectale (tarifit, tachelhit, tamazight du Moyen Atlas…)

---

*Développé avec ❤️ pour la communauté amazighe*
