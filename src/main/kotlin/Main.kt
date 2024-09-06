
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
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger

fun main(){
    embeddedServer(Netty, port = 8081, watchPaths = listOf("ktorServer"), module = Application::myApp).start(wait = true)

}

fun Application.myApp(){
      install(ContentNegotiation){
          jackson {  }
      }
    val database =  Database.connect(
        url = "jdbc:postgresql://localhost:5432/your-database-name",
        driver = "org.postgresql.Driver",
        user = "your-username",
        password = "your-password"
    )
    transaction {
         SchemaUtils.create(UserInfo)
    }
    routing {
        post("/user/signUp"){
            val user = call.receive<User>()
            if(!user.validate())
                call.respond(HttpStatusCode.BadRequest,ErrorResponse(HttpStatusCode.BadRequest.value,"Username Or Password length should be more than 5 letters!"))
            val userId = transaction {
                UserInfo.insert {
                    it[username] = user.username
                    it[password] = user.password
                } get UserInfo.id
            }
            call.respond(HttpStatusCode.OK,Response(responseCode = HttpStatusCode.OK.value, msg = "User Created Successfully Userid : $userId"))
        }
        post("/user/login") {
            val user = call.receive<User>()
            if(!user.validate()) {
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
                    UserInfo.selectAll().map { it.toUser() }
                }
                if(users.isEmpty())
                    call.respond(HttpStatusCode.BadRequest,ErrorResponse(HttpStatusCode.BadRequest.value,"You are not registered."))
                else {
                    call.respond(HttpStatusCode.OK, Response(HttpStatusCode.OK.value, "Login Successfully ${users[0]}"))
                }
            }


        }
    }
}
data class User(val username:String,val password:String){
fun validate() = !(username.length < 5 || password.length < 5)
}
object UserInfo : Table(){
    val username = varchar("username",50)
    val password = varchar("password",30)
    val id = integer("user_id").autoIncrement()
    override val primaryKey = PrimaryKey(id, name = "user_id")
}
data class ErrorResponse(val responseCode:Int,val msg:String)
data class Response(val responseCode:Int,val msg:String,val data:Any?=null)
fun ResultRow.toUser() = User(this[UserInfo.username],this[UserInfo.password])