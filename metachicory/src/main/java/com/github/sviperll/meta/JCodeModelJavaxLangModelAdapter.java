/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.meta;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.IJAnnotatable;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JTypeWildcard;
import com.helger.jcodemodel.JVar;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor6;
import javax.lang.model.util.Elements;

public class JCodeModelJavaxLangModelAdapter {
    private static int toJMod(Collection<Modifier> modifierCollection) {
        int modifiers = 0;
        for (Modifier modifier: modifierCollection) {
            modifiers |= toJMod(modifier);
        }
        return modifiers;
    }

    private static int toJMod(Modifier modifier) {
        switch (modifier) {
            case ABSTRACT:
                return JMod.ABSTRACT;
            case FINAL:
                return JMod.FINAL;
            case NATIVE:
                return JMod.NATIVE;
            case PRIVATE:
                return JMod.PRIVATE;
            case PROTECTED:
                return JMod.PROTECTED;
            case PUBLIC:
                return JMod.PUBLIC;
            case STATIC:
                return JMod.STATIC;
            case SYNCHRONIZED:
                return JMod.SYNCHRONIZED;
            case TRANSIENT:
                return JMod.TRANSIENT;
            case VOLATILE:
                return JMod.VOLATILE;
            default:
                throw new UnsupportedOperationException("Unsupported modifier: " + modifier);
        }
    }

    private static EClassType toClassType(ElementKind kind) {
        switch (kind) {
            case CLASS:
                return EClassType.CLASS;
            case ENUM:
                return EClassType.ENUM;
            case INTERFACE:
                return EClassType.INTERFACE;
            case ANNOTATION_TYPE:
                return EClassType.ANNOTATION_TYPE_DECL;
            default:
                throw new UnsupportedOperationException("Unsupported ElementKind: " + kind);
        }
    }

    private final JCodeModel codeModel;
    private final Elements elementUtils;

    public JCodeModelJavaxLangModelAdapter(JCodeModel codeModel, Elements elementUtils) {
        this.codeModel = codeModel;
        this.elementUtils = elementUtils;
    }

