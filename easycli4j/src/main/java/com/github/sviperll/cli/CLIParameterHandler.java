/*
 * Copyright (c) 2012, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Victor Nazarov nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.sviperll.cli;

/**
 * An implementer of this interface must handle
 * presence of keys in command line that require arguments.
 *
 * When application is started with following arguments
 *
 *     myapp -s arg1 -s arg2 -s arg3
 *
 * and some `CLIParameterHandler` is specified as a `-s`-handler
 * then CLIParameterHandler#handleCLIParameter method is called three times
 * with different arguments.
 *
 * `CLIParameterHandler`'s should throw `CLIParameterFormatException`
 * when argument doesn't follow required format or is not contained in required range.
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public interface CLIParameterHandler {
    /**
     * Method to call for each instance of command line key
     *
     * @see CLIParameterHandler
     */
    void handleCLIParameter(String param) throws CLIParameterFormatException;

    /**
     * Method returns a default value for the handler.
     *
     * Handler should behave exactly the same
     *
     *  * when it is not invoked at all, i. e. there is no key in command line
     *  * and when it is called once with default value as an argument
     *
     * Above proposition holds only when default value is present, i. e. not null.
     *
     * @return default value
     */
    String getDefaultValue();
}
