package com.randomthoughtsever.plainly.ui

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.randomthoughtsever.plainly.ads.BannerAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** One of the quick games. [best] describes how the best score reads ("3 wins", "18 moves", "212 ms"). */
data class Game(val id: String, val title: String, val blurb: String, val emoji: String, val from: Color, val to: Color)

val GAMES = listOf(
    Game("rps", "Rock Paper Scissors", "First to 3 against your phone", "✊", Color(0xFF8E3B6E), Color(0xFFC25A8C)),
    Game("match", "Money Match", "Flip the cards and find the pairs", "💵", Color(0xFF0F5F55), Color(0xFF2E9C88)),
    Game("rush", "Number Rush", "45 seconds of quick money maths", "⚡", Color(0xFFB0472A), Color(0xFFE0883C)),
    Game("ttt", "Tic-Tac-Toe", "Can you beat the unbeatable?", "❌", Color(0xFF1F4E79), Color(0xFF3B79B5)),
    Game("react", "Reaction Time", "Tap the moment it turns green", "🟢", Color(0xFF3B2F6B), Color(0xFF6E5BB5)),
)

/** Best scores, kept on the phone. Higher is better except where [lowerIsBetter]. */
object Scores {
    private fun p(c: Context) = c.getSharedPreferences("games", Context.MODE_PRIVATE)
    fun best(c: Context, id: String): Int? = p(c).getInt(id, -1).takeIf { it >= 0 }
    fun plays(c: Context) = p(c).getInt("plays", 0)
    /** Saves [score] if it beats the best; returns true when it's a new best. */
    fun submit(c: Context, id: String, score: Int, lowerIsBetter: Boolean = false): Boolean {
        val old = best(c, id)
        p(c).edit().putInt("plays", plays(c) + 1).apply()
        runCatching { AppGraph.progress.award("game:$id:${java.time.LocalDate.now()}", 5, "Game played", "games") }
        val better = old == null || (if (lowerIsBetter) score < old else score > old)
        if (better) p(c).edit().putInt(id, score).apply()
        return better
    }
    fun label(id: String, v: Int) = when (id) { "rps" -> "$v wins"; "match" -> "$v moves"; "rush" -> "$v points"; "ttt" -> "$v wins"; else -> "$v ms" }
}

// ---------- hub ----------

@Composable
fun GamesScreen(nav: Nav) {
    val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)) {
                Text("Games", style = MaterialTheme.typography.displaySmall)
                Text("A daily puzzle, a year of money to live, and five quick games. All offline.", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { PuzzleCard(nav) }
        item {
            androidx.compose.material3.TextButton(onClick = { nav.puzzles() }, modifier = Modifier.padding(horizontal = 12.dp)) {
                Text("🗓️  Past puzzles: catch up on the last 30 days")
            }
        }
        item { SimCard(nav) }
        items(GAMES, key = { it.id }) { g ->
            val best = remember { Scores.best(context, g.id) }
            Surface(onClick = { nav.game(g.id) }, shape = RoundedCornerShape(26.dp), color = Color.Transparent,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 7.dp)) {
                Row(Modifier.background(Brush.linearGradient(listOf(g.from, g.to))).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    val bob by androidx.compose.animation.core.rememberInfiniteTransition(label = "bob").animateFloat(-4f, 4f,
                        infiniteRepeatable(tween(900 + g.id.length * 90), RepeatMode.Reverse), label = "y")
                    Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
                        Text(g.emoji, fontSize = 34.sp, modifier = Modifier.offset(y = bob.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(g.title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Text(g.blurb, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .85f))
                        Text(best?.let { "Best: ${Scores.label(g.id, it)}" } ?: "Not played yet", style = MaterialTheme.typography.labelMedium,
                            color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    }
                    Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.PlayArrow, "Play", tint = g.from)
                    }
                }
            }
        }
        item { BannerAd("games:hub", Modifier.padding(horizontal = 20.dp)) }
    }
}

@Composable
fun GameScreen(id: String, nav: Nav) {
    val g = GAMES.firstOrNull { it.id == id } ?: GAMES.first()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = nav::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            Text(g.emoji + "  " + g.title, style = MaterialTheme.typography.titleLarge)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (g.id) {
                "rps" -> RockPaperScissors(g)
                "match" -> MoneyMatch(g)
                "rush" -> NumberRush(g)
                "ttt" -> TicTacToe(g)
                else -> ReactionTime(g)
            }
        }
    }
}

