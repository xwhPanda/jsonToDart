import Utils.showErrorMessage
import Utils.transformInputClassNameToFileName
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.jetbrains.lang.dart.DartFileType
import com.jetbrains.lang.dart.psi.DartFile

class JsonToDartAction :AnAction(){
    override fun actionPerformed(action: AnActionEvent) {

        val project = action.getData(PlatformDataKeys.PROJECT) ?: return

        val dataContext = action.dataContext
        val module = LangDataKeys.MODULE.getData(dataContext) ?: return

        val navigatable = LangDataKeys.NAVIGATABLE.getData(dataContext)
        val directory = when (navigatable) {
            is PsiDirectory -> navigatable
            is PsiFile -> navigatable.containingDirectory
            else -> {
                val root = ModuleRootManager.getInstance(module)
                root.sourceRoots
                    .asSequence()
                    .mapNotNull {
                        PsiManager.getInstance(project).findDirectory(it)
                    }.firstOrNull()
            }
        } ?: return
        val psiFileFactory = PsiFileFactory.getInstance(project)


        var listener = InputDialog.ClickOkListener { className, json ->
            val fileName = transformInputClassNameToFileName(className)
            if (Utils.containsDirectoryFile(directory,"${fileName}.dart")){
                project.showErrorMessage("The ${fileName}.dart already exists")
            } else {
                val generatorClassContent = ModelGenerator(className,json, project).generateDartClassesToString(fileName)
                generateDartDataClassFile(
                    fileName,
                    generatorClassContent,
                    project,
                    psiFileFactory,
                    directory
                )
            }
        }
        var dialog = InputDialog(listener)
        dialog.setSize(600,400)
        dialog.setLocationRelativeTo(null)
        dialog.isVisible = true
    }

    private fun generateDartDataClassFile(
        fileName: String,
        classCodeContent: String,
        project: Project?,
        psiFileFactory: PsiFileFactory,
        directory: PsiDirectory
    ) {

        project.executeCouldRollBackAction {

            val file = psiFileFactory.createFileFromText("$fileName.dart", DartFileType.INSTANCE, classCodeContent) as DartFile
            directory.add(file)
            //包名
//            val packageName = (directory.virtualFile.path + "/$fileName.dart").substringAfter("${project!!.name}/lib/")
//            生成单个helper
//            FileHelpers.generateDartEntityHelper(project, "import 'package:${project.name}/${packageName}';", FileHelpers.getDartFileHelperClassGeneratorInfo(file))
            //此时应该重新生成所有文件

        }
    }


    private fun Project?.executeCouldRollBackAction(action: (Project?) -> Unit) {
        CommandProcessor.getInstance().executeCommand(this, {
            ApplicationManager.getApplication().runWriteAction {
                action.invoke(this)
            }
        }, "FlutterJsonBeanFactory", "FlutterJsonBeanFactory")
    }
}