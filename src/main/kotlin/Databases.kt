package com.example

import com.example.auth.database.Tokens
import com.example.auth.database.Users
import com.example.models.AnswerDto
import com.example.models.QuestionDto
import com.example.models.QuizDto
import com.example.models.UserDto
import com.example.quiz.database.Answers
import com.example.quiz.database.Questions
import com.example.quiz.database.Quizzes
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextInt

private val mockUsers = List(20) {
    UserDto(
        id = UUID.randomUUID().toString(),
        avatar = null,
        name = "Test user $it"
    )
}

private val titles = listOf(
    "My quiz",
    "Test quiz",
    "Integers",
    "Statistic math",
    "Saturday night",
    "Leaders",
    "Politics",
    "Geography",
    "Russia",
    "Morning quiz"
)
private val types = listOf("multiple", "single")
private val themes = listOf("General knowledge", "Cities", "Countries", "Geography", "Math", "Programming", "Design", "Study")

private val mockQuizzes = List(50) {
    val solvers = mockUsers.shuffled().take(nextInt(1, 50))
    QuizDto(
        id = UUID.randomUUID().toString(),
        title = "${titles.random()} $it",
        durationMinutes = nextInt(1, 20),
        questions = List(nextInt(1, 5)) {
            QuestionDto(
                id = UUID.randomUUID().toString(),
                text = "Mock Question number $it",
                variants = List(nextInt(1, 10)) {
                    AnswerDto(
                        id = UUID.randomUUID().toString(),
                        text = "Answer №$it"
                    )
                },
                type = types.random()
            )
        },
        author = mockUsers.random(),
        solvedCount = solvers.size,
        lastSolvers = solvers,
        createdAt = Instant.now().minus(it.toLong(), ChronoUnit.DAYS).toString(),
        theme = themes.random()
    )
}

fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    transaction(database) {
        SchemaUtils.create(Users, Tokens, Quizzes, Questions, Answers)

        Users.batchInsert(mockUsers) { user ->
            this[Users.email] = UUID.randomUUID().toString()
            this[Users.password] = "psw"
            this[Users.name] = user.name
            this[Users.id] = UUID.fromString(user.id)
        }

        mockQuizzes.forEach { quiz ->
            val quizRow = Quizzes.insert {
                it[title] = quiz.title.orEmpty()
                it[duration] = quiz.durationMinutes ?: 5
                it[authorId] = UUID.fromString(quiz.author?.id)
                it[theme] = quiz.theme.orEmpty()
                it[createdAt] = Instant.parse(quiz.createdAt.orEmpty())
            }

            val questionsRow = Questions.batchInsert(
                data = quiz.questions.orEmpty(),
                shouldReturnGeneratedValues = true,
                body = { question ->
                    this[Questions.type] = question.type.orEmpty()
                    this[Questions.quizId] = quizRow[Quizzes.id]
                    this[Questions.text] = question.text.orEmpty()
                }
            )
            val markedAnswers = quiz.questions.orEmpty().flatMapIndexed { i, question ->
                val id = questionsRow[i][Questions.id]
                question.variants.orEmpty().map { answer ->
                    Triple(id, answer, nextBoolean())
                }
            }

            Answers.batchInsert(markedAnswers) { (questionId, answer, isRight) ->
                this[Answers.questionId] = questionId
                this[Answers.text] = answer.text.orEmpty() + if (isRight) "(right)" else ""
                this[Answers.isRight] = isRight
            }
        }
    }
}