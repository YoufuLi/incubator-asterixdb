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

import org.apache.asterix.aql.expression.visitor.IAqlExpressionVisitor;
import org.apache.asterix.aql.expression.visitor.IAqlVisitorWithVoidReturn;
import org.apache.asterix.common.exceptions.AsterixException;

public class OrderedListTypeDefinition extends TypeExpression {

    private TypeExpression itemTypeExpression;

    public OrderedListTypeDefinition(TypeExpression itemTypeExpression) {
        this.itemTypeExpression = itemTypeExpression;
    }

    @Override
    public TypeExprKind getTypeKind() {
        return TypeExprKind.ORDEREDLIST;
    }

    @Override
    public <R, T> R accept(IAqlExpressionVisitor<R, T> visitor, T arg) throws AsterixException {
        return visitor.visitOrderedListTypeDefiniton(this, arg);
    }

    @Override
    public <T> void accept(IAqlVisitorWithVoidReturn<T> visitor, T arg) throws AsterixException {
        visitor.visit(this, arg);
    }

    public TypeExpression getItemTypeExpression() {
        return itemTypeExpression;
    }

}
