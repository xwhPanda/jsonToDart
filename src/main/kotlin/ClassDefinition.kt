import com.android.tools.idea.connection.assistant.actions.Logger

class ClassDefinition(private val name: String, private val privateFields: Boolean = false) {
    val fields = mutableMapOf<String, TypeDefinition>()
    val dependencies: List<Dependency>
        get() {
            val dependenciesList = mutableListOf<Dependency>()
            val keys = fields.keys
            keys.forEach { k ->
                if (fields[k]!!.isPrimitive.not()) {
                    dependenciesList.add(Dependency(k, fields[k]!!))
                }
            }
            return dependenciesList;
        }

    fun addField(key: String, typeDef: TypeDefinition) {
        fields[key] = typeDef
    }

    fun hasField(otherField: TypeDefinition): Boolean {
        return fields.keys.firstOrNull { k -> fields[k] == otherField } != null
    }

    override operator fun equals(other: Any?): Boolean {
        if (other is ClassDefinition) {
            if (name != other.name) {
                return false;
            }
            return fields.keys.firstOrNull { k ->
                other.fields.keys.firstOrNull { ok ->
                    fields[k] == other.fields[ok]
                } == null
            } == null
        }
        return false
    }

    private fun _addTypeDef(typeDef: TypeDefinition, sb: StringBuffer, suffix: String) {
        if (typeDef.name == "Null") {
            sb.append("dynamic")
        } else {
            sb.append(typeDef.name)
            if (typeDef.subtype != null) {
                //如果是list,就把名字修改成单数
                sb.append("<${typeDef.subtype!!}>")
            }
            sb.append(suffix)
        }
    }

    //字段的集合
    private val _fieldList: String
        get() {
            val suffix = "?"
            return fields.keys.map { key ->
                val f = fields[key]
                val fieldName = fixFieldName(key, f, privateFields)
                val sb = StringBuffer();
                //如果驼峰命名后不一致,才这样
                if (fieldName != key) {
                    sb.append('\t')
                    sb.append("/// JSONField(name: \"${key}\")\n")
                }
                sb.append('\t')
                _addTypeDef(f!!, sb, suffix)
                sb.append(" $fieldName;")
                return@map sb.toString()
            }.joinToString("\n")
        }

    private val _thisFieldList: String
        get() {
            return fields.keys.map { key ->
                val f = fields[key]
                val fieldName = fixFieldName(key, f, privateFields)
                val sb = StringBuffer()
                sb.append("\t\tthis.${fieldName},")
                return@map sb.toString()
            }.joinToString("\n")
        }

    private val fromJson: String
        get() {
            var sb = StringBuffer()
            sb.append("${name}.fromJson(Map<String, dynamic> json) {\n")
            fields.keys.forEach { key ->
                val fieldName = fixFieldName(key, fields[key], privateFields)
                if (fields[key]!!.isPrimitive) {
                    if (fields[key]!!.name == "List") {
                        sb.append("\t\t$fieldName = json['$key'].cast<${fields[key]!!.subtype}>();\n")
                    } else {
                        sb.append("\t\t$fieldName = json['$key'];\n")
                    }
                } else if (fields[key]!!.name == "List" && fields[key]!!.name == "DateTime") {
                    sb.append("\t\t$fieldName = json['$key'].map((v) => DateTime.tryParse(v));\n")
                } else if (fields[key]!!.name == "DateTime") {
                    sb.append("\t\t$fieldName = DateTime.tryParse(json['$key']);\n")
                } else if (fields[key]!!.name == "List") {
                    sb.append("\t\tif (json['${key}'] != null){\n")
                    sb.append("\t\t\t\t$fieldName = <${fields[key]!!.subtype}>[];\n")
                    sb.append("\t\t\t\tjson['${key}'].forEach((v) {\n")
                    sb.append("\t\t\t\t\t\t${fieldName}!.add(${fields[key]!!.subtype}.fromJson(v));\n")
                    sb.append("\t\t\t\t});\n")
                    sb.append("\t\t}\n")
                } else {
                    sb.append("\t\t${fieldName} = json['${key}'] != null ? ")
                    _addTypeDef(fields[key]!!, sb, "")
                    sb.append(".fromJson(json['${key}']) : null;\n")
                }
            }
            sb.append("\t}\n")
            return sb.toString()
        }

    private val _toJson: String
        get() {
            var sb = StringBuilder()
            sb.append("Map<String, dynamic> toJson(){")
            sb.append("\n")
            sb.append("\t\tfinal Map<String, dynamic> data = <String, dynamic>{};")
            sb.append("\n")
            fields.keys.forEach { key ->
                val fieldName = fixFieldName(key, fields[key], privateFields)
                val listSubType = getListSubTypeCanNull(fields[key]!!.name)
                when {
                    fields[key]!!.isPrimitive -> {
                        sb.append("\t\tdata[\"${key}\"] = ${fieldName};\n")
                    }
                    fields[key]!!.name == "List" -> {
                        sb.append("\t\tif (${fieldName} != null){\n")
                        sb.append("\t\t\t\tdata[\"${key}\"] = ${fieldName}!.map((v) => v.toJson()).toList();\n")
                        sb.append("\t\t}\n")
                    }
                    else -> {
                        sb.append("\t\tif (${fieldName} != null){\n")
                        sb.append("\t\t\t\tdata[\"${key}\"] = ${fieldName}!.toJson();\n")
                        sb.append("\t\t}\n")
                    }
                }
            }
            sb.append("\t\treturn data;\n")
            sb.append("\t}")
            return sb.toString()
        }


    override fun toString(): String {
        return if (privateFields) {
//            "class $name {\n$_fieldList\n\n$_defaultPrivateConstructor\n\n$_gettersSetters\n\n$_jsonParseFunc\n\n$_jsonGenFunc\n}\n";
            ""
        } else {
            """
class $name {
$_fieldList
    ${name}({
$_thisFieldList});  
    $fromJson
    $_toJson
}""".trimIndent();
        }
    }
}


class Dependency(var name: String, var typeDef: TypeDefinition) {
    val className: String
        get() {
            return camelCase(name)
        }

    override fun toString(): String {
        return "name = ${name} ,typeDef = ${typeDef}"
    }
}

class TypeDefinition(var name: String, var subtype: String? = null) {


    val isPrimitive: Boolean = if (subtype == null) {
        isPrimitiveType(name)
    } else {
        isPrimitiveType("$name<${subtype!!.toUpperCaseFirstOne()}>")
    }
    private val isPrimitiveList: Boolean

    companion object {
        fun fromDynamic(obj: Any?): TypeDefinition {
            val type = getTypeName(obj)
            if (type == "List") {
                val list = obj as List<*>
                val firstElementType = if (list.isNotEmpty()) {
                    getTypeName(list[0])
                } else {
                    "dynamic"
                }
                return TypeDefinition(type, firstElementType)
            }
            return TypeDefinition(type)
        }
    }

    init {
        isPrimitiveList = isPrimitive && name == "List"
    }


    override operator fun equals(other: Any?): Boolean {
        if (other is TypeDefinition) {
            return name == other.name && subtype == other.subtype;
        }
        return false;
    }


    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (subtype?.hashCode() ?: 0)
        result = 31 * result + isPrimitive.hashCode()
        result = 31 * result + isPrimitiveList.hashCode()
        return result
    }

    override fun toString(): String {
        return "TypeDefinition(name='$name', subtype=$subtype, isPrimitive=$isPrimitive, isPrimitiveList=$isPrimitiveList)"
    }


}