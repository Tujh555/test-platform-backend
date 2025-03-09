package com.example.quiz.service

import com.example.auth.database.Users
import com.example.common.*
import com.example.models.AnswerDto
import com.example.models.QuestionDto
import com.example.models.QuizDto
import com.example.models.UserDto
import com.example.quiz.RegisterQuizRequest
import com.example.quiz.SolveQuizRequest
import com.example.quiz.database.Answers
import com.example.quiz.database.Questions
import com.example.quiz.database.Quizzes
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import java.time.Instant
import java.util.UUID

class QuizService {
    suspend fun allQuizzes(limit: Int, cursor: Instant) = success { quizzes(limit, cursor) }

    suspend fun ownQuizzes(limit: Int, cursor: Instant, userId: String) = success {
        quizzes(limit, cursor) { adjustWhere { Quizzes.authorId eq UUID.fromString(userId) } }
    }

    suspend fun search(limit: Int, cursor: Instant, query: String) = success {
        if (query.isNotBlank()) {
            quizzes(limit, cursor) { adjustWhere { (Quizzes.title like "%$query%") or (Quizzes.theme like "%$query%") } }
        } else {
            quizzes(limit, cursor)
        }
    }

    suspend fun register(request: RegisterQuizRequest) {
        val quiz = request.quiz
        val rightAnswers = request.rightAnswers
        query {
            val quizRow = Quizzes.insert {
                it[title] = quiz.title.orEmpty()
                it[duration] = quiz.durationMinutes ?: 5
                it[authorId] = UUID.fromString(quiz.author?.id)
                it[theme] = quiz.theme.orEmpty()
                it[createdAt] = Instant.now()
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
                val right = rightAnswers[i].orEmpty().toSet()
                question.variants.orEmpty().mapIndexed { index, answer ->
                    Triple(id, answer, index in right)
                }
            }

            Answers.batchInsert(markedAnswers) { (questionId, answer, isRight) ->
                this[Answers.questionId] = questionId
                this[Answers.text] = answer.text.orEmpty()
                this[Answers.isRight] = isRight
            }
        }
    }

    suspend fun solve(request: SolveQuizRequest, userId: String): Response<Map<String, Boolean>> {
        val quizId = UUID.fromString(request.id)
        val userAnswers = request.answers

        println("userAnswers = $userAnswers")

        val result = queryCatching {
            val solvers = Quizzes
                .selectAll()
                .where { Quizzes.id eq quizId }
                .firstOrNull()
                ?.get(Quizzes.solversId)
                .orEmpty()

            Quizzes.update({ Quizzes.id eq quizId }) { it[solversId] = solvers + userId }

            Questions.selectAll().where { Questions.quizId eq quizId }.associate { question ->
                val questionId = question[Questions.id]
                println("--> question = $questionId")
                val marked = userAnswers[questionId.toString()].orEmpty().toSet()
                println("marked = $marked")

                println("all answers")

                Answers.selectAll().where { Answers.questionId eq questionId }.forEach {
                    println("${it[Answers.id]} -> ${it[Answers.isRight]}")
                }

                val rightAnswers = Answers
                    .selectAll()
                    .where { Answers.questionId eq questionId }
                    .mapNotNullTo(mutableSetOf()) {
                        if (it[Answers.isRight]) {
                            it[Answers.id].toString()
                        } else {
                            null
                        }
                    }

                println("rightAnswers = $rightAnswers")

                questionId.toString() to (marked == rightAnswers)
            }
        }

        return result?.let { success { it } } ?: _error(404)
    }

    private suspend fun quizzes(limit: Int, cursor: Instant, quizBlock: Query.() -> Query = { this }) = query {
        Quizzes
            .selectAll()
            .orderBy(Quizzes.createdAt, SortOrder.DESC)
            .where { Quizzes.createdAt less cursor }
            .quizBlock()
            .limit(limit)
            .map { quiz ->
                val questions = Questions
                    .selectAll()
                    .where { Questions.quizId eq quiz[Quizzes.id] }
                    .map { question ->
                        val answers = Answers
                            .selectAll()
                            .where { Answers.questionId eq question[Questions.id] }
                            .map { answer ->
                                AnswerDto(
                                    id = answer[Answers.id].toString(),
                                    text = answer[Answers.text]
                                )
                            }
                        QuestionDto(
                            id = question[Questions.id].toString(),
                            text = question[Questions.text],
                            variants = answers,
                            type = question[Questions.type]
                        )
                    }

                val author = Users
                    .selectAll()
                    .where { Users.id eq quiz[Quizzes.authorId] }
                    .firstOrNull()?.let {
                        UserDto(
                            id = it[Users.id].toString(),
                            avatar = it[Users.avatar],
                            name = it[Users.name].orEmpty()
                        )
                    }

                val solversId = quiz[Quizzes.solversId].map(UUID::fromString)
                val lastSolvers = Users
                    .selectAll()
                    .where { Users.id inList solversId }
                    .limit(3)
                    .map {
                        UserDto(
                            id = it[Users.id].toString(),
                            avatar = it[Users.avatar],
                            name = it[Users.name].orEmpty()
                        )
                    }

                QuizDto(
                    id = quiz[Quizzes.id].toString(),
                    title = quiz[Quizzes.title],
                    durationMinutes = quiz[Quizzes.duration],
                    questions = questions,
                    author = author,
                    solvedCount = solversId.size,
                    lastSolvers = lastSolvers,
                    createdAt = quiz[Quizzes.createdAt].toString(),
                    theme = quiz[Quizzes.theme]
                )
            }
    }
}