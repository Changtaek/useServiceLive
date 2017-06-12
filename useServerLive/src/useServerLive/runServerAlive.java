/**
 * 
 */
package useServerLive;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @author allco
 * 서버 포트 점검을 하는 객체 쓰레트로 돌면서 각자의 시계로 동작한다.
 */
public class runServerAlive extends Thread {
	saveLog sl;
	int timeOut = 30;//소켓 타임아웃 초 
	public runServerAlive(){
		super();
	}
	
	private svcListOne slo= new svcListOne();
	
	public void run(){
		try{
			slo.defaultLog();
			while(!Thread.currentThread().isInterrupted()){
				if(slo.getSvcRun()==1){//동작중 요청이 되어 있을때만 동작한다. 나머지는 그냥 스킵
					if("http".equals(slo.getSvcType())) runAliveHttp();
					else if("https".equals(slo.getSvcType())) runAliveHttps();
					else runAliveTcp();
				}
				
				Thread.sleep(60*1000*slo.getChkTime());
			}
		}catch(InterruptedException e){
			
		}
	}
	
	private void runAliveHttp(){
		long sWorkTm = System.currentTimeMillis();
		long sPageTm = System.currentTimeMillis();
		long ePageTm = System.currentTimeMillis();
		svcListOne slo1 = new svcListOne();
		slo1.setSvcListOneConf(slo);
		slo1.defaultLog();
		try{
				URL url = new URL("http://"+slo1.getSvcIp()+":"+slo1.getSvcPort()+slo1.getChkPath());
        // HTTP Connection 구하기 
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // 요청 방식 설정 ( GET or POST or .. 별도로 설정하지않으면 GET 방식 )
        conn.setRequestMethod("GET"); 
        // 연결 타임아웃 설정 
        conn.setConnectTimeout(timeOut*1000); 
        // 읽기 타임아웃 설정 
        //conn.setReadTimeout(3000); // 3초 
		  conn.setInstanceFollowRedirects(true);

		  slo1.setResponseCode(conn.getResponseCode());
		    //slo1.setFileSize(conn.getContentLengthLong());
		    slo1.setStrEncode(conn.getContentEncoding());
		    slo1.setHeaderSize(conn.getHeaderFields().toString().getBytes().length);

        sPageTm = System.currentTimeMillis();
				ePageTm = System.currentTimeMillis();
        StringBuffer sb =  new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;  
			  while ((line = br.readLine()) != null) {  
			   sb.append(line);  
			  	}
		    
			  br.close();
			  conn.disconnect();
			  ePageTm = System.currentTimeMillis();
			  slo1.setFileSize(sb.toString().getBytes().length);
			  //페이지 체크
			  if(slo1.getChkMsg()==null || "".equals(slo1.getChkMsg())) slo1.setPageChek(true);
			  else if(sb.toString().indexOf(slo1.getChkMsg()) >=0)  slo1.setPageChek(true);
			  slo1.setPageTime(ePageTm-sPageTm);
			  slo1.setWorkTime(System.currentTimeMillis()-sWorkTm);
        // 접속 해제
			  sl.saveAccess(slo1, " response_code="+slo1.getResponseCode()+" message_check="+slo1.isPageChek());
			  slo1.setConectStr("done");
		}catch(IOException e){
				sl.saveError(slo1, e.getMessage());
		}finally{
			slo.setSvcListOneLog(slo1);//바뀐사실을 검토후 변경 저장
			sl.saveHttpLog(slo, "");
		}
		
	}
	
	private void runAliveHttps(){
		long sWorkTm = System.currentTimeMillis();
		long sPageTm = System.currentTimeMillis();
		long ePageTm = System.currentTimeMillis();
		svcListOne slo1 = new svcListOne();
		slo1.setSvcListOneConf(slo);
		slo1.defaultLog();
                                        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                            return null;
                                        }

                                        public void checkClientTrusted(
                                                java.security.cert.X509Certificate[] certs,
                                                String authType) {
                                        }

