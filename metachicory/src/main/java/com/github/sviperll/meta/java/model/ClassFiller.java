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
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JVar;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class ClassFiller {
    private final JDefinedClass newClass;
    private final JCodeModel codeModel;
    private final DecidedErrorTypesModelsAdapter modelsAdapter;

    ClassFiller(JCodeModel codeModel, DecidedErrorTypesModelsAdapter modelsAdapter, JDefinedClass newClass) {
        this.codeModel = codeModel;
        this.modelsAdapter = modelsAdapter;
        this.newClass = newClass;
    }

    void fillClass(TypeElement element, TypeEnvironment environment) throws CodeModelBuildingException {
        newClass.hide();
        Annotator classAnnotator = new Annotator(modelsAdapter, newClass, environment);
        classAnnotator.annotate(element.getAnnotationMirrors());
        for (TypeParameterElement parameter : element.getTypeParameters()) {
            JTypeVar typeVariable = newClass.generify(parameter.getSimpleName().toString());
            environment.put(typeVariable.name(), typeVariable);
            for (TypeMirror type : parameter.getBounds()) {
                typeVariable.bound((AbstractJClass)modelsAdapter.toJType(type, environment));
            }
        }
        TypeMirror superclass = element.getSuperclass();
        if (superclass != null && superclass.getKind() != TypeKind.NONE) {
            newClass._extends((AbstractJClass)modelsAdapter.toJType(superclass, environment));
        }
        for (TypeMirror iface : element.getInterfaces()) {
            newClass._implements((AbstractJClass)modelsAdapter.toJType(iface, environment));
        }
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind().equals(ElementKind.INTERFACE) || enclosedElement.getKind().equals(ElementKind.CLASS)) {
                TypeElement innerClassElement = (TypeElement)enclosedElement;
                modelsAdapter.defineInnerClass(newClass, innerClassElement, environment.enclosed());
            } else if (enclosedElement.getKind().equals(ElementKind.METHOD)) {
                ExecutableElement executable = (ExecutableElement)enclosedElement;
                JMethod method = newClass.method(DecidedErrorTypesModelsAdapter.toJMod(executable.getModifiers()),
                                                 codeModel.VOID, executable.getSimpleName().toString());
                TypeEnvironment methodEnvironment = environment.enclosed();
                Annotator methodAnnotator = new Annotator(modelsAdapter, method, environment);
                methodAnnotator.annotate(executable.getAnnotationMirrors());
                for (TypeParameterElement parameter : executable.getTypeParameters()) {
                    JTypeVar typeVariable = method.generify(parameter.getSimpleName().toString());
                    methodEnvironment.put(typeVariable.name(), typeVariable);
                    for (TypeMirror type : parameter.getBounds()) {
                        typeVariable.bound((AbstractJClass)modelsAdapter.toJType(type, methodEnvironment));
                    }
                }
                method.type(modelsAdapter.toJType(executable.getReturnType(), methodEnvironment));
                for (TypeMirror type : executable.getThrownTypes()) {
                    AbstractJClass throwable = (AbstractJClass)modelsAdapter.toJType(type, methodEnvironment);
                    method._throws(throwable);
                }
                List<? extends VariableElement> parameters = executable.getParameters();
                int n = 0;
                for (VariableElement variable : parameters) {
                    String parameterName = variable.getSimpleName().toString();
                    TypeMirror parameterTypeMirror = variable.asType();
                    AbstractJType parameterType = modelsAdapter.toJType(parameterTypeMirror, methodEnvironment);
                    JVar param;
                    if (executable.isVarArgs() && n == parameters.size() - 1) {
                        param = method.varParam(DecidedErrorTypesModelsAdapter.toJMod(variable.getModifiers()),
                                                parameterType.elementType(), parameterName);
                    } else {
                        param = method.param(DecidedErrorTypesModelsAdapter.toJMod(variable.getModifiers()),
                                             parameterType, parameterName);
                    }
                    Annotator parametorAnnotator = new Annotator(modelsAdapter, param, methodEnvironment);
                    parametorAnnotator.annotate(variable.getAnnotationMirrors());
                    n++;
                }
            }
        }
    }

}
