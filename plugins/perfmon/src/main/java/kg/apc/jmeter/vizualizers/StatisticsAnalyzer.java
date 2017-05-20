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
                "هیچ مورد غیر قابل قبولی مشاهده نمی شود",
                "هیچ مورد غیر قابل قبولی مشاهده نمی شود 2",
                "تراکم بالای درخواست ها در شبکه: congested network",
                "داده ها برای تصمیم گیری کافی نیست",
                "ساختار زیربنای شبکه ضعیف است",
                "قدرت پردازشی سیستم را افزایش دهید",
                "سیستم از حداکثر ظرفیت خود استفاده می نماید",
                "تاخیر های بلند مشاهده می گردد. مشکل در وب سرور است"
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
        System.out.println("Result " + index);
        return Result[index];
    }

}
