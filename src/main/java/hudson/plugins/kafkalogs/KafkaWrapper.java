/*
 * The MIT License
 *
 * Copyright (c) 2017 OC Tanner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.kafkalogs;

import java.util.Properties;
import java.lang.Thread;
import java.io.Serializable;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public final class KafkaWrapper implements Serializable {

	private String kafkaServers;
	private String kafkaTopic;
	private String jobName;
	private int buildId;
	private transient Producer<String, String> producer;

	public KafkaWrapper(int buildId, String jobName, String kafkaServers, String kafkaTopic) {
		this.kafkaServers = kafkaServers;
		this.kafkaTopic = kafkaTopic;
		this.jobName = jobName;
		this.buildId = buildId;

        Thread.currentThread().setContextClassLoader(null);
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "jenkins");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(props);
	}

	public void write(String data) {
		JSONObject value = new JSONObject();
		value.put("job", this.jobName);
		value.put("build", this.buildId);
		value.put("message", data.trim());
		this.producer.send(new ProducerRecord<>(this.kafkaTopic, this.jobName, value.toJSONString()));
	}

	public void flush() {
		this.producer.flush();
	}

	public void close() {
		this.producer.close();
	}



}