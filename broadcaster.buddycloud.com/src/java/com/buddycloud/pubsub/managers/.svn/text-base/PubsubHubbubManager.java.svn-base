package com.buddycloud.pubsub.managers;

import java.security.SignatureException;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.buddycloud.pubsub.Item.PubsubhubbubPacket;
import com.buddycloud.pubsub.log.LogMe;

public class PubsubHubbubManager {
	
	private static PubsubHubbubManager instance;
	
	protected LinkedBlockingQueue<PubsubhubbubPacket> queue = new LinkedBlockingQueue<PubsubhubbubPacket>();
	
	Thread[] consumers = new Thread[5];
	
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	private PubsubHubbubManager() {
		
		for (int i = 0; i < consumers.length; i++) {
			this.consumers[i] = new Thread(new Consumer());
			this.consumers[i].start();
		}
	}
	
	public static PubsubHubbubManager getInstance() {
		if(instance == null) {
			instance = new PubsubHubbubManager();
		}
		return instance;
	}
	
	public void addPacket(PubsubhubbubPacket packet) {
		try {
			this.queue.put(packet);
		} catch (InterruptedException e) {
			LogMe.warning("InterruptedException on addPacket: '" + e.getMessage() + "'!");
			e.printStackTrace();
		} catch (Exception e) {
			LogMe.warning("Error adding addPacket: '" + e.getMessage() + "'!");
		}
	}
	
	private class Consumer implements Runnable {
		
		@Override
		public void run() {
			while (true) {
				try {
					PubsubhubbubPacket packet = queue.take();
					LogMe.debug("PubsubHubbubManager is ready to call '" + packet.ps.getCallback() + "'.");
					LogMe.debug("Trying to post: '" +  packet.feedXML + "'.'");
					
					HttpClient httpclient = new DefaultHttpClient();
					StringEntity reqEntity = new StringEntity(packet.feedXML,"UTF-8");

					//PostMethod post = new PostMethod(packet.url);
					HttpPost post = new HttpPost(packet.ps.getCallback());
					post.setEntity(reqEntity);
					post.setHeader("Content-Type", "application/atom+xml");
					
					if(packet.ps.getSecret() != null && !packet.ps.getSecret().equals("")) {
						String sign = calculateRFC2104HMAC(packet.feedXML, packet.ps.getSecret());
						//String sign = "test";
						post.setHeader("X-Hub-Signature", "sha1=" + sign);
					}
					
					LogMe.debug("Executing request '" + post.getRequestLine() + "'.");
					HttpResponse response = httpclient.execute(post);
			        HttpEntity resEntity = response.getEntity();

			        LogMe.debug("Response '" + response.getStatusLine().toString() + "'.");
			        if (resEntity != null) {
			            LogMe.debug("Response content length: " + resEntity.getContentLength());
			        }
			        httpclient.getConnectionManager().shutdown();
				} catch (InterruptedException e) {
					LogMe.warning("Error consuming PubsubHubbub: '" + e.getMessage() + "'!");
					e.printStackTrace();
				} catch (Exception e) {
					LogMe.warning("Error consuming PubsubHubbub: '" + e.getMessage() + "'!");
				}
			}
		}
	}

	public static String calculateRFC2104HMAC(String data, String key) throws java.security.SignatureException {
		String result;
		try {

			/// Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM);

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            
            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());

            // Convert raw bytes to Hex
            byte[] hexBytes = new Hex().encode(rawHmac);

            //  Covert array of Hex bytes to a String
            result = new String(hexBytes, "ISO-8859-1");
    
            LogMe.debug("Created hash: '" + result + "' with key '" + key + "'.");

		} catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}
		return result;
    }

}
