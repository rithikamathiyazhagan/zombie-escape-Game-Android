package com.example.localization

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    SPANISH("es", "Spanish", "Español"),
    FRENCH("fr", "French", "Français"),
    GERMAN("de", "German", "Deutsch"),
    JAPANESE("ja", "Japanese", "日本語"),
    HINDI("hi", "Hindi", "हिंदी")
}

data class LocalizedStrings(
    val appTitle: String,
    val startEscape: String,
    val resume: String,
    val restart: String,
    val howToPlay: String,
    val analytics: String,
    val settings: String,
    val level: String,
    val health: String,
    val stamina: String,
    val sprint: String,
    val decoy: String,
    val keyRequired: String,
    val keyCollected: String,
    val exitUnlocked: String,
    val zombieAlert: String,
    val zombieIdle: String,
    val zombieChasing: String,
    val zombieSearching: String,
    val levelCleared: String,
    val nextLevel: String,
    val gameOver: String,
    val tryAgain: String,
    val mainMenu: String,
    val victoryTitle: String,
    val victoryDesc: String,
    val soundOn: String,
    val soundOff: String,
    val hapticsOn: String,
    val hapticsOff: String,
    val analyticsTitle: String,
    val totalEscapes: String,
    val totalDeaths: String,
    val balanceRating: String,
    val balanceHint: String,
    val howToPlayContent: String,
    val languageSelect: String,
    val fpsControl: String,
    val fps30: String,
    val fps60: String
)

object GameLocalization {
    private val english = LocalizedStrings(
        appTitle = "ZOMBIE ESCAPE",
        startEscape = "START ESCAPE",
        resume = "RESUME",
        restart = "RESTART LEVEL",
        howToPlay = "HOW TO PLAY",
        analytics = "ANALYTICS",
        settings = "SETTINGS",
        level = "Level",
        health = "HEALTH",
        stamina = "STAMINA",
        sprint = "SPRINT",
        decoy = "DECOY",
        keyRequired = "KEY REQUIRED TO UNLOCK EXIT",
        keyCollected = "KEY OBTAINED! REACH THE EXIT!",
        exitUnlocked = "EXIT UNLOCKED!",
        zombieAlert = "ALERT!",
        zombieIdle = "Patrolling",
        zombieChasing = "CHASING!",
        zombieSearching = "Investigating...",
        levelCleared = "ZONE CLEARED!",
        nextLevel = "NEXT ZONE",
        gameOver = "YOU WERE INFECTED",
        tryAgain = "TRY AGAIN",
        mainMenu = "MAIN MENU",
        victoryTitle = "EXTRACTION COMPLETE!",
        victoryDesc = "You successfully navigated all 4 quarantine zones and survived the infected horde!",
        soundOn = "Sound: ON",
        soundOff = "Sound: OFF",
        hapticsOn = "Haptics: ON",
        hapticsOff = "Haptics: OFF",
        analyticsTitle = "SURVIVAL ANALYTICS & BALANCE",
        totalEscapes = "Successful Escapes",
        totalDeaths = "Infections / Deaths",
        balanceRating = "AI Difficulty Rating",
        balanceHint = "BFS pathfinding algorithm calculates optimal chase routes. Use decoys and sprint wisely.",
        howToPlayContent = """
            SURVIVAL OBJECTIVE:
            1. Navigate the maze grid avoiding patrolling and chasing zombies.
            2. Collect the golden KEY in each sector to unlock the EXIT portal.
            3. Pick up MEDKITS to restore lost health.
            4. Hold SPRINT to run fast, but monitor stamina depletion!
            5. Throw DECOYS to lure zombie BFS pathfinding toward the noise.
            6. Complete all 4 sectors to win the game!
        """.trimIndent(),
        languageSelect = "Language",
        fpsControl = "Frame Rate (FPS)",
        fps30 = "30 FPS (Battery Saver)",
        fps60 = "60 FPS (Ultra Smooth)"
    )

