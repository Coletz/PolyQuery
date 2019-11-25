package com.coletz.polyquery.processor;

import com.coletz.polyquery.annotation.PolyQuery;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class PolyQueryProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Map<String, PolyQueryGroupedClasses> factoryClasses = new LinkedHashMap<>();

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
        Set<String> annotataions = new LinkedHashSet<>();
        annotataions.add(PolyQuery.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Itearate over all @Factory annotated elements
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(PolyQuery.class)) {
            // Check if a class has been annotated with @Factory
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, "Only classes can be annotated with @%s", PolyQuery.class.getSimpleName());
                return true; // Exit processing
            }

            // We can cast it, because we know that it of ElementKind.CLASS
            TypeElement typeElement = (TypeElement) annotatedElement;

            try {
                PolyQueryAnnotatedClass annotatedClass = new PolyQueryAnnotatedClass(typeElement); // throws IllegalArgumentException

                if (!isValidClass(annotatedClass)) {
                    return true; // Error message printed, exit processing
                }

                // Everything is fine, so try to add
                PolyQueryGroupedClasses factoryClass = factoryClasses.get(annotatedClass.getQualifiedGroupName());
                if (factoryClass == null) {
                    factoryClass = new PolyQueryGroupedClasses(annotatedClass);
                    factoryClasses.put(annotatedClass.getQualifiedGroupName(), factoryClass);
                }
                factoryClass.add(annotatedClass);
            } catch (IllegalArgumentException e) {
                error(typeElement, e.getMessage());
                return true;
            } catch (ProcessorException e) {
                PolyQueryAnnotatedClass existing = e.getExisting();
                error(annotatedElement, "Conflict: The class %s is annotated with @%s with id ='%s' but %s already uses the same id", typeElement.getQualifiedName().toString(), PolyQuery.class.getSimpleName(), existing.getTypeElement().getQualifiedName().toString());
                return true;
            }
        }

        try {
            for (PolyQueryGroupedClasses polyQueryClass : factoryClasses.values()) {
                note("PolyQuery - Processing class "+polyQueryClass.getSimpleClassName());
                polyQueryClass.init(elementUtils, filer);
                polyQueryClass.generateCode();
            }

            factoryClasses.clear();

        } catch (IOException e) {
            error(null, e.getMessage());
        }
        return true;
    }


    private boolean isValidClass(PolyQueryAnnotatedClass item) {

        // Cast to TypeElement, has more type specific methods
        TypeElement classElement = item.getTypeElement();

        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            error(classElement, "The class %s is not public.",
                    classElement.getQualifiedName().toString());
            return false;
        }

        // Check if it's an abstract class
        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error(classElement, "The class %s is abstract. You can't annotate abstract classes with @%", classElement.getQualifiedName().toString(), PolyQuery.class.getSimpleName());
            return false;
        }

        // Check inheritance: Class must be childclass as specified in @Factory.type();
        TypeElement superClassElement = elementUtils.getTypeElement(item.getQualifiedGroupName());
        if (superClassElement.getKind() == ElementKind.INTERFACE) {
            // Check interface implemented
            if (!classElement.getInterfaces().contains(superClassElement.asType())) {
                error(classElement, "The class %s annotated with @%s must implement the interface %s", classElement.getQualifiedName().toString(), PolyQuery.class.getSimpleName(), item.getQualifiedGroupName());
                return false;
            }
        } else {
            // Check subclassing
            TypeElement currentClass = classElement;
            while (true) {
                TypeMirror superClassType = currentClass.getSuperclass();

                if (superClassType.getKind() == TypeKind.NONE) {
                    // Basis class (java.lang.Object) reached, so exit
                    error(classElement, "The class %s annotated with @%s must inherit from %s", classElement.getQualifiedName().toString(), PolyQuery.class.getSimpleName(), item.getQualifiedGroupName());
                    return false;
                }

                if (superClassType.toString().equals(item.getQualifiedGroupName())) {
                    // Required super class found
                    break;
                }

                // Moving up in inheritance tree
                currentClass = (TypeElement) typeUtils.asElement(superClassType);
            }
        }

        // Check if an empty public constructor is given
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0 && constructorElement.getModifiers().contains(Modifier.PUBLIC)) {
                    // Found an empty constructor
                    return true;
                }
            }
        }

        // No empty constructor found
        error(classElement, "The class %s must provide an public empty default constructor", classElement.getQualifiedName().toString());
        return false;
    }

    private void note(String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }
}