@Composable
private fun ResultBanner(text: String, sub: String, g: Game, again: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(20.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(g.from, g.to))).padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, style = MaterialTheme.typography.headlineMedium, color = Color.White, textAlign = TextAlign.Center)
        Text(sub, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = .85f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Button(onClick = again, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = g.from)) {
            Text("Play again", fontWeight = FontWeight.Bold)
        }
    }
}

// ---------- 1. Rock Paper Scissors ----------

/** 0 rock, 1 paper, 2 scissors. Returns 1 if a beats b, -1 if b beats a, 0 for a draw. */
fun rpsOutcome(a: Int, b: Int): Int = when { a == b -> 0; (a - b + 3) % 3 == 1 -> 1; else -> -1 }

@Composable
private fun RockPaperScissors(g: Game) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val hands = listOf("✊" to "Rock", "✋" to "Paper", "✌️" to "Scissors")
    var you by remember { mutableIntStateOf(0) }
    var phone by remember { mutableIntStateOf(0) }
    var mine by remember { mutableStateOf<Int?>(null) }
    var theirs by remember { mutableStateOf<Int?>(null) }
    var shaking by remember { mutableStateOf(false) }
    var line by remember { mutableStateOf("Pick a hand") }
    var newBest by remember { mutableStateOf(false) }
    val shake = remember { Animatable(0f) }
    val over = you == 3 || phone == 3
    fun play(choice: Int) {
        if (shaking || over) return
        shaking = true; mine = null; theirs = null; line = ""
        scope.launch {
            listOf("Rock…", "Paper…", "Scissors…").forEach { w -> line = w; shake.animateTo(-30f, tween(140)); shake.animateTo(0f, tween(140)) }
            mine = choice; theirs = Random.nextInt(3)
            val r = rpsOutcome(choice, theirs!!)
            if (r == 1) you++ else if (r == -1) phone++
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            line = when (r) { 1 -> "You win this round!"; -1 -> "Phone wins this round"; else -> "Draw!" }
            if (you == 3) newBest = Scores.submit(context, "rps", (Scores.best(context, "rps") ?: 0) + 1)
            shaking = false
        }
    }
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ScoreBox("You", you, g); ScoreBox("Phone", phone, g)
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Text(mine?.let { hands[it].first } ?: "✊", fontSize = 72.sp, modifier = Modifier.offset(y = shake.value.dp).graphicsLayer { scaleX = -1f })
            Text("vs", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(theirs?.let { hands[it].first } ?: "✊", fontSize = 72.sp, modifier = Modifier.offset(y = shake.value.dp))
        }
        Spacer(Modifier.height(12.dp))
        AnimatedContent(line, transitionSpec = { (fadeIn() + scaleIn(initialScale = .8f)) togetherWith fadeOut() }, label = "line") { l ->
            Text(l, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.weight(1f))
        if (over) ResultBanner(if (you == 3) "You won! 🎉" else "Phone wins", if (you == 3 && newBest) "New record for games won" else "First to 3", g) {
            you = 0; phone = 0; mine = null; theirs = null; line = "Pick a hand"; newBest = false
        } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            hands.forEachIndexed { i, (e, name) ->
                Surface(onClick = { play(i) }, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(e, fontSize = 40.sp); Text(name, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreBox(who: String, n: Int, g: Game) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(who, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row { repeat(3) { i -> Box(Modifier.padding(3.dp).size(16.dp).clip(CircleShape).background(if (i < n) g.from else MaterialTheme.colorScheme.surfaceVariant)) } }
}

// ---------- 2. Money Match ----------

private val SYMBOLS = listOf("💰", "📈", "🏦", "💳", "💵", "💎", "🎯", "🧠")

@Composable
private fun MoneyMatch(g: Game) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var deck by remember { mutableStateOf((SYMBOLS + SYMBOLS).shuffled()) }
    val open = remember { mutableStateListOf<Int>() }
    val found = remember { mutableStateListOf<Int>() }
    var moves by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var newBest by remember { mutableStateOf(false) }
    val done = found.size == deck.size
    LaunchedEffect(done) { if (done) newBest = Scores.submit(context, "match", moves, lowerIsBetter = true) }
    fun flip(i: Int) {
        if (busy || i in open || i in found) return
        open += i
        if (open.size == 2) {
            moves++
            val (a, b) = open[0] to open[1]
            if (deck[a] == deck[b]) { found += a; found += b; open.clear() }
            else { busy = true; scope.launch { delay(750); open.clear(); busy = false } }
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Moves: $moves   ·   Pairs: ${found.size / 2} of 8", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        deck.chunked(4).forEachIndexed { r, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                row.forEachIndexed { c, sym ->
                    val i = r * 4 + c
                    val up = i in open || i in found
                    val rot by animateFloatAsState(if (up) 180f else 0f, tween(300), label = "flip$i")
                    val density = LocalDensity.current.density
                    Box(Modifier.weight(1f).aspectRatio(.8f).graphicsLayer { rotationY = rot; cameraDistance = 12 * density }
                        .clip(RoundedCornerShape(16.dp)).background(if (rot > 90f) (if (i in found) Gold.copy(alpha = .35f) else MaterialTheme.colorScheme.surface)
                        else g.from).clickable(onClickLabel = "Flip card") { flip(i) }
                        .semantics { contentDescription = "Card ${i + 1}: " + if (i in found) "matched, $sym" else if (up) sym else "face down" }, contentAlignment = Alignment.Center) {
                        if (rot > 90f) Text(sym, fontSize = 34.sp, modifier = Modifier.graphicsLayer { rotationY = 180f })
                        else Text("P", color = Color.White.copy(alpha = .35f), style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        if (done) ResultBanner("All pairs in $moves moves!", if (newBest) "New best" else "Best: ${Scores.best(context, "match")} moves", g) {
            deck = (SYMBOLS + SYMBOLS).shuffled(); open.clear(); found.clear(); moves = 0; newBest = false
        }
    }
}

// ---------- 3. Number Rush ----------

data class Sum(val text: String, val answer: Int, val options: List<Int>)

/** A quick question: a sum, a difference, a product, a percentage of, or an amount after interest. All answers are whole numbers. */
fun nextSum(r: Random = Random): Sum {
    val (text, answer) = when (r.nextInt(5)) {
        0 -> { val a = r.nextInt(12, 90); val b = r.nextInt(12, 90); "$a + $b" to a + b }
        1 -> { val a = r.nextInt(40, 150); val b = r.nextInt(10, a); "$a − $b" to a - b }
        2 -> { val a = r.nextInt(3, 10); val b = r.nextInt(11, 30); "$a × $b" to a * b }
        3 -> { val p = listOf(10, 20, 25, 50).random(r); val n = r.nextInt(2, 40) * 20; "$p% of $n" to n * p / 100 }
        else -> { val p = listOf(10, 20, 50).random(r); val n = r.nextInt(1, 20) * 10; "$n + $p% interest" to n + n * p / 100 }
    }
    val opts = mutableSetOf(answer)
    while (opts.size < 4) { val d = listOf(-10, -2, -1, 1, 2, 10, 5, -5).random(r); if (answer + d >= 0) opts += answer + d }
    return Sum(text, answer, opts.shuffled(r))
}

@Composable
private fun NumberRush(g: Game) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var left by remember { mutableIntStateOf(45) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var q by remember { mutableStateOf(nextSum()) }
    var flash by remember { mutableStateOf<Boolean?>(null) }
    var newBest by remember { mutableStateOf(false) }
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (left > 0) { delay(1000); left-- }
        running = false; finished = true; newBest = Scores.submit(context, "rush", score)
    }
    LaunchedEffect(flash) { if (flash != null) { delay(250); flash = null } }
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (!running && !finished) {
            Spacer(Modifier.weight(1f))
            Text("⚡", fontSize = 72.sp)
            Text("Answer as many as you can in 45 seconds. Right answers in a row score double.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { running = true; left = 45; score = 0; combo = 0; q = nextSum() }, shape = RoundedCornerShape(50)) { Text("Start", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.weight(1f))
            return@Column
        }
        LinearProgressIndicator(progress = { left / 45f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = g.to)
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
            Text("⏱ ${left}s", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("Score $score" + if (combo >= 2) "  🔥×2" else "", style = MaterialTheme.typography.titleMedium)
        }
        if (finished) {
            Spacer(Modifier.weight(1f))
            ResultBanner("$score points", if (newBest) "New best!" else "Best: ${Scores.best(context, "rush")} points", g) {
                finished = false; running = true; left = 45; score = 0; combo = 0; q = nextSum()
            }
            Spacer(Modifier.weight(1f))
            return@Column
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(when (flash) { true -> Color(0xFF2E7D5B); false -> Color(0xFFC0392B); null -> g.from }).padding(28.dp),
            contentAlignment = Alignment.Center) {
            Text(q.text, style = MaterialTheme.typography.displaySmall, color = Color.White, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(20.dp))
        q.options.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                row.forEach { o ->
                    Surface(onClick = {
                        val right = o == q.answer
                        flash = right
                        if (right) { combo++; score += if (combo >= 2) 2 else 1 } else { combo = 0; haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
                        q = nextSum()
                    }, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp, modifier = Modifier.weight(1f)) {
                        Text("$o", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 20.dp))
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

// ---------- 4. Tic-Tac-Toe ----------

private val LINES = listOf(listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), listOf(0, 4, 8), listOf(2, 4, 6))

/** The winning line for a board ('X', 'O' or ' '), or null. */
fun tttWinner(b: List<Char>): Pair<Char, List<Int>>? = LINES.firstOrNull { l -> b[l[0]] != ' ' && l.all { b[it] == b[l[0]] } }?.let { b[it[0]] to it }

/** The phone's move as [me]: perfect play (minimax) when [hard], otherwise a random free square, but it takes an obvious win. */
fun tttMove(b: List<Char>, me: Char, hard: Boolean, r: Random = Random): Int {
    val free = b.indices.filter { b[it] == ' ' }
    if (!hard) return free.firstOrNull { i -> tttWinner(b.toMutableList().also { it[i] = me })?.first == me } ?: free.random(r)
    val other = if (me == 'X') 'O' else 'X'
    fun score(board: MutableList<Char>, turn: Char, depth: Int): Int {
        tttWinner(board)?.let { return if (it.first == me) 10 - depth else depth - 10 }
        val f = board.indices.filter { board[it] == ' ' }
        if (f.isEmpty()) return 0
        val s = f.map { i -> board[i] = turn; val v = score(board, if (turn == me) other else me, depth + 1); board[i] = ' '; v }
        return if (turn == me) s.max() else s.min()
    }
    return free.maxBy { i -> val c = b.toMutableList(); c[i] = me; score(c, other, 1) }
}

@Composable
private fun TicTacToe(g: Game) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var board by remember { mutableStateOf(List(9) { ' ' }) }
    var hard by remember { mutableStateOf(false) }
    var wins by remember { mutableIntStateOf(0) }
    var losses by remember { mutableIntStateOf(0) }
    var draws by remember { mutableIntStateOf(0) }
    var thinking by remember { mutableStateOf(false) }
    val win = tttWinner(board)
    val full = board.none { it == ' ' }
    val over = win != null || full
    fun tap(i: Int) {
        if (over || thinking || board[i] != ' ') return
        val b = board.toMutableList().also { it[i] = 'X' }; board = b
        if (tttWinner(b) != null) { wins++; Scores.submit(context, "ttt", (Scores.best(context, "ttt") ?: 0) + 1); return }
        if (b.none { it == ' ' }) { draws++; return }
        thinking = true
        scope.launch {
            delay(450)
            val m = tttMove(b, 'O', hard)
            board = b.toMutableList().also { it[m] = 'O' }
            if (tttWinner(board) != null) losses++ else if (board.none { it == ' ' }) draws++
            thinking = false
        }
    }
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(!hard, { hard = false; board = List(9) { ' ' } }, SegmentedButtonDefaults.itemShape(0, 2)) { Text("Easy") }
            SegmentedButton(hard, { hard = true; board = List(9) { ' ' } }, SegmentedButtonDefaults.itemShape(1, 2)) { Text("Unbeatable") }
        }
        Text("You $wins · Phone $losses · Draws $draws", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 14.dp))
        Column(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (0 until 3).forEach { r ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0 until 3).forEach { c ->
                        val i = r * 3 + c
                        val inLine = win?.second?.contains(i) == true
                        val pop by animateFloatAsState(if (board[i] == ' ') .5f else 1f, spring(dampingRatio = .4f), label = "pop$i")
                        Box(Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(16.dp))
                            .background(if (inLine) Gold.copy(alpha = .5f) else MaterialTheme.colorScheme.surface)
                            .clickable(onClickLabel = "Place your cross") { tap(i) }
                            .semantics { contentDescription = "Row ${r + 1}, column ${c + 1}: " + when (board[i]) { 'X' -> "your cross"; 'O' -> "phone's circle"; else -> "empty" } },
                            contentAlignment = Alignment.Center) {
                            if (board[i] != ' ') Text(if (board[i] == 'X') "✕" else "◯", fontSize = 52.sp, modifier = Modifier.scale(pop),
                                color = if (board[i] == 'X') g.from else Color(0xFFC0602A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(when { win?.first == 'X' -> "You win! 🎉"; win?.first == 'O' -> "Phone wins"; full -> "Draw"; thinking -> "Phone is thinking…"; else -> "Your turn (✕)" },
            style = MaterialTheme.typography.headlineSmall)
        if (over) TextButton(onClick = { board = List(9) { ' ' } }) { Text("New game") }
    }
}

// ---------- 5. Reaction Time ----------

@Composable
private fun ReactionTime(g: Game) {
    val context = LocalContext.current
    // 0 ready, 1 waiting (red), 2 go (green), 3 result of a round, 4 too soon, 5 finished
    var phase by remember { mutableIntStateOf(0) }
    var startedAt by remember { mutableLongStateOf(0L) }
    val times = remember { mutableStateListOf<Long>() }
    var round by remember { mutableIntStateOf(0) }
    var newBest by remember { mutableStateOf(false) }
    LaunchedEffect(phase, round) {
        if (phase == 1) { delay(Random.nextLong(1500, 4000)); if (phase == 1) { phase = 2; startedAt = System.nanoTime() } }
    }
    fun tap() {
        when (phase) {
            0, 3, 4 -> phase = 1
            1 -> phase = 4
            2 -> {
                times += (System.nanoTime() - startedAt) / 1_000_000
                round++
                if (times.size == 5) { phase = 5; newBest = Scores.submit(context, "react", times.min().toInt(), lowerIsBetter = true) } else phase = 3
            }
            5 -> { times.clear(); round = 0; phase = 0; newBest = false }
        }
    }
    val bg = when (phase) { 1 -> Color(0xFFC0392B); 2 -> Color(0xFF2E7D5B); 4 -> Color(0xFFB0472A); else -> g.from }
    Box(Modifier.fillMaxSize().padding(16.dp).clip(RoundedCornerShape(30.dp)).background(bg).pointerInput(Unit) {
        awaitPointerEventScope { while (true) { val e = awaitPointerEvent(); if (e.changes.any { it.pressed && !it.previousPressed }) tap() } }
    }.semantics(mergeDescendants = true) { role = androidx.compose.ui.semantics.Role.Button; onClick(label = "Tap") { tap(); true } }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            val (big, small) = when (phase) {
                0 -> "Tap to start" to "Five rounds. When the screen turns green, tap as fast as you can."
                1 -> "Wait for green…" to "Round ${round + 1} of 5"
                2 -> "TAP!" to ""
                3 -> "${times.last()} ms" to "Tap for round ${round + 1} of 5"
                4 -> "Too soon!" to "Wait for green. Tap to try again."
                else -> "Average ${times.average().toInt()} ms" to "Best ${times.min()} ms" + (if (newBest) " · new record!" else "") + "\nTap to play again"
            }
            Text(big, style = MaterialTheme.typography.displaySmall, color = Color.White, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(small, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = .85f), textAlign = TextAlign.Center)
            if (times.isNotEmpty() && phase != 2) Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                times.forEach { t -> Text("$t", color = Color.White, modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = .2f)).padding(horizontal = 8.dp, vertical = 4.dp)) }
            }
        }
    }
}


/** A Year of Money, as a card for the Games hub. */
@Composable
fun SimCard(nav: Nav) {
    val pr = AppGraph.progress
    Surface(onClick = { nav.sim() }, shape = RoundedCornerShape(26.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 7.dp)) {
        Row(Modifier.background(Brush.linearGradient(listOf(Color(0xFF0F5F55), Color(0xFF1F4E79)))).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = .18f)), contentAlignment = Alignment.Center) { Text("💰", fontSize = 34.sp) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("MONEY SIMULATOR", color = Gold, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                Text("A Year of Money", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(if (pr.simBest() >= 0) "Best score ${pr.simBest()} · ${pr.simRuns()} years played" else "12 months, your choices, real lessons",
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = .85f))
            }
        }
    }
}
