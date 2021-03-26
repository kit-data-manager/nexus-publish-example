/*
 * Copyright 2021 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.nexus.publish.example;

import java.util.Optional;

/**
 * Dummy class to have some content inside the published artifacts.
 *
 * @author jejkal
 */
public class Dummy {

    /**
     * Default constructor.
     */
    public Dummy() {
    }

    /**
     * Dummy function for checking, if argument is present or not.
     *
     * @param arg Optional of type String.
     *
     * @return TRUE if arg is not null and present, FALSE otherwise.
     */
    public boolean doSomething(Optional<String> arg) {
        return (arg == null) ? false : arg.isPresent();
    }

}
