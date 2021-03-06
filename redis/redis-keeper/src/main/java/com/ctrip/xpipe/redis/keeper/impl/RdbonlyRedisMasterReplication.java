package com.ctrip.xpipe.redis.keeper.impl;

import java.io.IOException;

import com.ctrip.xpipe.api.server.PARTIAL_STATE;
import com.ctrip.xpipe.redis.core.protocal.Psync;
import com.ctrip.xpipe.redis.core.protocal.cmd.RdbOnlyPsync;
import com.ctrip.xpipe.redis.core.store.DumpedRdbStore;
import com.ctrip.xpipe.redis.keeper.RdbDumper;
import com.ctrip.xpipe.redis.keeper.RedisKeeperServer;
import com.ctrip.xpipe.redis.keeper.RedisMaster;
import com.ctrip.xpipe.redis.keeper.store.RdbOnlyReplicationStore;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * @author wenchao.meng
 *
 * Aug 24, 2016
 */
public class RdbonlyRedisMasterReplication extends AbstractRedisMasterReplication{

	private RdbOnlyReplicationStore rdbOnlyReplicationStore;
	private DumpedRdbStore dumpedRdbStore;
	
	public RdbonlyRedisMasterReplication(RedisKeeperServer redisKeeperServer, RedisMaster redisMaster, RdbDumper rdbDumper) {
		super(redisKeeperServer, redisMaster);
		setRdbDumper(rdbDumper);
	}
	
	@Override
	protected void doInitialize() throws Exception {
		super.doInitialize();
		
		dumpedRdbStore = getRdbDumper().prepareRdbStore();
		logger.info("[doInitialize][newRdbFile]{}", dumpedRdbStore);
		rdbOnlyReplicationStore = new RdbOnlyReplicationStore(dumpedRdbStore);
		
	}


	@Override
	protected void doConnect(Bootstrap b) {

		tryConnect(b).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if(!future.isSuccess()){
					logger.info("[operationComplete][fail]", future.cause());
					dumpFail(future.cause());
				}
			}
		});
	}

	@Override
	protected void kinfoFail(Throwable cause) {
		throw new IllegalStateException("impossible to be here");
	}

	@Override
	protected void psyncFail(Throwable cause) {
	}

	@Override
	protected Psync createPsync() {
		
		Psync psync = new RdbOnlyPsync(clientPool, rdbOnlyReplicationStore);
		psync.addPsyncObserver(this);
		return psync;
	}

	@Override
	public PARTIAL_STATE partialState() {
		return PARTIAL_STATE.FULL;
	}

	@Override
	protected void doReFullSync() {
		throw new IllegalStateException("impossible to be here");
	}

	@Override
	protected void doBeginWriteRdb(long fileSize, long masterRdbOffset) throws IOException {
	}

	@Override
	protected void doEndWriteRdb() {
		
		logger.info("[endWriteRdb]{}", masterChannel);
		masterChannel.close();
	}

	@Override
	protected void doOnContinue() {
		throw new IllegalStateException("impossible to be here");
	}

	@Override
	protected void doOnFullSync() {
		
	}
}
