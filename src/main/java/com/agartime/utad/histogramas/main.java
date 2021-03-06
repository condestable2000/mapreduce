package histogramas;

public class main { //Esta clase es para borrar

}
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import static com.googlecode.javacv.jna.cxcore.v21.*;
import static com.googlecode.javacv.jna.cv.v21.*;

import edu.vt.io.Image;
import edu.vt.io.LongArrayWritable;
import edu.vt.input.ImageInputFormat;

public class Histogram extends Configured implements Tool {
        public static class Map extends
                        Mapper<Text, Image, Text, LongArrayWritable> {
                private final static LongWritable one = new LongWritable(1);

                @Override
                public void map(Text key, Image value, Context context)
                                throws IOException, InterruptedException {
                        
                        // Convert to gray scale image
                        IplImage im1 = value.getImage();
                        IplImage im2 = cvCreateImage(cvSize(im1.width,im1.height), IPL_DEPTH_8U, 1);
                        cvCvtColor(im1, im2, CV_BGR2GRAY);
                        
                        // Initialize histogram array
                        LongWritable [] histogram = new LongWritable[256];
                        for(int i = 0; i < histogram.length; i++){
                                histogram[i] = new LongWritable();
                        }
                        
                        // Compute histogram
                        ByteBuffer buffer = im2.getByteBuffer();
                        while(buffer.hasRemaining()){
                                int val = buffer.get() + 128;
                                histogram[val].set(histogram[val].get() + 1);
                        }
                        
                        context.write(key, new LongArrayWritable(histogram));
                }
        }

        public static class Reduce extends
                        Reducer<Text, LongArrayWritable, Text, LongArrayWritable> {
                
                @Override
                public void reduce(Text key, Iterable<LongArrayWritable> values,
                                Context context) throws IOException, InterruptedException {
                        
                        // Initialize histogram array
                        LongWritable [] histogram = new LongWritable[256];
                        for(int i = 0; i < histogram.length; i++){
                                histogram[i] = new LongWritable();
                        }
                        
                        // Sum the parts
                        Iterator<LongArrayWritable> it = values.iterator();
                        while (it.hasNext()) {
                                LongWritable[] part = (LongWritable[]) it.next().toArray();
                                for(int i = 0; i < histogram.length; i++){
                                        histogram[i].set(histogram[i].get() + part[i].get());
                                }
                        }
                        
                        context.write(key, new LongArrayWritable(histogram));
                }
        }

        public int run(String[] args) throws Exception {
                Job job = new Job(getConf());
                job.setJarByClass(Histogram.class);

                // Specify various job-specific parameters 
                job.setJobName("Histogram");

                job.setOutputKeyClass(Text.class);
                job.setOutputValueClass(LongArrayWritable.class);

                job.setMapperClass(Map.class);
                //job.setCombinerClass(Reduce.class);
                job.setReducerClass(Reduce.class);

                job.setInputFormatClass(ImageInputFormat.class);
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
