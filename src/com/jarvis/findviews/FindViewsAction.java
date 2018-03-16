package com.jarvis.findviews;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.wm.impl.CommandProcessor;
import com.intellij.openapi.wm.impl.commands.FinalizableCommand;
import com.intellij.psi.*;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.jarvis.findviews.bean.ResIdBean;
import com.jarvis.findviews.util.ClassDataWriter;
import com.jarvis.findviews.util.PsiFileUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by JarvisLau on 2018/3/6.
 * Description:
 */
public class FindViewsAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getData(DataKeys.EDITOR);
        PsiFile psiFile = anActionEvent.getData(DataKeys.PSI_FILE);
        PsiElement psiElement = PsiFileUtils.getPsiElementByEditor(editor, psiFile);
        if (editor != null && psiFile != null && psiElement != null) {
            String name = String.format("%s.xml", psiElement.getText());
            PsiFile rootXmlFile = PsiFileUtils.getFileByName(psiElement, name);
            if (rootXmlFile != null) {
                ArrayList<ResIdBean> resIdBeans = new ArrayList<>();
                PsiFileUtils.getResIdBeans(rootXmlFile, resIdBeans);
                PsiClass psiClass = PsiFileUtils.getClassByClassFile(psiFile);
                new ClassDataWriter(psiFile, resIdBeans, psiClass).execute();
            }
        }
    }
}