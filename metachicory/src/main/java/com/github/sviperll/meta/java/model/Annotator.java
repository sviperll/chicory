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
import com.helger.jcodemodel.IJAnnotatable;
import com.helger.jcodemodel.JAnnotationArrayMember;
import com.helger.jcodemodel.JAnnotationUse;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
class Annotator {
    private final DecidedErrorTypesModelsAdapter modelsAdapter;

    private final IJAnnotatable annotatable;
    private final TypeEnvironment typeEnvironment;

    public Annotator(DecidedErrorTypesModelsAdapter modelsAdapter, IJAnnotatable annotatable, TypeEnvironment typeEnvironment) {
        this.modelsAdapter = modelsAdapter;
        this.annotatable = annotatable;
        this.typeEnvironment = typeEnvironment;
    }

    void annotate(List<? extends AnnotationMirror> annotationMirrors) throws CodeModelBuildingException {
        for (AnnotationMirror annotation : annotationMirrors) {
            annotate(annotation);
        }
    }

    private void annotate(AnnotationMirror annotation) throws CodeModelBuildingException, IllegalStateException {
        JAnnotationUse annotationUse = annotatable.annotate((AbstractJClass)modelsAdapter.toJType(annotation.getAnnotationType(),
                                                                                                  typeEnvironment));
        ArgumentAdder reader = new ArgumentAdder(annotationUse);
        reader.addArguments(annotation);
    }

    class ArgumentAdder {
        private final JAnnotationUse annotationUse;

        public ArgumentAdder(JAnnotationUse annotationUse) {
            this.annotationUse = annotationUse;
        }

        void addArguments(AnnotationMirror annotation) throws CodeModelBuildingException {
            Map<? extends ExecutableElement, ? extends AnnotationValue> annotationArguments = modelsAdapter.getElementValuesWithDefaults(annotation);
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annotationValueAssignment : annotationArguments.entrySet()) {
                String name = annotationValueAssignment.getKey().getSimpleName().toString();
                Object value = annotationValueAssignment.getValue().getValue();
                addArgument(name, value);
            }
        }