                                        public void checkServerTrusted(
                                                java.security.cert.X509Certificate[] certs,
                                                String authType) {
                                        }
                                    } };

                                    //Install the all-trusting trust manager
                                    try {
                                        SSLContext sc = SSLContext.getInstance("SSL");
                                        sc.init(null, trustAllCerts, new java.security.SecureRandom());
                                        HttpsURLConnection.setDefaultSSLSocketFactory(sc
                                                .getSocketFactory());
                                        HttpsURLConnection
                                                .setDefaultHostnameVerifier(new HostnameVerifier() {
                                                    public boolean verify(String paramString,
                                                            SSLSession paramSSLSession) {
                                                        return true;
                                                    }
                                                });
                                    } catch (Exception e) {
                                    }
                                    
		try{
		// Get HTTPS URL connection  
		  URL url = new URL("https://"+slo1.getSvcIp()+":"+slo1.getSvcPort()+slo1.getChkPath());
		  // System.out.println("https://"+slo.getSvcIp()+":"+slo.getSvcPort()+slo.getChkPath());
		  HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();  
/*		    
		  // Set Hostname verification  
		  conn.setHostnameVerifier(new HostnameVerifier() {  
		   @Override  
		   public boolean verify(String hostname, SSLSession session) {  
		    // Ignore host name verification. It always returns true.  
		    return true;  
		   }  
		     
		  }); 
     
		    
		  // SSL setting  
		  SSLContext context = SSLContext.getInstance("TLS");  
		  context.init(null, null, null);  // No validation for now  
                  
		  conn.setSSLSocketFactory(context.getSocketFactory());  
*/		    
	    sPageTm = System.currentTimeMillis();
			ePageTm = System.currentTimeMillis();
		  // Connect to host  
		  conn.connect();  
		  conn.setInstanceFollowRedirects(true);

		  slo1.setResponseCode(conn.getResponseCode());
	    //slo1.setFileSize(conn.getContentLengthLong());
	    slo1.setStrEncode(conn.getContentEncoding());
	    slo1.setHeaderSize(conn.getHeaderFields().toString().getBytes().length);
		    

		  // Print response from host
		  StringBuffer sb =  new StringBuffer();
		  InputStream in = conn.getInputStream();  
		  BufferedReader reader = new BufferedReader(new InputStreamReader(in));  
		  String line = null;  
		  while ((line = reader.readLine()) != null) {  
			  sb.append(line);
		  	}
		  reader.close();
		  conn.disconnect();
		  ePageTm = System.currentTimeMillis();
		  //페이지 체크
		  slo1.setFileSize(sb.toString().getBytes().length);//body size
		  if(slo1.getChkMsg()==null || "".equals(slo1.getChkMsg())) slo1.setPageChek(true);
		  else if(sb.toString().indexOf(slo1.getChkMsg()) >=0)  slo1.setPageChek(true);
		  slo1.setPageTime(ePageTm-sPageTm);
		  slo1.setWorkTime(System.currentTimeMillis()-sWorkTm);
    // 접속 해제
		  
		  sl.saveAccess(slo, " response_code="+slo1.getResponseCode()+" message_check="+slo1.isPageChek());
		  slo1.setConectStr("done");
//		}catch(NoSuchAlgorithmException e1){
//			sl.saveError(slo1, e1.getMessage());
//			e1.printStackTrace();
//		}catch(KeyManagementException e2){
//			sl.saveError(slo1, e2.getMessage());
//			e2.printStackTrace();
		}catch(IOException e3){
			sl.saveError(slo1, e3.getMessage());
			e3.printStackTrace();
		}
		slo.setSvcListOneLog(slo1);//바뀐사실을 검토후 변경 저장
		sl.saveHttpLog(slo, "");
	}
                        
	/**
	 * 설정된 포트가 열리는 지만 파악해서 반환한다.
	 */
	private void runAliveTcp(){
		SocketAddress socketAddress = new InetSocketAddress(slo.getSvcIp(), slo.getSvcPort());
		Socket socket = new Socket();
		try {
			//socket.setSoTimeout(timeout);			/* InputStream에서 데이터읽을때의 timeout */
			// System.out.println(slo.getSvcIp()+":"+slo.getSvcPort());
			socket.connect(socketAddress, (timeOut*1000));	/* socket연결 자체에대한 timeout */
			slo.setConectStr("done");
			sl.saveAccess(slo, "");
		} catch (SocketException e) {
			sl.saveError(slo, "SocketException");
			e.printStackTrace();
		} catch (IOException e) {
			sl.saveError(slo, "IOException");
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 바뀐환경을 등록해준다.
	 * @param o
	 */
	public void setSvcListOne(svcListOne o){
		slo.setSvcListOneConf(o);
	}
}
