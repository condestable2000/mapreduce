package histogramas;

import histogramas.Histogram.Map;
import histogramas.Histogram.Reduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.Job;

public class histogramasjob extends Configured implements Tool {
    @Override

	public int run(String[] args) throws Exception {
        Job job = new Job(getConf());
        job.setJarByClass(histogramasjob.class);

        job.setJobName("Histogramas");

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongArrayWritable.class);

        job.setMapperClass(Map.class);
        //job.setCombinerClass(Reduce.class);
        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        return job.waitForCompletion(true) ? 0 : 1;
}

public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new Histogram(), args);
        System.exit(res);
}
}
