import com.android.tools.idea.connection.assistant.actions.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory

object Utils {
    fun transformInputClassNameToFileName(className:String): String {
        return if (!className.contains("_")) {
            className.upperCharToUnderLine()
        } else {
            className
        }
    }

    //下划线和小写转大写字母
    fun upperTable(str: String): String {
        // 字符串缓冲区
        val sbf = StringBuffer()
        // 如果字符串包含 下划线
        if (str.contains("_")) {
            // 按下划线来切割字符串为数组
            val split = str.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            // 循环数组操作其中的字符串
            var i = 0
            val index = split.size
            while (i < index) {
                // 递归调用本方法
                val upperTable = upperTable(split[i])
                // 添加到字符串缓冲区
                sbf.append(upperTable)
                i++
            }
        } else {// 字符串不包含下划线
            // 转换成字符数组
            val ch = str.toCharArray()
            // 判断首字母是否是字母
            if (ch[0] in 'a'..'z') {
                // 利用ASCII码实现大写
                ch[0] = (ch[0].toInt() - 32).toChar()
            }
            // 添加进字符串缓存区
            sbf.append(ch)
        }
        // 返回
        return sbf.toString()
    }

    /**
     * 判断Directory中是否包含这个file
     */
    fun containsDirectoryFile(directory: PsiDirectory, fileName: String): Boolean {
        var name = directory.files.filter { it.name.endsWith(".dart") }
            .firstOrNull { it.name.contains(fileName) }
        return name != null
    }

    fun Project.showErrorMessage(notifyMessage: String) {
        Messages.showInfoMessage(this, notifyMessage, "Info");
    }
}