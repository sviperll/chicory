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
package com.github.sviperll.meta.java.model;

import com.github.sviperll.meta.CodeModelBuildingException;
import com.github.sviperll.meta.ErrorTypeFound;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class JCodeModelJavaxLangModelAdapter {
    private final JCodeModel codeModel;
    private final Elements elementUtils;

    /**
     * Creates new instance of JCodeModelJavaxLangModelAdapter.
     */
    public JCodeModelJavaxLangModelAdapter(JCodeModel codeModel, Elements elementUtils) {
        this.codeModel = codeModel;
        this.elementUtils = elementUtils;
    }

    /**
     * Returns jcodemodel class definition for given element.
     *
     * @param element element to convert to class definition
     *
     * @throws ErrorTypeFound if {@code element} argument contains references to so called "error"-types.
     */
    public JDefinedClass getClass(TypeElement element) throws ErrorTypeFound, CodeModelBuildingException {
        DecidedErrorTypesModelsAdapter errorTypeDecision = new DecidedErrorTypesModelsAdapter(codeModel, elementUtils, false);
        try {
            return errorTypeDecision.getClass(element);
        } catch (RuntimeErrorTypeFound ex) {
            throw ex.getCause();
        }
    }

    /**
     * Returns jcodemodel class definition for given element.
     * <p>
     * This method result-class definition can include references to "error"-types.
     * Error-types are used only if they are present in {@code element} argument
     *
     * @param element element to convert to class definition
     */
    public JDefinedClass getClassWithErrorTypes(TypeElement element) throws CodeModelBuildingException {
        DecidedErrorTypesModelsAdapter errorTypeDecision = new DecidedErrorTypesModelsAdapter(codeModel, elementUtils, true);
        return errorTypeDecision.getClass(element);
    }

}
