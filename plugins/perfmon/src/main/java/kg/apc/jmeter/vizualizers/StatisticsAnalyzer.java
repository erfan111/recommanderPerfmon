package kg.apc.jmeter.vizualizers;

import org.apache.regexp.RE;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Erfan Sharafzadeh on 5/11/17.
 */
public class StatisticsAnalyzer {

    //public
    private Boolean[][] SystemStateDecisionHelper;
    private String[] Result;


    public StatisticsAnalyzer()
    {

        SystemStateDecisionHelper = new Boolean[][]{
                {
                        false, false, false
                },
                {
                        false, false, true
                },
                {
                        false, true, true
                },
                {
                        true, true, true
                },
                {
                        true, true, false
                },
                {
                        true, false, false
                },
                {
                        true, false, true
                },
                {
                        false, true, false
                }
        };

        Result = new String[] {
                "Don't Care",
                "OK",
                "Congested Net",
                "Don't Care",
                "Network Infrastructure",
                "Add CPU Power",
                "Fully Utilized System",
                "Client Queue, Network Infrastructure"
        };
    }

    public String getStatus(Boolean CPU, Boolean Tail, Boolean Network)
    {
        int index = 0;
        for(int i = 0; i < 8; i++)
        {
            if(SystemStateDecisionHelper[i][0] == CPU &&
                    SystemStateDecisionHelper[i][1] == Tail &&
                    SystemStateDecisionHelper[i][2] == Network)
            {
                index = i;
            }
        }
        return Result[index];
    }

}
