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
package org.apache.asterix.aql.expression;

import org.apache.asterix.aql.base.Statement;
import org.apache.asterix.aql.expression.visitor.IAqlExpressionVisitor;
import org.apache.asterix.aql.expression.visitor.IAqlVisitorWithVoidReturn;
import org.apache.asterix.common.exceptions.AsterixException;

public class SetStatement implements Statement {

    private String propName;
    private String propValue;

    public SetStatement(String propName, String propValue) {
        this.propName = propName;
        this.propValue = propValue;
    }

    public String getPropName() {
        return propName;
    }

    public String getPropValue() {
        return propValue;
    }

    @Override
    public Kind getKind() {
        return Kind.SET;
    }

    @Override
    public <R, T> R accept(IAqlExpressionVisitor<R, T> visitor, T arg) throws AsterixException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> void accept(IAqlVisitorWithVoidReturn<T> visitor, T arg) throws AsterixException {
        visitor.visit(this, arg);
    }

}
