package com.coletz.polyquery.processor;

import com.coletz.polyquery.Constants;
import com.coletz.polyquery.core.QueryBuilder;
import com.coletz.polyquery.core.SupportedOperation;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

class PolyQueryGroupedClasses {

    private PolyQueryAnnotatedClass annotatedClass;
    private Elements elementUtils;
    private Filer filer;
    private ClassName realmClass, realmListClass, realmModelClass, realmQueryClass;

    private ArrayList<Class> realmDataTypes = new ArrayList<>(10);

    private Map<String, PolyQueryAnnotatedClass> itemsMap = new LinkedHashMap<>();

    PolyQueryGroupedClasses(PolyQueryAnnotatedClass annotatedClass) {
        this.annotatedClass = annotatedClass;

        realmDataTypes.add(Date.class);
        realmDataTypes.add(Boolean.class);
        realmDataTypes.add(Byte.class);
        realmDataTypes.add(byte[].class);
        realmDataTypes.add(Double.class);
        realmDataTypes.add(Float.class);
        realmDataTypes.add(Integer.class);
        realmDataTypes.add(Long.class);
        realmDataTypes.add(Short.class);
        realmDataTypes.add(String.class);
    }

    private String getQualifiedClassName() {
        return annotatedClass.getQualifiedGroupName();
    }

    String getSimpleClassName() {
        return annotatedClass.getSimpleGroupName();
    }

    void add(PolyQueryAnnotatedClass toInsert) throws ProcessorException {

        PolyQueryAnnotatedClass existing = itemsMap.get(toInsert.getTypeElement().getSimpleName().toString());
        if (existing != null) {
            throw new ProcessorException(existing);
        }

        itemsMap.put(toInsert.getTypeElement().getSimpleName().toString(), toInsert);
    }

    void init(Elements elementUtils, Filer filer){
        this.elementUtils = elementUtils;
        this.filer = filer;

        // Realm classes
        realmClass = ClassName.get("io.realm", "Realm");
        realmListClass = ClassName.get("io.realm", "RealmList");
        realmModelClass = ClassName.get("io.realm", "RealmModel");
        realmQueryClass = ClassName.get("io.realm", "RealmQuery");
    }

    void generateCode() throws IOException {
        TypeElement superClassName = elementUtils.getTypeElement(getQualifiedClassName());
        String polyClassName = superClassName.getSimpleName() + Constants.GENERATED_CLASS_SUFFIX;
        TypeSpec.Builder mainBuilder = TypeSpec.classBuilder(polyClassName);

        FieldSpec realmField = createRealmField();
        FieldSpec queryParametersField = createQueryParametersField();

        MethodSpec constructor = createConstructor();

        MethodSpec addAllParameters = createAddAllParameters();

        MethodSpec privateEqualTo = createPrivateMethods("equalTo");
        MethodSpec privateNotEqualTo = createPrivateMethods("notEqualTo");

        MethodSpec publicEqualTo = createPublicMethods(polyClassName, "equalTo", SupportedOperation.EQUAL_TO);
        MethodSpec publicNotEqualTo = createPublicMethods(polyClassName, "notEqualTo", SupportedOperation.NOT_EQUAL_TO);

        MethodSpec queryMethod = createQueryMethod(superClassName);
        MethodSpec queryFirstMethod = createQueryFirstMethod(superClassName);

        TypeSpec.Builder typeSpec = mainBuilder.addModifiers(Modifier.PUBLIC);
                //.addTypeVariable(TypeVariableName.get("T", realmModelClass));


        typeSpec.addField(realmField)
                .addField(queryParametersField);

        typeSpec.addMethod(constructor)
                .addMethod(addAllParameters)
                .addMethod(privateEqualTo)
                .addMethod(privateNotEqualTo)
                .addMethod(publicEqualTo)
                .addMethod(publicNotEqualTo)
                .addMethod(queryMethod)
                .addMethod(queryFirstMethod);

        // Create the real file
        JavaFile.Builder jfBuilder = JavaFile.builder(Constants.GENERATED_PACKAGE, typeSpec.build());

        jfBuilder.build().writeTo(filer);
    }

    private FieldSpec createRealmField() {
        return FieldSpec.builder(realmClass, "realm")
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec createQueryParametersField() {
        ParameterizedTypeName queryBuilderList = ParameterizedTypeName.get(ArrayList.class, QueryBuilder.class);
        return FieldSpec.builder(queryBuilderList, "queryParameters")
                .addModifiers(Modifier.PRIVATE)
                .initializer(CodeBlock.of("new ArrayList<>()"))
                .build();
    }

    private MethodSpec createConstructor() {
        MethodSpec.Builder constructor =  MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(realmClass, "realm");

        constructor.addStatement("this.realm = realm");
        return constructor.build();
    }

    private MethodSpec createQueryMethod(TypeElement superClassName) {
        TypeName realmParametrizedListType = ParameterizedTypeName.get(realmListClass, ClassName.get(superClassName));

        MethodSpec.Builder method = MethodSpec.methodBuilder("query")
                .addModifiers(Modifier.PUBLIC)
                .returns(realmParametrizedListType);

        method.addStatement("RealmList<$L> retVal = new RealmList()", superClassName.getSimpleName());

        method.addStatement("ArrayList<Class> classesList = new $T<>()", ArrayList.class);
        for (Map.Entry<String, PolyQueryAnnotatedClass> entry : itemsMap.entrySet()){
            ClassName cn = ClassName.get(elementUtils.getPackageOf(entry.getValue().getTypeElement().getEnclosingElement()).toString(), entry.getKey());
            method.addStatement("classesList.add($T.class)",cn);
        }
        method.addStatement("for(Class cl : classesList){");
        method.addStatement("    $T<$T> query = realm.where(cl)", realmQueryClass, superClassName);
        method.addStatement("    query = addAllParameters(query)");
        method.addStatement("    for(Object obj : query.findAll()){");
        method.addStatement("        retVal.add(($T)obj)", superClassName);
        method.addStatement("    }");
        method.addStatement("}");

        method.addStatement("return retVal", superClassName);

        return method.build();
    }

    private MethodSpec createQueryFirstMethod(TypeElement superClassName) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("queryFirst")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(superClassName.asType()));

