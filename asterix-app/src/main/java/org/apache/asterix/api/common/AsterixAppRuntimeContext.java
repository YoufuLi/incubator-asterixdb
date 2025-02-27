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
package org.apache.asterix.api.common;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.asterix.common.api.AsterixThreadExecutor;
import org.apache.asterix.common.api.IAsterixAppRuntimeContext;
import org.apache.asterix.common.config.AsterixCompilerProperties;
import org.apache.asterix.common.config.AsterixExternalProperties;
import org.apache.asterix.common.config.AsterixFeedProperties;
import org.apache.asterix.common.config.AsterixMetadataProperties;
import org.apache.asterix.common.config.AsterixPropertiesAccessor;
import org.apache.asterix.common.config.AsterixStorageProperties;
import org.apache.asterix.common.config.AsterixTransactionProperties;
import org.apache.asterix.common.config.IAsterixPropertiesProvider;
import org.apache.asterix.common.context.AsterixFileMapManager;
import org.apache.asterix.common.context.DatasetLifecycleManager;
import org.apache.asterix.common.exceptions.ACIDException;
import org.apache.asterix.common.exceptions.AsterixException;
import org.apache.asterix.common.feeds.api.IFeedManager;
import org.apache.asterix.common.transactions.IAsterixAppRuntimeContextProvider;
import org.apache.asterix.common.transactions.ITransactionSubsystem;
import org.apache.asterix.feeds.FeedManager;
import org.apache.asterix.metadata.bootstrap.MetadataPrimaryIndexes;
import org.apache.asterix.transaction.management.resource.PersistentLocalResourceRepository;
import org.apache.asterix.transaction.management.resource.PersistentLocalResourceRepositoryFactory;
import org.apache.asterix.transaction.management.service.logging.LogManager;
import org.apache.asterix.transaction.management.service.transaction.TransactionSubsystem;
import org.apache.hyracks.api.application.INCApplicationContext;
import org.apache.hyracks.api.exceptions.HyracksDataException;
import org.apache.hyracks.api.io.IIOManager;
import org.apache.hyracks.api.lifecycle.ILifeCycleComponent;
import org.apache.hyracks.api.lifecycle.ILifeCycleComponentManager;
import org.apache.hyracks.storage.am.common.api.IIndexLifecycleManager;
import org.apache.hyracks.storage.am.lsm.common.api.ILSMIOOperationScheduler;
import org.apache.hyracks.storage.am.lsm.common.api.ILSMMergePolicyFactory;
import org.apache.hyracks.storage.am.lsm.common.api.ILSMOperationTracker;
import org.apache.hyracks.storage.am.lsm.common.api.IVirtualBufferCache;
import org.apache.hyracks.storage.am.lsm.common.impls.AsynchronousScheduler;
import org.apache.hyracks.storage.am.lsm.common.impls.PrefixMergePolicyFactory;
import org.apache.hyracks.storage.common.buffercache.BufferCache;
import org.apache.hyracks.storage.common.buffercache.ClockPageReplacementStrategy;
import org.apache.hyracks.storage.common.buffercache.DelayPageCleanerPolicy;
import org.apache.hyracks.storage.common.buffercache.HeapBufferAllocator;
import org.apache.hyracks.storage.common.buffercache.IBufferCache;
import org.apache.hyracks.storage.common.buffercache.ICacheMemoryAllocator;
import org.apache.hyracks.storage.common.buffercache.IPageCleanerPolicy;
import org.apache.hyracks.storage.common.buffercache.IPageReplacementStrategy;
import org.apache.hyracks.storage.common.file.IFileMapManager;
import org.apache.hyracks.storage.common.file.IFileMapProvider;
import org.apache.hyracks.storage.common.file.ILocalResourceRepository;
import org.apache.hyracks.storage.common.file.ILocalResourceRepositoryFactory;
import org.apache.hyracks.storage.common.file.ResourceIdFactory;
import org.apache.hyracks.storage.common.file.ResourceIdFactoryProvider;

