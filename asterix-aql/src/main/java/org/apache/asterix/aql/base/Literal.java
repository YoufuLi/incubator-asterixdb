/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.asterix.aql.base;

import java.io.Serializable;

public abstract class Literal implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6468144574890768345L;

    public enum Type {
        STRING,
        INTEGER,
        NULL,
        TRUE,
        FALSE,
        FLOAT,
        DOUBLE,
        LONG
    }

    abstract public Object getValue();

    abstract public Type getLiteralType();

    public String getStringValue() {
        return getValue().toString();
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Literal)) {
            return false;
        }
        Literal literal = (Literal) obj;
        return getValue().equals(literal.getValue());
    }

    @Override
    public String toString() {
        return getStringValue();
    }
}
