/*
 * Copyright (c) 2015, Victor Nazarov <asviraspossible@gmail.com>
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
package com.github.sviperll.meta.java.model;

import com.github.sviperll.meta.CodeModelBuildingException;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class DecidedErrorTypesModelsAdapter {
    static int toJMod(Collection<Modifier> modifierCollection) {
        int modifiers = 0;
        for (Modifier modifier: modifierCollection) {
            modifiers |= toJMod(modifier);
        }
        return modifiers;
    }

    static int toJMod(Modifier modifier) {
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

    private final Elements elementUtils;
    private final boolean includesErrorTypes;
    private final JCodeModel codeModel;

    DecidedErrorTypesModelsAdapter(JCodeModel codeModel, Elements elementUtils, boolean includesErrorTypes) {
        this.elementUtils = elementUtils;
        this.includesErrorTypes = includesErrorTypes;
        this.codeModel = codeModel;
    }

    public JDefinedClass getClass(TypeElement element) throws CodeModelBuildingException {
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
            for (JDefinedClass innerClass : enclosingClass.classes()) {
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

    private JDefinedClass defineClass(TypeElement element) throws CodeModelBuildingException {
        Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof PackageElement) {
            return defineTopLevelClass(element, new TypeEnvironment());
        } else {
            // Only top-level classes can be directly defined
            return getClass(element);
        }
    }

    private JDefinedClass defineTopLevelClass(TypeElement element, TypeEnvironment environment) throws
                                                                                                       CodeModelBuildingException {
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
            throw new CodeModelBuildingException(ex);
        }
        declareInnerClasses(newClass, element, environment);
        ClassFiller filler = new ClassFiller(codeModel, this, newClass);
        filler.fillClass(element, environment);
        return newClass;
    }

    private void declareInnerClasses(JDefinedClass klass, TypeElement element, TypeEnvironment environment) throws
                                                                                                                   CodeModelBuildingException {
        for (Element enclosedElement : element.getEnclosedElements()) {
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
                    throw new CodeModelBuildingException(ex);
                }
                declareInnerClasses(enclosedClass, (TypeElement)enclosedElement, environment);
            }
        }
    }

    void defineInnerClass(JDefinedClass enclosingClass, TypeElement element, TypeEnvironment environment) throws
                                                                                                                         CodeModelBuildingException {
        for (JDefinedClass innerClass : enclosingClass.classes()) {
            if (innerClass.fullName().equals(element.getQualifiedName().toString())) {
                ClassFiller filler = new ClassFiller(codeModel, this, innerClass);
                filler.fillClass(element, environment);
                return;
            }
        }
        throw new IllegalStateException(MessageFormat.format("Inner class should always be defined if outer class is defined: inner class {0}, enclosing class {1}",
                                                             element, enclosingClass));
    }

    AbstractJClass ref(TypeElement element) throws CodeModelBuildingException {
        try {
            Class<?> klass = Class.forName(element.getQualifiedName().toString());
            AbstractJType declaredClass = codeModel.ref(klass);
            return (AbstractJClass)declaredClass;
        } catch (ClassNotFoundException ex) {
            return getClass(element);
        }
    }

    AbstractJType toJType(TypeMirror type, final TypeEnvironment environment) throws CodeModelBuildingException {
        try {
            return type.accept(new TypeMirrorToJTypeVisitor(codeModel, this, includesErrorTypes, environment), null);
        } catch (RuntimeCodeModelBuildingException ex) {
            throw ex.getCause();
        }
    }



    Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror annotation) {
        return elementUtils.getElementValuesWithDefaults(annotation);
    }
}
