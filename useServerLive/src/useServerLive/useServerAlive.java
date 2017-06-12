/**
 * 
 */
package useServerLive;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * 이 프로그램은 서버의 상태를 체크하는 프로그램입니다.
 * 환경파일은 useServer.conf라는 파일에서 출발하며
 * svclist=svclist.conf
 * logpath=logs 
 * udpport=9001
 * 두가지의 값으로 출발합니다 
 * @author allco
 *
 */
public class useServerAlive extends Thread {
	private String svclist;//서버 포트 목록이 있는 파일 경로
	private String logpath;//로그를 기록할 기본디렉토리
	private int udpport;//이것은 모니터링을 위해 그냥 선언
	private HashMap<String,svcListOne> svcList;//점검대상 서버:포트 정보
	private HashMap<String,runServerAlive>runSvcList;//진행중인 서버점검 객체를 넣어둔다.
	private ArrayList<String> svcTypes = new ArrayList<String>(Arrays.asList("http","https","tcp"));
	saveLog sl;
	
	public useServerAlive(){
		super();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 
		useServerAlive usa = new useServerAlive();
		usa.sl = new saveLog();
		usa.readConf();
		usa.sl.setLogpath(usa.logpath);
		usa.runSvcList = new HashMap<String,runServerAlive>();
		usa.start();
	}

	public void run(){
		ArrayList<String> dropSvc = new ArrayList<String>();//서버점검중 종료 시킬 목록
		while(true){
			this.readSvcList();
			//종료 시키고 정리할 점검서버를 정리한다.
			if(dropSvc.size() > 0){
				for(int i=dropSvc.size(); i > 0;i--){
					String s = (String) dropSvc.get(i-1);
					if(runSvcList.containsKey(s)){
						runServerAlive one = (runServerAlive) runSvcList.get(s);
						if(!one.isAlive()){//종료되면 정리한다.
							runSvcList.remove(s);
							dropSvc.remove((i-1));
						}else one.interrupt();
					}else dropSvc.remove((i-1));
				}//end for
			}//end if
			//기존에 있다면 환경을 수정한 후 정지되어 있다면 진행하도록 start시켜준다.
			//기존에 없다면 신규 생성하여 목록에 추가한다.
			if(svcList.size() > 0){
				Set key = svcList.keySet();
				  for(Iterator iterator = key.iterator(); iterator.hasNext();) {
				      String keyName = (String) iterator.next();
				      if(dropSvc.contains(keyName)) dropSvc.remove(keyName);// 제거 목록에 있다면 해당 목록을 지운다.
				      		//기존 점검목록에 없다면 추가 있다면 불러들인다.
				      runServerAlive one;
				      if(!this.runSvcList.containsKey(keyName)){
				    	  one = new runServerAlive();
				    	  one.sl = this.sl;
				    	  runSvcList.put(keyName,one);
				      }else{
				    	  one = (runServerAlive) runSvcList.get(keyName);
				      		}
				      one.setSvcListOne(svcList.get(keyName));
				      if(!one.isAlive()) one.start();
				  }//for end
				
			}
			//종료 시킬 서버를 구분하여 종료 명령을 내린다. 인터럽트 
			if(runSvcList.size() > 0){
				Set key = runSvcList.keySet();
				  for(Iterator iterator = key.iterator(); iterator.hasNext();) {
				      String keyName = (String) iterator.next();
				      		System.out.println(keyName);
				      		//새로 갱신한 목록에 없다면 제거대상이다.
				      if(!this.svcList.containsKey(keyName)){
				    	  runServerAlive one = (runServerAlive) runSvcList.get(keyName);
				    	  one.interrupt();
				    	  dropSvc.add(keyName);
				      		}
				  }//for end
			}
			try{
				Thread.sleep(60000);//1분간격으로 계속 반복
			}catch(InterruptedException e){
				
			}
		}//end while
	}
	/**
	 * 기본환경을 읽어들인다. 없으면 기본값으로 설정한다.
	 */
	private synchronized void readConf(){
			String filename = "useServer.conf";
			String myPath = System.getProperty("user.dir");
			if(sl.file_is_read(filename)){
				  try {
				      BufferedReader in = new BufferedReader(new FileReader(filename));
				      String s;

				      while ((s = in.readLine()) != null) {
				    	  		String[] ss = s.split("=");
				    	  		String sss = ss[0].replaceAll(" ","").toLowerCase();
				    	  		
				    	  		if("svclist".equals(sss)) svclist = ss[1].replaceAll(" ","");
				    	  		else if("logpath".equals(sss)) logpath = ss[1].replaceAll(" ","");
				    	  		else if("udpport".equals(sss)){
				    	  			try{
				    	  				udpport = Integer.parseInt(ss[1].replaceAll(" ",""));
				    	  			}catch(Exception e){}
				    	  				}
				      		}
				      in.close();
				    } catch (IOException e) {
				        System.err.println(e); // 에러가 있다면 메시지 출력
				        //System.exit(1);
				    }
			}
			// 환경이 맞는지 점검을 한다.
			//svclist 환경 점검
			if(svclist==null || "".equals(svclist)) svclist = myPath+"/svclist.conf";
			if(!sl.file_is_read(svclist)){
				svclist = myPath+"/svclist.conf";
				if(!sl.file_is_read(svclist)){
					System.out.println("server port list file not found");
					System.exit(1);
				}
			}
			//logpath
			if(logpath==null || "".equals(logpath)) logpath = myPath+"/logs";
			if(!sl.path_is_edit(logpath)){
				logpath = myPath+"/logs";
				if(!sl.path_is_edit(logpath)){
					System.out.println(logpath+" wirte log file fail");
					System.exit(1);
				}
			}
	}
	
