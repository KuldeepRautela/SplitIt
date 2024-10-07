import org.jetbrains.exposed.sql.Table

object UserInfo : Table() {
    val username = varchar("username", 50)
    val password = varchar("password", 30)
    val id = integer("user_id").autoIncrement()
    override val primaryKey = PrimaryKey(id, name = "user_id")
}

object Groups : Table() {
    val groupId = integer("group_id").autoIncrement() // Primary Key
    val groupName = varchar("group_name", 255)
    val createdBy = varchar("created_by", 255) // userId
    val createdAt = varchar("created_at",100)

    override val primaryKey = PrimaryKey(groupId) // Define primary key
}

object GroupMembers : Table() {
    val groupId = integer("group_id").references(Groups.groupId) // Foreign Key
    val groupMember = varchar("group_member", 255)
    val totalAmountSpent = integer("total_amount_spent")

    override val primaryKey = PrimaryKey(groupId, groupMember) // Composite primary key
}