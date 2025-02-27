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
package org.apache.asterix.aql.expression.visitor;

import org.apache.asterix.aql.expression.JoinClause;
import org.apache.asterix.aql.expression.MetaVariableClause;
import org.apache.asterix.aql.expression.MetaVariableExpr;
import org.apache.asterix.common.exceptions.AsterixException;

public interface IAqlPlusExpressionVisitor<R, T> extends IAqlExpressionVisitor<R, T> {
    R visitJoinClause(JoinClause c, T arg) throws AsterixException;

    R visitMetaVariableClause(MetaVariableClause c, T arg) throws AsterixException;

    R visitMetaVariableExpr(MetaVariableExpr v, T arg) throws AsterixException;
}
