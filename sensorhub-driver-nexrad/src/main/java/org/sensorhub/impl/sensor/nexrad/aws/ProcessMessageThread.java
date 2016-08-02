package org.sensorhub.impl.sensor.nexrad.aws;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.sensor.nexrad.aws.sqs.AwsSqsService;
import org.sensorhub.impl.sensor.nexrad.aws.sqs.ChunkPathQueue;
import org.sensorhub.impl.sensor.nexrad.aws.sqs.ProcessChunkThread;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.model.Message;

/**
 * <p>Title: MessageProcessingThread.java</p>
 * <p>Description: </p>
 *
 * @author T
 * @date Mar 2, 2016
 */
public class ProcessMessageThread implements Runnable {

	private AwsSqsService sqsService;
	List<String> sitesToKeep;
	private String outputPath;
	private AmazonS3Client s3client;  // may no longer need this
	ChunkPathQueue chunkQueue;
	
	public ProcessMessageThread(AwsSqsService sqsService, AmazonS3Client client, List<String> sites, String outputPath) throws IOException {
		this.sqsService = sqsService;
		this.s3client = client;
		this.sitesToKeep = sites;
		this.outputPath = outputPath;
	}

	public ProcessMessageThread(AwsSqsService sqsService, AmazonS3Client client, List<String> sites, ChunkPathQueue chunkQueue) {
		this.sqsService = sqsService;
		this.s3client = client;
		this.sitesToKeep = sites;
		this.chunkQueue = chunkQueue;
	}

	@Override
	public void run() {
		for(;;) {
			List<Message> messages = this.sqsService.receiveMessages();
			for(Message msg: messages) {
				String body = msg.getBody();
				String chunkPath = AwsNexradUtil.getChunkPath(body);
				//				String time = AwsNexradUtil.getEventTime(body);
				String site = chunkPath.substring(0, 4);
				if(sitesToKeep.contains(site)) {
					chunkQueue.add(chunkPath);
				}
			}
			sqsService.deleteMessages(messages);		}
	}

	private void processMessagesFile(List<Message> messages) {
		for(Message msg: messages) {
			String body = msg.getBody();
			String chunkPath = AwsNexradUtil.getChunkPath(body);
			//			String time = AwsNexradUtil.getEventTime(body);
			String site = chunkPath.substring(0, 4);
			if(sitesToKeep.size() == 0 || sitesToKeep.contains(site)) {
				ProcessChunkThread t = new ProcessChunkThread(s3client, new LdmLevel2FileWriter(outputPath), chunkPath);
				t.run();
			}

		}
		sqsService.deleteMessages(messages);
	}

	private void processMessagesStream(List<Message> messages) {
		for(Message msg: messages) {
			String body = msg.getBody();
			String chunkPath = AwsNexradUtil.getChunkPath(body);
			//			String time = AwsNexradUtil.getEventTime(body);
			String site = chunkPath.substring(0, 4);
			if(sitesToKeep.size() == 0 || sitesToKeep.contains(site)) {
				ProcessChunkThread t = new ProcessChunkThread(s3client, new LdmLevel2Reader(), chunkPath);
				t.run();
			}
		}
		sqsService.deleteMessages(messages);
	}


	public void addSite(String site) {
		sitesToKeep.add(site);
	}

	public void removeSite(String site) {
		sitesToKeep.remove(site);
	}
}