	/**
	 * 서버 점검 포트 리스트를 모두 불러 들여 설정을 한다.
	 */
	private synchronized void readSvcList(){
		if(sl.file_is_read(svclist)){
			this.svcList = new HashMap<String,svcListOne>();
			  try {
			      BufferedReader in = new BufferedReader(new FileReader(svclist));
			      String s;
			      int i=0;
			      while ((s = in.readLine()) != null) {
			    	  i++;
			    	  if(i==1) this.setSvcListOne(true,s);
			    	  else this.setSvcListOne(false,s);
			      		}
			      in.close();
			    } catch (IOException e) {
			        System.err.println(e); // 에러가 있다면 메시지 출력
			        //System.exit(1);
			    }	
		}
	}

	/**
	 * 한줄을 읽어들여 파서한후 서버 포트 리스트에  추가한다.
	 * @param line
	 *# * 0. 서비스 동작여부를 알려줌 1동작 0 멈춤
	# * 1. 서비스 가능 종류 [http,https,telnet,ftp,ssh,tcp] tcp는 포트만 확인함.
	# * 2. 서비스 아이피 또는 도메인 
	# * 3. 서비스 포트 
	# * 4. 서비스의 체크 간격 분단위
	# * 5. 혹시 파일이나 가져올 것이 있는 경우 경로명  예제)/home/test/test.txt 또는 /test/test.html 없으면 공백
	# * 6. 가져온 데이터에 체크 되어야할 값 예제)우리나라 대한민국 또는 serviceOk
	# * 7. 접근시 로그인이 필요한경우 아이디{telnet,ftp,ssh}
	# * 8. 접근시 로그인이 필요한경우 패스워드{telnet,ftp,ssh}
	## 0/1:telnet[ssh|ftp|http|https|ssh|tcp]:ip:port:2:::::
	 */
	private boolean setSvcListOne(boolean start,String line){
		String[] ss = line.split(":");
		//반드시 첫 번필드는 0.1 둘중하나여야한다.
		if(!("0".equals(ss[0]) || "1".equals(ss[0]))){
			sl.saveListSetup(start, false, line);
			return false;
		}else{
			svcListOne one = new svcListOne();
			one.setSvcRun(Integer.parseInt(ss[0]));//0. 서비스 동작여부를 알려줌 1동작 0 멈춤
			//검증 1. 서비스 가능 종류 [http,https,telnet,ftp,ssh,tcp] tcp는 포트만 확인함.
			if(!svcTypes.contains(ss[1].toLowerCase())){
				sl.saveListSetup(start, false, line);
				return false;
			}
			one.setSvcType(ss[1]);
			// 2. 서비스 아이피 또는 도메인
			one.setSvcIp(ss[2]);
			//3. 서비스 포트
			try{
				one.setSvcPort(Integer.parseInt(ss[3]));
			}catch(Exception e){
				sl.saveListSetup(start, false, line);
				e.printStackTrace();
				return false;
			}
			//4. 서비스의 체크 간격 분단위
			try{
				one.setChkTime(Integer.parseInt(ss[4]));
			}catch(Exception e){
				sl.saveListSetup(start, false, line);
				e.printStackTrace();
				return false;
			}
			//5. 혹시 파일이나 가져올 것이 있는 경우 경로명  예제)/home/test/test.txt 또는 /test/test.html 없으면 공백
			if(ss.length > 5 && !"".equals(ss[5])) one.setChkPath(ss[5]);
			else one.setChkPath("/");
			//6. 가져온 데이터에 체크 되어야할 값 예제)우리나라 대한민국 또는 serviceOk
			if(ss.length > 6 && !"".equals(ss[6])) one.setChkMsg(ss[6]);
			//7. 접근시 로그인이 필요한경우 아이디{telnet,ftp,ssh}
			if(ss.length > 7 && !"".equals(ss[7])) one.setLogId(ss[7]);
			//8. 접근시 로그인이 필요한경우 패스워드{telnet,ftp,ssh}
			if(ss.length > 8 && !"".equals(ss[8])) one.setLogPs(ss[8]);
			this.svcList.put(one.getSvcIp()+":"+one.getSvcPort()+one.getChkPath(), one);
			sl.saveListSetup(start, true, line);
			return true;
		}
	}
	
}
