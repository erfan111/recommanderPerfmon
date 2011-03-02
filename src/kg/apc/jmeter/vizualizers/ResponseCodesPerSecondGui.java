package kg.apc.jmeter.vizualizers;

import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.charting.AbstractGraphRow;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleResult;

/**
 *
 * @author Stephane Hoblingre
 */
public class ResponseCodesPerSecondGui
        extends AbstractOverTimeVisualizer
{
    private String prefix = null;

    /**
     *
     */
    public ResponseCodesPerSecondGui()
    {
        super();
        setGranulation(1000);
        graphPanel.getGraphObject().setyAxisLabel("Number of Reponses /sec");
    }

    private void addResponse(String threadGroupName, long time)
    {
        AbstractGraphRow row = model.get(threadGroupName);

        if (row == null)
        {
         row = getNewRow(model, AbstractGraphRow.ROW_SUM_VALUES, threadGroupName, AbstractGraphRow.MARKER_SIZE_SMALL, false, false, false, true, true);
        }

        //fix to have /sec values in all cases
        if (getGranulation() > 0)
        {
            row.add(time, 1 * 1000.0d / getGranulation());
        }
    }

    @Override
    public String getLabelResource()
    {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getStaticLabel()
    {
        return JMeterPluginsUtils.prefixLabel("Response Codes per Second");
    }

    @Override
    public void clearData()
    {
        super.clearData();
        prefix = null;
    }

    private String getRespCodeLabel(SampleResult res)
    {
        //double ref to be thread safe on clearData call
        String ret = prefix;
        if(ret == null)
        {
            if(res instanceof HTTPSampleResult)
            {
                prefix = "HTTP_";
                ret = "HTTP_";
            } else {
                prefix = "";
                ret = "";
            }
        }
        String respCode = res.getResponseCode();
        if(respCode == null || respCode.length() == 0)
        {
            ret = ret + "Transaction failed (no code)";
        }
        else
        {
            ret = ret + respCode;
        }
        return ret;
    }

    @Override
    public void add(SampleResult res)
    {
        super.add(res); 
        addResponse(getRespCodeLabel(res), normalizeTime(res.getEndTime()));
        updateGui(null);
    }

    @Override
    protected JSettingsPanel getSettingsPanel()
    {
        return new JSettingsPanel(this, true, true, false, true, true, false, false, false, true);
    }
}