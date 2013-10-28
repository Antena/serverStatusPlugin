package co.antena.newrelic.plugins.serverstatus;


import java.util.logging.Level;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.DiskUsage;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.binding.Context;

public class Status extends Agent {

	private Sigar sigar = new Sigar();
	
	private String name = "server-monitor";
	
    public Status(String name ) {
    	super("co.antena.newrelic.plugins.serverstatus", "1.0.1");

    	this.name = (name != null && !name.isEmpty()) ? name : this.name;
    	
    }
    
	@Override
	public String getComponentHumanLabel() {
		return name;
	}
	
	@Override
	public void pollCycle() {

		String root = "Monitor/";
		reportCpu(root);
		reportLoad(root);
		reportMemory(root);
		reportSwap(root);
		reportDisks(root);
		reportNetworkUsage(root);

	}

	
/* ------------------------ CPU ----------------------------------------------*/	
	private void reportCpu(String prefix){

		try {
			CpuPerc cpuPerc = this.sigar.getCpuPerc();
			reportThisCpu(cpuPerc, prefix.concat("CPUs/"));
		} catch (SigarException e) {
			Context.getLogger().log(Level.WARNING,"Error while getting CPU info", e);
		}
		
		try {
			CpuPerc cpuPerc[] = this.sigar.getCpuPercList();
			for( int i = 0; i < cpuPerc.length; reportThisCpu(cpuPerc[i], prefix.concat( "CPU" + i++ + "/")));
		} catch (SigarException e) {
			Context.getLogger().log(Level.WARNING,"Error while getting CPU info", e);
		}
		
	}
	
	private void reportThisCpu(CpuPerc cpuPerc, String prefix){
		reportMetric(prefix.concat("Combined"), "percent", cpuPerc.getCombined());
		reportMetric(prefix.concat("Irq"), "percent", cpuPerc.getIrq());
		reportMetric(prefix.concat("Nice"), "percent", cpuPerc.getNice());
		reportMetric(prefix.concat("SoftIrq"), "percent", cpuPerc.getSoftIrq());
		reportMetric(prefix.concat("Stolen"), "percent", cpuPerc.getStolen());
		reportMetric(prefix.concat("Sys"), "percent", cpuPerc.getSys());
		reportMetric(prefix.concat("User"), "percent", cpuPerc.getUser());
		reportMetric(prefix.concat("Wait"), "percent", cpuPerc.getWait());
	}

/* ----------------------- Load ---------------------------------------------*/
	private void reportLoad(String prefix){
		try {
			double average[] = this.sigar.getLoadAverage();
			reportMetric(prefix.concat("Load/Average1m"), "load", average[0]);
			reportMetric(prefix.concat("Load/Average5m"), "load", average[1]);
			reportMetric(prefix.concat("Load/Average15m"), "load", average[2]);
		} catch (SigarException e) {
			Context.getLogger().log(Level.WARNING,"Error while getting Load info", e);
		}
		
		
	}
	
/* ----------------------- Memory ---------------------------------------------*/
	private void reportMemory(String prefix){
		
		try {
			Mem mem = this.sigar.getMem();
			reportMetric(prefix.concat("Memory/PercentUsed"), "percent", mem.getUsedPercent() );
			reportMetric(prefix.concat("Memory/ActualUsed"), "bytes", mem.getActualUsed() );
			reportMetric(prefix.concat("Memory/Used"), "bytes", mem.getUsed() );		
		} catch (SigarException e) {
			Context.getLogger().log(Level.WARNING,"Error while getting Memory info", e);
		}
	
	}

/* ----------------------- Swap ---------------------------------------------*/	
	private void reportSwap(String prefix){
		Swap swap;
		try {
			swap = this.sigar.getSwap();
			reportMetric(prefix.concat("Swap/Used"), "bytes",swap.getUsed() );
			reportMetric(prefix.concat("Swap/PageIn"), "pages",swap.getPageIn() );
			reportMetric(prefix.concat("Swap/PageOut"), "pages",swap.getPageOut() );
		} catch (SigarException e) {
			Context.getLogger().log(Level.WARNING,"Error while getting Swap info", e);
		}

	}
	
/* ----------------------- Disk ---------------------------------------------*/	
	private void reportDisks(String prefix){
		
        FileSystem[] fslist;
		try {
			fslist = sigar.getFileSystemList();

			for (int i=0; i<fslist.length; i++) {
	            if (fslist[i].getType() == FileSystem.TYPE_LOCAL_DISK) {
	                   reportThisDisk(sigar.getDiskUsage(  fslist[i].getDevName() ), prefix.concat( "DISK" + fslist[i].getDevName() + "/")  );
	                   reportThisFileSystemUsage(sigar.getFileSystemUsage(fslist[i].getDirName()), prefix.concat( "FILESYSTEM" + fslist[i].getDirName() + "/"));
	            }
	        }
		
		} catch (SigarException e) {
			Context.getLogger().log(Level.WARNING,"Error while getting Disk info", e);
		}
		
	}
	
	
	private void reportThisFileSystemUsage(FileSystemUsage fileSystemUsage,
		String concat) {
		
		reportMetric(concat.concat("Files"), "files", fileSystemUsage.getFiles());
		reportMetric(concat.concat("Used"), "bytes", fileSystemUsage.getUsed());
		reportMetric(concat.concat("UsePercent"), "percent", fileSystemUsage.getUsePercent());
		reportMetric(concat.concat("DiskReads"), "reads", fileSystemUsage.getDiskReads());
		reportMetric(concat.concat("DiskReadBytes"), "bytes", fileSystemUsage.getDiskReadBytes());
		reportMetric(concat.concat("DiskWrites"), "writes", fileSystemUsage.getDiskWrites());
		reportMetric(concat.concat("DiskWriteBytes"), "bytes", fileSystemUsage.getDiskWriteBytes());

	}

	private void reportThisDisk(DiskUsage du, String prefix){
		reportMetric(prefix.concat("Reads"), "reads",du.getReads() );
		reportMetric(prefix.concat("ReadBytes"), "bytes",du.getReadBytes() );
		reportMetric(prefix.concat("WriteBytes"), "writes",du.getWriteBytes() );
		reportMetric(prefix.concat("Writes"), "bytes",du.getWrites() );
	}
	
/* ----------------------- Network ---------------------------------------------*/		
	private void reportNetworkUsage(String prefix){
		String[] ifList;
		try {
			ifList = sigar.getNetInterfaceList();
			for (int i=0; i<ifList.length; i++) {
				reportThisIFUsage(sigar.getNetInterfaceStat(ifList[i]), prefix.concat( "NETWORK" + ifList[i] + "/") );
	        }
		} catch (SigarException e) {
			Context.getLogger().log(Level.WARNING,"Error while getting Network info", e);
		}
	}

	private void reportThisIFUsage(NetInterfaceStat netInterfaceStat, String concat) {
	
		reportMetric(concat.concat("Speed"), "bytes/second",netInterfaceStat.getSpeed() );
		reportMetric(concat.concat("RxBytes"), "bytes", netInterfaceStat.getRxBytes() );
		reportMetric(concat.concat("RxDropped"), "packets",netInterfaceStat.getRxDropped() );
		reportMetric(concat.concat("RxErrors"), "errors", netInterfaceStat.getRxErrors() );
		reportMetric(concat.concat("TxBytes"), "bytes", netInterfaceStat.getTxBytes() );
		reportMetric(concat.concat("TxDropped"), "packets", netInterfaceStat.getTxDropped() );
		reportMetric(concat.concat("TxErrors"), "errors", netInterfaceStat.getTxErrors() );
		
	}
	
	
}