public class AsterixAppRuntimeContext implements IAsterixAppRuntimeContext, IAsterixPropertiesProvider {

    private static final AsterixPropertiesAccessor ASTERIX_PROPERTIES_ACCESSOR;

    static {
        try {
            ASTERIX_PROPERTIES_ACCESSOR = new AsterixPropertiesAccessor();
        } catch (AsterixException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final int METADATA_IO_DEVICE_ID = 0;

    private ILSMMergePolicyFactory metadataMergePolicyFactory;
    private final INCApplicationContext ncApplicationContext;

    private AsterixCompilerProperties compilerProperties;
    private AsterixExternalProperties externalProperties;
    private AsterixMetadataProperties metadataProperties;
    private AsterixStorageProperties storageProperties;
    private AsterixTransactionProperties txnProperties;
    private AsterixFeedProperties feedProperties;


    private AsterixThreadExecutor threadExecutor;
    private DatasetLifecycleManager indexLifecycleManager;
    private IFileMapManager fileMapManager;
    private IBufferCache bufferCache;
    private ITransactionSubsystem txnSubsystem;

    private ILSMIOOperationScheduler lsmIOScheduler;
    private ILocalResourceRepository localResourceRepository;
    private ResourceIdFactory resourceIdFactory;
    private IIOManager ioManager;
    private boolean isShuttingdown;

    private IFeedManager feedManager;

    public AsterixAppRuntimeContext(INCApplicationContext ncApplicationContext) throws AsterixException {
        this.ncApplicationContext = ncApplicationContext;
        compilerProperties = new AsterixCompilerProperties(ASTERIX_PROPERTIES_ACCESSOR);
        externalProperties = new AsterixExternalProperties(ASTERIX_PROPERTIES_ACCESSOR);
        metadataProperties = new AsterixMetadataProperties(ASTERIX_PROPERTIES_ACCESSOR);
        storageProperties = new AsterixStorageProperties(ASTERIX_PROPERTIES_ACCESSOR);
        txnProperties = new AsterixTransactionProperties(ASTERIX_PROPERTIES_ACCESSOR);
        feedProperties = new AsterixFeedProperties(ASTERIX_PROPERTIES_ACCESSOR);
    }

    public void initialize() throws IOException, ACIDException, AsterixException {
        Logger.getLogger("org.apache").setLevel(externalProperties.getLogLevel());

        threadExecutor = new AsterixThreadExecutor(ncApplicationContext.getThreadFactory());
        fileMapManager = new AsterixFileMapManager();
        ICacheMemoryAllocator allocator = new HeapBufferAllocator();
        IPageCleanerPolicy pcp = new DelayPageCleanerPolicy(600000);
        ioManager = ncApplicationContext.getRootContext().getIOManager();
        IPageReplacementStrategy prs = new ClockPageReplacementStrategy(allocator,
                storageProperties.getBufferCachePageSize(), storageProperties.getBufferCacheNumPages());
        bufferCache = new BufferCache(ioManager, prs, pcp, fileMapManager,
                storageProperties.getBufferCacheMaxOpenFiles(), ncApplicationContext.getThreadFactory());

        AsynchronousScheduler.INSTANCE.init(ncApplicationContext.getThreadFactory());
        lsmIOScheduler = AsynchronousScheduler.INSTANCE;

        metadataMergePolicyFactory = new PrefixMergePolicyFactory();

        ILocalResourceRepositoryFactory persistentLocalResourceRepositoryFactory = new PersistentLocalResourceRepositoryFactory(
                ioManager);
        localResourceRepository = (PersistentLocalResourceRepository) persistentLocalResourceRepositoryFactory
                .createRepository();
        resourceIdFactory = (new ResourceIdFactoryProvider(localResourceRepository)).createResourceIdFactory();

        IAsterixAppRuntimeContextProvider asterixAppRuntimeContextProvider = new AsterixAppRuntimeContextProdiverForRecovery(
                this);
        txnSubsystem = new TransactionSubsystem(ncApplicationContext.getNodeId(), asterixAppRuntimeContextProvider,
                txnProperties);
        
        indexLifecycleManager = new DatasetLifecycleManager(storageProperties, localResourceRepository,
                MetadataPrimaryIndexes.FIRST_AVAILABLE_USER_DATASET_ID,(LogManager)txnSubsystem.getLogManager());
        
        isShuttingdown = false;

        feedManager = new FeedManager(ncApplicationContext.getNodeId(), feedProperties,
                compilerProperties.getFrameSize());

        // The order of registration is important. The buffer cache must registered before recovery and transaction managers.
        ILifeCycleComponentManager lccm = ncApplicationContext.getLifeCycleComponentManager();
        lccm.register((ILifeCycleComponent) bufferCache);
        lccm.register((ILifeCycleComponent) txnSubsystem.getTransactionManager());
        lccm.register((ILifeCycleComponent) txnSubsystem.getLogManager());
        lccm.register((ILifeCycleComponent) indexLifecycleManager);
        lccm.register((ILifeCycleComponent) txnSubsystem.getLockManager());
        lccm.register((ILifeCycleComponent) txnSubsystem.getRecoveryManager());
    }

    public boolean isShuttingdown() {
        return isShuttingdown;
    }

    public void setShuttingdown(boolean isShuttingdown) {
        this.isShuttingdown = isShuttingdown;
    }

    public void deinitialize() throws HyracksDataException {
    }

    public IBufferCache getBufferCache() {
        return bufferCache;
    }

    public IFileMapProvider getFileMapManager() {
        return fileMapManager;
    }

    public ITransactionSubsystem getTransactionSubsystem() {
        return txnSubsystem;
    }

    public IIndexLifecycleManager getIndexLifecycleManager() {
        return indexLifecycleManager;
    }

    public double getBloomFilterFalsePositiveRate() {
        return storageProperties.getBloomFilterFalsePositiveRate();
    }

    public ILSMIOOperationScheduler getLSMIOScheduler() {
        return lsmIOScheduler;
    }

    public ILocalResourceRepository getLocalResourceRepository() {
        return localResourceRepository;
    }

    public ResourceIdFactory getResourceIdFactory() {
        return resourceIdFactory;
    }

    public IIOManager getIOManager() {
        return ioManager;
    }

    public int getMetaDataIODeviceId() {
        return METADATA_IO_DEVICE_ID;
    }

    @Override
    public AsterixStorageProperties getStorageProperties() {
        return storageProperties;
    }

    @Override
    public AsterixTransactionProperties getTransactionProperties() {
        return txnProperties;
    }

    @Override
    public AsterixCompilerProperties getCompilerProperties() {
        return compilerProperties;
    }

    @Override
    public AsterixMetadataProperties getMetadataProperties() {
        return metadataProperties;
    }

    @Override
    public AsterixExternalProperties getExternalProperties() {
        return externalProperties;
    }
    
    @Override
    public AsterixFeedProperties getFeedProperties() {
        return feedProperties;
    }

    @Override
    public List<IVirtualBufferCache> getVirtualBufferCaches(int datasetID) {
        return indexLifecycleManager.getVirtualBufferCaches(datasetID);
    }

    @Override
    public ILSMOperationTracker getLSMBTreeOperationTracker(int datasetID) {
        return indexLifecycleManager.getOperationTracker(datasetID);
    }

    @Override
    public AsterixThreadExecutor getThreadExecutor() {
        return threadExecutor;
    }

    public ILSMMergePolicyFactory getMetadataMergePolicyFactory() {
        return metadataMergePolicyFactory;
    }

    @Override
    public IFeedManager getFeedManager() {
        return feedManager;
    }
}
