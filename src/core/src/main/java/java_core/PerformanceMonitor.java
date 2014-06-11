package java_core;

import java.util.ArrayList;
import java.util.HashMap;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarLoader;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.Shell;
import org.hyperic.sigar.cmd.SigarCommandBase;

public class PerformanceMonitor extends SigarCommandBase {
    public boolean displayTimes = true;

    public PerformanceMonitor(Shell shell) {
        super(shell);
    }

    public PerformanceMonitor() {
        super();
    }

    public String getUsageShort() {
        return "Display cpu information";
    }

    public ArrayList<HashMap<String,String>> getCpuInfo() throws SigarException {
        ArrayList<HashMap<String,String>> arr = new ArrayList<HashMap<String,String>>();

        CpuPerc[] cpus = this.sigar.getCpuPercList();
        for (int i=0; i<cpus.length; i++) {
            HashMap<String,String> hm = new HashMap<String,String>();
            CpuPerc cpu = cpus[i];

            hm.put("UserTime", CpuPerc.format(cpu.getUser()).substring(0, CpuPerc.format(cpu.getUser()).length() - 1));
            hm.put("SysTime", CpuPerc.format(cpu.getSys()).substring(0, CpuPerc.format(cpu.getSys()).length() - 1));
            hm.put("IdleTime", CpuPerc.format(cpu.getIdle()).substring(0, CpuPerc.format(cpu.getIdle()).length() - 1));
            hm.put("WaitTime", CpuPerc.format(cpu.getWait()).substring(0, CpuPerc.format(cpu.getWait()).length() - 1));
            hm.put("NiceTime", CpuPerc.format(cpu.getNice()).substring(0, CpuPerc.format(cpu.getNice()).length() - 1));
            hm.put("CombinedTime", CpuPerc.format(cpu.getCombined()).substring(0, CpuPerc.format(cpu.getCombined()).length() - 1));
            hm.put("IrqTime", CpuPerc.format(cpu.getIrq()).substring(0, CpuPerc.format(cpu.getIrq()).length() - 1));
            if (SigarLoader.IS_LINUX) {
                hm.put("SoftIrqTime", CpuPerc.format(cpu.getSoftIrq()).substring(0, CpuPerc.format(cpu.getSoftIrq()).length() - 1));
                hm.put("StolenTime", CpuPerc.format(cpu.getStolen()).substring(0, CpuPerc.format(cpu.getStolen()).length() - 1));
            }

            arr.add(hm);
        }

        return arr;
    }
    public HashMap<String,Long> getMemoryInfo() throws SigarException {
        int mb = 1024*1024;

        HashMap<String,Long> hm = new HashMap<String,Long>();

        Runtime runtime = Runtime.getRuntime();
        hm.put("Used",(runtime.totalMemory()-runtime.freeMemory()) / mb);
        hm.put("Free",runtime.freeMemory()/mb);
        hm.put("total",runtime.totalMemory()/mb);
        hm.put("max",runtime.maxMemory()/mb);
        return hm;
    }

    private void output(CpuPerc cpu) {
        println("User Time....." + CpuPerc.format(cpu.getUser()));
        println("Sys Time......" + CpuPerc.format(cpu.getSys()));
        println("Idle Time....." + CpuPerc.format(cpu.getIdle()));
        println("Wait Time....." + CpuPerc.format(cpu.getWait()));
        println("Nice Time....." + CpuPerc.format(cpu.getNice()));
        println("Combined......" + CpuPerc.format(cpu.getCombined()));
        println("Irq Time......" + CpuPerc.format(cpu.getIrq()));
        if (SigarLoader.IS_LINUX) {
            println("SoftIrq Time.." + CpuPerc.format(cpu.getSoftIrq()));
            println("Stolen Time...." + CpuPerc.format(cpu.getStolen()));
        }
        println("");
    }

    public void output(String[] args) throws SigarException {
        org.hyperic.sigar.CpuInfo[] infos = this.sigar.getCpuInfoList();

        CpuPerc[] cpus = this.sigar.getCpuPercList();

        org.hyperic.sigar.CpuInfo info = infos[0];
        long cacheSize = info.getCacheSize();
        println("Vendor........." + info.getVendor());
        println("Model.........." + info.getModel());
        println("Mhz............" + info.getMhz());
        println("Total CPUs....." + info.getTotalCores());
        if ((info.getTotalCores() != info.getTotalSockets()) || (info.getCoresPerSocket() > info.getTotalCores()))
        {
            println("Physical CPUs.." + info.getTotalSockets());
            println("Cores per CPU.." + info.getCoresPerSocket());
        }

        if (cacheSize != Sigar.FIELD_NOTIMPL) {
            println("Cache size...." + cacheSize);
        }

        println("");

        if (!this.displayTimes) {
            return;
        }

        for (int i=0; i<cpus.length; i++) {
            println("CPU " + i + ".........");
            output(cpus[i]);
        }

        println("Totals........");
        output(this.sigar.getCpuPerc());
    }
}