    private val spanish = LocalizedStrings(
        appTitle = "ESCAPE ZOMBI",
        startEscape = "COMENZAR ESCAPE",
        resume = "CONTINUAR",
        restart = "REINICIAR",
        howToPlay = "CÓMO JUGAR",
        analytics = "ESTADÍSTICAS",
        settings = "AJUSTES",
        level = "Nivel",
        health = "SALUD",
        stamina = "RESISTENCIA",
        sprint = "CORRER",
        decoy = "SEÑUELO",
        keyRequired = "LLAVE NECESARIA PARA SALIR",
        keyCollected = "¡LLAVE OBTENIDA! ¡VE A LA SALIDA!",
        exitUnlocked = "¡SALIDA DESBLOQUEADA!",
        zombieAlert = "¡ALERTA!",
        zombieIdle = "Patrullando",
        zombieChasing = "¡PERSIGUIENDO!",
        zombieSearching = "Investigando...",
        levelCleared = "¡ZONA COMPLETADA!",
        nextLevel = "SIGUIENTE ZONA",
        gameOver = "FUISTE INFECTADO",
        tryAgain = "REINTENTAR",
        mainMenu = "MENÚ PRINCIPAL",
        victoryTitle = "¡EXTRACCIÓN EXITOSA!",
        victoryDesc = "¡Superaste las 4 zonas de cuarentena y sobreviviste a la horda!",
        soundOn = "Sonido: SÍ",
        soundOff = "Sonido: NO",
        hapticsOn = "Vibración: SÍ",
        hapticsOff = "Vibración: NO",
        analyticsTitle = "ANÁLISIS DE SUPERVIVENCIA",
        totalEscapes = "Escapes Exitosos",
        totalDeaths = "Infecciones / Muertes",
        balanceRating = "Evaluación de Dificultad IA",
        balanceHint = "El algoritmo BFS calcula la ruta óptima de persecución. Usa señuelos.",
        howToPlayContent = """
            OBJETIVO:
            1. Recorre el laberinto evitando a los zombis.
            2. Encuentra la LLAVE dorada para desbloquear la SALIDA.
            3. Recoge botiquines para recuperar salud.
            4. Mantén CORRER para moverte rápido (consume resistencia).
            5. Lanza SEÑUELOS para distraer el camino BFS del zombi.
            6. ¡Completa los 4 niveles para ganar!
        """.trimIndent(),
        languageSelect = "Idioma",
        fpsControl = "Tasa de Cuadros (FPS)",
        fps30 = "30 FPS (Ahorro Batería)",
        fps60 = "60 FPS (Ultra Fluido)"
    )

    private val french = LocalizedStrings(
        appTitle = "ÉVASION ZOMBIE",
        startEscape = "COMMENCER",
        resume = "REPRENDRE",
        restart = "RECOMMENCER",
        howToPlay = "COMMENT JOUER",
        analytics = "STATISTIQUES",
        settings = "PARAMÈTRES",
        level = "Niveau",
        health = "SANTÉ",
        stamina = "ENDURANCE",
        sprint = "SPRINT",
        decoy = "LEURRE",
        keyRequired = "CLÉ REQUISE POUR LA SORTIE",
        keyCollected = "CLÉ TROUVÉE ! REJOIGNEZ LA SORTIE !",
        exitUnlocked = "SORTIE DÉVERROUILLÉE !",
        zombieAlert = "ALERTE !",
        zombieIdle = "En patrouille",
        zombieChasing = "CHASSE EN COURS !",
        zombieSearching = "Recherche...",
        levelCleared = "ZONE SÉCURISÉE !",
        nextLevel = "ZONE SUIVANTE",
        gameOver = "VOUS ÊTES INFECTÉ",
        tryAgain = "RÉESSAYER",
        mainMenu = "MENU PRINCIPAL",
        victoryTitle = "ÉVACUATION RÉUSSIE !",
        victoryDesc = "Vous avez traversé les 4 zones contaminées et survécu !",
        soundOn = "Son : OUI",
        soundOff = "Son : NON",
        hapticsOn = "Vibrations : OUI",
        hapticsOff = "Vibrations : NON",
        analyticsTitle = "ANALYTIQUE & ÉQUILIBRE",
        totalEscapes = "Évasions Réussies",
        totalDeaths = "Morts / Infections",
        balanceRating = "Évaluation Difficulté IA",
        balanceHint = "L'algorithme BFS calcule le plus court chemin du zombie vers le joueur.",
        howToPlayContent = """
            OBJECTIF :
            1. Naviguez dans le labyrinthe sans vous faire attraper.
            2. Récupérez la CLÉ dorée pour ouvrir la SORTIE.
            3. Trouvez des trousses de soins pour restaurer votre vie.
            4. Utilisez le SPRINT prudemment (consomme l'endurance).
            5. Lancez des LEURRES pour détourner les zombies.
            6. Terminez les 4 niveaux pour vous échapper !
        """.trimIndent(),
        languageSelect = "Langue",
        fpsControl = "Images par seconde (FPS)",
        fps30 = "30 FPS (Économie d'énergie)",
        fps60 = "60 FPS (Ultra Fluide)"
    )

