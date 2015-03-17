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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.github.sviperll.meta.Visitor")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class VisitorProcessor extends AbstractProcessor {
    private final List<String> errors = new ArrayList<String>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            for (String error: errors) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error);
            }
        } else {
            for (Element element: roundEnv.getElementsAnnotatedWith(Visitor.class)) {
                try {
                    validateVisitor((TypeElement)element);
                } catch (SourceValidationException ex) {
                    errors.add(ex.getMessage());
                }
            }
        }
        return true;
    }

    private void validateVisitor(TypeElement element) throws SourceValidationException {
        Visitor visitorAnnotation = element.getAnnotation(Visitor.class);
        TypeParameterElement resultTypeParameterElement = null;
        for (TypeParameterElement typeParameter: element.getTypeParameters()) {
            if (typeParameter.getSimpleName().contentEquals(visitorAnnotation.resultVariableName()))
                resultTypeParameterElement = typeParameter;
        }
        if (resultTypeParameterElement == null)
            throw new SourceValidationException(MessageFormat.format("No result type variable named {0} found for {1} declared as {2}",
                                                           visitorAnnotation.resultVariableName(),
                                                           element.getQualifiedName(),
                                                           Visitor.class.getName()));

        TypeParameterElement exceptionTypeParameterElement = null;
        if (!visitorAnnotation.exceptionVariableName().equals(":none")) {
            for (TypeParameterElement typeParameter: element.getTypeParameters()) {
                if (typeParameter.getSimpleName().contentEquals(visitorAnnotation.exceptionVariableName()))
                    exceptionTypeParameterElement = typeParameter;
            }
            if (exceptionTypeParameterElement == null)
                throw new SourceValidationException(MessageFormat.format("No exception type variable named {0} found for {1} declared as {2}",
                                                               visitorAnnotation.exceptionVariableName(),
                                                               element.getQualifiedName(),
                                                               Visitor.class.getName()));
        }

        TypeParameterElement selfReferenceTypeParameterElement = null;
        if (!visitorAnnotation.selfReferenceVariableName().equals(":none")) {
            for (TypeParameterElement typeParameter: element.getTypeParameters()) {
                if (typeParameter.getSimpleName().contentEquals(visitorAnnotation.selfReferenceVariableName()))
                    selfReferenceTypeParameterElement = typeParameter;
            }
            if (selfReferenceTypeParameterElement == null)
                throw new SourceValidationException(MessageFormat.format("No self-reference type variable named {0} found for {1} declared as {2}",
                                                               visitorAnnotation.selfReferenceVariableName(),
                                                               element.getQualifiedName(),
                                                               Visitor.class.getName()));
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found " + element.getQualifiedName() + " visitor");
    }
}
