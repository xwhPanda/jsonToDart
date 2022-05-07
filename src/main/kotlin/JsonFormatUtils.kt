import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper

object JsonFormatUtils {
    @Throws(JsonProcessingException::class)
    fun formatJson(jsonStr: String?): String {
        val jsonObject = Holder.MAPPER.readValue(jsonStr, Any::class.java)
        return Holder.MAPPER.writer(Holder.DEFAULT_PRETTY_PRINTER).writeValueAsString(jsonObject)
    }

    @Throws(JsonProcessingException::class)
    fun minifyJson(jsonStr: String?): String {
        val jsonObject = Holder.MAPPER.readValue(jsonStr, Any::class.java)
        return Holder.MAPPER.writeValueAsString(jsonObject)
    }

    @Throws(JsonProcessingException::class)
    fun verifyJson(jsonStr: String?) {
        Holder.MAPPER.readValue(jsonStr, Any::class.java)
    }
}

object Holder{
    val MAPPER = ObjectMapper()
    val DEFAULT_PRETTY_PRINTER = CustomPrettyPrinter()
}

class CustomPrettyPrinter : DefaultPrettyPrinter {
    private val UNIX_LINE_FEED_INSTANCE = DefaultIndenter(" ", "\n")

    constructor() {
        super._objectFieldValueSeparatorWithSpaces = ": "
        super._arrayIndenter = UNIX_LINE_FEED_INSTANCE
        super._objectIndenter = UNIX_LINE_FEED_INSTANCE
    }

    override fun createInstance(): DefaultPrettyPrinter {
        return CustomPrettyPrinter()
    }
}