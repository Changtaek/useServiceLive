/**
 * 
 */
package useServerLive;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * 로그파일에 로그를 기록하는 일을 맞음.
 * 로그파일의 종류는 
 * access.log 서버 포트 체크 정상이면 이곳에
 * error.log 서버 포트 체크 실패이면 이곳에
 * svclistsetup.log 서비스를 불러들이면서 서비스 불러들인것이 성공인지를 확인하는 로그 
 * http_access.log 웹포트에 대해서만 별도로 기록함.
 * @author allco
 *
 */
public class saveLog {
	private String logpath;

	/**
	 *  웹서버의 점검에만 쌓이는 로그
	 * @param slo
	 * @param log
	 */
	public synchronized void saveHttpLog(svcListOne slo,String log){
		String logfile = logpath+"/http/"+nowDtYmdHis().replaceAll("-","").replaceAll(":", "").replaceAll(" ","").substring(0, 10)+".log";
		//로그를 기록할수 있으며 로그가 수정되거나 시간이 되어 파일이 바뀌면 저장한다. 
		if(path_is_edit(logpath+"/http") && (slo.isChangeLog() || !logfile.equals(slo.getOldlogfile()))){
		 try {
			 	
		      BufferedWriter out = new BufferedWriter(new FileWriter(logfile,true));
		      String s = nowDtYmdHis()+", url="+slo.getSvcIp()+":"+slo.getSvcPort()+slo.getChkPath()+", web_status="+slo.getConectStr();
		      s += ", web_respone="+slo.getResponseCode()+", res_header_size="+slo.getHeaderSize()+", res_body_size="+slo.getFileSize()+", total_delay="+slo.getWorkTime();
		      s += ", delay="+slo.getPageTime()+", page_check="+slo.isPageChek()+" "+log;
		      out.write(s); out.newLine();
		      out.close();
		      slo.setChangeLog(false);
		      slo.setOldlogfile(logfile);
		    } catch (IOException e) {
		        System.err.println(e); // 에러가 있다면 메시지 출력
		        //System.exit(1);
		    }
		}
	}
	/**
	 * 점검 서버 포트 목록을 환경설정에 정상적으로 처리했는지 확인해주는 로그를 기록한다.
	 * 
	 * @param start 첫라인인경우 true 나머지는 모두 false
	 * @param ok 성공이면 true 실패면 false
	 * @param line 작업한 라인을 그대로 다시 입력함.
	 */
	public synchronized void saveListSetup(boolean start,boolean ok,String line){
		 try {
			 		BufferedWriter out;
			 		if(start) out = new BufferedWriter(new FileWriter(logpath+"/svclistsetup.log"));
			 		else out = new BufferedWriter(new FileWriter(logpath+"/svclistsetup.log",true));
		      if(ok) out.write("OK "+line);
		      else out.write("NOT "+line);
		      out.newLine();
		      out.close();
		    } catch (IOException e) {
		        System.err.println(e); // 에러가 있다면 메시지 출력
		        //System.exit(1);
		    }
	}
	/**
	 * 포트연결에 성공한경우에 기록한다.
	 * @param slo 점검할 기본정보
	 * @param log //기록할 로그
	 */
	public synchronized void saveAccess(svcListOne slo,String log){
		 try {
		      BufferedWriter out = new BufferedWriter(new FileWriter(logpath+"/access.log",true));
		      String s = nowDtYmdHis()+" "+slo.getSvcIp()+":"+slo.getSvcPort()+" "+slo.getSvcType()+" connect "+log;

		      out.write(s); out.newLine();
		      out.close();
		    } catch (IOException e) {
		        System.err.println(e); // 에러가 있다면 메시지 출력
		        //System.exit(1);
		    }
	}
	/**
	 * 포트 연결이 실패 할경우 기록한다.
	 * @param slo 점검할 기본정보
	 * @param log 기록할 로그
	 */
	public synchronized void saveError(svcListOne slo,String log){
		 try {
		      BufferedWriter out = new BufferedWriter(new FileWriter(logpath+"/error.log",true));
		      String s = nowDtYmdHis()+" "+slo.getSvcIp()+":"+slo.getSvcPort()+" "+slo.getSvcType()+" disconnect "+log;

		      out.write(s); out.newLine();
		      out.close();
		    } catch (IOException e) {
		        System.err.println(e); // 에러가 있다면 메시지 출력
		        //System.exit(1);
		    }
	}
	/**
	 * 현재의 년월일시분초를 기록하기 위해 가져옴.
	 * @return yyyy-mm-dd hh:mm:ss
	 */
	public String nowDtYmdHis(){
		long time = System.currentTimeMillis();
		SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dayTime.format(time);
	}
	/**
	 * 경과 시간을 분초밀리초로 알려준다.
	 * @param time 밀리초 long
	 * @return ii:ss.SSS
	 */
	public String time2StrISSS(long time){
		SimpleDateFormat dayTime = new SimpleDateFormat("mm:ss.SSS");
		return dayTime.format(time);
	}
	/**
	 * 파일에 대한 읽기 권한이 있는지 확인해봄
	 * @param filename
	 * @return 권한이 있으면 true 없으면 false
	 */
	public synchronized boolean file_is_read(String filename){
		File f = new File(filename);
		if(f.exists()){
			return f.canRead();
		}else return false;
	}
	/**
	 * 주어진 경로에 대한 쓰기권한이 있는지 확인해본다.
	 * @param path
	 * @return 권한이 있으면 true 없으면 false
	 */
	public boolean path_is_edit(String path){
		File f = new File(path);
		if(f.exists()){
			try{
				File f1 = new File(path+".tmp");
				f1.mkdirs();
				f1.delete();
				return true;
			}catch(Exception e){
				return false;
			}
		}else{
			try{
				f.mkdirs();
				return path_is_edit(path);
			}catch(Exception e){
				return false;
			}
		}
	}
	
	public String getLogpath() {
		return logpath;
	}

	public void setLogpath(String logpath) {
		this.logpath = logpath;
	}
	
}
