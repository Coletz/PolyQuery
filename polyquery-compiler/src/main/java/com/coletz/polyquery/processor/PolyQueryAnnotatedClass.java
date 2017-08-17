package com.coletz.polyquery.processor;

import com.coletz.polyquery.annotation.PolyQuery;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

public class PolyQueryAnnotatedClass {

    private TypeElement annotatedClassElement;
    private String qualifiedSuperClassName;
    private String simpleTypeName;

    public PolyQueryAnnotatedClass(TypeElement classElement) throws IllegalArgumentException {
        this.annotatedClassElement = classElement;
        PolyQuery annotation = classElement.getAnnotation(PolyQuery.class);

        // Get the full QualifiedTypeName
        try {
            Class<?> clazz = annotation.value();
            qualifiedSuperClassName = clazz.getCanonicalName();
            simpleTypeName = clazz.getSimpleName();
        } catch (MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedSuperClassName = classTypeElement.getQualifiedName().toString();
            simpleTypeName = classTypeElement.getSimpleName().toString();
        }
    }

    /**
     * Get the full qualified name of the type specified in  {@link PolyQuery#value()}.
     *
     * @return qualified name
     */
    public String getQualifiedGroupName() {
        return qualifiedSuperClassName;
    }

    /**
     * Get the simple name of the type specified in  {@link PolyQuery#value()}.
     *
     * @return qualified name
     */
    public String getSimpleGroupName() {
        return simpleTypeName;
    }

    /**
     * The original element that was annotated with @Factory
     */
    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }
}