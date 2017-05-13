package kg.apc.jmeter.vizualizers;

/**
 * Created by Erfan Sharafzadeh on 5/12/17.
 */

public class SampleData {

    private Double data;
    private int thread;

    public SampleData()
    {
        data = 0.0;
        thread = 0;
    }

    public SampleData(Double data, int thread)
    {
        this.data = data;
        this.thread = thread;
    }

    public Double getData()
    {
        return data;
    }

    public int getThread()
    {
        return thread;
    }

    public void setData(Double data)
    {
        this.data = data;
    }

    public void setThread(int thread)
    {
        this.thread = thread;
    }

    public String toString()
    {
        return "data: " + data + " thread: " + thread;
    }
}
