package kg.apc.jmeter.vizualizers;

import org.apache.commons.jexl2.internal.AbstractExecutor;
import org.apache.xpath.operations.Bool;
import sun.nio.ch.Net;
import sun.nio.ch.Secrets;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Erfan Sharafzadeh on 5/11/17.
 */

public class StatisticsCollector {

    //private ArrayList<Double> CPU;
    //private ArrayList<Double> Mem;
    //private ArrayList<Double> Network;
    //private ArrayList<Double> Disk;
    //private ArrayList<Double> Latency;

    private int tempThread;

    private ArrayList<SampleData> CPU;
    private ArrayList<SampleData> Network;
    private ArrayList<SampleData> Disk;
    private ArrayList<SampleData> Mem;
    private ArrayList<Integer> Tail;
    File latencyFile = new File("/home/erfan/httpraw.txt");
    BufferedReader reader = null;


    private ArrayList<SampleData> CPUAverages;
    private ArrayList<SampleData> MemAverages;
    private ArrayList<SampleData> NetworkAverages;
    private ArrayList<SampleData> DiskAverages;

    private ArrayList<Integer> CpuStartGapIndex;
    private ArrayList<Integer> CpuEndGapIndex;

    private ArrayList<Integer> NetworkStartGapIndex;
    private ArrayList<Integer> NetworkEndGapIndex;

    private ArrayList<Integer> MemStartGapIndex;
    private ArrayList<Integer> MemEndGapIndex;

    private ArrayList<Integer> DiskStartGapIndex;
    private ArrayList<Integer> DiskEndGapIndex;

    private StatisticsAnalyzer Analyzer;


    private static final double CpuGapThreshold = 5.0;
    private static double NetworkGapThreshold = 500000.0;
    private static final double MemGapThreshold = 5.0;
    private static final double DiskGapThreshold = 0.01;

