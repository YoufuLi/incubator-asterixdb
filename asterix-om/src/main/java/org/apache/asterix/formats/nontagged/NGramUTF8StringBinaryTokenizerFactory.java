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

package org.apache.asterix.formats.nontagged;

import org.apache.hyracks.storage.am.lsm.invertedindex.tokenizers.IBinaryTokenizer;
import org.apache.hyracks.storage.am.lsm.invertedindex.tokenizers.IBinaryTokenizerFactory;
import org.apache.hyracks.storage.am.lsm.invertedindex.tokenizers.ITokenFactory;
import org.apache.hyracks.storage.am.lsm.invertedindex.tokenizers.NGramUTF8StringBinaryTokenizer;

// TODO: Should be moved into Hyracks with the rest of the tokenizer code.
public class NGramUTF8StringBinaryTokenizerFactory implements IBinaryTokenizerFactory {

    private static final long serialVersionUID = 1L;
    private final int gramLength;
    private final boolean usePrePost;
    private final boolean ignoreTokenCount;
    private final boolean sourceHasTypeTag;
    private final ITokenFactory tokenFactory;

    public NGramUTF8StringBinaryTokenizerFactory(int gramLength, boolean usePrePost, boolean ignoreTokenCount,
            boolean sourceHasTypeTag, ITokenFactory tokenFactory) {
        this.gramLength = gramLength;
        this.usePrePost = usePrePost;
        this.ignoreTokenCount = ignoreTokenCount;
        this.sourceHasTypeTag = sourceHasTypeTag;
        this.tokenFactory = tokenFactory;
    }

    @Override
    public IBinaryTokenizer createTokenizer() {
        return new NGramUTF8StringBinaryTokenizer(gramLength, usePrePost, ignoreTokenCount, sourceHasTypeTag,
                tokenFactory);
    }
}
