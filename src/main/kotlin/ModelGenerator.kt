import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.project.Project


class ModelGenerator(
    val className: String,
    val jsonData: String,
    val project: Project
) {
    var isFirstClass = true
    var allClasses = mutableListOf<ClassDefinition>()

    //parentType 父类型 是list 或者class
    private fun generateClassDefinition(
        className: String,
        jsonRawData: Any,
        parentType: String = ""
    ): MutableList<ClassDefinition> {
        if (jsonRawData is List<*>) {
            // if first element is an array, start in the first element.
            generateClassDefinition(className, jsonRawData[0]!!)
        } else if (jsonRawData is Map<*, *>) {
            val keys = jsonRawData.keys
            //如果是list,就把名字修改成单数
            val classDefinition = ClassDefinition(className)
            keys.forEach { key ->
                val typeDef = TypeDefinition.fromDynamic(jsonRawData[key])
                if (typeDef.name == "Class") {
                    typeDef.name = camelCase(key as String)
                }
                if (typeDef.subtype != null && typeDef.subtype == "Class") {
                    typeDef.subtype = camelCase(key as String)
                }
                classDefinition.addField(key as String, typeDef)
            }
            if (allClasses.firstOrNull { cd -> cd == classDefinition } == null) {
                allClasses.add(classDefinition)
            }
            val dependencies = classDefinition.dependencies
            dependencies.forEach { dependency ->
                if (dependency.typeDef.name == "List") {
                    if (((jsonRawData[dependency.name]) as? List<*>)?.isNotEmpty() == true) {
                        val names = (jsonRawData[dependency.name] as List<*>)
                        generateClassDefinition(dependency.className, names[0]!!, "list")
                    }
                } else {
                    generateClassDefinition(dependency.className, jsonRawData[dependency.name]!!)
                }
            }
        }
        return allClasses
    }

    fun generateDartClassesToString(fileName: String): String {
        //用阿里的防止int变为double 已解决 还是用google的吧 https://www.codercto.com/a/73857.html
//        val jsonRawData = JSON.parseObject(collectInfo.userInputJson)
        val originalStr = jsonData.trim()
        val gson = GsonBuilder()
            .registerTypeAdapter(object : TypeToken<Map<String, Any>>() {}.type, GsonUtil.MapTypeAdapter()).create()

        val jsonRawData = if (originalStr.startsWith("[")) {
            val list: List<Any> = gson.fromJson(originalStr, object : TypeToken<List<Any>>() {}.type)
            try {
                (JsonUtils.jsonMapMCompletion(list) as List<*>).first()
            } catch (e: Exception) {
                mutableMapOf<String, Any>()
            }
        } else {
            gson.fromJson<Map<String, Any>>(originalStr, object : TypeToken<Map<String, Any>>() {}.type)
        }
        val classContentList = generateClassDefinition(
            firstClassName(), JsonUtils.jsonMapMCompletion(jsonRawData)
                ?: mutableMapOf<String, Any>()
        )
        val classContent = classContentList.joinToString("\n\n")
        classContentList.fold(mutableListOf<TypeDefinition>()) { acc, de ->
            acc.addAll(de.fields.map { it.value })
            acc
        }
        val stringBuilder = StringBuilder()
        stringBuilder.append(classContent)
        //生成helper类

        //生成
        return stringBuilder.toString()
    }

    //用户输入的名字转为首个class的名字(文件中的类名)
    fun firstClassName(): String {
        return if (className.contains("_")) {
            (upperTable(className)).toUpperCaseFirstOne()
        } else {
            (className).toUpperCaseFirstOne()
        }
    }
}