        method.addStatement("$L retVal = null", superClassName.getSimpleName());

        method.addStatement("ArrayList<Class> classesList = new $T<>()", ArrayList.class);
        for (Map.Entry<String, PolyQueryAnnotatedClass> entry : itemsMap.entrySet()){
            ClassName cn = ClassName.get(elementUtils.getPackageOf(entry.getValue().getTypeElement().getEnclosingElement()).toString(), entry.getKey());
            method.addStatement("classesList.add($T.class)",cn);
        }
        method.addStatement("for(Class cl : classesList){");
        method.addStatement("    $T<$T> query = realm.where(cl)", realmQueryClass, superClassName);
        method.addStatement("    query = addAllParameters(query)");
        method.addStatement("    retVal = ($T) query.findFirst()", superClassName);
        method.addStatement("    if(retVal != null){", superClassName.getSimpleName());
        method.addStatement("        return retVal", superClassName.getSimpleName());
        method.addStatement("    }", superClassName.getSimpleName());
        method.addStatement("}");

        method.addStatement("return retVal", superClassName);

        return method.build();
    }

    private MethodSpec createAddAllParameters() {
        ParameterizedTypeName retType = ParameterizedTypeName.get(realmQueryClass, TypeVariableName.get("E"));
        MethodSpec.Builder method = MethodSpec.methodBuilder("addAllParameters")
                .addModifiers(Modifier.PRIVATE)
                .addTypeVariable(TypeVariableName.get("E", realmModelClass))
                .addParameter(retType, "receiver")
                .returns(retType);

        method.addStatement("for ($T param : queryParameters) {", QueryBuilder.class);
        method.beginControlFlow("    switch(param.getOperation())")
                .addStatement("        case EQUAL_TO: receiver = privateEqualTo(receiver, param); break")
                .addStatement("        case NOT_EQUAL_TO: receiver = privateNotEqualTo(receiver, param); break")
                .addStatement("    }")
                .endControlFlow();
        method.addStatement("return receiver");

        return method.build();
    }

    /**
     * Private functions used to convert the QueryBuilder to a real realm query for EVERY annotated class
     **/
    private MethodSpec createPrivateMethods(String methodName){
        String privateMethodName = "private" + StringUtils.capitalize(methodName);
        ParameterizedTypeName paramType = ParameterizedTypeName.get(realmQueryClass, TypeVariableName.get("E"));
        MethodSpec.Builder method = MethodSpec.methodBuilder(privateMethodName)
                .addModifiers(Modifier.PRIVATE)
                .addTypeVariable(TypeVariableName.get("E", realmModelClass))
                .addParameter(paramType, "receiver")
                .addParameter(QueryBuilder.class, "builder")
                .returns(paramType);

        method.addStatement("Object val = builder.getValue()");

        method.beginControlFlow("if(val != null)");
        method.beginControlFlow("if(val instanceof $T)", realmDataTypes.get(0))
                .addStatement("        receiver."+methodName+"(builder.getField(), ($T)val)", realmDataTypes.get(0));
        for (int i = 1; i < realmDataTypes.size(); i++) {
            Class type = realmDataTypes.get(i);
                method.nextControlFlow("else if(val instanceof $T)", realmDataTypes.get(i));
                method.addStatement("        receiver."+methodName+"(builder.getField(), ($T)val)", type);
        }
        method.endControlFlow();
        method.endControlFlow();
        method.addStatement("return receiver");

        return method.build();
    }

    /**
     * Public functions that user will call to add query parameters.
     * These parameters will be internally converted to real RealmQuery#equalTo/RealmQuery#notEqualTo/... when
     * the PolyQuery#query() or PolyQuery#queryFirst() are called
     **/
    private MethodSpec createPublicMethods(String polyClass, String methodName, SupportedOperation operation){
        ClassName retVal = ClassName.get(Constants.GENERATED_PACKAGE, polyClass);
        MethodSpec.Builder method = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "field")
                .addParameter(Object.class, "value")
                .returns(retVal);
        
        method.addStatement("queryParameters.add(new QueryBuilder($T."+operation+", field, value))", SupportedOperation.class);
        method.addStatement("return this");
        
        return method.build();
    }
}