package com.ctrip.xpipe.redis.integratedtest.keeper;

import java.net.InetSocketAddress;

import com.ctrip.xpipe.redis.core.entity.KeeperMeta;
import com.ctrip.xpipe.redis.core.entity.RedisMeta;
import com.ctrip.xpipe.redis.core.meta.KeeperState;
import com.ctrip.xpipe.redis.core.protocal.cmd.AbstractKeeperCommand.KeeperGetStateCommand;
import com.ctrip.xpipe.redis.core.protocal.cmd.AbstractKeeperCommand.KeeperSetStateCommand;
import com.ctrip.xpipe.redis.integratedtest.AbstractIntegratedTest;
import com.ctrip.xpipe.redis.keeper.config.KeeperConfig;
import com.ctrip.xpipe.redis.keeper.config.TestKeeperConfig;

/**
 * @author wenchao.meng
 *
 * Aug 17, 2016
 */
public abstract class AbstractKeeperIntegrated extends AbstractIntegratedTest{

	protected int replicationStoreCommandFileSize = 1024;
	private int replicationStoreCommandFileNumToKeep = 2;
	private int replicationStoreMaxCommandsToTransferBeforeCreateRdb = 1024;
	private int minTimeMilliToGcAfterCreate = 2000;
	

	@Override
	protected String getRedisTemplate() {
		return "conf/redis_raw.conf";
	}

	protected KeeperMeta getKeeperActive(RedisMeta redisMeta) {
		
		for(KeeperMeta keeper : redisMeta.parent().getKeepers()){
			if(keeper.isActive()){
				return keeper;
			}
		}
		return null;
	}

	protected void setKeeperState(KeeperMeta keeperMeta, KeeperState keeperState, String ip, Integer port) throws Exception {
		KeeperSetStateCommand command = new KeeperSetStateCommand(keeperMeta, keeperState, new InetSocketAddress(ip, port));
		command.execute().sync();
	}

	protected KeeperState getKeeperState(KeeperMeta keeperMeta) throws Exception {
		KeeperGetStateCommand command = new KeeperGetStateCommand(keeperMeta);
		return command.execute().get();
	}

	
	protected KeeperConfig getKeeperConfig() {
		return new TestKeeperConfig(replicationStoreCommandFileSize, replicationStoreCommandFileNumToKeep, 
				replicationStoreMaxCommandsToTransferBeforeCreateRdb, minTimeMilliToGcAfterCreate);
	}

}