    public StatisticsCollector()
    {
        CPU = new ArrayList<>();
        Mem = new ArrayList<>();
        Network = new ArrayList<>();
        Disk = new ArrayList<>();
        Tail = new ArrayList<>();

        CPUAverages = new ArrayList<>();
        MemAverages = new ArrayList<>();
        NetworkAverages = new ArrayList<>();
        DiskAverages = new ArrayList<>();

        CpuStartGapIndex = new ArrayList<>();
        CpuEndGapIndex = new ArrayList<>();

        NetworkEndGapIndex = new ArrayList<>();
        NetworkStartGapIndex = new ArrayList<>();

        DiskEndGapIndex = new ArrayList<>();
        DiskStartGapIndex = new ArrayList<>();



        MemEndGapIndex = new ArrayList<>();
        MemStartGapIndex = new ArrayList<>();

        Analyzer = new StatisticsAnalyzer();

        tempThread = 10;

        try {
            reader = new BufferedReader(new FileReader(latencyFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void CollectCpuStat(Double data)
    {
        CPU.add(new SampleData(data, tempThread));
    }

    private void CollectMemStat(Double data)
    {
        Mem.add(new SampleData(data, tempThread));
    }

    private void CollectNetStat(Double data)
    {
        Network.add(new SampleData(data, tempThread));
    }

    private void CollectDiskStat(Double data)
    {
        Disk.add(new SampleData(data, tempThread));
    }

    private SampleData GetAverage(ArrayList<SampleData> data)
    {
//        Double sum = 0.0;
//        Double avg;
//        for (Double item: data) {
//            sum += item;
//        }
//        avg = sum/data.size();
//        data.clear();
//        return avg.toString();
        System.out.println("MAAAAAAX");


        //String max =  Collections.max(data).toString();
        Double max = 0.0;
        SampleData max_item = new SampleData();
        for (SampleData item: data
             ) {
            System.out.println(item);
            if(item.getData() > max)
            {
                max = item.getData();
                max_item = item;
            }
        }




        System.out.println("%%%%%%%%%%%%%%%%%1");
        data.clear();
        System.out.println("%%%%%%%%%%%%%%%%%2");

        return max_item;
    }

    private SampleData GetCpuStat()
    {
        return GetAverage(CPU);
    }

    private SampleData GetMemStat()
    {
        return GetAverage(Mem);
    }

    private SampleData GetNetStat()
    {
        return GetAverage(Network);
    }

    private SampleData GetDiskStat()
    {
        return GetAverage(Disk);
    }

    public void CollectData(String type, Double value) {

        System.out.println(type + " " + value);
        if (type != null) {
            switch (type.split(" ")[1]) {
                case "CPU":
                    CollectCpuStat(value);
                    break;
                case "Memory":
                    CollectMemStat(value);
                    break;
                case "Network":
                    CollectNetStat(value);
                    break;
                case "Disks":
                    CollectDiskStat(value);
                    break;
                case "EXEC":
                    //Do something
                    if(value != tempThread)
                        tempThread = value.intValue();
                    break;
                default:
                    System.out.println("Wrong Type");
            }
        }
    }

    public void periodicCollection()
    {
        System.out.println("1");
        CPUAverages.add(GetCpuStat());
        System.out.println("2");
        MemAverages.add(GetMemStat());
        System.out.println("3");
        NetworkAverages.add(GetNetStat());
        System.out.println("4");
        DiskAverages.add(GetDiskStat());
    }


    public void findCpuGaps()
    {
        System.out.println("findCpuGaps");

        CpuStartGapIndex.clear();
        CpuEndGapIndex.clear();

        boolean StartGap = false;
        double Abs;
        for(int i = 0; i < CPUAverages.size(); i++)
        {
            if(i == 0 && !StartGap)
            {
                System.out.println("startGap " + i);
                CpuStartGapIndex.add(i);
                StartGap = true;
            }
            else if( i <= CPUAverages.size() - 2)
            {
                Abs = Math.abs(CPUAverages.get(i).getData() - CPUAverages.get(i - 1).getData());

                if(StartGap && Abs >= CpuGapThreshold)
                {
                    System.out.println("EndGap " + (i - 1) + "Start Gap " + i);
                    CpuEndGapIndex.add(i - 1);
                    CpuStartGapIndex.add(i);
                }
            }
            else
            {
                System.out.println("EndGap " + i);
                CpuEndGapIndex.add(i);
                StartGap = false;
            }
        }

        for (SampleData item: CPUAverages
             ) {
            System.out.print(item + " ");
        }

        System.out.println();


        for(int item: CpuStartGapIndex
             ) {
             System.out.print(item + " ");
        }
        System.out.println();
        for( int item: CpuEndGapIndex
             ) {
            System.out.print(item + " ");
        }
        System.out.println();
    }

    public void findNetGaps()
    {
        System.out.println("findNetGaps");

        NetworkStartGapIndex.clear();
        NetworkEndGapIndex.clear();

        boolean modified = false;

        boolean StartGap = false;
        double Abs;
        for(int i = 0; i < NetworkAverages.size(); i++)
        {
            System.out.println(NetworkGapThreshold);
            if(i == 0 && !StartGap)
            {
                System.out.println("startGap " + i);
                NetworkStartGapIndex.add(i);
                StartGap = true;
            }
            else if( i <= NetworkAverages.size() - 2)
            {
                Abs = Math.abs(NetworkAverages.get(i).getData() - NetworkAverages.get(i - 1).getData());

                if(StartGap && Abs >= NetworkGapThreshold)
                {

                    System.out.println("EndGap " + (i - 1) + "Start Gap " + i);
                    NetworkEndGapIndex.add(i - 1);
                    NetworkStartGapIndex.add(i);
                    if(modified) {
                        NetworkGapThreshold *= 1.2;
                        modified = false;
                    }
                }
                else
                {
                    if(StartGap && Abs >= NetworkGapThreshold/2)
                    {
                        System.out.println("EndGap " + (i - 1) + "Start Gap " + i);
                        NetworkEndGapIndex.add(i - 1);
                        NetworkStartGapIndex.add(i);
                        if(!modified)
                            NetworkGapThreshold /= 1.2;
                            modified = true;
                    }
                }
            }
            else
            {
                System.out.println("EndGap " + i);
                NetworkEndGapIndex.add(i);
                StartGap = false;
            }
        }

        NetworkGapThreshold = 500000.0;

        for (SampleData item: NetworkAverages
                ) {
            System.out.print(item + " ");
        }

        System.out.println();

        for(int item: NetworkStartGapIndex
                ) {
            System.out.print(item + " ");
        }
        System.out.println();
        for( int item: NetworkEndGapIndex
                ) {
            System.out.print(item + " ");
        }
        System.out.println();
    }

    public void findMemGaps()
    {
        System.out.println("findMemGaps");

        MemStartGapIndex.clear();
        MemEndGapIndex.clear();

        boolean StartGap = false;
        double Abs;
        for(int i = 0; i < MemAverages.size(); i++)
        {
            if(i == 0 && !StartGap)
            {
                System.out.println("startGap " + i);
                MemStartGapIndex.add(i);
                StartGap = true;
            }
            else if( i <= MemAverages.size() - 2)
            {
                Abs = Math.abs(MemAverages.get(i).getData() - MemAverages.get(i - 1).getData());

                if(StartGap && Abs >= MemGapThreshold)
                {
                    System.out.println("EndGap " + (i - 1) + "Start Gap " + i);
                    MemEndGapIndex.add(i - 1);
                    MemStartGapIndex.add(i);
                }
            }
            else
            {
                System.out.println("EndGap " + i);
                MemEndGapIndex.add(i);
                StartGap = false;
            }
        }


        for (SampleData item: MemAverages
                ) {
            System.out.print(item + " ");
        }

        System.out.println();


        for(int item: MemStartGapIndex
                ) {
            System.out.print(item + " ");
        }

        System.out.println();

        for( int item: MemEndGapIndex
                ) {
            System.out.print(item + " ");
        }

        System.out.println();
    }

    public void findDiskGaps()
    {
        System.out.println("findDiskGaps");

        DiskStartGapIndex.clear();
        DiskEndGapIndex.clear();

        boolean StartGap = false;
        double Abs;
        for(int i = 0; i < DiskAverages.size(); i++)
        {
            if(i == 0 && !StartGap)
            {
                System.out.println("startGap " + i);
                DiskStartGapIndex.add(i);
                StartGap = true;
            }
            else if( i <= DiskAverages.size() - 2)
            {
                Abs = Math.abs(DiskAverages.get(i).getData() - DiskAverages.get(i - 1).getData());

                if(StartGap && Abs >= DiskGapThreshold)
                {
                    System.out.println("EndGap " + (i - 1) + "Start Gap " + i);
                    DiskEndGapIndex.add(i - 1);
                    DiskStartGapIndex.add(i);
                }
            }
            else
            {
                System.out.println("EndGap " + i);
                DiskEndGapIndex.add(i);
                StartGap = false;
            }
        }

        for (SampleData item: DiskAverages
                ) {
            System.out.print(item + " ");
        }

        System.out.println();

        for(int item: DiskStartGapIndex
                ) {
            System.out.print(item + " ");
        }

        System.out.println();

        for( int item: DiskEndGapIndex
                ) {
            System.out.print(item + " ");
        }

        System.out.println();
    }

    public String makeDecision() {
        Double sum = 0.0;
        Double averageRange = 0.0;
        for (int i = 0; i < CpuStartGapIndex.size(); i++) {
            sum = sum + (CpuEndGapIndex.get(i) - CpuStartGapIndex.get(i));
        }

        averageRange = sum / CpuStartGapIndex.size();

        int firstDistance = 0;
        int secondDistance;

        int iteration = 1;

        for(int i = 0; i < CpuStartGapIndex.size(); i++)
        {
            secondDistance = CpuEndGapIndex.get(i) - CpuStartGapIndex.get(i);
            if( firstDistance != 0 && secondDistance != 0 &&
                    secondDistance > averageRange && secondDistance > firstDistance)
            {
                //Tail in Second Distance
                if(CPUAverages.get((CpuEndGapIndex.get(i) + CpuStartGapIndex.get(i))/2).getData() > 10) {
                    iteration++;
                }
            }

            firstDistance = secondDistance;
        }

        return iteration + "";
    }

    private String getSystemStatus()
    {
        Double CpuAvg = 0.0;
        Double MemAvg = 0.0;
        Double NetAvg = 0.0;
        Double TailAvg = 0.0;

        Boolean CpuStat;
        Boolean TailStat;
        Boolean NetStat;

        for(SampleData item: CPUAverages)
        {
            CpuAvg = CpuAvg + item.getData();
        }

        for(SampleData item: NetworkAverages)
        {
            NetAvg = NetAvg + item.getData();
        }

        for(SampleData item: MemAverages)
        {
            MemAvg = MemAvg + item.getData();
        }

        // =e
        String temp = "";
        try {
            while((temp = reader.readLine()) != null){
                Tail.add(Integer.parseInt(temp));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //

        for(Integer item: Tail)
        {
            TailAvg = TailAvg + item;
        }

        Tail.clear();


        CpuStat = CpuAvg > 80;
        NetStat = NetAvg > 10000000;
        TailStat = TailAvg > 20;

        return Analyzer.getStatus(CpuStat, TailStat, NetStat);

    }
}
