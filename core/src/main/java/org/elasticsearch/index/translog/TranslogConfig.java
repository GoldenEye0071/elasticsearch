/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.translog;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.index.translog.Translog.TranslogGeneration;
import org.elasticsearch.threadpool.ThreadPool;

import java.nio.file.Path;

/*
 * Holds all the configuration that is used to create a {@link Translog}.
 * Once {@link Translog} has been created with this object, changes to this
 * object will affect the {@link Translog} instance.
 */
public final class TranslogConfig {

    public static final ByteSizeValue DEFAULT_BUFFER_SIZE = new ByteSizeValue(8, ByteSizeUnit.KB);
    private final BigArrays bigArrays;
    private volatile TranslogGeneration translogGeneration;
    private final IndexSettings indexSettings;
    private final ShardId shardId;
    private final Path translogPath;
    private final ByteSizeValue bufferSize;

    /**
     * Creates a new TranslogConfig instance
     * @param shardId the shard ID this translog belongs to
     * @param translogPath the path to use for the transaction log files
     * @param indexSettings the index settings used to set internal variables
     * @param bigArrays a bigArrays instance used for temporarily allocating write operations
     */
    public TranslogConfig(ShardId shardId, Path translogPath, IndexSettings indexSettings, BigArrays bigArrays) {
        this(shardId, translogPath, indexSettings, bigArrays, DEFAULT_BUFFER_SIZE);
    }

    TranslogConfig(ShardId shardId, Path translogPath, IndexSettings indexSettings, BigArrays bigArrays, ByteSizeValue bufferSize) {
        this.bufferSize = bufferSize;
        this.indexSettings = indexSettings;
        this.shardId = shardId;
        this.translogPath = translogPath;
        this.bigArrays = bigArrays;
    }

    /**
     * Returns <code>true</code> iff each low level operation shoudl be fsynced
     */
    public boolean isSyncOnEachOperation() {
        return indexSettings.getTranslogSyncInterval().millis() == 0;
    }

    /**
     * Returns the index indexSettings
     */
    public IndexSettings getIndexSettings() {
        return indexSettings;
    }

    /**
     * Returns the shard ID this config is created for
     */
    public ShardId getShardId() {
        return shardId;
    }

    /**
     * Returns a BigArrays instance for this engine
     */
    public BigArrays getBigArrays() {
        return bigArrays;
    }

    /**
     * Returns the translog path for this engine
     */
    public Path getTranslogPath() {
        return translogPath;
    }

    /**
     * Returns the translog generation to open. If this is <code>null</code> a new translog is created. If non-null
     * the translog tries to open the given translog generation. The generation is treated as the last generation referenced
     * form already committed data. This means all operations that have not yet been committed should be in the translog
     * file referenced by this generation. The translog creation will fail if this generation can't be opened.
     */
    public TranslogGeneration getTranslogGeneration() {
        return translogGeneration;
    }

    /**
     * Set the generation to be opened. Use <code>null</code> to start with a fresh translog.
     * @see #getTranslogGeneration()
     */
    public void setTranslogGeneration(TranslogGeneration translogGeneration) {
        this.translogGeneration = translogGeneration;
    }

    /**
     * The translog buffer size. Default is <tt>8kb</tt>
     */
    public ByteSizeValue getBufferSize() {
        return bufferSize;
    }
}