    private JDefinedClass defineClass(TypeElement element) throws ProcessingException {
        Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof PackageElement) {
            return defineClass(element, new TypeEnvironment());
        } else {
            // Only top-level classes can be directly defined
            return getClass(element);
        }
    }

    private JDefinedClass defineClass(TypeElement element, TypeEnvironment environment) throws ProcessingException {
        EClassType classType = toClassType(element.getKind());
        int modifiers = toJMod(element.getModifiers());
        if (classType.equals(EClassType.INTERFACE)) {
            modifiers &= ~JMod.ABSTRACT;
            modifiers &= ~JMod.STATIC;
        }

        Element enclosingElement = element.getEnclosingElement();
        if (!(enclosingElement instanceof PackageElement)) {
            throw new IllegalStateException("Expecting top level class");
        }
        PackageElement packageElement = (PackageElement)enclosingElement;
        JPackage _package = codeModel._package(packageElement.getQualifiedName().toString());
        JDefinedClass newClass;
        try {
            newClass = _package._class(modifiers, element.getSimpleName().toString(), classType);
        } catch (JClassAlreadyExistsException ex) {
            throw new CodeGenerationException(ex);
        }
        declareInnerClasses(newClass, element, environment);
        ClassFiller filler = new ClassFiller(newClass);
        filler.fillClass(element, environment);
        return newClass;
    }

    private void declareInnerClasses(JDefinedClass klass, TypeElement element, TypeEnvironment environment) throws ProcessingException {
        for (Element enclosedElement: element.getEnclosedElements()) {
            if (enclosedElement.getKind().equals(ElementKind.INTERFACE) || enclosedElement.getKind().equals(ElementKind.CLASS)) {
                EClassType classType = toClassType(enclosedElement.getKind());
                int modifiers = toJMod(enclosedElement.getModifiers());
                if (classType.equals(EClassType.INTERFACE)) {
                    modifiers &= ~JMod.ABSTRACT;
                    modifiers &= ~JMod.STATIC;
                }
                JDefinedClass enclosedClass;
                try {
                    enclosedClass = klass._class(modifiers, enclosedElement.getSimpleName().toString(), classType);
                } catch (JClassAlreadyExistsException ex) {
                    throw new CodeGenerationException(ex);
                }
                declareInnerClasses(enclosedClass, (TypeElement)enclosedElement, environment);
            }
        }
    }

    private void defineInnerClass(JDefinedClass enclosingClass, TypeElement element, TypeEnvironment environment) throws ProcessingException {
        for (JDefinedClass innerClass: enclosingClass.classes()) {
            if (innerClass.fullName().equals(element.getQualifiedName().toString())) {
                ClassFiller filler = new ClassFiller(innerClass);
                filler.fillClass(element, environment);
                return;
            }
        }
        throw new IllegalStateException(MessageFormat.format("Inner class should always be defined if outer class is defined: inner class {0}, enclosing class {1}",
                                                            element, enclosingClass));
    }

    private AbstractJClass ref(TypeElement element) throws ProcessingException {
        try {
            Class<?> klass = Class.forName(element.getQualifiedName().toString());
            AbstractJType declaredClass = codeModel.ref(klass);
            return (AbstractJClass)declaredClass;
        } catch (ClassNotFoundException ex) {
            return getClass(element);
        }
    }

    public JDefinedClass getClass(TypeElement element) throws ProcessingException {
        Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof PackageElement) {
            PackageElement packageElement = (PackageElement)enclosingElement;
            JPackage jpackage = codeModel._package(packageElement.getQualifiedName().toString());
            JDefinedClass result = jpackage._getClass(element.getSimpleName().toString());
            if (result != null)
                return result;
            else {
                JDefinedClass jclass = defineClass(element);
                jclass.hide();
                return jclass;
            }
        } else if (enclosingElement instanceof TypeElement) {
            JDefinedClass enclosingClass = getClass((TypeElement)enclosingElement);
            for (JDefinedClass innerClass: enclosingClass.classes()) {
                String fullName = innerClass.fullName();
                if (fullName != null && fullName.equals(element.getQualifiedName().toString())) {
                    return innerClass;
                }
            }
            throw new IllegalStateException(MessageFormat.format("Inner class should always be defined if outer class is defined: inner class {0}, enclosing class {1}",
                                                                 element, enclosingClass));
        } else
            throw new IllegalStateException("Enclosing element should be package or class");
    }

    private AbstractJType toJType(TypeMirror type, final TypeEnvironment environment) throws ProcessingException {
        try {
            return type.accept(new AbstractTypeVisitor6<AbstractJType, Void>() {

                @Override
                public AbstractJType visitPrimitive(PrimitiveType t, Void p) {
                    switch (t.getKind()) {
                        case BOOLEAN:
                            return codeModel.BOOLEAN;
                        case BYTE:
                            return codeModel.BYTE;
                        case CHAR:
                            return codeModel.CHAR;
                        case INT:
                            return codeModel.INT;
                        case LONG:
                            return codeModel.LONG;
                        case FLOAT:
                            return codeModel.FLOAT;
                        case DOUBLE:
                            return codeModel.DOUBLE;
                        case SHORT:
                            return codeModel.SHORT;
                        default:
                            throw new IllegalArgumentException("Unrecognized primitive " + t.getKind());
                    }
                }

                @Override
                public AbstractJType visitNull(NullType t, Void p) {
                    throw new IllegalArgumentException("null can't be JClass."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public AbstractJType visitArray(ArrayType t, Void p) {
                    try {
                        AbstractJType componentType = toJType(t.getComponentType(), environment);
                        return componentType.array();
                    } catch (ProcessingException ex) {
                        throw new RuntimeProcessingException(ex);
                    }
                }

                @Override
                public AbstractJType visitDeclared(DeclaredType t, Void p) {
                    try {
                        TypeElement element = (TypeElement)t.asElement();
                        AbstractJClass _class = ref(element);
                        for (TypeMirror typeArgument: t.getTypeArguments()) {
                            _class = _class.narrow(toJType(typeArgument, environment));
                        }
                        return _class;
                    } catch (ProcessingException ex) {
                        throw new RuntimeProcessingException(ex);
                    }
                }

                @Override
                public AbstractJType visitError(ErrorType t, Void p) {
                    try {
                        throw new ErrorTypeFound();
                    } catch (ErrorTypeFound ex) {
                        throw new RuntimeProcessingException(ex);
                    }
                }

                @Override
                public AbstractJType visitTypeVariable(TypeVariable t, Void p) {
                    return environment.get(t.asElement().getSimpleName().toString());
                }

                @Override
                public AbstractJType visitWildcard(WildcardType t, Void p) {
                    try {
                        TypeMirror extendsBoundMirror = t.getExtendsBound();
                        if (extendsBoundMirror != null) {
                            AbstractJClass extendsBound = (AbstractJClass)toJType(extendsBoundMirror, environment);
                            return extendsBound.wildcard(JTypeWildcard.EBoundMode.EXTENDS);
                        }
                        TypeMirror superBoundMirror = t.getSuperBound();
                        if (superBoundMirror != null) {
                            AbstractJClass superBound = (AbstractJClass)toJType(superBoundMirror, environment);
                            return superBound.wildcard(JTypeWildcard.EBoundMode.SUPER);
                        }
                        return codeModel.wildcard();
                    } catch (ProcessingException ex) {
                        throw new RuntimeProcessingException(ex);
                    }
                }

                @Override
                public AbstractJType visitExecutable(ExecutableType t, Void p) {
                    throw new IllegalArgumentException("executable can't be JClass."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public AbstractJType visitNoType(NoType t, Void p) {
                    throw new IllegalArgumentException("'no type' can't be JClass."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public AbstractJType visitUnknown(TypeMirror t, Void p) {
                    throw new IllegalArgumentException("unknown can't be JClass."); //To change body of generated methods, choose Tools | Templates.
                }
            }, null);
        } catch (RuntimeProcessingException ex) {
            throw ex.getCause();
        }
    }

    private class ClassFiller {
        private final JDefinedClass newClass;
        ClassFiller(JDefinedClass newClass) {
            this.newClass = newClass;
        }

        private void fillClass(TypeElement element, TypeEnvironment environment) throws ProcessingException {
            newClass.hide();
            Annotator classAnnotator = new Annotator(newClass, environment);
            classAnnotator.annotate(element.getAnnotationMirrors());
            for (TypeParameterElement parameter: element.getTypeParameters()) {
                JTypeVar typeVariable = newClass.generify(parameter.getSimpleName().toString());
                environment.put(typeVariable.name(), typeVariable);
                for (TypeMirror type: parameter.getBounds()) {
                    typeVariable.bound((AbstractJClass)toJType(type, environment));
                }
            }
            TypeMirror superclass = element.getSuperclass();
            if (superclass != null && superclass.getKind() != TypeKind.NONE) {
                newClass._extends((AbstractJClass)toJType(superclass, environment));
            }
            for (TypeMirror iface: element.getInterfaces()) {
                newClass._implements((AbstractJClass)toJType(iface, environment));
            }
            for (Element enclosedElement: element.getEnclosedElements()) {
                if (enclosedElement.getKind().equals(ElementKind.INTERFACE) || enclosedElement.getKind().equals(ElementKind.CLASS)) {
                    defineInnerClass(newClass, (TypeElement)enclosedElement, environment.enclosed());
                } else if (enclosedElement.getKind().equals(ElementKind.METHOD)) {
                    ExecutableElement executable = (ExecutableElement)enclosedElement;
                    JMethod method = newClass.method(toJMod(executable.getModifiers()), codeModel.VOID, executable.getSimpleName().toString());
                    TypeEnvironment methodEnvironment = environment.enclosed();
                    Annotator methodAnnotator = new Annotator(method, environment);
                    methodAnnotator.annotate(executable.getAnnotationMirrors());
                    for (TypeParameterElement parameter: executable.getTypeParameters()) {
                        JTypeVar typeVariable = method.generify(parameter.getSimpleName().toString());
                        methodEnvironment.put(typeVariable.name(), typeVariable);
                        for (TypeMirror type: parameter.getBounds()) {
                            typeVariable.bound((AbstractJClass)toJType(type, methodEnvironment));
                        }
                    }
                    method.type(toJType(executable.getReturnType(), methodEnvironment));
                    for (TypeMirror type: executable.getThrownTypes()) {
                        AbstractJClass throwable = (AbstractJClass)toJType(type, methodEnvironment);
                        method._throws(throwable);
                    }

                    List<? extends VariableElement> parameters = executable.getParameters();
                    int n = 0;
                    for (VariableElement variable: parameters) {
                        String parameterName = variable.getSimpleName().toString();
                        TypeMirror parameterTypeMirror = variable.asType();
                        AbstractJType parameterType = toJType(parameterTypeMirror, methodEnvironment);

                        JVar param;
                        if (executable.isVarArgs() && n == parameters.size() - 1) {
                            param = method.varParam(toJMod(variable.getModifiers()), parameterType.elementType(), parameterName);
                        } else {
                            param = method.param(toJMod(variable.getModifiers()), parameterType, parameterName);
                        }
                        Annotator parametorAnnotator = new Annotator(param, methodEnvironment);
                        parametorAnnotator.annotate(variable.getAnnotationMirrors());
                        n++;
                    }
                }
            }
        }
    }

    private class Annotator {
        private final IJAnnotatable annotatable;
        private final TypeEnvironment typeEnvironment;
        public Annotator(IJAnnotatable annotatable, TypeEnvironment typeEnvironment) {
            this.annotatable = annotatable;
            this.typeEnvironment = typeEnvironment;
        }

        private void annotate(List<? extends AnnotationMirror> annotationMirrors) throws ProcessingException {
            for (AnnotationMirror annotation: annotationMirrors) {
                JAnnotationUse annotationUse = annotatable.annotate((AbstractJClass)toJType(annotation.getAnnotationType(), typeEnvironment));
                Map<? extends ExecutableElement, ? extends AnnotationValue> annotationArguments = elementUtils.getElementValuesWithDefaults(annotation);
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annotationValueAssignment: annotationArguments.entrySet()) {
                    String name = annotationValueAssignment.getKey().getSimpleName().toString();
                    Object value = annotationValueAssignment.getValue().getValue();
                    if (value instanceof String)
                        annotationUse.param(name, (String)value);
                    else if (value instanceof Integer)
                        annotationUse.param(name, (Integer)value);
                    else if (value instanceof Long)
                        annotationUse.param(name, (Long)value);
                    else if (value instanceof Short)
                        annotationUse.param(name, (Short)value);
                    else if (value instanceof Float)
                        annotationUse.param(name, (Float)value);
                    else if (value instanceof Double)
                        annotationUse.param(name, (Double)value);
                    else if (value instanceof Byte)
                        annotationUse.param(name, (Byte)value);
                    else if (value instanceof Character)
                        annotationUse.param(name, (Character)value);
                    else if (value instanceof Boolean)
                        annotationUse.param(name, (Boolean)value);
                    else if (value instanceof Class)
                        annotationUse.param(name, (Class)value);
                    else if (value instanceof VariableElement) {
                        try {
                            annotationUse.param(name, actualEnumConstantValue((VariableElement)value));
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(Annotator.class.getName()).log(Level.WARNING, "Not processing annotation argument: {0}: {1}", new Object[]{name, value});
                        }
                    } else if (value instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<? extends AnnotationValue> list = (List<? extends AnnotationValue>)value;
                        Iterator<? extends AnnotationValue> iterator = list.iterator();
                        if (iterator.hasNext()) {
                            AnnotationValue firstElementValue = iterator.next();
                            Object element = firstElementValue.getValue();
                            if (element instanceof String) {
                                String[] elements = new String[list.size()];
                                int i = 0;
                                for (AnnotationValue elementValue: list) {
                                    elements[i] = (String)elementValue.getValue();
                                    i++;
                                }
                                annotationUse.paramArray(name, elements);
                            } else if (element instanceof Integer) {
                                int[] elements = new int[list.size()];
                                int i = 0;
                                for (AnnotationValue elementValue: list) {
                                    elements[i] = (Integer)elementValue.getValue();
                                    i++;
                                }
                                annotationUse.paramArray(name, elements);
                            } else if (element instanceof Long) {
                                long[] elements = new long[list.size()];
                                int i = 0;
                                for (AnnotationValue elementValue: list) {
                                    elements[i] = (Long)elementValue.getValue();
                                    i++;
                                }
                                annotationUse.paramArray(name, elements);
                            } else if (element instanceof Short) {
                                short[] elements = new short[list.size()];
                                int i = 0;
                                for (AnnotationValue elementValue: list) {
                                    elements[i] = (Short)elementValue.getValue();
                                    i++;
                                }
                                annotationUse.paramArray(name, elements);
                            } else if (element instanceof Float) {
                                float[] elements = new float[list.size()];
                                int i = 0;
                                for (AnnotationValue elementValue: list) {
                                    elements[i] = (Float)elementValue.getValue();
                                    i++;
                                }
                                annotationUse.paramArray(name, elements);
                            } else if (element instanceof Double) {
                                double[] elements = new double[list.size()];
                                int i = 0;
                                for (AnnotationValue elementValue: list) {
                                    elements[i] = (Double)elementValue.getValue();
                                    i++;
                                }
                                annotationUse.paramArray(name, elements);
                            } else if (element instanceof Byte) {
                                byte[] elements = new byte[list.size()];
                                int i = 0;
                                for (AnnotationValue elementValue: list) {
                                    elements[i] = (Byte)elementValue.getValue();
                                    i++;
                                }
                                annotationUse.paramArray(name, elements);
                            } else if (element instanceof Character) {
                                char[] elements = new char[list.size()];
                                int i = 0;
                                for (AnnotationValue elementValue: list) {
                                    elements[i] = (Character)elementValue.getValue();
                                    i++;
                                }
                                annotationUse.paramArray(name, elements);
                            } else if (element instanceof Boolean) {
                                boolean[] elements = new boolean[list.size()];
                                int i = 0;
                                for (AnnotationValue elementValue: list) {
                                    elements[i] = (Boolean)elementValue.getValue();
                                    i++;
                                }
                                annotationUse.paramArray(name, elements);
                            } else if (element instanceof Class) {
                                Class<?>[] elements = new Class<?>[list.size()];
                                int i = 0;
                                for (AnnotationValue elementValue: list) {
                                    elements[i] = (Class<?>)elementValue.getValue();
                                    i++;
                                }
                                annotationUse.paramArray(name, elements);
                            } else if (element instanceof VariableElement) {
                                try {
                                    Enum<?>[] elements = new Enum<?>[list.size()];
                                    int i = 0;
                                    for (AnnotationValue elementValue: list) {
                                        elements[i] = actualEnumConstantValue((VariableElement)elementValue.getValue());
                                        i++;
                                    }
                                    annotationUse.paramArray(name, elements);
                                } catch (ClassNotFoundException ex) {
                                    Logger.getLogger(Annotator.class.getName()).log(Level.WARNING, "Not processing annotation argument: {0}: {1}", new Object[]{name, list});
                                }
                            }
                        }
                    } else
                        throw new IllegalStateException(MessageFormat.format("Unknown annotation argument: {0}: {1} ({2})",
                                                                             name, value, value.getClass()));
                }
            }
        }

        private Enum<?> actualEnumConstantValue(VariableElement variableElement) throws ClassNotFoundException {
            TypeElement enumClassElement = (TypeElement)variableElement.getEnclosingElement();
            Class<?> enumClass = Class.forName(enumClassElement.getQualifiedName().toString());
            Field enumConstantField;
            try {
                enumConstantField = enumClass.getField(variableElement.getSimpleName().toString());
            } catch (NoSuchFieldException ex) {
                throw new IllegalStateException(MessageFormat.format("Unable to load enum constant: {0}.{1}",
                                                                     enumClassElement.getQualifiedName().toString(),
                                                                     variableElement.getSimpleName().toString()), ex);
            } catch (SecurityException ex) {
                throw new IllegalStateException(MessageFormat.format("Unable to load enum constant: {0}.{1}",
                                                                     enumClassElement.getQualifiedName().toString(),
                                                                     variableElement.getSimpleName().toString()), ex);
            }
            Enum<?> enumValue;
            try {
                enumValue = (Enum<?>)enumConstantField.get(null);
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException(MessageFormat.format("Unable to load enum constant actual value: {0}.{1}",
                                                                     enumClassElement.getQualifiedName().toString(),
                                                                     variableElement.getSimpleName().toString()), ex);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(MessageFormat.format("Unable to load enum constant actual value: {0}.{1}",
                                                                     enumClassElement.getQualifiedName().toString(),
                                                                     variableElement.getSimpleName().toString()), ex);
            }
            return enumValue;
        }
    }
}