        private void addArgument(String name, Object value) throws IllegalStateException, CodeModelBuildingException {
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
                annotationUse.param(name, (Class<?>)value);
            else if (value instanceof DeclaredType) {
                annotationUse.param(name, modelsAdapter.toJType((DeclaredType)value, typeEnvironment));
            } else if (value instanceof VariableElement) {
                try {
                    annotationUse.param(name, actualEnumConstantValue((VariableElement)value));
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Annotator.class.getName()).log(Level.WARNING,
                                                                        "Not processing annotation argument: {0}: {1}",
                                                                        new Object[]{name, value});
                }
            } else if (value instanceof AnnotationMirror) {
                AnnotationMirror annotation = (AnnotationMirror)value;
                AbstractJClass annotationClass = (AbstractJClass)modelsAdapter.toJType(annotation.getAnnotationType(), typeEnvironment);
                JAnnotationUse annotationParam = annotationUse.annotationParam(name, annotationClass);
                ArgumentAdder adder = new ArgumentAdder(annotationParam);
                adder.addArguments(annotation);
            } else if (value instanceof List) {
                @SuppressWarnings(value = "unchecked")
                        List<? extends AnnotationValue> list = (List<? extends AnnotationValue>)value;
                Iterator<? extends AnnotationValue> iterator = list.iterator();
                if (iterator.hasNext()) {
                    AnnotationValue firstElementValue = iterator.next();
                    Object element = firstElementValue.getValue();
                    if (element instanceof String) {
                        String[] elements = new String[list.size()];
                        int i = 0;
                        for (AnnotationValue elementValue : list) {
                            elements[i] = (String)elementValue.getValue();
                            i++;
                        }
                        annotationUse.paramArray(name, elements);
                    } else if (element instanceof Integer) {
                        int[] elements = new int[list.size()];
                        int i = 0;
                        for (AnnotationValue elementValue : list) {
                            elements[i] = (Integer)elementValue.getValue();
                            i++;
                        }
                        annotationUse.paramArray(name, elements);
                    } else if (element instanceof Long) {
                        long[] elements = new long[list.size()];
                        int i = 0;
                        for (AnnotationValue elementValue : list) {
                            elements[i] = (Long)elementValue.getValue();
                            i++;
                        }
                        annotationUse.paramArray(name, elements);
                    } else if (element instanceof Short) {
                        short[] elements = new short[list.size()];
                        int i = 0;
                        for (AnnotationValue elementValue : list) {
                            elements[i] = (Short)elementValue.getValue();
                            i++;
                        }
                        annotationUse.paramArray(name, elements);
                    } else if (element instanceof Float) {
                        float[] elements = new float[list.size()];
                        int i = 0;
                        for (AnnotationValue elementValue : list) {
                            elements[i] = (Float)elementValue.getValue();
                            i++;
                        }
                        annotationUse.paramArray(name, elements);
                    } else if (element instanceof Double) {
                        double[] elements = new double[list.size()];
                        int i = 0;
                        for (AnnotationValue elementValue : list) {
                            elements[i] = (Double)elementValue.getValue();
                            i++;
                        }
                        annotationUse.paramArray(name, elements);
                    } else if (element instanceof Byte) {
                        byte[] elements = new byte[list.size()];
                        int i = 0;
                        for (AnnotationValue elementValue : list) {
                            elements[i] = (Byte)elementValue.getValue();
                            i++;
                        }
                        annotationUse.paramArray(name, elements);
                    } else if (element instanceof Character) {
                        char[] elements = new char[list.size()];
                        int i = 0;
                        for (AnnotationValue elementValue : list) {
                            elements[i] = (Character)elementValue.getValue();
                            i++;
                        }
                        annotationUse.paramArray(name, elements);
                    } else if (element instanceof Boolean) {
                        boolean[] elements = new boolean[list.size()];
                        int i = 0;
                        for (AnnotationValue elementValue : list) {
                            elements[i] = (Boolean)elementValue.getValue();
                            i++;
                        }
                        annotationUse.paramArray(name, elements);
                    } else if (element instanceof Class) {
                        Class<?>[] elements = new Class<?>[list.size()];
                        int i = 0;
                        for (AnnotationValue elementValue : list) {
                            elements[i] = (Class<?>)elementValue.getValue();
                            i++;
                        }
                        annotationUse.paramArray(name, elements);
                    } else if (element instanceof DeclaredType) {
                        AbstractJType[] elements = new AbstractJType[list.size()];
                        int i = 0;
                        for (AnnotationValue elementValue : list) {
                            elements[i] = modelsAdapter.toJType((DeclaredType)elementValue.getValue(), typeEnvironment);
                            i++;
                        }
                        annotationUse.paramArray(name, elements);
                    } else if (element instanceof VariableElement) {
                        try {
                            Enum<?>[] elements = new Enum<?>[list.size()];
                            int i = 0;
                            for (AnnotationValue elementValue : list) {
                                elements[i] = actualEnumConstantValue((VariableElement)elementValue.getValue());
                                i++;
                            }
                            annotationUse.paramArray(name, elements);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(Annotator.class.getName()).log(Level.WARNING,
                                                                                "Not processing annotation argument: {0}: {1}",
                                                                                new Object[]{name, list});
                        }
                    } else if (element instanceof AnnotationMirror) {
                        JAnnotationArrayMember paramArray = annotationUse.paramArray(name);
                        for (AnnotationValue elementValue : list) {
                            AnnotationMirror annotation = (AnnotationMirror)elementValue.getValue();
                            AbstractJClass annotationClass = (AbstractJClass)modelsAdapter.toJType(annotation.getAnnotationType(), typeEnvironment);
                            JAnnotationUse annotationParam = paramArray.annotate(annotationClass);
                            ArgumentAdder adder = new ArgumentAdder(annotationParam);
                            adder.addArguments(annotation);
                        }
                    } else {
                        throw new IllegalStateException(MessageFormat.format("Unknown annotation array argument: {0}: {1} ({2})",
                                                                                 name, element, element.getClass()));
                    }
                }
            } else
                throw new IllegalStateException(MessageFormat.format("Unknown annotation argument: {0}: {1} ({2})",
                                                                         name, value, value.getClass()));
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
