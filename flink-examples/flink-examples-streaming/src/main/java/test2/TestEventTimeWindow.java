package test2;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestEventTimeWindow {

	public static void main(String[] args) throws Exception {

		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		DataStream<String> text = env.socketTextStream("localhost", 9000, "\n");


		DataStream<AggResult> windowCounts = text
			.map(new MapFunction<String, WordWithCount>() {
				@Override
				public WordWithCount map(String value) throws Exception {
					final String[] split = value.split("\\s");
					return new WordWithCount(split[0], Long.parseLong(split[1]), Long.parseLong(split[2]));
				}
			})
			.assignTimestampsAndWatermarks(new LogTraceTimer())
			.keyBy("word")
			.timeWindow(Time.seconds(10))
			.apply(new WindowFunction<WordWithCount, AggResult, Tuple, TimeWindow>() {
				@Override
				public void apply(Tuple tuple, TimeWindow window, Iterable<WordWithCount> input, Collector<AggResult> out) throws Exception {
					final long start = window.getStart();
					final long end = window.getEnd();

					StringBuilder sb = new StringBuilder("\n<----------------------->\n");
					sb.append("triggerTime=").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()))).append("\n");
					sb.append("startTime:").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(start)));
					sb.append(" endTime:").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(end))).append("\n");

					for (WordWithCount count : input) {
						sb.append(count.toString()).append("\n");
					}
					sb.append("<----------------------->\n");
					System.out.println(sb.toString());

				}
			});

		windowCounts.print().setParallelism(1);

		env.execute("Socket Window WordCount");
	}

	//也就是说一种聚合类型输出一个Re结果。
	public static class AggResult {
		String key;
		Long Sum;

		@Override
		public String toString() {
			return "AggResult{" + "key='" + key + '\'' + ", Sum=" + Sum + '}';
		}
	}

	public static class LogTraceTimer implements AssignerWithPeriodicWatermarks<WordWithCount> {

		@Override
		public long extractTimestamp(WordWithCount element, long previousElementTimestamp) {
			return element.timestamp;
		}

		@Nullable
		@Override
		public Watermark getCurrentWatermark() {
			return new Watermark(System.currentTimeMillis() - 1000 * 60);
		}
	}


	public static class WordWithCount {
		@Getter
		@Setter
		public String word;
		@Getter
		@Setter
		public long count;
		@Getter
		@Setter
		public long timestamp;


		public WordWithCount() {
		}

		public WordWithCount(String word, long count, long timestamp) {
			this.word = word;
			this.count = count;
			this.timestamp = timestamp;
		}

		@Override
		public String toString() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "  WordWithCount{" +
				"word='" + word + '\'' +
				", count=" + count +
				", logCollectTimestamp=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(this.timestamp)) +
				'}';
		}

	}
}