    private val german = LocalizedStrings(
        appTitle = "ZOMBIE FLUCHT",
        startEscape = "STARTEN",
        resume = "WEITER",
        restart = "NEUSTART",
        howToPlay = "SPIELANLEITUNG",
        analytics = "STATISTIK",
        settings = "EINSTELLUNGEN",
        level = "Level",
        health = "GESUNDHEIT",
        stamina = "AUSDAUER",
        sprint = "SPRINT",
        decoy = "KÖDER",
        keyRequired = "SCHLÜSSEL ZUM AUSGANG BENÖTIGT",
        keyCollected = "SCHLÜSSEL GEFUNDEN! ZUM AUSGANG!",
        exitUnlocked = "AUSGANG OFFEN!",
        zombieAlert = "ALARM!",
        zombieIdle = "Patrouilliert",
        zombieChasing = "JAGT!",
        zombieSearching = "Sucht...",
        levelCleared = "SEKTOR GESICHERT!",
        nextLevel = "NÄCHSTE ZONE",
        gameOver = "INFIZIERT",
        tryAgain = "WIEDERHOLEN",
        mainMenu = "HAUPTMENÜ",
        victoryTitle = "EVAKUIERUNG ERFOLGREICH!",
        victoryDesc = "Sie haben alle 4 Quarantänezonen überlebt und sind entkommen!",
        soundOn = "Ton: AN",
        soundOff = "Ton: AUS",
        hapticsOn = "Vibration: AN",
        hapticsOff = "Vibration: AUS",
        analyticsTitle = "ÜBERLEBENS-ANALYTIK",
        totalEscapes = "Erfolgreiche Fluchten",
        totalDeaths = "Infektionen / Tode",
        balanceRating = "KI-Schwierigkeitsgrad",
        balanceHint = "Der BFS-Algorithmus berechnet stets den kürzesten Weg.",
        howToPlayContent = """
            ZIEL:
            1. Weiche den Zombies im Labyrinth aus.
            2. Finde den goldenen SCHLÜSSEL für den AUSGANG.
            3. Nutze Medipacks zur Heilung.
            4. Halte SPRINT für mehr Tempo gedrückt.
            5. Nutze KÖDER zur Ablenkung der Zombies.
            6. Meistere alle 4 Level für den Sieg!
        """.trimIndent(),
        languageSelect = "Sprache",
        fpsControl = "Bildrate (FPS)",
        fps30 = "30 FPS (Energiesparen)",
        fps60 = "60 FPS (Ultra Flüssig)"
    )

