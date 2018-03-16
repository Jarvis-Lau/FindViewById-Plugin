package com.jarvis.findviews.util;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.jarvis.findviews.bean.ResIdBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by JarvisLau on 2018/3/16.
 * Description:
 */
public class ClassDataWriter extends WriteCommandAction {

    private List<ResIdBean> resIdBeans;
    private PsiClass psiClass;
    private PsiFile psiFile;

    public ClassDataWriter(PsiFile psiFile, List<ResIdBean> resIdBeans, PsiClass psiClass) {
        this(psiFile.getProject(), psiFile);
        this.psiFile = psiFile;
        this.resIdBeans = resIdBeans;
        this.psiClass = psiClass;
    }

    private ClassDataWriter(@Nullable Project project, PsiFile... files) {
        super(project, files);
    }

    @Override
    protected void run(@NotNull Result result) throws Throwable {
        writeFindViews();
    }

    private void writeFindViews() {
        StringBuilder method = new StringBuilder();
        String methodBegin = "";
        if (isActivity()) {
            methodBegin = "private void findViews(){";
        } else {
            methodBegin = "private void findViews(View view){";
        }
        String methodEnd = "}";
        PsiElementFactory psiElementFactory = PsiElementFactory.SERVICE.getInstance(psiFile.getProject());
        for (ResIdBean resIdBean : resIdBeans) {
            if (psiClass.findFieldByName(resIdBean.getId(), false) == null) {
                String field = "private " +
                        resIdBean.getName() +
                        " " +
                        resIdBean.getId() +
                        ";";
                PsiField fieldElement = psiElementFactory.createFieldFromText(field, psiClass);
                psiClass.add(fieldElement);
            }
            PsiMethod[] methods = psiClass.findMethodsByName("findViews", false);
            PsiMethod findViewsMethod = methods.length > 0 ? methods[0] : null;
            if (findViewsMethod == null) {
                psiClass.add(psiElementFactory.createMethodFromText((methodBegin + methodEnd), psiClass));
            }
            if (findViewsMethod != null) {
                PsiCodeBlock body = findViewsMethod.getBody();
                if (body != null && !body.getText().contains(resIdBean.getId())) {
                    appendFindViewsMethodBody(method, resIdBean);
                }
            }
        }
        if (method.length() != 0) {
            PsiMethod[] methods = psiClass.findMethodsByName("findViews", false);
            PsiMethod findViewsMethod = methods.length > 0 ? methods[0] : null;
            if (findViewsMethod != null) {
                PsiCodeBlock body = findViewsMethod.getBody();
                if (body != null) {
                    StringBuilder codeBlock = new StringBuilder(body.getText());
                    body.delete();
                    codeBlock.insert(codeBlock.length() - 1, method.toString());
                    findViewsMethod.add(psiElementFactory.createCodeBlockFromText(codeBlock.toString(), findViewsMethod));
                }
            }
        }
        NotifyUtils.showInfo(psiFile.getProject(),"success");
    }

    private void appendFindViewsMethodBody(StringBuilder method, ResIdBean resIdBean) {
        String findViewById = "";
        if (isActivity()) {
            findViewById = "findViewById(";
        } else {
            findViewById = "view.findViewById(";
        }
        method.append(resIdBean.getId())
                .append(" = ")
                .append("(")
                .append(resIdBean.getName())
                .append(")")
                .append(findViewById)
                .append("R.id.")
                .append(resIdBean.getId())
                .append(");");
    }

    private boolean isActivity() {
        GlobalSearchScope scope = GlobalSearchScope.allScope(psiFile.getProject());
        PsiClass activityClass = JavaPsiFacade.getInstance(psiFile.getProject()).findClass(
                "android.app.Activity", scope);
        return activityClass != null && psiClass.isInheritor(activityClass, false);
    }
}