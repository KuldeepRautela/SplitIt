import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import models.ErrorResponse
import models.Group
import models.Response
import models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    embeddedServer(
        Netty,
        port = 8081,
        watchPaths = listOf("ktorServer"),
        module = Application::myApp
    ).start(wait = true)

}
fun Application.myApp() {
    install(ContentNegotiation) {
        jackson { }
    }
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/SplitIt",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "pswd"
    )
    transaction {
        SchemaUtils.create(UserInfo,Groups,GroupMembers)
    }
    routing {
        get("/first") {
            call.respondText("Server Runnin")
        }
        post("/user/signUp") {
            val user = call.receive<User>()
            if (!user.validate())
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        HttpStatusCode.BadRequest.value,
                        "Username Or Password length should be more than 5 letters!"
                    )
                )
            val userId = transaction {
                UserInfo.insert {
                    it[username] = user.username
                    it[password] = user.password
                } get UserInfo.id
            }
            call.respond(
                HttpStatusCode.OK,
                Response(responseCode = HttpStatusCode.OK.value, msg = "User Created Successfully Userid : $userId")
            )
        }

        post("/user/login") {
            val user = call.receive<User>()
            if (!user.validate()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        HttpStatusCode.BadRequest.value,
                        "Username Or Password length should be more than 5 letters!"
                    )
                )
                return@post
            }
            runBlocking {
                val users = transaction {
                    UserInfo.selectAll().map { it.toUser() }.filter {
                        it.username == user.username && it.password == user.password
                    }
                }
                if (users.isEmpty())
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(HttpStatusCode.BadRequest.value, "You are not registered.")
                    )
                else {
                    call.respond(HttpStatusCode.OK, Response(HttpStatusCode.OK.value, "Login Successfully ${users[0]}"))
                }
            }


        }
        post("/user/createGroup") {
            val group = call.receive<Group>()
            var groupId1 : Int? = null
            var memberGroupId : Int? = null
            transaction {
              groupId1 =   Groups.insert {
                    it[groupName] = group.groupName
                    it[createdBy] = group.createdBy
                    it[createdAt] = group.createdAt
                } get Groups.groupId
                group.groupMemberContact.forEach { contact ->
                    if (groupId1 != null) {
                        memberGroupId =   GroupMembers.insert {
                            it[groupId] = groupId1!!
                            it[groupMember] = contact
                            it[totalAmountSpent] = 0
                        } get GroupMembers.groupId
                    }
                }
            }

            if(groupId1 !=null && memberGroupId !=null)
            call.respond(HttpStatusCode.OK,"Your group is created")
            else
                call.respond(HttpStatusCode.Conflict,"Something went wrong!")
        }
    }
}

fun ResultRow.toUser() = User(this[UserInfo.username], this[UserInfo.password])
//curl -X POST http://localhost:8081/user/signUp -H "Content-Type: application/json" -d '{"username": "Kanchan Rautela", "password": "password123"}'
//Invoke-WebRequest -Uri "http://localhost:8081/user/signUp" -ContentType "application/json" -Method POST -Body "{"username": "Kanchan Rautela", "password": "password123"}"
