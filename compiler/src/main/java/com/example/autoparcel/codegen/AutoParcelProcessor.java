package com.example.autoparcel.codegen;

import com.example.autoparcel.AutoParcel;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.example.autoparcel.AutoParcel")
public final class AutoParcelProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(AutoParcel.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(AutoParcel.class)) {

            // We can cast it, because we know that it of ElementKind.CLASS
            TypeElement typeElement = (TypeElement) annotatedElement;

            Name simpleName = typeElement.getSimpleName();

            PackageElement pckg = (PackageElement) typeElement.getEnclosingElement();
            pckg.getQualifiedName();

            List<VariableElement> list = typeElement.getEnclosedElements().stream()
                    .filter(o -> o.getKind() == ElementKind.FIELD)
                    .map(o -> (VariableElement) o)
                    .filter(variableElement -> !variableElement.getModifiers().contains(Modifier.PUBLIC)
                            && !variableElement.getModifiers().contains(Modifier.TRANSIENT))
                    .collect(Collectors.toList());


            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(simpleName.toString() + "Columns")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            list.forEach(variableElement -> {
//                System.out.println("-----------------------------");
//                System.out.println(varToString(variableElement));
//                System.out.println("-----------------------------");
                FieldSpec field = FieldSpec.builder(String.class, transformToConstantCase(variableElement.getSimpleName().toString()))
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", variableElement.getSimpleName())
                        .build();

                classBuilder.addField(field);
            });

            JavaFile javaFile = JavaFile.builder(pckg.toString() + ".columns", classBuilder.build()).build();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                error(typeElement, "failed to generate class for variableElement");
            }
        }
        return true;
    }

    private String transformToConstantCase(String camelCaseString) {
        StringBuilder builder = new StringBuilder();
        for(char ch: camelCaseString.toCharArray()) {
            builder.append(Character.isUpperCase(ch) ? "_" : "");
            builder.append(Character.toUpperCase(ch));
        }
        return builder.toString();
    }

    private String varToString(VariableElement variableElement) {
        Element enclosingElement = variableElement.getEnclosingElement();
        return variableElement.getSimpleName() + ", " + enclosingElement + ", " + variableElement.asType();
    }


    private void error(Element e, String msg, Object... args) {
        System.out.println("******************* error start **********************");
        System.out.println(e.toString());
        System.out.println("******************* error ending **********************");
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }
}