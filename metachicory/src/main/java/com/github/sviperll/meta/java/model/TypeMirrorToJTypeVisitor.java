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
import com.github.sviperll.meta.ErrorTypeFound;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JTypeWildcard;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor6;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class TypeMirrorToJTypeVisitor extends AbstractTypeVisitor6<AbstractJType, Void> {
    private final boolean includesErrorTypes;
    private final TypeEnvironment environment;
    private final JCodeModel codeModel;
    private final DecidedErrorTypesModelsAdapter modelsAdapter;

    public TypeMirrorToJTypeVisitor(JCodeModel codeModel, DecidedErrorTypesModelsAdapter modelsAdapter, boolean includesErrorTypes, TypeEnvironment environment) {
        this.codeModel = codeModel;
        this.modelsAdapter = modelsAdapter;
        this.includesErrorTypes = includesErrorTypes;
        this.environment = environment;
    }

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
            AbstractJType componentType = modelsAdapter.toJType(t.getComponentType(), environment);
            return componentType.array();
        } catch (CodeModelBuildingException ex) {
            throw new RuntimeCodeModelBuildingException(ex);
        }
    }

    @Override
    public AbstractJType visitDeclared(DeclaredType t, Void p) {
        try {
            TypeElement element = (TypeElement)t.asElement();
            AbstractJClass _class = modelsAdapter.ref(element);
            for (TypeMirror typeArgument : t.getTypeArguments()) {
                _class = _class.narrow(modelsAdapter.toJType(typeArgument, environment));
            }
            return _class;
        } catch (CodeModelBuildingException ex) {
            throw new RuntimeCodeModelBuildingException(ex);
        }
    }

    @Override
    public AbstractJType visitError(ErrorType t, Void p) {
        if (includesErrorTypes)
            return codeModel.errorClass(t.asElement().getSimpleName().toString() + " in annotated source code");
        else {
            try {
                throw new ErrorTypeFound();
            } catch (ErrorTypeFound ex) {
                throw new RuntimeErrorTypeFound(ex);
            }
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
                AbstractJClass extendsBound = (AbstractJClass)modelsAdapter.toJType(extendsBoundMirror, environment);
                return extendsBound.wildcard(JTypeWildcard.EBoundMode.EXTENDS);
            }
            TypeMirror superBoundMirror = t.getSuperBound();
            if (superBoundMirror != null) {
                AbstractJClass superBound = (AbstractJClass)modelsAdapter.toJType(superBoundMirror, environment);
                return superBound.wildcard(JTypeWildcard.EBoundMode.SUPER);
            }
            return codeModel.wildcard();
        } catch (CodeModelBuildingException ex) {
            throw new RuntimeCodeModelBuildingException(ex);
        }
    }

    @Override
    public AbstractJType visitExecutable(ExecutableType t, Void p) {
        throw new IllegalArgumentException("executable can't be JClass.");
    }

    @Override
    public AbstractJType visitNoType(NoType t, Void p) {
        if (includesErrorTypes)
            return codeModel.errorClass("'no type' in annotated source code");
        else {
            try {
                throw new ErrorTypeFound();
            } catch (ErrorTypeFound ex) {
                throw new RuntimeErrorTypeFound(ex);
            }
        }
    }

    @Override
    public AbstractJType visitUnknown(TypeMirror t, Void p) {
        if (includesErrorTypes)
            return codeModel.errorClass("'unknown type' in annotated source code");
        else {
            try {
                throw new ErrorTypeFound();
            } catch (ErrorTypeFound ex) {
                throw new RuntimeErrorTypeFound(ex);
            }
        }
    }

}
