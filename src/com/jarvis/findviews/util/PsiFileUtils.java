package com.jarvis.findviews.util;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.jarvis.findviews.bean.ResIdBean;

import java.util.ArrayList;

/**
 * Created by JarvisLau on 2018/3/14.
 * Description:
 */
public class PsiFileUtils {

    public static PsiElement getPsiElementByEditor(Editor editor, PsiFile psiFile) {
        if (editor == null || psiFile == null) {
            return null;
        }
        CaretModel caret = editor.getCaretModel();
        PsiElement psiElement = psiFile.findElementAt(caret.getOffset());
        if (psiElement != null) {
            if (psiElement.getParent().getText().startsWith("R.layout.")) {
                return psiElement;
            }
        }
        NotifyUtils.showError(psiFile.getProject(), "No Layout Found");
        return null;
    }

    public static PsiFile getFileByName(PsiElement psiElement, String fileName) {
        Module moduleForPsiElement = ModuleUtil.findModuleForPsiElement(psiElement);
        if (moduleForPsiElement != null) {
            GlobalSearchScope searchScope = GlobalSearchScope.moduleScope(moduleForPsiElement);
            Project project = psiElement.getProject();
            PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, fileName, searchScope);
            if (psiFiles.length != 0) {
                return psiFiles[0];
            }
        }
        NotifyUtils.showError(psiElement.getProject(), "No Layout Found");
        return null;
    }

    public static void getResIdBeans(PsiFile psiFile, ArrayList<ResIdBean> container) {
        psiFile.accept(new XmlRecursiveElementVisitor(true) {
            @Override
            public void visitXmlTag(XmlTag tag) {
                super.visitXmlTag(tag);
                if (tag.getName().equals("include")) {
                    XmlAttribute layout = tag.getAttribute("layout");
                    if (layout != null) {
                        String value = layout.getValue();
                        if (value != null && value.startsWith("@layout/")) {
                            String[] split = value.split("/");
                            String name = split[1];
                            String xmlName = String.format("%s.xml", name);
                            PsiFile fileByName = PsiFileUtils.getFileByName(psiFile, xmlName);
                            getResIdBeans(fileByName, container);
                        }
                    }
                } else {
                    XmlAttribute attribute = tag.getAttribute("android:id");
                    if (attribute != null) {
                        String idValue = attribute.getValue();
                        if (idValue != null && idValue.startsWith("@+id/")) {
                            String[] split = idValue.split("/");
                            String className;
                            if (tag.getName().startsWith("com.")) {
                                String[] custom = tag.getName().split("\\.");
                                className = custom[custom.length - 1];
                            } else {
                                className = tag.getName();
                            }
                            String id = split[1];
                            container.add(new ResIdBean(className, id));
                        }
                    }
                }
            }
        });
    }

    public static PsiClass getClassByClassFile(PsiFile classFile) {
        GlobalSearchScope globalSearchScope = GlobalSearchScope.fileScope(classFile);
        String fullName = classFile.getName();
        String className = fullName.split("\\.")[0];
        return PsiShortNamesCache.getInstance(classFile.getProject()).getClassesByName(className, globalSearchScope)[0];
    }
}
