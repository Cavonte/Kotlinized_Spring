package temper.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class User(@Id @GeneratedValue val userId: Long? = null, private val firstName: String = "", private val lastName: String = "", private val email: String = "")
{
    fun getIdentifier(): String
    {
        return "${lastName}_$email"
    }
}