    private val japanese = LocalizedStrings(
        appTitle = "ゾンビ・エスケープ",
        startEscape = "脱出開始",
        resume = "再開",
        restart = "リスタート",
        howToPlay = "遊び方",
        analytics = "統計・分析",
        settings = "設定",
        level = "レベル",
        health = "体力",
        stamina = "スタミナ",
        sprint = "ダッシュ",
        decoy = "オトリ",
        keyRequired = "出口の鍵が必要です",
        keyCollected = "鍵を入手！出口へ急げ！",
        exitUnlocked = "脱出ゲート開放！",
        zombieAlert = "警戒！",
        zombieIdle = "巡回中",
        zombieChasing = "追跡中！",
        zombieSearching = "索敵中...",
        levelCleared = "エリア突破！",
        nextLevel = "次のエリア",
        gameOver = "感染してしまった…",
        tryAgain = "もう一度",
        mainMenu = "タイトルへ",
        victoryTitle = "全エリア脱出成功！",
        victoryDesc = "全4つの隔離区域を突破し、生還を果たしました！",
        soundOn = "効果音: ON",
        soundOff = "効果音: OFF",
        hapticsOn = "振動: ON",
        hapticsOff = "振動: OFF",
        analyticsTitle = "サバイバル分析＆バランス",
        totalEscapes = "脱出成功回数",
        totalDeaths = "感染・死亡回数",
        balanceRating = "AI難易度判定",
        balanceHint = "BFS探索アルゴリズムが最短経路を計算して追跡します。オトリを活用しましょう。",
        howToPlayContent = """
            生存ガイド:
            1. 迷路を進み、ゾンビの追跡を回避してください。
            2. 各エリアの「金の鍵」を拾って出口ゲートを開放します。
            3. 救急箱でダメージを回復。
            4. ダッシュボタンで高速移動（スタミナ消費）。
            5. オトリを投げてゾンビのBFS経路を誘導できます。
            6. 全4レベルをクリアして完全生還を目指せ！
        """.trimIndent(),
        languageSelect = "言語切り替え",
        fpsControl = "フレームレート (FPS)",
        fps30 = "30 FPS (省電力)",
        fps60 = "60 FPS (高滑らか)"
    )

    private val hindi = LocalizedStrings(
        appTitle = "ज़ोंबी एस्केप",
        startEscape = "शुरू करें",
        resume = "जारी रखें",
        restart = "फिर से खेलें",
        howToPlay = "कैसे खेलें",
        analytics = "एनालिटिक्स",
        settings = "सेटिंग्स",
        level = "स्तर",
        health = "स्वास्थ्य",
        stamina = "सहनशक्ति",
        sprint = "दौड़ें",
        decoy = "झांसा",
        keyRequired = "निकास के लिए चाबी चाहिए",
        keyCollected = "चाबी मिल गई! निकास की ओर बढ़ें!",
        exitUnlocked = "निकास खुला!",
        zombieAlert = "खतरा!",
        zombieIdle = "गश्त",
        zombieChasing = "पीछा कर रहा है!",
        zombieSearching = "खोज रहा है...",
        levelCleared = "क्षेत्र साफ़!",
        nextLevel = "अगला स्तर",
        gameOver = "आप संक्रमित हो गए",
        tryAgain = "पुनः प्रयास",
        mainMenu = "मुख्य मेनू",
        victoryTitle = "मिशन सफल!",
        victoryDesc = "आपने सभी 4 स्तरों को पार कर लिया और जीवित बच निकले!",
        soundOn = "ध्वनि: चालू",
        soundOff = "ध्वनि: बंद",
        hapticsOn = "कंपन: चालू",
        hapticsOff = "कंपन: बंद",
        analyticsTitle = "जीवित रहने के आंकड़े",
        totalEscapes = "सफल बचाव",
        totalDeaths = "मौतें / संक्रमण",
        balanceRating = "कठिनाई स्तर",
        balanceHint = "BFS एल्गोरिदम ज़ोंबी को आपके सबसे छोटे रास्ते पर भेजता है। झांसे का उपयोग करें।",
        howToPlayContent = """
            नियम:
            1. ज़ोंबी से बचते हुए भूलभुलैया पार करें।
            2. निकास खोलने के लिए सुनहरी चाबी खोजें।
            3. स्वास्थ्य बहाल करने के लिए मेडकिट लें।
            4. तेजी से दौड़ने के लिए स्प्रिंट दबाएं।
            5. ज़ोंबी का ध्यान भटकाने के लिए झांसा फेंकें।
            6. जीतने के लिए सभी 4 स्तर पूरे करें!
        """.trimIndent(),
        languageSelect = "भाषा",
        fpsControl = "फ्रेम दर (FPS)",
        fps30 = "30 FPS (बैटरी बचत)",
        fps60 = "60 FPS (अल्ट्रा स्मूथ)"
    )

    fun getStrings(language: AppLanguage): LocalizedStrings = when (language) {
        AppLanguage.ENGLISH -> english
        AppLanguage.SPANISH -> spanish
        AppLanguage.FRENCH -> french
        AppLanguage.GERMAN -> german
        AppLanguage.JAPANESE -> japanese
        AppLanguage.HINDI -> hindi
    }
}
